package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class StartRideActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String email;
    private ProgressDialog pDialog;
    public static final String mypreference = "mypref";
    private static String url_start_ride="http://192.168.58.165/mc/start_ride.php";
    private static String url_stop_ride="http://192.168.58.165/mc/stop_ride.php";
    private ShowMessage toast=new ShowMessage();
    Button bstart,bstop;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_ride);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i=getIntent();
        email=i.getStringExtra("user");
        bstart=(Button)findViewById(R.id.startride_button);
        bstop=(Button)findViewById(R.id.stopride_button);
        bstop.setEnabled(false);
    }
    public void startride(View view)
    {
        Log.d("Inside start","start");

        new AcceptRide().execute(email,"start");
        bstop.setEnabled(true);
        bstart.setEnabled(false);
    }
    public void stopride(View view)
    {
        Log.d("Inside stop","stop");
        new AcceptRide().execute(email,"stop");
        new AlertDialog.Builder(StartRideActivity.this)
                .setTitle("Ride Finished")
                .setMessage("Your journey completed !!!!!!!")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        })
                .show();

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(28.5473, 77.2732);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }
    class AcceptRide extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(StartRideActivity.this);
            pDialog.setMessage("Accepting..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {
            String final_url;
            String user = args[0];
            String status=args[1];
            if(status.equals("start"))
            {
                final_url=url_start_ride;
            }
            else
            {
                final_url=url_stop_ride;
            }
            SharedPreferences preferences = getSharedPreferences(mypreference,
                    Context.MODE_PRIVATE);

            String driveremail=preferences.getString("email",null);

            // Building Parameters


            List<NameValuePair> params = new ArrayList<NameValuePair>();



            params.add(new BasicNameValuePair("email", user));
            params.add(new BasicNameValuePair("driver", driveremail));



            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(final_url);
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
                    Log.d("Accept Response", json_string);
                    jsonresponse = new JSONObject(json_string);
                    Log.d("Accept JSON response", jsonresponse.toString());

                    authstatus = jsonresponse.getString("success");
                    Log.d("AuthStatus Response", authstatus);
                    if (authstatus.equals("0")) {

                        Log.d("Accept Info","Cannot be accepted");

                    } else {
                        if (authstatus.equals("1")) {

                            Log.d("Accept Info", jsonresponse.toString());

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


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (authstatus.equals("0")) {
                Log.d("User Info","INVALID DETAILS");
                toast.showmessage(getApplicationContext(), "Request cannot be accepted");
            }
        }

    }
}
