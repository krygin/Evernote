package ru.bmstu.evernote.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Ivan on 08.12.2014.
 */
public class EvernoteAuthenticatorService extends Service {
    private EvernoteAuthenticator mEvernoteAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mEvernoteAuthenticator = new EvernoteAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mEvernoteAuthenticator.getIBinder();
    }
}