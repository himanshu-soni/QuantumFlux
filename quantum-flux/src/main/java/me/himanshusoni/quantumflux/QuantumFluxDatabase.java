package me.himanshusoni.quantumflux;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import me.himanshusoni.quantumflux.logger.QuantumFluxLog;
import me.himanshusoni.quantumflux.model.generate.ReflectionHelper;
import me.himanshusoni.quantumflux.model.generate.TableDetails;
import me.himanshusoni.quantumflux.model.generate.TableGenerator;
import me.himanshusoni.quantumflux.model.generate.TableView;
import me.himanshusoni.quantumflux.model.generate.TableViewGenerator;
import me.himanshusoni.quantumflux.model.util.ManifestHelper;
import me.himanshusoni.quantumflux.model.util.QuantumFluxCursorFactory;
import me.himanshusoni.quantumflux.model.util.TableDetailsCache;

import static me.himanshusoni.quantumflux.model.generate.ReflectionHelper.getDomainClasses;

/**
 * Handles the creation of the database and all of its objects
 */
public class QuantumFluxDatabase extends SQLiteOpenHelper {

    private final Context mContext;
    private final TableDetailsCache mTableDetailsCache;
    private final boolean isQueryLoggingEnabled;
    private QuantumFluxDatabaseUpgradeListener mQuantumFluxDatabaseUpgradeListener;

    public QuantumFluxDatabase(Context context, QuantumFluxDatabaseUpgradeListener quantumFluxDatabaseUpgradeListener) {
        super(context, ManifestHelper.getDatabaseName(context), new QuantumFluxCursorFactory(ManifestHelper.isQueryLogEnabled(context)), ManifestHelper.getDatabaseVersion(context));
        this.mContext = context;
        this.mTableDetailsCache = new TableDetailsCache();
        this.mTableDetailsCache.init(context, getDomainClasses(context, ReflectionHelper.TableType.TABLE));
        this.isQueryLoggingEnabled = ManifestHelper.isQueryLogEnabled(context);
        this.mQuantumFluxDatabaseUpgradeListener = quantumFluxDatabaseUpgradeListener;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (Class<?> dataModelObject : getDomainClasses(mContext, ReflectionHelper.TableType.TABLE)) {
            String createStatement = TableGenerator.generateTableCreate(findTableDetails(dataModelObject), false);
            if (isQueryLoggingEnabled) {
                QuantumFluxLog.d("Creating Table: " + createStatement);
            }
            sqLiteDatabase.execSQL(createStatement);
        }

        for (Class<?> dataModelObject : getDomainClasses(mContext, ReflectionHelper.TableType.TABLE_VIEW)) {
            String createStatement = TableViewGenerator.createViewStatement(findTableDetails(dataModelObject), (Class<? extends TableView>) dataModelObject);
            if (isQueryLoggingEnabled) {
                QuantumFluxLog.d("Creating View: " + createStatement);
            }
            sqLiteDatabase.execSQL(createStatement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        for (Class<?> dataModelObject : getDomainClasses(mContext, ReflectionHelper.TableType.TABLE)) {
            String createStatement = TableGenerator.generateTableDrop(findTableDetails(dataModelObject), false);
            if (isQueryLoggingEnabled) {
                QuantumFluxLog.d("Dropping Table: " + createStatement);
            }
            sqLiteDatabase.execSQL(createStatement);
        }

        for (Class<?> dataModelObject : getDomainClasses(mContext, ReflectionHelper.TableType.TABLE_VIEW)) {
            String createStatement = TableViewGenerator.createDropViewStatement(findTableDetails(dataModelObject));
            if (isQueryLoggingEnabled) {
                QuantumFluxLog.d("Dropping View: " + createStatement);
            }
            sqLiteDatabase.execSQL(createStatement);
        }

        if (mQuantumFluxDatabaseUpgradeListener != null) {
            mQuantumFluxDatabaseUpgradeListener.onDatabaseUpgraded(oldVersion, newVersion);
        }

        onCreate(sqLiteDatabase);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (!db.isReadOnly()) {
            db.enableWriteAheadLogging();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            db.enableWriteAheadLogging();
        }
    }

    private TableDetails findTableDetails(Class<?> object) {
        return mTableDetailsCache.findTableDetails(mContext, object);
    }

    /**
     * Returns the table details cache that can be used to lookup table details for java objects.  This
     * should be used instead of {@link ReflectionHelper}, so that we do not
     * try to do reflection to much
     *
     * @return The table details cache object that can be used to obtain table details
     */
    public TableDetailsCache getTableDetailsCache() {
        return mTableDetailsCache;
    }

}
