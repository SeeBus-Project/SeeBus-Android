package com.opensource.seebus.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import com.opensource.seebus.R;

import androidx.annotation.NonNull;

public class NetworkDialog extends Dialog {
    private Context context;
    private CustomDialogClickListener customDialogClickListener;

    private Button customDialogOKButton;
    private Button customDialogCancelButton;

    public NetworkDialog(@NonNull Context context,
                      CustomDialogClickListener customDialogClickListener) {
        super(context,android.R.style.Theme_Black_NoTitleBar);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_dialog);

        customDialogOKButton = findViewById(R.id.networkRetryButton);
        customDialogCancelButton = findViewById(R.id.networkExitButton);
        customDialogOKButton.setOnClickListener(v -> {
            // 재연결버튼 클릭
            this.customDialogClickListener.onPositiveClick();
            dismiss();
        });
        customDialogCancelButton.setOnClickListener(v -> {
            // 종료버튼 클릭
            this.customDialogClickListener.onNegativeClick();
            dismiss();
        });
    }
}
