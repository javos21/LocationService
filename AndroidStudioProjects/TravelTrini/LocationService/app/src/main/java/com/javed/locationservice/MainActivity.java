package com.javed.locationservice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


public class MainActivity extends Activity{

    private static final String TAG = "MainActivity";

    TextView mTextViewDestination, mTextViewPrediction;
    LocationManager manager;
    DBHelper db;
    Intent intent;
    String lat, lon;
    Float speed;
    String mUsername, mDestination, mPrediction;
    Drawer result;
    PrimaryDrawerItem item1, item2, item3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(getApplicationContext());

        mTextViewDestination = (TextView) findViewById(R.id.currDestView);
        mTextViewPrediction = (TextView) findViewById(R.id.predView);

        //Grab username from login
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mUsername = null;
                mDestination = null;
            } else {
                mUsername = extras.getString("username");
                mDestination = extras.getString("destination");
                mPrediction = extras.getString("prediction");
            }
        } else {
            mUsername = (String) savedInstanceState.getSerializable("username");
            mDestination = (String) savedInstanceState.getSerializable("destination");
            mPrediction = (String) savedInstanceState.getSerializable("prediction");
        }

        item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_home);
        item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_dest);
        item3 = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.drawer_logout);

        result = new DrawerBuilder()
                    .withActivity(this)
                    .withActionBarDrawerToggle(true)
                    .addDrawerItems(
                        item1,
                        item2,
                        item3
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener(){
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            if(drawerItem.getIdentifier() == 2){
                                intent = new Intent(getApplicationContext(), DestinationActivity.class);
                                intent.putExtra("username", mUsername);
                                startActivity(intent);
                                finish();
                            }
                            if(drawerItem.getIdentifier() == 3){
                                db.logoutUser(mUsername);
                                stopService(new Intent(getBaseContext(), LocationService.class));
                                intent = new Intent(getApplicationContext(), SignInActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            return false;
                        }
                    })
                    .build();

        //Check if GPS is enabled
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
            startActivity(getIntent());
        }
        startService(new Intent(getBaseContext(), LocationService.class));
        mTextViewDestination.setText(mDestination);
        mTextViewPrediction.setText(mPrediction);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    void updateUI() {

    }

    void updateDB() {

        //db.updateUserCoordinates(mUsername,lat,lon,speed.toString());
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is Setting");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu ?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

}
