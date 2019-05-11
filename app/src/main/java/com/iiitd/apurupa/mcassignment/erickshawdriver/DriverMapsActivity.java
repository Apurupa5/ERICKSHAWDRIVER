package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BroadcastReceiver broadcastReceiver;
    public static final String mypreference = "mypref";
    private String useremail;
  int f=0;
    private ProgressDialog pDialog;
    private static String url_create_account = "http://192.168.58.165/mc/location.php";
    private static String url_get_count="http://192.168.58.165/mc/count.php";
    private static String url_update_details="http://192.168.58.165/mc/update.php";
    private ShowMessage toast=new ShowMessage();
    private DrawerLayout mdrawerlayout;
    private ActionBarDrawerToggle mactiontoggle;
    private NavigationView mnavigationview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("Firebase:","firebase");

        init();
    }

    private void init()
    {
        SharedPreferences prefs = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        useremail=prefs.getString("email","");
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();

        if(!runtime_permissions())
            enable_service();
        mdrawerlayout=(DrawerLayout)findViewById(R.id.drawerlayout);
        mnavigationview=(NavigationView)findViewById(R.id.navigationview);
        mactiontoggle=new ActionBarDrawerToggle(this,mdrawerlayout,R.string.open,R.string.close);
        mdrawerlayout.addDrawerListener(mactiontoggle);
        mactiontoggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mnavigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.logout: toast.showmessage(getApplicationContext(),"Logout Clicked");
                        SharedPreferences preferences = getSharedPreferences(mypreference,
                                Context.MODE_PRIVATE);

                        SharedPreferences.Editor edt = preferences.edit();
                        edt.putBoolean("isloggedin",false);
                        edt.apply();
                        edt.commit();
                        Intent i1=new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(i1);
                        break;
                    case R.id.profile:

                        Intent i2=new Intent(getApplicationContext(),ViewProfileActivity.class);
                        startActivity(i2);
                        break;
                }
                return false;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(mactiontoggle.onOptionsItemSelected(item)){

            return true;
        }
        return super.onOptionsItemSelected(item);

    }


//Updating Present Location of Driver
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double latitude=intent.getExtras().getDouble("latitude");
                    double longitude=intent.getExtras().getDouble("longitude");
                    mMap.clear();
                    Log.d("username",useremail);

                    Log.d("Before Driver","GE");
                    Log.d("Driver", String.valueOf(intent.getExtras().getDouble("longitude")));
                    Log.d("After Driver","GE");
               if(latitude!=0.0 && longitude!=0.0) {
                   LatLng iiitd = new LatLng(latitude, longitude);
                   mMap.addMarker(new MarkerOptions().position(iiitd).title("Marker in IIITD"));
                   mMap.moveCamera(CameraUpdateFactory.newLatLng(iiitd));
                   moveToCurrentLocation(iiitd);
               }

                    new UpdateLocation().execute(useremail,String.valueOf(latitude),String.valueOf(longitude),"available");
                  //  mcoord.append("\n" +intent.getExtras().getDouble("latitude"));

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }
    private void setUpMapIfNeeded()
    {
        if(mMap==null)
        {
            //  mMap=((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync();
        }
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//Starting a Service
    private void enable_service() {


        Intent i = new Intent(getApplicationContext(), GPSService.class);
        startService(i);

    }
    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_service();
            }else {
                runtime_permissions();
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng iiitd = new LatLng(28.5473,77.2732);
        mMap.addMarker(new MarkerOptions().position(iiitd).title("Marker in IIITD"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(iiitd));
        moveToCurrentLocation(iiitd);
    }
    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }
    class UpdateLocation extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DriverMapsActivity.this);
            pDialog.setMessage("Updating..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            //pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {

            String email = args[0];
            String latitude = args[1];
            String longitude=args[2];
            String status=args[3];
            double lat=Double.parseDouble(latitude);
            double lng=Double.parseDouble(longitude);
            // Building Parameters
            String count = getCount(email);
            Log.d("count",count);
            String finalurl="";
            if(count.equals("0"))
            {
                finalurl=url_create_account;

            }
            else if(count.equals("1"))
            {
                finalurl=url_update_details;
            }
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            Location locationA = new Location("point A");

            locationA.setLatitude(28.5473);
            locationA.setLongitude(77.2732);

            Location locationB = new Location("point B");

            locationB.setLatitude(lat);
            locationB.setLongitude(lng);

           double  distance = locationA.distanceTo(locationB);
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("latitude", Double.toString(lat)));
            params.add(new BasicNameValuePair("longitude", Double.toString(lng)));
            params.add(new BasicNameValuePair("availability", status));
            params.add(new  BasicNameValuePair("distance", Double.toString(distance)));

            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(finalurl);
                Log.d("url", String.valueOf(url));
                conn= (java.net.HttpURLConnection ) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String json_string = "";
            OutputStream os = null;
            try {
                os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //response from request
            int responseCode= 0;
            JSONObject jsonresponse=null;
            try {
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        json_string += line;

                    }
                    Log.d("Create Response in try", json_string);
                    jsonresponse = new JSONObject(json_string);
                    Log.d("json Response in try", jsonresponse.toString());

                    authstatus = jsonresponse.getString("success");
                    Log.d("AuthStatus Response", authstatus);
                    if (authstatus.equals("0")) {
                        Log.d("Save Info","An Error Occured");

                    } else {
                        if (authstatus.equals("1")) {

                            Log.d("Json Response in try", jsonresponse.toString());

                        }
                    }
                }
                else {
                    json_string="";

                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("Create Response", json_string);

            //Log.d("Json Response",jsonresponse.toString());
            return json_string;
        }
        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
        {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params)
            {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }

            return result.toString();
        }

        private String getCount(String email)
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            String rowcount=null;
            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(url_get_count);
                Log.d("url", String.valueOf(url));
                conn= (java.net.HttpURLConnection ) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String json_string = "";
            OutputStream os = null;
            try {
                os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //response from request
            int responseCode= 0;
            JSONObject jsonresponse=null;
            try {
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        json_string += line;

                    }
                    Log.d("Create Response  count", json_string);
                    jsonresponse = new JSONObject(json_string);
                    Log.d("json Response in count", jsonresponse.toString());

                    rowcount = jsonresponse.getString("count");
                    Log.d("row count",rowcount);
                    return rowcount;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rowcount;
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (authstatus.equals("0")) {
                Log.d("User Info","INVALID DETAILS");
                toast.showmessage(getApplicationContext(), "Invalid Details");
            }
        }

    }

}
