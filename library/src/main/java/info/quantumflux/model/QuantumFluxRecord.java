package info.quantumflux.model;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

import info.quantumflux.QuantumFlux;
import info.quantumflux.model.annotation.Column.Column;
import info.quantumflux.model.annotation.Column.PrimaryKey;

import java.io.Serializable;

/**
 * Basic Content Provider Record implementation that contains a id field
 * that will be android list view compatible, and some helper methods.
 * This class implements Serializable, so domain objects can be passed as
 * serializable objects in android bundles.
 */
public abstract class QuantumFluxRecord<T> extends QuantumFluxBaseRecord<T> implements Serializable {

    @Column(columnName = "_id")
    @PrimaryKey(autoIncrement = true)
    protected Long _id;

    /**
     * Finds a record based on the id column
     *
     * @param context Current context
     * @param id      the id of the record to find
     * @return The record, if found.
     */
    public static <T> T findById(Context context, Class<T> object, long id) {
        return QuantumFlux.findByPrimaryKey(object, id);
    }

    /**
     * @see #findById(Context, Class, long)
     */
    public static <T> T findById(Class<T> object, long id) {
        return QuantumFlux.findByPrimaryKey(object, id);
    }

    /**
     * Checks if this record has an id, if the id is present this record will be updated,
     * if it is null, it will be inserted instead, and the inserted id assigned to this one.
     *
     * @param context The context used to save the record.
     */
    public void save(Context context) {
        if (_id == null) {
            _id = QuantumFlux.insertAndReturn(this)._id;
        } else {
            QuantumFlux.update(this);
        }
    }

    /**
     * @see #save(Context)
     */
    public void save() {
        save(QuantumFlux.getApplicationContext());
    }

    public Long getId() {
        return _id;
    }

    public <T> Uri getItemUri() {
        return ContentUris.withAppendedId(QuantumFlux.getItemUri(getClass()), getId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getId();
    }
}
