package ru.bmstu.evernote.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import java.io.IOException;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.account.EvernoteAccount;
import ru.bmstu.evernote.provider.database.EvernoteContract;


public class SplashScreen extends Activity {

    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mContext = SplashScreen.this;
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        final AccountManager accountManager = AccountManager.get(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Account[] accounts = accountManager.getAccountsByType(EvernoteAccount.TYPE);
                if(accounts.length == 0)
                    addNewAccount(accountManager);
                else {
                    Account account = accounts[0];
                    ContentResolver.requestSync(account, EvernoteContract.AUTHORITY, new Bundle());
                    MainActivity.startMainActivity(mContext);
                }
            }
        }, 2000);
    }

    private void addNewAccount(AccountManager am) {
        am.addAccount(EvernoteAccount.TYPE, EvernoteAccount.TOKEN_FULL_ACCESS, null, null, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            future.getResult();
                            MainActivity.startMainActivity(mContext);
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            SplashScreen.this.finish();
                        }
                    }
                }, null
        );
    }
}