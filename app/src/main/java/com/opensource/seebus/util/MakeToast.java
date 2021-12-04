package com.opensource.seebus.util;

import android.content.Context;
import android.widget.Toast;

public class MakeToast {
    private static Toast toast;
    public static Toast makeToast(Context context, String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context,message,Toast.LENGTH_SHORT);
        return toast;
    }
}
