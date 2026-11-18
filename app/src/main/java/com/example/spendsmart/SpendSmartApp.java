package com.example.spendsmart;

import android.app.Application;

import com.example.spendsmart.di.AppContainer;

public class SpendSmartApp extends Application {
    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    public AppContainer getContainer() {
        return container;
    }
}
