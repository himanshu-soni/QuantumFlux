package me.himanshusoni.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.UUID;

import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;

public class UUIDType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return UUID.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "TEXT";
    }

    @Override
    public String toSqlType(Object source) {
        return source.toString();
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return UUID.fromString(cursor.getString(columnIndex));
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }
}
