package com.opensource.seebus.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.opensource.seebus.R;

import androidx.annotation.NonNull;

public class CustomDialog  extends Dialog{

    private Context context;
    private CustomDialogClickListener customDialogClickListener;

    private String departureStationText;
    private String destinationStationText;
    private String busText;

    private Button customDialogOKButton;
    private Button customDialogCancelButton;

    private TextView customDialogDepartureStationTextView;
    private TextView customDialogDestinationStationTextView;
    private TextView customDialogBusTextView;

    public CustomDialog(@NonNull Context context,
                        CustomDialogClickListener customDialogClickListener,
                        String departureStationText,
                        String destinationStationText,
                        String busText) {
        super(context,android.R.style.Theme_Black_NoTitleBar);
        this.context=context;
        this.customDialogClickListener=customDialogClickListener;
        this.departureStationText=departureStationText;
        this.destinationStationText=destinationStationText;
        this.busText=busText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);

        customDialogOKButton = findViewById(R.id.customDialogOKButton);
        customDialogCancelButton = findViewById(R.id.customDialogCancelButton);
        customDialogDepartureStationTextView = findViewById(R.id.customDialogDepartureStationTextView);
        customDialogDestinationStationTextView = findViewById(R.id.customDialogDestinationStationTextView);
        customDialogBusTextView = findViewById(R.id.customDialogBusTextView);

        customDialogDepartureStationTextView.setText("출발 정류장 : "+departureStationText);
        customDialogDestinationStationTextView.setText("도착 정류장 : "+destinationStationText);
        customDialogBusTextView.setText("탑승 버스 : "+busText);
        customDialogOKButton.setOnClickListener(v -> {
            // 확인버튼 클릭
            this.customDialogClickListener.onPositiveClick();
            dismiss();
        });
        customDialogCancelButton.setOnClickListener(v -> {
            // 취소버튼 클릭
            this.customDialogClickListener.onNegativeClick();
            dismiss();
        });
    }
}