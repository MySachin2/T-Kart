package com.phacsin.t__kart;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sachin on 22/10/17.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
