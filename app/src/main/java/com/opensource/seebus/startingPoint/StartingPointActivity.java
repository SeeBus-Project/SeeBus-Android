package com.opensource.seebus.startingPoint;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.selectBus.SelectBusActivity;
import com.opensource.seebus.subService.Gps;
import com.opensource.seebus.util.MakeToast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StartingPointActivity extends AppCompatActivity implements View.OnClickListener{

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    private Button backBtn;
    private Button homeBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_point);

        backBtn=findViewById(R.id.startingPointBackBtn);
        backBtn.setOnClickListener(this);
        homeBtn=findViewById(R.id.startingPointHomeBtn);
        homeBtn.setOnClickListener(this);
        progressBar=findViewById(R.id.startingPointProgressBar);
        ListView listView=findViewById(R.id.startingPointListView);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String getStationsByPosListUrl = getString(R.string.getStationByPos)+getString(R.string.serviceKey)+
                "&tmX="+ Gps.longitude+"&tmY="+Gps.latitude+"&radius=1000";
        List<String> arsId=new ArrayList<>();       //???????????????
        List<String> dist=new ArrayList<>();        //??????
        List<String> stationId=new ArrayList<>();   //??????????????????
        List<String> stationNm=new ArrayList<>();   //???????????????
        List<String> stationTp=new ArrayList<>();   //?????? ???????????? ????????????

        List<String> nextStationName=new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        Observable.fromCallable(()->{
            try {
    //            System.out.println(url);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(getStationsByPosListUrl);

                doc.getDocumentElement().normalize();
    //            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("itemList");
                if(nList.getLength()==0) {
                    return false;
                } else {
                    for (int temp = 0; temp < nList.getLength() && temp<10; temp++) {
                        Node nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element eElement = (Element) nNode;
                            //                    System.out.println("######################");
                            //                    System.out.println(eElement.getTextContent());
                            //                    System.out.println("????????????????????? : " + getTagValue("arsId", eElement));
                            //                    System.out.println("?????? : " + getTagValue("dist", eElement));
                            //                    System.out.println("????????? ID : " + getTagValue("stationId", eElement));
                            //                    System.out.println("???????????? : " + getTagValue("stationNm", eElement));
                            //                    System.out.println("??????????????? : " + getTagValue("stationTp", eElement));
                            arsId.add(getTagValue("arsId", eElement));
                            dist.add(getTagValue("dist", eElement));
                            stationId.add(getTagValue("stationId", eElement));
                            stationNm.add(getTagValue("stationNm", eElement));
                            stationTp.add(getTagValue("stationTp", eElement));
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("???????????????.");
            }

            try {
                for(int i=0;i<arsId.size();i++) {
                    String getStationByUidItemUrl = getString(R.string.getStationByUid) + getString(R.string.serviceKey) +
                            "&arsId=" + arsId.get(i);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(getStationByUidItemUrl);

                    doc.getDocumentElement().normalize();
                    //                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                    NodeList nList = doc.getElementsByTagName("itemList");
                    if (nList.getLength() == 0) {
                        return false;
                    } else {
                        Node nNode = nList.item(0);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            //                    System.out.println("######################");
                            //                    System.out.println(eElement.getTextContent());
                            //                    System.out.println("?????? ????????? : " + getTagValue("nxtStn",eElement));
                            nextStationName.add(getTagValue("nxtStn", eElement));
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("???????????????.");
            }
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result)->{
                    if(result==true) {
                        ArrayList<StartingPointListData> listViewData = new ArrayList<>();

                        // ????????? ?????? 10?????? ??????
                        for (int i = 0; i < stationNm.size() && i < 10; i++) {
                            StartingPointListData listData = new StartingPointListData();

                            listData.station = stationNm.get(i);
                            listData.distAndStationNumberAndNextStationName =
                                    dist.get(i) + "m | " + arsId.get(i) + " | " + nextStationName.get(i) + " ??????";

                            listViewData.add(listData);
                        }
                        ListAdapter oAdapter = new StartingPointCustomView(listViewData);
                        listView.setAdapter(oAdapter);
                    } else {
                        MakeToast.makeToast(getApplicationContext(),"???????????? ?????? ???????????????.").show();
                    }
                    progressBar.setVisibility(View.GONE);
                });

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

    @Override
    public void onClick(View v) {
        if (v==backBtn) {
            onBackPressed();
        }
        else if (v==homeBtn) {

            Intent mainIntent= new Intent(v.getContext(), MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // ????????? ???????????? ??????
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // ????????? ???????????? ??????
            startActivity(mainIntent);
        }
    }
}
