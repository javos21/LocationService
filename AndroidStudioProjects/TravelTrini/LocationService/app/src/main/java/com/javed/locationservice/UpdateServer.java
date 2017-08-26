package com.javed.locationservice;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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
 * Created by Javed on 20/07/2016.
 */
public class UpdateServer extends AsyncTask<String,Void,String> {

    private static final String TAG = "UpdateServer";
    private Context mContext;
    RequestQueue queue;
    String updateLocationURL = "http://162.243.254.172/locationapi/updatelocation.php";
    User user;

    public UpdateServer(Context context){
        mContext = context;
    }

    DBHelper db;

    @Override
    protected String doInBackground(String... params) {
        try {
            Thread.sleep(30000);
            updateServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result){

    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        db = new DBHelper(mContext);
        queue = Volley.newRequestQueue(mContext);
        user = new User();
    }

    @Override
    protected void onProgressUpdate(Void... values){

    }

    void updateServer() {

        user = db.getUser(db.getUserLoggedIn());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateLocationURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //JSONObject jsonresponse = new JSONObject(response).getJSONObject("form")
                        //String site = jsonrespons
                        Log.i(TAG, response);
                        Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "error");
                        Toast.makeText(mContext, "error", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", user.getUsername());
                params.put("lat", user.getLatitude().toString());
                params.put("long", user.getLongitude().toString());
                params.put("speed", user.getSpeed());
                return params;
            }
        };

        // Set the tag on the request
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }
}
