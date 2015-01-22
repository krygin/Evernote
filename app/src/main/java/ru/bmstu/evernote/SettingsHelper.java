package ru.bmstu.evernote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import ru.bmstu.evernote.account.EvernoteAccount;
import ru.bmstu.evernote.provider.database.EvernoteContract;

/**
 * Created by Ivan on 22.01.2015.
 */
public class SettingsHelper {
    public static void setPeriodicSync(Context context, long period) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
        ContentResolver.addPeriodicSync(account, EvernoteContract.AUTHORITY, new Bundle(), period);
    }

    public static void unsetPeriodicSync(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
        ContentResolver.removePeriodicSync(account, EvernoteContract.AUTHORITY, new Bundle());
    }

    public static void setSyncAutomatically(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
        ContentResolver.setSyncAutomatically(account, EvernoteContract.AUTHORITY, true);
    }

    public static void unsetSyncAutomatically(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
        ContentResolver.setSyncAutomatically(account, EvernoteContract.AUTHORITY, false);
    }

}
