package com.opensource.seebus.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.opensource.seebus.R;

import androidx.annotation.NonNull;

public class HelpDialog extends Dialog {
    private Context context;
    private CustomDialogClickListener customDialogClickListener;

    private Button customDialogOKButton;
    private Button customDialogCancelButton;

    private String helpTitle;
    private String helpText;

    private TextView helpTitleTextView;
    private TextView helpTextView;

    public HelpDialog(@NonNull Context context,
                        CustomDialogClickListener customDialogClickListener,
                        String helpTitle,
                        String helpText) {
        super(context,android.R.style.Theme_Black_NoTitleBar);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
        this.helpTitle=helpTitle;
        this.helpText=helpText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_dialog);

        customDialogOKButton = findViewById(R.id.helpDialogHomeBtn);
        customDialogCancelButton = findViewById(R.id.helpDialogBackBtn);
        helpTitleTextView=findViewById(R.id.helpTitleTextView);
        helpTextView = findViewById(R.id.helpTextView);
        helpTitleTextView.setText(helpTitle);
        helpTextView.setText(helpText);
        customDialogOKButton.setOnClickListener(v -> {
            // 확인버튼 클릭
            this.customDialogClickListener.onPositiveClick();
        });
        customDialogCancelButton.setOnClickListener(v -> {
            // 취소버튼 클릭
            this.customDialogClickListener.onNegativeClick();
            dismiss();
        });
    }
}
