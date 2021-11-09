package com.opensource.seebus.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView mRv_history;
    private ArrayList<HistoryItem> mHistoryItems;
    private DBHelper mDBHelper;
    private HistoryRvCustomAdaptor mHistoryAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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
}