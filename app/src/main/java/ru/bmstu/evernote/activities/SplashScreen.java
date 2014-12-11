package ru.bmstu.evernote.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import ru.bmstu.evernote.MainActivity;
import ru.bmstu.evernote.account.EvernoteSession;
import ru.bmstu.evernote.R;


public class SplashScreen extends Activity {
    private boolean mDataLoaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EvernoteSession.initInstance(SplashScreen.this, EvernoteSession.EvernoteService.SANDBOX);
                startMainActivity();
            }
        }, 2000);
    }

    public void startMainActivity () {
        if (mDataLoaded) {
            MainActivity.startMainActivity(this);
        }
    }
    public void setDataLoaded(boolean dataLoaded) {
        mDataLoaded = dataLoaded;
    }
}