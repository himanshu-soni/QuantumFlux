package info.quantumflux.sample.data.type;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import info.quantumflux.model.map.SqlColumnMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Himanshu on 8/5/2015.
 */
public class BlobTypeMapping implements SqlColumnMapping {

    @Override
    public Class<?> getJavaType() {
        return byte[].class;
    }

    @Override
    public String getSqlColumnTypeName() {
        return "BLOB";
    }

    @Override
    public Object toSqlType(Object source) {
        ByteArrayOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(source);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException io) {
            Log.e(getClass().getSimpleName(), "Failed to serialize object for storage", io);
            return null;
        } finally {
            closeStreams(outputStream, objectOutputStream);
        }
    }

    @Override
    public Object getColumnValue(Cursor cursor, int columnIndex) {
        byte[] columnValue = cursor.getBlob(columnIndex);
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;

        try {
            inputStream = new ByteArrayInputStream(columnValue);
            objectInputStream = new ObjectInputStream(inputStream);

            return objectInputStream.readObject();
        } catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Failed to deserialize object", ex);
        } finally {
            closeStreams(inputStream, objectInputStream);
        }
        return null;
    }

    @Override
    public void setColumnValue(ContentValues contentValues, String key, Object value) {
        contentValues.put(key, (byte[]) toSqlType(value));
    }

    private void closeStreams(Closeable... streams) {
        try {
            for (Closeable stream : streams) {
                if (stream != null) stream.close();
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Failed to close streams");
        }
    }
}
