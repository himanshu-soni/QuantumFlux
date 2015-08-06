package info.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import info.quantumflux.model.map.SqlColumnMapping;

public class ShortType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Short.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "INTEGER";
    }

    @Override
    public Object toSqlType(Object source) {
        return source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getShort(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (Short) value);
    }
}
