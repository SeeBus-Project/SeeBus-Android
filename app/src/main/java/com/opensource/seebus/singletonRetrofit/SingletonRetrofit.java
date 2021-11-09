package com.opensource.seebus.singletonRetrofit;

import android.content.Context;

import com.opensource.seebus.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SingletonRetrofit {
    private static Retrofit singletonRetrofit = null;

    private SingletonRetrofit() { }
    public static synchronized Retrofit getInstance(Context context) {
        if (singletonRetrofit == null) {
            Retrofit retrofit= new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.server_address))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            singletonRetrofit = retrofit;
        }

        return singletonRetrofit;
    }
}