package me.himanshusoni.quantumflux.sample.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Himanshu on 8/6/2015.
 */
public class SyncUtils {

    public static final String ACCOUNT_TYPE = "quantumflux";
    public static final long SYNC_INTERVAL = 60L * 60L * 1;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context, String name) {
        // Create the account type and default account
        Account newAccount = new Account(name, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, CONTENT_AUTHORITY, 1)
             * here.
             */
            // Turn on automatic syncing for the default account and authority
//            mResolver.setSyncAutomatically(newAccount, SyncConstants.AUTHORITY, true);
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }

        return newAccount;
    }

    public static void refreshManually(Account account) {
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(account, SyncConstants.AUTHORITY, settingsBundle);
    }

    /**
     * Turn on periodic syncing
     */
    public static void startPeriodicSync(Account account) {
        ContentResolver.addPeriodicSync(
                account,
                SyncConstants.AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL);
    }
}
