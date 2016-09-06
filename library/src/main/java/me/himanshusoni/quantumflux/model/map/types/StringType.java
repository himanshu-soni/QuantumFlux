package me.himanshusoni.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;

public class StringType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return String.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "TEXT";
    }

    @Override
    public Object toSqlType(Object source) {
        return (String) source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getString(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (String) value);
    }
}
