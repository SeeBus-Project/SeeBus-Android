package com.opensource.seebus.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.opensource.seebus.R;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView mRv_favorite;
    private ArrayList<HistoryItem> mFavoriteItems;
    private DBHelper mDBHelper;
    private FavoriteRvCustomAdaptor mFavoriteAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        setInit();
    }

    private void setInit() {
        // 기본적인 초기화
        mDBHelper = new DBHelper(this);
        // 즐겨찾기와 최근기록의 리스트는 동일하기 때문에 rv_history layout 그대로 가져옴.
        mRv_favorite = findViewById(R.id.rv_history);
        mFavoriteItems = new ArrayList<>();

        // load recent DB
        loadRecentDB();
    }

    private void loadRecentDB() {
        // 저장되어있던 DB를 가져온다.
        mFavoriteItems = mDBHelper.getFavorite();

        if (mFavoriteAdaptor == null) {
            mFavoriteAdaptor = new FavoriteRvCustomAdaptor(mFavoriteItems, this);
            mRv_favorite.setHasFixedSize(true);
            mRv_favorite.setAdapter(mFavoriteAdaptor);
        }
    }
}