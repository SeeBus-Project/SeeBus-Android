package com.opensource.seebus.singleton;

import android.content.Context;

import java.util.Timer;

public class SingletonTimer {
    //보통의 싱글턴이면 private였어야하지만 사용용도에 맞춰 public으로 변경
    public static Timer singletonTimer = null;

    private SingletonTimer() { }
    public static synchronized Timer getInstance(Context context) {
        if (singletonTimer == null) {
            Timer timer = new Timer();
            singletonTimer = timer;
        }
        return singletonTimer;
    }
}
