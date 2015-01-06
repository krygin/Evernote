package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Locale;

import ru.bmstu.evernote.data.ClientFactory;

/**
 * Created by Ivan on 10.12.2014.
 */
public class EvernoteSession {
    public static final String HOST_SANDBOX = "https://sandbox.evernote.com";
    public static final String HOST_PRODUCTION = "https://www.evernote.com";
    private static final String LOGTAG = EvernoteSession.class.getSimpleName();
    private static EvernoteSession sInstance = null;
    private final Context mContext;
    private EvernoteService mEvernoteService;
    private ClientFactory mClientFactory;
    private String mNoteStoreUrl;
    private String mWebApiUrlPrefix;


    private EvernoteSession(Context context, EvernoteService evernoteService) {
        mEvernoteService = evernoteService;
        mContext = context;
        mClientFactory = new ClientFactory(generateUserAgentString(mContext), mContext.getFilesDir());
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(EvernoteAccount.TYPE);
        if (accounts.length != 0) {
            Account account = accounts[0];
            this.mNoteStoreUrl = accountManager.getUserData(account, EvernoteAccount.EXTRA_NOTE_STORE_URL);
            this.mWebApiUrlPrefix = accountManager.getUserData(account, EvernoteAccount.EXTRA_WEB_API_URL_PREFIX);
        }
    }

    public static EvernoteSession getInstance() {
        return sInstance;
    }

    public static EvernoteSession initInstance(Context ctx, EvernoteService evernoteService) {
        return sInstance = new EvernoteSession(ctx, evernoteService);
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

        Locale locale = Locale.getDefault();
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
}