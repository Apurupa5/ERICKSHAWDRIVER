package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends Activity {
    private static int SPLASH_TIME_OUT = 4000;
    public static final String mypreference = "mypref";
    ImageView app_icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        app_icon = (ImageView) findViewById(R.id.imageView);
//Animation

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
        app_icon.setAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        SharedPreferences preferences = getSharedPreferences(mypreference,
                                Context.MODE_PRIVATE);
                        if (preferences.getBoolean("isloggedin", true)) {

                            Intent i = new Intent(getApplicationContext(), DriverMapsActivity.class);
                            startActivity(i);
                        } else {
                            // close this activity
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                        // finish();
                    }
                }, SPLASH_TIME_OUT);
            }


            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }
}
