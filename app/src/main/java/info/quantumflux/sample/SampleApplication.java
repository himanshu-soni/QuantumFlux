package info.quantumflux.sample;

import android.app.Application;
import android.content.Context;

import info.quantumflux.QuantumFlux;
import info.quantumflux.model.map.SqlColumnMapping;
import info.quantumflux.sample.data.type.BlobTypeMapping;

import java.util.ArrayList;

/**
 * Created by Himanshu on 8/5/2015.
 */
public class SampleApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ArrayList<SqlColumnMapping> mappings = new ArrayList<>();
        mappings.add(new BlobTypeMapping());

        QuantumFlux.initialize(this, mappings);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
