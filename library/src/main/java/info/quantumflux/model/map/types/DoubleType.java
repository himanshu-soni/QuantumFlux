package info.quantumflux.model.map.types;

import android.content.ContentValues;
import android.database.Cursor;

import info.quantumflux.model.map.SqlColumnMapping;

public class DoubleType implements SqlColumnMapping {
    @Override
    public Class<?> getJavaType() {
        return Double.class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "REAL";
    }

    @Override
    public Object toSqlType(Object source) {
        return source;
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        return cursor.getDouble(columnIndex);
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (Double) value);
    }
}
