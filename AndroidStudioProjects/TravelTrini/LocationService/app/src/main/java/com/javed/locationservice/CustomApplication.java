package com.javed.locationservice;

import android.app.Application;

/**
 * Created by Javed on 17/07/2016.
 */
public class CustomApplication extends Application {

    public void onCreate(){
        super.onCreate();
        //Stetho.initializeWithDefaults(this);
    }
}
