package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {

    public TextView ntTextView;
    public TextView muserTextView;
    public TextView mpickupTextView;
    public TextView mdestTextView;
    public Button macceptButton;
    public Button mfinishButton;

    public static final String mypreference = "mypref";
    String user,pickup,dest;
    private ProgressDialog pDialog;
    private static String url_accept_request="http://192.168.58.165/mc/accept_request.php";
    private static String url_finish_ride="http://192.168.58.165/mc/finish.php";
    private ShowMessage toast=new ShowMessage();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ntTextView=(TextView)findViewById(R.id.notifcationtextView);
        muserTextView=(TextView)findViewById(R.id.usertextView);
        mpickupTextView=(TextView)findViewById(R.id.pickuptextView);
        mdestTextView=(TextView)findViewById(R.id.desttextView);
        macceptButton=(Button)findViewById(R.id.acceptRequestButton);
        mfinishButton=(Button)findViewById(R.id.finishRideButton);
      String message;
        macceptButton.setOnClickListener(this);
        mfinishButton.setOnClickListener(this);

        if(getIntent().getExtras()!=null)
        {
            message=getIntent().getExtras().getString("message");
            user=getIntent().getExtras().getString("user");
            pickup=getIntent().getExtras().getString("pickup");
            dest=getIntent().getExtras().getString("destination");
            if(message==null)
            {
                message="NO NEW MESSAGE";
            }
           // ntTextView.setText("Message:"+message);
            muserTextView.setText(user);
            mpickupTextView.setText(pickup);
            mdestTextView.setText(dest);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.acceptRequestButton:
                macceptButton.setEnabled(false);
                new AcceptRide().execute(user,pickup,dest);
//                Intent I=new Intent(this,StartRideActivity.class);
//                I.putExtra("user",user);
//                I.putExtra("pickup",pickup);
//                I.putExtra("destination",dest);
//                startActivity(I);
                mfinishButton.setVisibility(View.VISIBLE);
                break;
            case R.id.finishRideButton:
                new finishRide().execute();
                break;
        }
    }

    //Driver Accepting a Ride

    class AcceptRide extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NotificationActivity.this);
            pDialog.setMessage("Accepting..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {

            String user = args[0];
            String pickup = args[1];
            String dest=args[2];
            SharedPreferences preferences = getSharedPreferences(mypreference,
                    Context.MODE_PRIVATE);

            String driveremail=preferences.getString("email",null);

            // Building Parameters


            List<NameValuePair> params = new ArrayList<NameValuePair>();



            params.add(new BasicNameValuePair("email", user));
            params.add(new BasicNameValuePair("pickup", pickup));
            params.add(new BasicNameValuePair("destination",dest));
            params.add(new BasicNameValuePair("driver", driveremail));


            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(url_accept_request);
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

    class finishRide extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NotificationActivity.this);
            pDialog.setMessage("Accepting..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {


            SharedPreferences preferences = getSharedPreferences(mypreference,
                    Context.MODE_PRIVATE);

            String driveremail=preferences.getString("email",null);

            // Building Parameters


            List<NameValuePair> params = new ArrayList<NameValuePair>();



            params.add(new BasicNameValuePair("email", driveremail));


            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(url_finish_ride);
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

                }
                else {
                    json_string="";

                }
            } catch (IOException e1) {
                e1.printStackTrace();
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

        }

    }
}
