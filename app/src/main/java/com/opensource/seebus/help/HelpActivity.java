package com.opensource.seebus.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.opensource.seebus.MainActivity;
import com.opensource.seebus.R;
import com.opensource.seebus.dialog.CustomDialogClickListener;
import com.opensource.seebus.dialog.HelpDialog;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    Button helpStartingPointButton;
    Button helpGuideButton;
    Button helpHistoryButton;
    Button helpFavoriteButton;
    Button helpHomeBtn;
    Button helpBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        helpStartingPointButton = findViewById(R.id.helpStartingPointButton);
        helpGuideButton=findViewById(R.id.helpGuideButton);
        helpHistoryButton=findViewById(R.id.helpHistoryButton);
        helpFavoriteButton=findViewById(R.id.helpFavoriteButton);
        helpHomeBtn=findViewById(R.id.helpHomeBtn);
        helpBackBtn=findViewById(R.id.helpBackBtn);

        helpHomeBtn.setOnClickListener(view -> {
            onBackPressed();
        });
        helpBackBtn.setOnClickListener(view -> {
            onBackPressed();
        });

        helpStartingPointButton.setOnClickListener(view -> {
            makeHelpDialog(view,"출발지 선택 도움말",getString(R.string.helpSelectStart));
        });

        helpGuideButton.setOnClickListener(view -> {
            makeHelpDialog(view,"경로안내 도움말",getString(R.string.helpGuide));
        });

        helpHistoryButton.setOnClickListener(view -> {
            makeHelpDialog(view,"최근 기록 도움말",getString(R.string.helpHistory));
        });

        helpFavoriteButton.setOnClickListener(view -> {
            makeHelpDialog(view,"즐겨찾기 도움말",getString(R.string.helpFavorite));
        });
    }

    private void makeHelpDialog(View view,String helpTitle, String helpText) {
        HelpDialog helpDialog=new HelpDialog(HelpActivity.this, new CustomDialogClickListener() {
            @Override
            public void onPositiveClick() {
                Intent mainIntent= new Intent(view.getContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존의 액티비티 삭제
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 액티비티 생성
                startActivity(mainIntent);
            }

            @Override
            public void onNegativeClick() {
            }
        },helpTitle,helpText);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.setCancelable(false);
        helpDialog.show();
    }
}