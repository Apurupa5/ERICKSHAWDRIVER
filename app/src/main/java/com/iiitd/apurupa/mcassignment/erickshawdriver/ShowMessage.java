package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by NB VENKATESHWARULU on 10/20/2016.
 */
public class ShowMessage {
    public void showmessage(Context context,String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
