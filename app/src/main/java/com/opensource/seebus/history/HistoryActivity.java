package com.opensource.seebus.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRv_history;
    private ArrayList<HistoryItem> mHistoryItems;
    private DBHelper mDBHelper;
    private HistoryRvCustomAdaptor mHistoryAdaptor;

    private Button backBtn;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        backBtn=findViewById(R.id.historyBackBtn);
        backBtn.setOnClickListener(this);
        homeBtn=findViewById(R.id.historyHomeBtn);
        homeBtn.setOnClickListener(this);


        setInit();
    }

    private void setInit() {
        // 기본적인 초기화
        mDBHelper = new DBHelper(this);
        mRv_history = findViewById(R.id.rv_history);
        mHistoryItems = new ArrayList<>();

        // load recent DB
        loadRecentDB();
    }


    private void loadRecentDB() {
        // 저장되어있던 DB를 가져온다.
        mHistoryItems = mDBHelper.getHistory();

        if (mHistoryAdaptor == null) {
            mHistoryAdaptor = new HistoryRvCustomAdaptor(mHistoryItems, this);
            mRv_history.setHasFixedSize(true);
            mRv_history.setAdapter(mHistoryAdaptor);
        }
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