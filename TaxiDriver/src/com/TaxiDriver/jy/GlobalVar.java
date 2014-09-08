package com.TaxiDriver.jy;

import java.util.ArrayList;

import android.app.Application;

public class GlobalVar extends Application{

	// jobList array
    private String[][] jobList;
    public String[][] jobList() {
        return jobList;
    }

    public void jobList(String[][] jobList) {
        this.jobList = jobList;
    }
    
    //driver lat
    private int driverLat;
    public int driverLat() {
        return driverLat;
    }

    public void driverLat(int driverLat) {
        this.driverLat = driverLat;
    }
     
    //driver longi
    private int driverLongi;
    public int driverLongi() {
        return driverLongi;
    }

    public void driverLongi(int driverLongi) {
        this.driverLongi = driverLongi;
    }	
	
    //driver avail
    private String driverAvail;
    public String driverAvail() {
        return driverAvail;
    }

    public void driverAvail(String driverAvail) {
        this.driverAvail = driverAvail;
    }
    
    //driver id
    private String driver_id;
    public String driver_id() {
        return driver_id;
    }

    public void driver_id(String driver_id) {
        this.driver_id = driver_id;
    }  
    
    //reject list
    private ArrayList<Object> rejectList;
    public ArrayList<Object> rejectList() {
        return rejectList;
    }

    public void rejectList(ArrayList<Object> rejectList) {
        this.rejectList = rejectList;
    } 
    
    //TDA on-off
    private boolean isTDAActive;
    public boolean isTDAActive() {
        return isTDAActive;
    }

    public void isTDAActive(boolean isTDAActive) {
        this.isTDAActive = isTDAActive;
    }  
    
    //OA
    private boolean isOAActive;
    public boolean isOAActive() {
        return isOAActive;
    }

    public void isOAActive(boolean isOAActive) {
        this.isOAActive = isOAActive;
    }  
}
