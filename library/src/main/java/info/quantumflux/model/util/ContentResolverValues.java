package info.quantumflux.model.util;

import android.net.Uri;

import info.quantumflux.model.generate.TableDetails;

public class ContentResolverValues {

    private final TableDetails mTableDetails;
    private final Uri mUri;
    private final String[] mProjection;
    private final String mWhere;
    private final String[] mWhereArgs;
    private final String mSortOrder;

    public ContentResolverValues(TableDetails tableDetails, Uri itemUri, String[] projection, String where, String[] whereArgs, String sortOrder) {
        this.mTableDetails = tableDetails;
        this.mUri = itemUri;
        this.mProjection = projection;
        this.mWhere = where;
        this.mWhereArgs = whereArgs;
        this.mSortOrder = sortOrder;
    }

    public TableDetails getTableDetails() {
        return mTableDetails;
    }

    public Uri getItemUri() {
        return mUri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public String getWhere() {
        return mWhere;
    }

    public String[] getWhereArgs() {
        return mWhereArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }
}
