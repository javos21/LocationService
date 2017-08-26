package com.javed.locationservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

/**
 * Created by Javed on 20/07/2016.
 */
public class NotificationMessage extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    DBHelper db;
    private Spinner spinnerTransport;
    private Button btnSubmit;
    String transport;
    Intent intent;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        db = new DBHelper(getApplicationContext());
        spinnerTransport = (Spinner) findViewById(R.id.spinnerTransport);
        spinnerTransport.setOnItemSelectedListener(this);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        transport = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        db.updateUserTransport(db.getUserLoggedIn(),transport);
        intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
