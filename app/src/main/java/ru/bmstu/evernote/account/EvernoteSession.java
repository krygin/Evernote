package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import ru.bmstu.evernote.activities.SplashScreen;
import ru.bmstu.evernote.data.ClientFactory;

/**
 * Created by Ivan on 10.12.2014.
 */
public class EvernoteSession {
    public static final String HOST_SANDBOX = "https://sandbox.evernote.com";
    public static final String HOST_PRODUCTION = "https://www.evernote.com";
    private static final String LOGTAG = EvernoteSession.class.getSimpleName();
    private static EvernoteSession sInstance = null;
    private EvernoteService mEvernoteService;
    private ClientFactory mClientFactory;
    private String mNoteStoreUrl;
    private String mWebApiUrlPrefix;

    private Activity mActivity;

    private EvernoteSession(Activity activity, EvernoteService evernoteService) {
        mEvernoteService = evernoteService;
        mActivity = activity;
        mEvernoteService = evernoteService;
        mClientFactory = new ClientFactory(generateUserAgentString(activity), activity.getFilesDir());
        AccountManager accountManager = AccountManager.get(activity);
        Account[] accounts = accountManager.getAccountsByType(EvernoteAccount.TYPE);
        if (accounts.length != 0) {
            Account account = accounts[0];
            this.mNoteStoreUrl = accountManager.getUserData(account, EvernoteAccount.EXTRA_NOTE_STORE_URL);
            this.mWebApiUrlPrefix = accountManager.getUserData(account, EvernoteAccount.EXTRA_WEB_API_URL_PREFIX);
            ((SplashScreen)mActivity).setDataLoaded(true);
            ((SplashScreen)mActivity).startMainActivity();
        } else {
            addNewAccount(accountManager);
        }
    }

    public static EvernoteSession getInstance() {
        return sInstance;
    }

    public static EvernoteSession initInstance(Activity activity,
                                               EvernoteService evernoteService) {
        return sInstance = new EvernoteSession(activity, evernoteService);
    }

    public ClientFactory getClientFactory() {
        return mClientFactory;
    }

    public String getNoteStoreUrl() {
        return mNoteStoreUrl;
    }

    public enum EvernoteService implements Parcelable {
        SANDBOX,
        PRODUCTION;
        public static final Creator<EvernoteService> CREATOR = new Creator<EvernoteService>() {
            @Override
            public EvernoteService createFromParcel(final Parcel source) {
                return EvernoteService.values()[source.readInt()];
            }

            @Override
            public EvernoteService[] newArray(final int size) {
                return new EvernoteService[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }
    }

    private String generateUserAgentString(Context ctx) {
        // com.evernote.sample Android/216817 (en); Android/4.0.3; Xoom/15;"

        String packageName = null;
        int packageVersion = 0;
        try {
            packageName = ctx.getPackageName();
            packageVersion = ctx.getPackageManager().getPackageInfo(packageName, 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOGTAG, e.getMessage());
        }

        String userAgent = packageName + " Android/" + packageVersion;

        Locale locale = java.util.Locale.getDefault();
        if (locale == null) {
            userAgent += " (" + Locale.US + ");";
        } else {
            userAgent += " (" + locale.toString() + "); ";
        }
        userAgent += "Android/" + Build.VERSION.RELEASE + "; ";
        userAgent +=
                Build.MODEL + "/" + Build.VERSION.SDK_INT + ";";
        return userAgent;
    }

    private void addNewAccount(AccountManager am) {
        am.addAccount(EvernoteAccount.TYPE, EvernoteAccount.TOKEN_FULL_ACCESS, null, null, mActivity,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            future.getResult();
                            //MainActivity.startMainActivity(mActivity);
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            (mActivity).finish();
                        }
                    }
                }, null
        );
    }
}