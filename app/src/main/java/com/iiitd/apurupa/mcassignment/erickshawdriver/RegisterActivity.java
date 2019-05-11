package com.iiitd.apurupa.mcassignment.erickshawdriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mFullName;
    private EditText mEmailId;
    private EditText mPassword;
    private EditText mCPassword;
    private EditText mPhoneNo;
    private EditText mLicenceNo;
    private Button mRegister;
    private Button mLogin;
    Spinner mspinner;
    boolean isdriver=false;
    private ProgressDialog pDialog;
    private static String url_create_account = "http://192.168.58.165/mc/insertuser.php";
    private static String url_create_driveraccount="http://192.168.58.165/mc/insertdriver.php";
    private String emailPattern = "[a-zA-Z0-9._-]+@iiitd.ac.in";

    private static final String TAG_SUCCESS = "success";
    private ShowMessage toast=new ShowMessage();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        mFullName=(EditText)findViewById(R.id.RegFullnameEditText);
        mEmailId=(EditText)findViewById(R.id.RegEmailEditText);
        mPassword=(EditText)findViewById(R.id.RegPasswordEditText);
        mCPassword=(EditText)findViewById(R.id.RegConfirmPasswordEditText);
        mPhoneNo=(EditText)findViewById(R.id.RegPhonenoEditText);
        mLicenceNo=(EditText)findViewById(R.id.RegLicenceEditText);

        mRegister=(Button)findViewById(R.id.registeraccButton);
        mLogin=(Button)findViewById(R.id.RegAlloginButton);
        mRegister.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        mLicenceNo.setVisibility(View.VISIBLE);

    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.registeraccButton:

                String fullname = mFullName.getText().toString();
                String email = mEmailId.getText().toString();
                String password = mPassword.getText().toString();
                String cpassword=mCPassword.getText().toString();
                String phone=mPhoneNo.getText().toString();
                String licence=null;
                    licence=mLicenceNo.getText().toString();

                if(fullname.equals("")||email.equals("")||password.equals("")||cpassword.equals("")||phone.equals(""))
                {
                    if(isdriver && mLicenceNo.equals("")) {toast.showmessage(getApplicationContext(),"Please Fill All The Fields");break;}
                    else if(!isdriver) {toast.showmessage(getApplicationContext(),"Please Fill All The Fields");break;}
                }
                if(!isEmailValid(email))
                {
                    toast.showmessage(getApplicationContext(),"Not a Correct Email Format");
                    break;
                }
              /*  if(!email.matches(emailPattern))
                {
                    toast.showmessage(getApplicationContext(),"Please Enter a IIITD Email ID");
                    break;
                }*/
                if(!password.equals(cpassword))
                {
                    toast.showmessage(getApplicationContext(),"Entered Passwords dont match");
                    mPassword.setText("");
                    mCPassword.setText("");
                    break;
                }
                Log.d("In regaccButton","ONclick");
                new CreateNewAccount().execute(fullname,email,password,phone,licence);
                break;
            case R.id.RegAlloginButton:Intent back=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(back);
                break;
        }
    }

    /**
     * Background Async Task to Create new account
     * */
    class CreateNewAccount extends AsyncTask<String, String, String> {

        /**
         * Show Progress Dialog
         */
        private String authstatus = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Creating Account..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating account
         */
        protected String doInBackground(String... args) {
            String fullname = args[0];
            String email = args[1];
            String password = args[2];
            String phone = args[3];
            String license = args[4];
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("fullname", fullname));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("license", license));

            URL url = null;
            HttpURLConnection conn = null;
            try {

                url = new URL(url_create_driveraccount);

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
            int responseCode = 0;
            JSONObject jsonresponse = null;
            try {
                responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        json_string += line;
                    }

                } else {
                    json_string = "";

                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }


            JSONObject json = null;


            Log.d("Create Response", json_string);
            try {
                json = new JSONObject(json_string);
                String success = json.getString("success");
                Log.d("SUCCESS",success);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return json_string;
        }

        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params) {
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
         **/
        protected void onPostExecute(String response) {
            // dismiss the dialog once done
            pDialog.dismiss();
            try {
                JSONObject json = new JSONObject(response);
                String success = json.getString("success");
                Log.d("SUCCESS",success);
                if (success.equals("0")) {
                    Log.d("User Info", "INVALID DETAILS");
                    toast.showmessage(getApplicationContext(), "Email id Already Present");
                } else {
                    toast.showmessage(getApplicationContext(), "Details Saved Successfully");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
          /*
*/
        }
    }

    }