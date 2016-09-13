package me.himanshusoni.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import java.math.BigDecimal;

import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;

public class BigDecimalType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return BigDecimal.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "TEXT";
    }

    @Override
    public String toSqlType(Object source) {
        return (source.toString());
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return new BigDecimal(cursor.getString(columnIndex));
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }
}
