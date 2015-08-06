package info.quantumflux.provider.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

import info.quantumflux.model.generate.TableDetails;
import info.quantumflux.model.util.ManifestHelper;
import info.quantumflux.model.util.QuantumFluxException;
import info.quantumflux.model.util.TableDetailsCache;

import java.util.LinkedHashMap;
import java.util.Map;

import info.quantumflux.model.generate.ReflectionHelper;

/**
 * The uri matcher helper that will register all of the valid model url's that can be accessed.
 * Every item will have two URLs exposed, one to access all items, and another to access a single item.
 * <p/>
 * Each item is separated by a interval (Default 100) for the match code on the UriMatcher.  With the 'all' and 'single' urls containing their own index
 * withing the 100 index gap between model items.
 */
public class UriMatcherHelper {

    public static int MATCHER_CODE_INTERVALS = 100;
    public static int MATCHER_ALL = 1;
    public static int MATCHER_SINGLE = 2;

    private final Map<Integer, TableDetails> mMatcherCodes;
    private final String mAuthority;
    private UriMatcher mUriMatcher;

    public UriMatcherHelper(Context context) {
        this.mMatcherCodes = new LinkedHashMap<Integer, TableDetails>();
        mAuthority = ManifestHelper.getAuthority(context);
    }

    public void init(Context context, TableDetailsCache detailsCache) {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        int matcherInterval = MATCHER_CODE_INTERVALS;

        for (Class<?> dataModelObject : ReflectionHelper.getDomainClasses(context, ReflectionHelper.TableType.TABLE)) {

            TableDetails tableDetails = detailsCache.findTableDetails(context, dataModelObject);

            mMatcherCodes.put(matcherInterval, tableDetails);
            mUriMatcher.addURI(mAuthority, tableDetails.getTableName(), matcherInterval + MATCHER_ALL);
            mUriMatcher.addURI(mAuthority, tableDetails.getTableName() + "/*", matcherInterval + MATCHER_SINGLE);

            matcherInterval += MATCHER_CODE_INTERVALS;
        }
    }

    public TableDetails getTableDetails(Uri uri) {

        int matchCode = mUriMatcher.match(uri);
        try {
            return findTableDetails(matchCode);
        } catch (Exception ex) {
            throw new QuantumFluxException("Could not find table information for Uri, make sure the model factory knows of this table: " + uri, ex);
        }
    }

    public String getType(Uri uri) {
        int matchCode = mUriMatcher.match(uri);
        TableDetails tableDetails = findTableDetails(matchCode);
        StringBuilder mimeType = new StringBuilder();
        mimeType.append("android.cursor.");

        if (isSingleItemRequested(matchCode)) mimeType.append(".item");
        else mimeType.append(".dir");
        mimeType.append("/");

        mimeType.append("vnd.");
        mimeType.append(mAuthority);
        if (mimeType.charAt(mimeType.length() - 1) != '.') mimeType.append(".");

        mimeType.append(tableDetails.getTableName());

        return mimeType.toString();
    }

    public boolean isSingleItemRequested(int code) {
        return mMatcherCodes.containsKey(code - MATCHER_SINGLE);
    }

    public boolean isSingleItemRequested(Uri uri) {
        return isSingleItemRequested(mUriMatcher.match(uri));
    }

    public Uri generateItemUri(TableDetails tableDetails) {
        return new Uri.Builder()
                .scheme("content")
                .authority(tableDetails.getAuthority())
                .appendEncodedPath(tableDetails.getTableName())
                .build();

    }

    public Uri generateSingleItemUri(TableDetails tableDetails, String itemId) {
        return new Uri.Builder()
                .scheme("content")
                .authority(tableDetails.getAuthority())
                .appendEncodedPath(tableDetails.getTableName() + "/")
                .appendEncodedPath(itemId)
                .build();

    }

    public Uri generateSingleItemUri(TableDetails tableDetails, long itemId) {
        return ContentUris.withAppendedId(generateItemUri(tableDetails), itemId);
    }

    private TableDetails findTableDetails(int code) {
        if (mMatcherCodes.containsKey(code)) return mMatcherCodes.get(code);
        else if (mMatcherCodes.containsKey(code - MATCHER_ALL))
            return mMatcherCodes.get(code - MATCHER_ALL);
        else if (mMatcherCodes.containsKey(code - MATCHER_SINGLE))
            return mMatcherCodes.get(code - MATCHER_SINGLE);
        else throw new QuantumFluxException("No URI match found for code: " + code);
    }

    public static Uri.Builder generateItemUriBuilder(TableDetails tableDetails) {
        String authority = tableDetails.getAuthority();
        return new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .appendEncodedPath(tableDetails.getTableName());
    }

    public static Uri.Builder generateItemUriBuilder(TableDetails tableDetails, String itemId) {
        String authority = tableDetails.getAuthority();

        return new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .appendEncodedPath(tableDetails.getTableName())
                .appendEncodedPath(itemId);
    }
}
