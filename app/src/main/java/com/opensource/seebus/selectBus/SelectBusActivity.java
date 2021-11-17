package com.opensource.seebus.selectBus;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.busRoute.BusRouteActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;

public class SelectBusActivity extends AppCompatActivity implements View.OnClickListener {

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    private Button backBtn;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bus);

        backBtn=findViewById(R.id.selectBusBackBtn);
        backBtn.setOnClickListener(this);
        homeBtn=findViewById(R.id.selectBusHomeBtn);
        homeBtn.setOnClickListener(this);

        Intent selectBusIntent=getIntent();
        String arsId=selectBusIntent.getExtras().getString("arsId");
        String departure=selectBusIntent.getExtras().getString("departure");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String url = getString(R.string.getStationByUid)+getString(R.string.serviceKey)+
                "&arsId="+arsId;
        List<String> rtNm=new ArrayList<>();//버스번호
        List<String> nxtStn=new ArrayList<>();//다음정거장
        List<String> adirection=new ArrayList<>();//종점
        List<String> arrmsg1=new ArrayList<>();//버스 몇분뒤 도착하는 메시지
        List<String> arrmsg2=new ArrayList<>();
        List<String> busRouteId=new ArrayList<>();//버스노선Id
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
//                    System.out.println("버스번호 : " + getTagValue("rtNm", eElement));
//                    System.out.println("다음정류장 : " + getTagValue("nxtStn", eElement));
//                    System.out.println("종점 : " + getTagValue("adirection", eElement));
//                    System.out.println("첫버스 도착시간 : " + getTagValue("arrmsg1", eElement));
//                    System.out.println("두번째버스 도착시간 : " + getTagValue("arrmsg2", eElement));
//                    System.out.println("버스노선Id : " + getTagValue("busRouteId", eElement));
                    rtNm.add(getTagValue("rtNm",eElement));
                    nxtStn.add(getTagValue("nxtStn",eElement));
                    adirection.add(getTagValue("adirection",eElement));
                    arrmsg1.add(getTagValue("arrmsg1",eElement));
                    arrmsg2.add(getTagValue("arrmsg2",eElement));
                    busRouteId.add(getTagValue("busRouteId",eElement));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("오류입니다.");
        }

        ListView listView=findViewById(R.id.selectBusListView);
        ArrayList<SelectBusListData> listViewData = new ArrayList<>();
        for (int i=0; i<rtNm.size(); i++)
        {
            SelectBusListData listData = new SelectBusListData();

            listData.busNumber = rtNm.get(i);
            listData.nextStation = " | "+nxtStn.get(i)+ "방면";
            listData.endStation = adirection.get(i);
            listData.message1 = arrmsg1.get(i);
            listData.message2 = arrmsg2.get(i);
            listViewData.add(listData);
        }

        ListAdapter oAdapter = new SelectBusCustomView(listViewData);
        listView.setAdapter(oAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent busRouteActivity= new Intent(view.getContext(), BusRouteActivity.class);
                busRouteActivity.putExtra("busNm", rtNm.get(position));//버스이름(번호)
                busRouteActivity.putExtra("busRouteId",busRouteId.get(position));//버스노선Id
                busRouteActivity.putExtra("adirection",adirection.get(position));//종점정거장
                busRouteActivity.putExtra("nxtStn",nxtStn.get(position));//다음정거장
                busRouteActivity.putExtra("departure",departure);//출발정거장 이름
                startActivity(busRouteActivity);
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
}