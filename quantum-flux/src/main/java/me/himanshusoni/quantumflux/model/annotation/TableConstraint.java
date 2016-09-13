package me.himanshusoni.quantumflux.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An additional constraint that should be applied to the table
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableConstraint {
    public enum Type {
        PRIMARY_KEY,
        UNIQUE
    }

    /**
     * The name of this constraint
     */
    String name();

    /**
     * The type of constraint.  Primary key should not be used as currently we only support a single primary key field
     */
    Type constraintType();

    /**
     * The columns on which the constraint applies
     */
    String[] constraintColumns();
}
