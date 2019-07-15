package com.example.pocketmanager;

import android.app.Application;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

public class PocketManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Analytics analytics = new Analytics.Builder(this, "O9UXujH68HoaTdvddxqXhvCo1VYY9Vdq")
                .trackApplicationLifecycleEvents()
                .recordScreenViews()
                .build();

        Analytics.setSingletonInstance(analytics);
        Analytics.with(this).track("Application Started");
    }





}
