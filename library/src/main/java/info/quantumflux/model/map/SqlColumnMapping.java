package info.quantumflux.model.map;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * This allows us to handle the mapping of objects to and from sql
 * in an easy way without having to worry about the implicit type
 */
public interface SqlColumnMapping {

    /** The java type this mapping will represent */
    Class<?> getJavaType();

    /** The SQL equivalent column name that will be used to store this type */
    String getSqlColumnTypeName();

    /** Converts the source object to the correct representation to be stored in the sql database.
     *  For example, a Date might be stored as a millisecond long, this will convert the date object to milliseconds.
     */
    Object toSqlType(Object source);

    /** Gets the appropriate column value from the column in the cursor, it will then
     *  convert that to the correct java type.
     *  For example, a Date might be stored as a millisecond long, this will take the long, and a create a new Data object.
     */
    Object getColumnValue(Cursor cursor, int columnIndex);

    /**
     * This will convert and set the correct object type for supplied value.
     * An explanation for the conversion can be found on toSqlType(Object source);
     * @param contentValues The content values that the object will be placed in
     * @param key The key that the object should placed into
     * @param value The source object to be converted
     */
    void setColumnValue(ContentValues contentValues, String key, Object value);
}
