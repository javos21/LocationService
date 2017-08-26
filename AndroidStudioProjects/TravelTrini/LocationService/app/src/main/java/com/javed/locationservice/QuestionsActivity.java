package com.javed.locationservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

/**
 * Created by Javed on 09/06/2016.
 */
public class QuestionsActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    public static final String TAG = "RegisterActivity";
    RequestQueue queue;
    String url = "http://162.243.254.172/locationapi/questions.php";

    String username, transport;

    TextView mTextView;
    private EditText editTextDestination;
    private EditText editTextTime;
    private EditText editTextCommmute;
    private Spinner  spinnerTransport;

    DBHelper db;
    private Button btnSubmit;
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        db = new DBHelper(getApplicationContext());

        editTextDestination = (EditText) findViewById(R.id.editTextDestination);
        editTextTime = (EditText) findViewById(R.id.editTextTime);
        editTextCommmute = (EditText) findViewById(R.id.editTextCommute);

        spinnerTransport = (Spinner) findViewById(R.id.spinnerTransport);
        spinnerTransport.setOnItemSelectedListener(this);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(this);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                username = null;
            } else {
                username = extras.getString("username");
            }
        } else {
            username = (String) savedInstanceState.getSerializable("username");
        }
        queue  = Volley.newRequestQueue(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnSubmit){
            SubmitAnswers();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        transport = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void SubmitAnswers (){
        final String destination = editTextDestination.getText().toString().trim();
        final String time = editTextTime.getText().toString().trim();
        //final String transport = editTextTransport.getText().toString().trim();
        final String commute = editTextCommmute.getText().toString().trim();
        // Make string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //JSONObject jsonresponse = new JSONObject(response).getJSONObject("form")
                        //String site = jsonrespons
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        Log.i("QuestionsActivity", username + ' '+ destination + ' ' + time + ' ' + transport + ' ' + commute);
                        if (response.trim().equals("Questions updated")){
                            intent = new Intent(getApplicationContext(), DestinationActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("username",username);
                params.put("destination",destination);
                params.put("time",time);
                params.put("transport",transport);
                params.put("commute",commute);
                return params;
            }
        };

        // Set the tag on the request
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

}
