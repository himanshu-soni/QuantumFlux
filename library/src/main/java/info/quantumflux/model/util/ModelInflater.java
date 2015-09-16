package info.quantumflux.model.util;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.List;

import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.map.SqlColumnMapping;

/**
 * Handles the inflation and deflation of Java objects to and from content values/cursors
 */
public class ModelInflater {

    public static Object deflateColumn(TableDetails tableDetails, TableDetails.ColumnDetails columnDetails, Object dataModelObject) {
        try {
            Object value = columnDetails.getColumnField().get(dataModelObject);

            if (value == null) return null;
            else return columnDetails.getColumnTypeMapping().toSqlType(value);
        } catch (IllegalAccessException e) {
            throw new QuantumFluxException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
        }
    }

    public static ContentValues deflate(TableDetails tableDetails, Object dataModelObject) {
        List<TableDetails.ColumnDetails> columns = tableDetails.getColumns();
        ContentValues contentValues = new ContentValues(columns.size());

        for (int i = 0; i < columns.size(); i++) {
            TableDetails.ColumnDetails columnDetails = columns.get(i);

            if (columnDetails.isAutoIncrement()) continue;

            try {
                String key = columnDetails.getColumnName();
                Object value = columnDetails.getColumnField().get(dataModelObject);

                if (value == null) contentValues.putNull(key);
                else columnDetails.getColumnTypeMapping().setColumnValue(contentValues, key, value);
            } catch (IllegalAccessException e) {
                throw new QuantumFluxException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
            }
        }

        return contentValues;
    }


    public static ContentValues[] deflateAll(TableDetails tableDetails, Object... dataModelObjects) {
        List<TableDetails.ColumnDetails> columns = tableDetails.getColumns();
        ContentValues[] contentValuesArray = new ContentValues[dataModelObjects.length];
        for (int i = 0; i < dataModelObjects.length; i++) {
            contentValuesArray[i] = new ContentValues(columns.size());
        }

        for (int i = 0; i < columns.size(); i++) {
            TableDetails.ColumnDetails columnDetails = columns.get(i);

            if (columnDetails.isAutoIncrement()) continue;

            String key = columnDetails.getColumnName();
            Field columnField = columnDetails.getColumnField();
            SqlColumnMapping columnMapping = columnDetails.getColumnTypeMapping();

            for (int j = 0; j < dataModelObjects.length; j++) {
                try {
                    Object dataModelObject = dataModelObjects[j];
                    Object value = columnField.get(dataModelObject);
                    ContentValues contentValues = contentValuesArray[j];

                    if (value == null) contentValues.putNull(key);
                    else columnMapping.setColumnValue(contentValues, key, value);
                } catch (IllegalAccessException e) {
                    throw new QuantumFluxException("Unable to access protected field, change the access level: " + columnDetails.getColumnName());
                }
            }
        }
        return contentValuesArray;
    }

    public static <T> T inflate(Cursor cursor, TableDetails tableDetails) {
        T dataModelObject;

        try {
            dataModelObject = (T) tableDetails.getTableClass().getConstructor().newInstance();
        } catch (Exception ex) {
            throw new QuantumFluxException("Could not create a new instance of data model object: " + tableDetails.getTableName());
        }

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String columnName = cursor.getColumnName(i);
            TableDetails.ColumnDetails columnDetails = tableDetails.findColumn(columnName);
            inflateColumn(cursor, dataModelObject, columnDetails, i);

        }

        return dataModelObject;
    }

    private static <T> void inflateColumn(Cursor cursor, T dataModelObject, TableDetails.ColumnDetails columnDetails, int columnIndex) {

        //If the column details is not required, then check if it is null
        if (!columnDetails.isRequired() && cursor.isNull(columnIndex)) {
            return;
        }

        Field columnField = columnDetails.getColumnField();
        try {
            columnField.set(dataModelObject, columnDetails.getColumnTypeMapping().getColumnValue(cursor, columnIndex));
        } catch (IllegalAccessException e) {
            throw new QuantumFluxException("Not allowed to alter the value of the field, please change the access level: " + columnDetails.getColumnName());
        }
    }
}
