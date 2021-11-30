package com.opensource.seebus.busRoute;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.dialog.CustomDialog;
import com.opensource.seebus.dialog.CustomDialogClickListener;
import com.opensource.seebus.history.DBHelper;
import com.opensource.seebus.sendGpsInfo.SendGpsInfoActivity;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoRequestDto;
import com.opensource.seebus.sendRouteInfo.SendRouteInfoService;
import com.opensource.seebus.singleton.SingletonRetrofit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BusRouteActivity extends AppCompatActivity  implements View.OnClickListener {

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

    private Button backBtn;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);

        backBtn=findViewById(R.id.busRouteBackBtn);
        backBtn.setOnClickListener(this);
        homeBtn=findViewById(R.id.busRouteHomeBtn);
        homeBtn.setOnClickListener(this);

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
        textView.setText(adirection+" 방면 " + busNm +"번");
        textView.setTextSize(40);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#FFFF00"));
        textView.setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setFontFeatureSettings(String.valueOf(R.font.font));
        }

        listView.addHeaderView(textView);
        ListAdapter oAdapter = new BusRouteCustomView(listViewData);
        listView.setAdapter(oAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 데이터 할당
                mAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                mDestinationArsId = stationNo.get(memoryPosition+position-1);
                mDestinationName = stationNm.get(memoryPosition+position-1);
                mRtNm = busNm;
                mStartArsId = departureNo;

                // history DB에 경로 insert 하기
                mDBHelper.insertHistory(mRtNm, mStartArsId, departure, mDestinationArsId, mDestinationName);

                CustomDialog customDialog=new CustomDialog(BusRouteActivity.this, new CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick() {
                        //EC2 신호전달(TCP)
                        ClientThread thread = new ClientThread();
                        thread.data[0] = "in";
                        thread.data[1] = departure;
                        thread.data[2] = stationNm.get(memoryPosition + position - 1);
                        thread.getPort = 5000;
                        thread.start();
                        sendRouteInfo(SingletonRetrofit.getInstance(getApplicationContext()));
                    }

                    @Override
                    public void onNegativeClick() {
                        Toast.makeText(getApplicationContext(), "취소 했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },departure,stationNm.get(memoryPosition + position - 1));
                customDialog.setCanceledOnTouchOutside(false);
                customDialog.setCancelable(false);
//                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                customDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                customDialog.show();
            }
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
//                    Toast.makeText(getApplicationContext(), "안내를 시작합니다.", Toast.LENGTH_SHORT).show();

                    // SendGpsInfoActivity로 넘어가기
                    Intent gpsIntent = new Intent(BusRouteActivity.this, SendGpsInfoActivity.class);
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    gpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    gpsIntent.putExtra("isReboot","No");
                    startActivity(gpsIntent);
                } else { // 통신 실패(응답 코드로 판단)
                    // 확인용 toast
                    Toast.makeText(getApplicationContext(), "현재 서비스하지 않는 경로입니다.", Toast.LENGTH_SHORT).show();

                    // MainActivity로 돌아가기
                    Intent mainIntent = new Intent(BusRouteActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                    startActivity(mainIntent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 확인용 toast
                Toast.makeText(getApplicationContext(), "데이터를 키고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();

                // MainActivity로 돌아가기
                Intent mainIntent = new Intent(BusRouteActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                startActivity(mainIntent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v==backBtn) {
            onBackPressed();
        }
        else if (v==homeBtn) {

            Intent mainIntent= new Intent(v.getContext(), MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
            startActivity(mainIntent);
        }
    }

    // TCP 쓰레드
    class ClientThread extends Thread {
        String data[] = new String[3];
        int getPort;
        @Override
        public void run() {
            String host2 = "183.101.12.31";
            String host = "ec2-3-35-208-56.ap-northeast-2.compute.amazonaws.com";
            try {
                Socket socket = new Socket(host, getPort);

                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream()); //소켓의 출력 스트림 참조
                outstream.writeObject(data[0]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력

                // 출발정류장 전송
                outstream.writeObject(data[1]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력

                // 도착정류장 전송
                outstream.writeObject(data[2]); // 출력 스트림에 데이터 넣기
                outstream.flush(); // 출력
                //ObjectInputStream instream = new ObjectInputStream(socket.getInputStream());
                //String response = (String)instream.readObject();

                //response = (String)instream.readObject();

                outstream.close();
                //instream.close();
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}