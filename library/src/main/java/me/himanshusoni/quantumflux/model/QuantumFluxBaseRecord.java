package me.himanshusoni.quantumflux.model;

import android.content.ContentProviderOperation;

import java.util.Iterator;

import me.himanshusoni.quantumflux.QuantumFlux;

/**
 * This class is just a wrapper for {@link QuantumFlux}, sub classes can extend this
 * to invoke the basic crud operations on the class itself.
 */
public abstract class QuantumFluxBaseRecord<T> {

    public QuantumFluxBaseRecord() {
    }

    public Iterator<T> findAll() {
        return (Iterator<T>) QuantumFlux.findAll(getClass());
    }

    public T findByPrimaryKey(Object key) {
        return (T) QuantumFlux.findByPrimaryKey(getClass(), key);
    }

    public void insert() {
        QuantumFlux.insert(this);
    }

    public ContentProviderOperation prepareInsert() {
        return QuantumFlux.prepareInsert(this);
    }

    public T insertAndReturn() {
        return (T) QuantumFlux.insertAndReturn(this);
    }

    public void update() {
        QuantumFlux.update(this);
    }

    public ContentProviderOperation prepareUpdate() {
        return QuantumFlux.prepareUpdate(this);
    }

    public void delete() {
        QuantumFlux.delete(this);
    }

    public ContentProviderOperation prepareDelete() {
        return QuantumFlux.prepareDelete(this);
    }
}
