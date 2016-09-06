package me.himanshusoni.quantumflux.sample;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

import me.himanshusoni.quantumflux.QuantumFlux;
import me.himanshusoni.quantumflux.QuantumFluxDatabaseUpgradeListener;
import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;
import me.himanshusoni.quantumflux.sample.data.type.BlobTypeMapping;

/**
 * Created by Himanshu on 8/5/2015.
 */
public class SampleApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ArrayList<SqlColumnMapping> mappings = new ArrayList<>();
        mappings.add(new BlobTypeMapping());

        QuantumFlux.initialize(this, mappings, new QuantumFluxDatabaseUpgradeListener() {
            @Override
            public void onDatabaseUpgraded(int oldVersion, int newVersion) {

            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
