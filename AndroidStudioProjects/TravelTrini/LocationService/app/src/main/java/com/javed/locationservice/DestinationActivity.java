package com.javed.locationservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Javed on 18/07/2016.
 */
public class DestinationActivity extends Activity implements PlaceSelectionListener, View.OnClickListener{

    private static final String TAG = "DestinationActivity";
    RequestQueue queue;
    String url = "http://162.243.254.172/locationapi/prediction.php";

    private TextView mPlaceDetailsText, mPlaceAttribution;
    private Button mBtnSubmitDestination;
    String mUsername, selectedAddress, mPrediction;
    LatLng latLng;
    Double lat, lng;
    Intent intent;
    DBHelper db;
    private ProgressDialog pDialog;
    Drawer result;
    PrimaryDrawerItem item1, item2, item3;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        // Retrieve the TextViews that will display details about the selected place.
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);
        mBtnSubmitDestination = (Button) findViewById(R.id.btnDestSubmit);
        mBtnSubmitDestination.setOnClickListener(this);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new DBHelper(getApplicationContext());
        //Grab username from login
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mUsername = null;
            } else {
                mUsername = extras.getString("username");
            }
        } else {
            mUsername = (String) savedInstanceState.getSerializable("username");
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
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1) {
                            intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("username", mUsername);
                            startActivity(intent);
                            finish();
                        }
                        if (drawerItem.getIdentifier() == 3) {
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
        queue  = Volley.newRequestQueue(this);


    }

    @Override
    public void onClick(View v) {
        if(v == mBtnSubmitDestination){
            latLng = getLocationFromAddress(this,selectedAddress);
            String currentTimeString = DateFormat.getTimeInstance().format(new Date());
            if (db.checkUserDest(mUsername) != ""){
                db.updateUserDestination(mUsername, selectedAddress, String.valueOf(latLng.latitude), String.valueOf(latLng.longitude), currentTimeString);
            }
            else{
                db.storeUserDestination(mUsername,selectedAddress,String.valueOf(latLng.latitude),String.valueOf(latLng.longitude),currentTimeString);
            }
            makePrediction();
        }
    }

    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        // Format the returned place's details and display them in the TextView.
        mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),
                place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));
        selectedAddress = (String)place.getAddress();
        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            mPlaceAttribution.setText("");
        }

    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */

    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public void makePrediction(){
        pDialog.setMessage("Making Prediction ...");
        pDialog.show();
        // Make string request

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.hide();
                        mPrediction = response.trim();
                        Log.i(TAG, response);
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("username", mUsername);
                        intent.putExtra("destination",selectedAddress);
                        intent.putExtra("prediction", mPrediction);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"HTTP request Error",Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Set the tag on the request
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue
        queue.add(stringRequest);

    }
}
