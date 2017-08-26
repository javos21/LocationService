package com.javed.locationservice;

/**
 * Created by Javed on 18/07/2016.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    Location location;
    Criteria criteria;
    String provider;
    public Location previousBestLocation = null;
    Timer timer;

    DBHelper db;
    String username;
    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DBHelper(this);
        intent = new Intent(BROADCAST_ACTION);
        timer = new Timer();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        repeatingServerUpdates();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service started");
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"No permissions");
            return START_NOT_STICKY  ;
        }

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        provider = locationManager.getBestProvider(criteria,true);
        if (provider == null){
            Log.i(TAG, provider + " not working. Its null");
        }
        locationManager.requestLocationUpdates(provider, TWO_MINUTES, 0, listener);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        locationManager.removeUpdates(listener);
        locationManager = null;
        stopSelf();
        timer.cancel();
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    private void repeatingServerUpdates(){
        final Handler handler = new Handler();
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UpdateServer updateServer = new UpdateServer(getApplicationContext());
                            updateServer.execute();
                            Log.i(TAG,"Server updated successfully");
                        }catch(Exception e){

                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, TWO_MINUTES);
    }

    public void sendNotification() {
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("TriniTravel")
                .setContentText("Are you currently travelling by taxi, bus or ferry ?")
                .setSmallIcon(R.drawable.notification_icon);
        Intent intent = new Intent(this, NotificationMessage.class);

        //Stack ensures navigating backward form the activity goes to the home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationMessage.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, mBuilder.build());

    }

    public class MyLocationListener implements LocationListener {

        String TAG = "MyLocationListener";

        public void onLocationChanged(final Location loc) {
            if(isBetterLocation(loc, previousBestLocation)) {
                username = db.getUserLoggedIn();
                if(loc != null) {
                    if (db.updateUserCoordinates(username, loc.getLatitude(), loc.getLongitude(), loc.getSpeed()) > 0) {
                        Log.i(TAG, "User Coordinates updated");
                    }
                    if (loc.getSpeed() > 30){
                        sendNotification();
                    }

                    Log.i(TAG, "Location changed " + loc.getLatitude() + " " + loc.getLongitude());

                    //intent.putExtra("Latitude", loc.getLatitude());
                    //intent.putExtra("Longitude", loc.getLongitude());
                    //intent.putExtra("Provider", loc.getProvider());
                    //sendBroadcast(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Location is null", Toast.LENGTH_SHORT).show();
                }

            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}