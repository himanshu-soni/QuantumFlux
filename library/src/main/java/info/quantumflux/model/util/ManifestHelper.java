package info.quantumflux.model.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import info.quantumflux.logger.QuantumFluxLog;

public class ManifestHelper {

    public static final String METADATA_AUTHORITY = "AUTHORITY";

    public final static String METADATA_DATABASE_NAME = "DATABASE_NAME";
    public final static String METADATA_VERSION = "DATABASE_VERSION";
    public final static String METADATA_PACKAGE_NAME = "PACKAGE_NAME";
    public final static String METADATA_QUERY_LOG = "QUERY_LOG";

    public static final String DATABASE_DEFAULT_NAME = "QuantumFlux.db";

    private static String authority;
    private static String databaseName;
    private static String packageName;
    private static int databaseVersion;

    public static String getAuthority(Context context) {
        if (authority == null) {
            authority = getMetaDataString(context, METADATA_AUTHORITY);
            if (TextUtils.isEmpty(authority))
                throw new QuantumFluxException("AUTHORITY must be provided in meta data");
        }

        return authority;
    }

    public static String getDatabaseName(Context context) {
        if (databaseName == null) {
            databaseName = getMetaDataString(context, METADATA_DATABASE_NAME);
            if (TextUtils.isEmpty(databaseName)) {
                QuantumFluxLog.d("DATABASE_NAME is not provided in meta data, using default name");
                databaseName = DATABASE_DEFAULT_NAME;
            }
        }

        return databaseName;
    }

    public static String getPackageName(Context context) {
        if (packageName == null) {
            packageName = getMetaDataString(context, METADATA_PACKAGE_NAME);
            if (TextUtils.isEmpty(packageName))
                throw new QuantumFluxException("PACKAGE_NAME must be provided in meta data");
        }

        return packageName;
    }

    public static int getDatabaseVersion(Context context) {
        if (databaseVersion == 0) {
            databaseVersion = getMetaDataInteger(context, METADATA_VERSION);
            if (databaseVersion == 0)
                throw new QuantumFluxException("DATABASE_VERSION must be provided in meta data");
        }

        return databaseVersion;
    }

    public static boolean isQueryLogEnabled(Context context) {
        return getMetaDataBoolean(context, METADATA_QUERY_LOG);
    }

    private static String getMetaDataString(Context context, String name) {
        String value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            QuantumFluxLog.d("Couldn't find config value: " + name);
        }

        return value;
    }

    private static Integer getMetaDataInteger(Context context, String name) {
        Integer value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            QuantumFluxLog.d("Couldn't find config value: " + name);
        }

        return value;
    }

    private static Boolean getMetaDataBoolean(Context context, String name) {
        Boolean value = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            QuantumFluxLog.d("Couldn't find config value: " + name);
        }

        return value;
    }
}
