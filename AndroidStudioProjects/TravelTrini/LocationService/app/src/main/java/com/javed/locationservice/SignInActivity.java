package com.javed.locationservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


public class SignInActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "SignInActivity";
    RequestQueue queue;
    String url = "http://162.243.254.172/locationapi/login.php";

    TextView mTextView;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private ProgressDialog pDialog;
    DBHelper db;
    Intent intent;

    Button btnLogin;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DBHelper(getApplicationContext());
        if (db.numofRows() > 0) {
            if (db.getUserLoggedIn() != "") {
                intent = new Intent(getApplicationContext(), DestinationActivity.class);
                intent.putExtra("username", db.getUserLoggedIn());
                startActivity(intent);
                finish();
            }
        }
        mTextView = (TextView)findViewById(R.id.registerlink);
        mTextView.setOnClickListener(this);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        queue  = Volley.newRequestQueue(this);
        btnLogin = (Button) findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin){
            signInUser();
        }
        if (v == mTextView){
            intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void signInUser(){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        pDialog.setMessage("Logging in ...");
        pDialog.show();
        // Make string request

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.hide();
                        if (response.trim().equals("success")){
                            if(db.loginUser(username) > 0){
                                intent = new Intent(getApplicationContext(), DestinationActivity.class);
                                intent.putExtra("username", username);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Log.i(TAG, response + " Error updating UserLoggedIn Table");
                                Toast.makeText(getApplicationContext(),"Error updating UserLoggedIn Table",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"HTTP request Error",Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };

        // Set the tag on the request
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    public void checkUserLoggedIn(){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        // Make string request

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("success")){
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"HTTP request Error",Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };

        // Set the tag on the request
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }
}
