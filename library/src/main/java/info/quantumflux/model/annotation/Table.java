package info.quantumflux.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java object as table model
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * the table name to be used for this object.  The default will converted the object name and use that instead.
     */
    String tableName() default "";

    /**
     * Any additional constraints that should be added for the table.  Todo: Not implemented yet
     */
    TableConstraint[] constraints() default {};
}
