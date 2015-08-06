package info.quantumflux.model.annotation.Column;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field is a primary key field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

    /**If this field should be auto incremented. Default is true.  Requires that the field type is long*/
    boolean autoIncrement() default true;
}
