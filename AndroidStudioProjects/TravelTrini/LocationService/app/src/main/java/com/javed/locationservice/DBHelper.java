package com.javed.locationservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Javed on 04/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper{

    //Logcat Tag
    private static final String TAG = "DBHelper";

    //Database
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "UserDB";

    //Table Names
    public static final String TABLE_USER = "user";
    public static final String TABLE_USER_DESTINATION = "userdestination";
    public static final String TABLE_USER_LOGGED_IN = "userloggedin";

    //Common Column
    public static final String USERNAME = "username";

    //User Table Columns
    public static final String USER_LATITUDE = "latitude";
    public static final String USER_LONGITUDE = "longitude";
    public static final String USER_SPEED = "speed";

    //User DestinationActivity Columns
    public static final String USER_DESTINATION = "destination";
    public static final String USER_DESTINATION_LATITUDE = "destlatitude";
    public static final String USER_DESTINATION_LONGITUDE = "destlongitude";
    public static final String USER_CURRENT_TIME = "currtime";
    public static final String USER_TRANSPORT = "transport";

    //User Logged In Columns
    public static final String USER_LOGGED_IN_ID = "id";
    public static final String USER_LOGGED_IN_LOGGED_IN = "loggedin";

    //Table Create Statements
    //User Table
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "(" +
            USERNAME + " TEXT PRIMARY KEY," + USER_LATITUDE + " TEXT," +
            USER_LONGITUDE + " TEXT," + USER_SPEED + " NUMERIC" + ")";

    //User DestinationActivity Table
    private static final String CREATE_TABLE_USER_DESTINATION = "CREATE TABLE " + TABLE_USER_DESTINATION + "(" +
            USERNAME + " TEXT PRIMARY KEY, " + USER_DESTINATION + " TEXT, " + USER_DESTINATION_LATITUDE + " TEXT, " +
            USER_DESTINATION_LONGITUDE + " TEXT, " + USER_CURRENT_TIME + " TEXT, " + USER_TRANSPORT +
            " TEXT" + ")";

    //User Logged In Table
    private static final String CREATE_TABLE_USER_LOGGED_IN = "CREATE TABLE " + TABLE_USER_LOGGED_IN + "(" +
            USER_LOGGED_IN_ID + " INTEGER PRIMARY KEY," + USERNAME + " TEXT," +
            USER_LOGGED_IN_LOGGED_IN + " NUMERIC" + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_USER_DESTINATION);
        db.execSQL(CREATE_TABLE_USER_LOGGED_IN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DESTINATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LOGGED_IN);
        onCreate(db);
    }

    // USER FUNCTIONS

    public long storeUser (String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username.trim());
        values.put(USER_LATITUDE, 0.0);
        values.put(USER_LONGITUDE, 0.0);
        values.put(USER_SPEED, 0);
        return db.insert(TABLE_USER, null, values);
    }

    public int updateUserCoordinates(String username, Double lat, Double lng, Float speed){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username.trim());
        values.put(USER_LATITUDE, String.valueOf(lat));
        values.put(USER_LONGITUDE, String.valueOf(lng));
        values.put(USER_SPEED, String.valueOf(speed));
        return db.update(TABLE_USER, values, USERNAME + " = ?", new String[]{username});
    }

    public User getUser(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        User user = new User();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " +
                USERNAME + " = ?";
        Cursor c = db.rawQuery(query, new String[]{username});
        Log.i(TAG, query);
        if (c != null && c.moveToFirst()){
            Log.i(TAG, c.getCount() + c.getString(c.getColumnIndex("username")));
            user.setUsername(c.getString(c.getColumnIndex(USERNAME)));
            user.setLatitude(c.getDouble(c.getColumnIndex(USER_LATITUDE)));
            user.setLongitude(c.getDouble(c.getColumnIndex(USER_LATITUDE)));
            user.setSpeed(c.getString(c.getColumnIndex(USER_SPEED)));
        }

        return user;
    }

    // USER DESTINATION FUNCTIONS

    public String checkUserDest(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        String result = "";
        String query = "SELECT " + USERNAME + " FROM " + TABLE_USER_DESTINATION +
                " WHERE " +  USERNAME + " = ?";
        Cursor c = db.rawQuery(query, new String[]{username});
        Log.i(TAG, query);
        if (c != null && c.moveToFirst()){
            result = c.getString(c.getColumnIndex("username"));
            Log.i(TAG, c.getCount() + c.getString(c.getColumnIndex("username")));
            return result;
        }
        else{
            return "";
        }

    }

    public long storeUserDestination (String username, String dest, String destLat, String destLng, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username.trim());
        values.put(USER_DESTINATION, dest);
        values.put(USER_DESTINATION_LATITUDE, destLat);
        values.put(USER_DESTINATION_LONGITUDE, destLng);
        values.put(USER_CURRENT_TIME, time);
        values.put(USER_TRANSPORT, "");
        return db.insert(TABLE_USER_DESTINATION, null, values);
    }

    public int updateUserDestination(String username, String dest, String destLat, String destLng, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username.trim());
        values.put(USER_DESTINATION, dest);
        values.put(USER_DESTINATION_LATITUDE, destLat);
        values.put(USER_DESTINATION_LONGITUDE, destLng);
        values.put(USER_CURRENT_TIME, time);
        return db.update(TABLE_USER_DESTINATION, values, USERNAME + " = ?", new String[]{username});
    }

    public int updateUserTransport(String username, String trans){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(USER_TRANSPORT, trans);
        return db.update(TABLE_USER_DESTINATION, values, USERNAME + " = ?", new String[]{username});
    }

    // USER LOGGED IN FUNCTIONS

    public String getUserLoggedIn(){
        SQLiteDatabase db = this.getReadableDatabase();
        String result = "";
        String query = "SELECT " + USERNAME + " FROM " + TABLE_USER_LOGGED_IN +
                " WHERE " + USER_LOGGED_IN_LOGGED_IN + " = 1";
        Cursor c = db.rawQuery(query, null);
        Log.i(TAG, query);
        if (c != null && c.moveToFirst()){
            result = c.getString(c.getColumnIndex("username"));
            Log.i(TAG, c.getCount() + c.getString(c.getColumnIndex("username")));
            return result;
        }
        else{
            return "";
        }

    }

    public int numofRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_USER_LOGGED_IN);
        return numRows;
    }

    public long registerUser(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username.trim());
        values.put(USER_LOGGED_IN_LOGGED_IN, 1);
        return db.insert(TABLE_USER_LOGGED_IN, null, values);
    }

    public int loginUser(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(USER_LOGGED_IN_LOGGED_IN, 1);
        return db.update(TABLE_USER_LOGGED_IN, values, USERNAME + " = ?", new String[]{username});
    }

    public int logoutUser(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(USER_LOGGED_IN_LOGGED_IN, 0);
        return db.update(TABLE_USER_LOGGED_IN, values, USERNAME + " = ?", new String[]{username});
    }


}
