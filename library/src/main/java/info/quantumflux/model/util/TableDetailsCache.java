package info.quantumflux.model.util;

import android.content.Context;

import info.quantumflux.model.generate.ReflectionHelper;
import info.quantumflux.model.generate.TableDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will maintain a mCache of all the java objects and their relevant table details,
 * the main function of this is to reduce the amount of times we have to use reflection to get the table details.
 * All of the table details are loaded on demand.  The methods on this class is synchronized to prevent
 * multiple threads from altering the mCache at the same time.
 */
public class TableDetailsCache {

    private final Map<Class<?>, TableDetails> mCache;

    public TableDetailsCache() {
        mCache = new HashMap<Class<?>, TableDetails>();
    }

    /**
     * Initializes the mCache with all of the supplied entries
     *
     * @param objects The objects for which to retrieve table details
     */
    public synchronized void init(Context context, List<Class<?>> objects) {
        for (int i = 0; i < objects.size(); i++) {
            Class<?> object = objects.get(i);
            findTableDetails(context, object);
        }
    }

    /**
     * Attempts to find the table details for the supplied object from the local mCache.
     *
     * @param object The object to find the table details for
     * @return The {@link TableDetails} for the supplied object if it is found
     */
    public synchronized TableDetails findTableDetails(Context context, Class<?> object) {

        if (!mCache.containsKey(object)) {
            try {
                mCache.put(object, ReflectionHelper.getTableDetails(context, object));
            } catch (Exception ex) {
                throw new QuantumFluxException("Failed load table details for object " + object.getSimpleName(), ex);
            }

            //Check if it exists after we attempted to add it
            if (!mCache.containsKey(object))
                throw new QuantumFluxException("No table details could be found for supplied object: " + object.getSimpleName());
        }

        return mCache.get(object);
    }
}
