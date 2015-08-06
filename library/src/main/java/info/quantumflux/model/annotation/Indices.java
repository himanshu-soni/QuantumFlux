package info.quantumflux.model.annotation;

import java.lang.annotation.*;

/**
 * Used to indicate all of the indices to be created for a table.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Indices {

    /**
     * A collection of indices to be created for the table
     */
    Index[] indices();
}
