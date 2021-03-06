package me.himanshusoni.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;

public class CalendarType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Calendar.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "INTEGER";
    }

    @Override
    public Long toSqlType(Object source) {
        return ((Calendar) source).getTimeInMillis();
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        long time = cursor.getLong(columnIndex);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, toSqlType(value));
    }
}
