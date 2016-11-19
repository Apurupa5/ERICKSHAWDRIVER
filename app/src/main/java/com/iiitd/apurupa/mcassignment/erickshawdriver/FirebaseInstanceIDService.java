package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by NB VENKATESHWARULU on 11/17/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
       Log.d("Token:",token);
        registerToken(token);
    }

    private void registerToken(String token) {
         final String mypreference = "mypref";
         String useremail;



        SharedPreferences preferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor edt = preferences.edit();
        edt.putString("Token",token);
        edt.apply();
        edt.commit();

       /* OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token",token)
                .add("email",useremail)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.58.165/mc/register.php")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
            Log.d("In ID Service:",token);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
