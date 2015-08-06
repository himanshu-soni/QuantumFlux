package info.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import info.quantumflux.model.map.SqlColumnMapping;

import java.util.Date;

public class DateType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Date.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "INTEGER";
    }

    @Override
    public Long toSqlType(Object source) {
        return ((Date) source).getTime();
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return new Date(cursor.getLong(columnIndex));
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }
}
