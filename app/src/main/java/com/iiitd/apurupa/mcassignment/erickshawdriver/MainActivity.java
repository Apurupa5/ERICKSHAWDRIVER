package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText memailEditText;
    private EditText mpasswordEditText;
    private Button mLoginButton;
    private Button mSignUpButton;
    private ProgressDialog pDialog;
    public static final String mypreference = "mypref";
    JSONParser jsonParser = new JSONParser();
    private static String url_create_account = "http://192.168.58.165/mc/logintest.php";
    private static final String TAG_SUCCESS = "success";
    private static MainActivity parent;
    private ShowMessage toast=new ShowMessage();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        memailEditText=(EditText)findViewById(R.id.LoginEmailEditText);
        mpasswordEditText=(EditText)findViewById(R.id.LoginPasswordEditText);
        mLoginButton=(Button)findViewById(R.id.loginButton);
        mSignUpButton=(Button)findViewById(R.id.signUpButton);
        mLoginButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.loginButton:
                String email=memailEditText.getText().toString();
                String password=mpasswordEditText.getText().toString();
                if(email.equals("")||password.equals(""))
                {
                    toast.showmessage(getApplicationContext(),"Please Fill All The fields");
                    break;
                }
                new LoginCheck().execute(email,password);

                break;
//            case R.id.signUpButton:
//
//                Intent I=new Intent(getApplicationContext(),RegisterActivity.class);
//                startActivity(I);
//                break;
        }
    }

    class LoginCheck extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Authenticating..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {

            String email = args[0];
            String password = args[1];

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            URL url = null;
            HttpURLConnection conn=null;
            try {
                url = new URL(url_create_account);
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
                        Log.d("User Info","INVALID DETAILS");
                        //toast.showmessage(getApplicationContext(), "Invalid Details");
                    } else {
                        if (authstatus.equals("1")) {
                            String usertype = jsonresponse.getString("type");
                            if (usertype.equals("driver")) {
                                SharedPreferences preferences = getSharedPreferences(mypreference,
                                        Context.MODE_PRIVATE);

                                SharedPreferences.Editor edt = preferences.edit();
                                edt.putString("email", email);
                                edt.apply();
                                edt.commit();
                                //register();
                                SharedPreferences prefs = getSharedPreferences(mypreference,
                                        Context.MODE_PRIVATE);
                                String useremail=prefs.getString("email","");
                                String token=prefs.getString("Token","");
                                Log.d("Username:",useremail);
                                Log.d("Token In Driver:",token);
                                register(useremail,token);

                                Intent driver = new Intent(getApplicationContext(), DriverMapsActivity.class);
                                startActivity(driver);
                            } else {
                                if (usertype.equals("user")) {
                                    // Intent driver=new Intent(getApplicationContext(),UserActivity.class);
                                    //startActivity(driver);
                                  /*  Uri gmmIntentUri = Uri.parse("geo:28.5473,77.2732");
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    startActivity(mapIntent);*/
//
//                                    Intent user=new Intent(getApplicationContext(),TrialMapsActivity.class);
//                                    startActivity(user);
                                }
                            }

                            Log.d("Json Response in try", jsonresponse.toString());
                            Log.d("USer type", usertype);
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

    private void register(String useremail,String token) {
          Log.d("In reigster:",token);
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("email",useremail)
                    .add("Token",token)
                    .build();

            Request request = new Request.Builder()
                    .url("http://192.168.58.165/mc/registertest.php")
                    .post(body)
                    .build();

            try {
                client.newCall(request).execute();
                Log.d("In ID Service:",token);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                toast.showmessage(getApplicationContext(), "Invalid Details");
            }
        }

    }
}
