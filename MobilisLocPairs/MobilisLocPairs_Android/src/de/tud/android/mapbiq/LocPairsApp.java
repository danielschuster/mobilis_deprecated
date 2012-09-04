package de.tud.android.mapbiq;

import android.content.Context;

public class LocPairsApp extends android.app.Application {

    private static LocPairsApp instance;

    public LocPairsApp() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}
