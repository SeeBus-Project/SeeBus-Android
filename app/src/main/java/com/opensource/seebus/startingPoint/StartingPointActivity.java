package com.opensource.seebus.startingPoint;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.opensource.seebus.R;
import com.opensource.seebus.selectBus.SelectBusActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;

public class StartingPointActivity extends AppCompatActivity {

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_point);

        Intent startingPointIntent=getIntent();

        double longitude=startingPointIntent.getDoubleExtra("longitude",0);
        double latitude=startingPointIntent.getDoubleExtra("latitude",0);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String url = getString(R.string.getStationByPos)+getString(R.string.serviceKey)+
                "&tmX="+longitude+"&tmY="+latitude+"&radius=1000";
        List<String> arsId=new ArrayList<>();//정거장번호
        List<String> dist=new ArrayList<>();//거리
        List<String> stationId=new ArrayList<>();//정거장아이디
        List<String> stationNm=new ArrayList<>();//정거장이름
        List<String> stationTp=new ArrayList<>();//어떤버스인지 저상버스
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
//                    System.out.println("정류소고유번호 : " + getTagValue("arsId", eElement));
//                    System.out.println("거리 : " + getTagValue("dist", eElement));
//                    System.out.println("정류소 ID : " + getTagValue("stationId", eElement));
//                    System.out.println("정류소명 : " + getTagValue("stationNm", eElement));
//                    System.out.println("정류소타입 : " + getTagValue("stationTp", eElement));
                    arsId.add(getTagValue("arsId",eElement));
                    dist.add(getTagValue("dist",eElement));
                    stationId.add(getTagValue("stationId",eElement));
                    stationNm.add(getTagValue("stationNm",eElement));
                    stationTp.add(getTagValue("stationTp",eElement));
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("오류입니다.");
        }

        ListView listView=findViewById(R.id.startingPointListView);
        ArrayList<StartingPointListData> listViewData = new ArrayList<>();
        for (int i=0; i<stationNm.size(); i++)
        {
            StartingPointListData listData = new StartingPointListData();

            listData.station = stationNm.get(i);
            listData.distAndStationNumber = dist.get(i)+ "m | " +arsId.get(i);

            listViewData.add(listData);
        }


        ListAdapter oAdapter = new StartingPointCustomView(listViewData);
        listView.setAdapter(oAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent selectBusActivity= new Intent(view.getContext(), SelectBusActivity.class);
                selectBusActivity.putExtra("arsId",arsId.get(position));
                selectBusActivity.putExtra("departure",stationNm.get(position));
                startActivity(selectBusActivity);
            }
        });

    }
}