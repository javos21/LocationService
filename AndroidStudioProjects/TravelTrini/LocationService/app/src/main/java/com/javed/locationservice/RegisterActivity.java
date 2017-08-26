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


public class RegisterActivity extends Activity implements View.OnClickListener{

    public static final String TAG = "RegisterActivity";
    RequestQueue queue;
    String url = "http://162.243.254.172/locationapi/register.php";

    TextView mTextView;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ProgressDialog pDialog;
    DBHelper db;
    Intent intent;

    private Button btnRegister;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = new DBHelper(getApplicationContext());
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextEmail= (EditText) findViewById(R.id.editTextEmail);
        btnRegister = (Button) findViewById(R.id.buttonRegister);
        btnRegister.setOnClickListener(this);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        queue  = Volley.newRequestQueue(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnRegister){
            registerUser();
        }
    }

    private void registerUser (){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        pDialog.setMessage("Registering User ...");
        pDialog.show();

        // Make string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.hide();
                if (response.trim().equals("Successfully Registered")){
                    if (db.registerUser(username) != -1){
                        if (db.storeUser(username)!= -1) {
                            intent = new Intent(getApplicationContext(), QuestionsActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Log.i(TAG, response + " Error entering user in User Table");
                            Toast.makeText(getApplicationContext(), "Error entering user in User Table", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Log.i(TAG, response + " Error entering user in UserLoggedIn Table");
                        Toast.makeText(getApplicationContext(), "Error entering user in UserLoggedIn Table", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "That didn't Work !", Toast.LENGTH_SHORT).show();
                }
            }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("username",username);
                params.put("email",email);
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
