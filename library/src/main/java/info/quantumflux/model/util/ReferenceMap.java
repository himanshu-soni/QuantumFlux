package info.quantumflux.model.util;

import android.content.Context;


import info.quantumflux.QuantumFlux;
import info.quantumflux.model.annotation.References;
import info.quantumflux.model.generate.ReflectionHelper;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;

public class ReferenceMap extends IdentityHashMap<Class<?>, SoftReference<Object>> {

    private final Object mReferenceObject;

    public ReferenceMap(Object referenceObject) {
        this.mReferenceObject = referenceObject;
    }

    @SuppressWarnings("unchecked")
    public <T> T findReferent(Class<T> referenceToFind) {
        if (containsKey(referenceToFind)) {
            SoftReference<Object> softReference = super.get(referenceToFind);
            Object referent = softReference.get();
            if (referent != null) {
                return (T) referent;
            }
        }

        for (Field field : ReflectionHelper.getAllObjectFields(mReferenceObject.getClass())) {
            try {
                if (field.isAnnotationPresent(References.class) && field.getAnnotation(References.class).value() == referenceToFind) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    T reference = QuantumFlux.findByPrimaryKey(referenceToFind, field.get(mReferenceObject));

                    if (reference != null) {
                        put(referenceToFind, new SoftReference<Object>(reference));
                    }

                    return reference;
                }
            } catch (IllegalAccessException e) {
                throw new QuantumFluxException("Could not access required field " + field.getName(), e);
            }
        }
        throw new QuantumFluxException("No Reference found to " + referenceToFind.getSimpleName() + " from " + mReferenceObject.getClass().getSimpleName());
    }
}
