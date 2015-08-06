package info.quantumflux.model.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

import info.quantumflux.logger.QuantumFluxLog;

public class QuantumFluxCursorFactory implements SQLiteDatabase.CursorFactory {

    private final boolean isDebugEnabled;

    public QuantumFluxCursorFactory(TableDetailsCache tableDetailCache) {
        this(false);
    }

    public QuantumFluxCursorFactory(boolean debugEnabled) {
        this.isDebugEnabled = debugEnabled;
    }

    @Override
    public Cursor newCursor(SQLiteDatabase sqLiteDatabase, SQLiteCursorDriver sqLiteCursorDriver, String tableName, SQLiteQuery sqLiteQuery) {
        if (isDebugEnabled) {
            QuantumFluxLog.d(sqLiteQuery.toString());
        }

        return new SQLiteCursor(sqLiteCursorDriver, tableName, sqLiteQuery);
    }
}
