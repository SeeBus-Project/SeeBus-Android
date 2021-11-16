package com.opensource.seebus.busRoute;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.history.DBHelper;
import com.opensource.seebus.sendGpsInfo.SendGpsInfoActivity;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoRequestDto;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoService;
import com.opensource.seebus.singletonRetrofit.SingletonRetrofit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BusRouteActivity extends AppCompatActivity {

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    boolean flag=false;
    int memoryPosition=0;

    // 즐겨찾기를 삽입하기 위해서 수정한 내용
    private DBHelper mDBHelper;

    // 서버에게 경로 정보 전송
    private String mAndroidId;
    private String mDestinationArsId;
    private String mDestinationName;
    private String mRtNm;
    private String mStartArsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);

        // 즐겨찾기 삽입 위하여 수정한 내용
        mDBHelper = new DBHelper(this);

        Intent busRouteIntent=getIntent();
        String busNm=busRouteIntent.getExtras().getString("busNm");
        String busRouteId=busRouteIntent.getExtras().getString("busRouteId");
        String adirection=busRouteIntent.getExtras().getString("adirection");
        String nxtStn=busRouteIntent.getExtras().getString("nxtStn");
        String departure=busRouteIntent.getExtras().getString("departure");
        String departureNo=busRouteIntent.getExtras().getString("departureNo");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String url = getString(R.string.getStaionByRoute)+getString(R.string.serviceKey)+
                "&busRouteId="+busRouteId;
        List<String> stationNo=new ArrayList<>();//정거장번호
        List<String> stationNm=new ArrayList<>();//정거장이름
        List<String> direction=new ArrayList<>();//종점

        try {
//            System.out.println(url);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(url);

            doc.getDocumentElement().normalize();
//            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("itemList");

            for(int temp = 0; temp < nList.getLength(); temp++){
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE){

                    Element eElement = (Element) nNode;
//                    System.out.println("######################");
//                    System.out.println(eElement.getTextContent());
//                    System.out.println("정류장이름 : " + getTagValue("stationNm", eElement));
//                    System.out.println("정류장아이디 : " + getTagValue("stationNo", eElement));
//                    System.out.println("종점 : " + getTagValue("direction", eElement));
                    stationNm.add(getTagValue("stationNm",eElement));
                    stationNo.add(getTagValue("stationNo",eElement));
                    direction.add(getTagValue("direction",eElement));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("오류입니다.");
        }

        ListView listView=findViewById(R.id.busRouteListView);
        ArrayList<BusRouteListData> listViewData = new ArrayList<>();
        for (int i=0; i<stationNo.size(); i++)
        {
            BusRouteListData listData = new BusRouteListData();

            if(nxtStn.equals(stationNm.get(i)) || flag) {
                if(nxtStn.equals(stationNm.get(i)) && flag==false) {
                    memoryPosition=i;
                }
                if(adirection.equals(direction.get(i))) {
                    listData.busRouteStation = stationNm.get(i);
                    listData.busRouteStationNumber = stationNo.get(i);
                    listData.direction = direction.get(i);
                    listViewData.add(listData);
                    flag=true;
                }
            }
        }

        TextView textView=new TextView(this);
        textView.setText(adirection+" 방면");
        textView.setTextSize(40);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#FFFF00"));
        textView.setClickable(true);

        listView.addHeaderView(textView);
        ListAdapter oAdapter = new BusRouteCustomView(listViewData);
        listView.setAdapter(oAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mainIntent= new Intent(view.getContext(), MainActivity.class);
                //TODO 팝업창 띄우기
                //TODO 서버로 출발지와 목적지 보내기
                //TODO 시간측정해서 1분전이면 푸시알림 보내기
                //TODO 도착알림보내기
                //출발지
                mainIntent.putExtra("departure",departure);
                //도착지
                mainIntent.putExtra("destination",stationNm.get(memoryPosition +position-1));

                // history DB에 경로 insert 하기 - 수정 예정
                mDBHelper.InsertHistory(busNm, busRouteId, departure, stationNm.get(memoryPosition+position-1));

                // 서버에 RouteInfo 보내기
                mAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                mDestinationArsId = stationNo.get(memoryPosition+position-1);
                mDestinationName = stationNm.get(memoryPosition+position-1);
                mRtNm = busNm;
                mStartArsId = departureNo;

                sendRouteInfo(SingletonRetrofit.getInstance(getApplicationContext()));

/* // 나중에 필요없으면 삭제해주세요
                Toast.makeText(getApplicationContext(),
                        "출발지 = " + departure +
                                "\n도착지 = " + stationNm.get(memoryPosition+position-1),
                        Toast.LENGTH_SHORT).show();
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                startActivity(mainIntent);
*/            }
        });
    }

    private void sendRouteInfo(Retrofit retrofit) {
        SendRouteInfoService sendRouteInfoService = retrofit.create(SendRouteInfoService.class);
        Call<Void> call = sendRouteInfoService.requestSendRoute(new SendRouteInfoRequestDto(mAndroidId, mDestinationArsId, mDestinationName, mRtNm, mStartArsId));

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { // 정상적으로 통신 성공
                    // 확인용 toast
                    Toast.makeText(getApplicationContext(), "통신 성공", Toast.LENGTH_SHORT).show();

                    // SendGpsInfoActivity로 넘어가기
                    Intent gpsIntent = new Intent(BusRouteActivity.this, SendGpsInfoActivity.class);
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    startActivity(gpsIntent);
                } else { // 통신 실패(응답 코드로 판단)
                    // 확인용 toast
                    Toast.makeText(getApplicationContext(), "통신 실패 (응답 코드: 3xx, 4xx 등)", Toast.LENGTH_SHORT).show();

                    // MainActivity로 돌아가기
                    Intent mainIntent = new Intent(BusRouteActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    startActivity(mainIntent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("SENDROUTE", t.toString());

                // 확인용 toast
                Toast.makeText(getApplicationContext(), "통신 실패 (시스템적인 이유로)", Toast.LENGTH_SHORT).show();

                // MainActivity로 돌아가기
                Intent mainIntent = new Intent(BusRouteActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                startActivity(mainIntent);
            }
        });
    }
}