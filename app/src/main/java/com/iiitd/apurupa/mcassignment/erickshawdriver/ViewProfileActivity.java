package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ViewProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mphone;
    private EditText mname;
    private EditText mpassword;
    private TextView memail;
    private TextView mstatus;
    private Button mupdatebutton;
    public static final String mypreference = "mypref";
    private static String url_get_profiledetails = "http://192.168.58.165/mc/view_driver.php";
    private static String url_update_profiledetails="http://192.168.58.165/mc/update_driver.php";
    private ProgressDialog pDialog,pDialog1;
    public boolean update_clicked=false;
    private ShowMessage toast=new ShowMessage();
    private String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();
        SharedPreferences preferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        email=preferences.getString("email","");
        memail.setText(email);
        new ProfileDetails().execute(email);
    }

    private void init() {
        mphone=(EditText)findViewById(R.id.phoneeditText);
        mname=(EditText)findViewById(R.id.nameeditText);
        mpassword=(EditText)findViewById(R.id.pwdeditText);
        memail=(TextView) findViewById(R.id.emailtextView);
        mstatus=(TextView)findViewById(R.id.statustextView);

        mupdatebutton=(Button)findViewById(R.id.updateProfileButton);
        mupdatebutton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.updateProfileButton:
                String name=mname.getText().toString().trim();
                String pwd=mpassword.getText().toString().trim();
                String mobile=mphone.getText().toString().trim();
                update_clicked=true;

                new ProfileDetails().execute(email,name,pwd,mobile);
                // update_clicked=false;
                break;
        }
    }


    class ProfileDetails extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog1 = new ProgressDialog(ViewProfileActivity.this);
            pDialog1.setMessage("Waiting...");
            pDialog1.setIndeterminate(false);
            pDialog1.setCancelable(true);
            pDialog1.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {


            String useremail=args[0];
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", useremail));
            String finalurl="";
            if(update_clicked==true)
            {
                params.add(new BasicNameValuePair("name",args[1]));
                params.add(new BasicNameValuePair("password", args[2]));
                params.add(new BasicNameValuePair("mobile", args[3]));
                finalurl=url_update_profiledetails;
                Log.d("Final url",finalurl);
            }
            else
            {
                finalurl=url_get_profiledetails;
            }
            Log.d("Final",finalurl);
            Log.d("Update",String.valueOf(update_clicked));
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL(finalurl);
                Log.d("url", String.valueOf(url));
                conn = (java.net.HttpURLConnection) url.openConnection();
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


            int responseCode = 0;

            String json_string ="";
            try {
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        json_string += line;

                    }
                    Log.d("ride response", json_string);


                } else {
                    json_string = "";

                }

                return json_string;
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            Log.d("GET QUERY",result.toString());
            return result.toString();
        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String response) {
            // dismiss the dialog once done

            Log.d("POST PUSH",response);
            if(update_clicked==false) {
                JSONObject jsonresponse = null;
                try {
                    jsonresponse = new JSONObject(response);
                    if (jsonresponse.length() != 0) {


                        String name = jsonresponse.getString("fullname");
                        String phone = jsonresponse.getString("mobile");
                        String pwd = jsonresponse.getString("password");

                        mpassword.setText(pwd);
                        mphone.setText(phone);
                        mname.setText(name);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                JSONObject jsonresponse = null;
                try {
                    jsonresponse = new JSONObject(response);
                    if (jsonresponse.length() != 0) {
                        String status = jsonresponse.getString("status");
                        mstatus.setText(status);
                        update_clicked=false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            pDialog1.dismiss();
        }

    }

}

