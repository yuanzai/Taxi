package com.TakeTaxi.jy;

import android.app.Application;

public class LastAddress extends Application {

    private String lastPickup;

    public String lastPickup() {
        return lastPickup;
    }

    public void lastPickup(String lastPickup) {
        this.lastPickup = lastPickup;
    }
    
    private String lastDesti;

    public String lastDesti() {
        return lastDesti;
    }

    public void lastDesti(String lastDesti) {
        this.lastDesti = lastDesti;
    }
    
    private int lastAddtime;

    public int lastAddtime() {
        return lastAddtime;
    }

    public void lastAddtime(int lastAddtime) {
        this.lastAddtime = lastAddtime;
    }
}
