package info.quantumflux.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates the specified index for the table
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Index {

    /**
     * The name of this index
     */
    String indexName();

    /**
     * The columns that will be included in the index
     */
    String[] indexColumns();
}
