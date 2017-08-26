package com.javed.locationservice;

/**
 * Created by Javed on 16/07/2016.
 */
public class User {

    String username;
    Double latitude;
    Double longitude;
    String speed;
    String currDestination;
    String currtime;
    String transport;

    //constructors
    public User(){

    }

    public User(String username){
        this.username = username;
    }

    public User(String username, Double lat, Double lng, String speed, String dest, String time, String trans){
        this.username = username;
        this.latitude = lat;
        this.longitude = lng;
        this.speed = speed;
        this.currDestination = dest;
        this.currtime = time;
        this.transport = trans;
    }

    //Getters
    public String getUsername(){
        return this.username;
    }

    public Double getLatitude(){
        return this.latitude;
    }

    public Double getLongitude(){
        return this.longitude;
    }

    public String getSpeed(){
        return this.speed;
    }

    public String getCurrDestination(){
        return this.currDestination;
    }

    public String getCurrtime(){
        return this.currtime;
    }

    public String getTransport(){
        return this.transport;
    }

    //Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setCurrDestination(String currDestination) {
        this.currDestination = currDestination;
    }

    public void setCurrtime(String currtime) {
        this.currtime = currtime;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}
