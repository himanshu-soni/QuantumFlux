package me.himanshusoni.quantumflux;

/**
 * Created by himanshusoni on 06/09/16.
 */
public interface QuantumFluxDatabaseUpgradeListener {
    void onDatabaseUpgraded(int oldVersion, int newVersion);
}
