package ru.bmstu.evernote.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.account.EvernoteAccount;
import ru.bmstu.evernote.provider.database.EvernoteContract;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch (s) {
                case "sync":
                    boolean x = sharedPreferences.getBoolean(s, false);

                    if (x) {
                        ContentResolver.setMasterSyncAutomatically(true);
                        setSyncAutomatically();
                    } else {
                        unsetPeriodicSync();
                        unsetSyncAutomatically();
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    break;
                case "sync_period":
                    //long period = sharedPreferences.getLong(s, 0);
                    setPeriodicSync(15);
                    break;
                default:
                    break;
            }
        }

        public void setPeriodicSync(long period) {
            AccountManager accountManager = AccountManager.get(getActivity());
            Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
            ContentResolver.addPeriodicSync(account, EvernoteContract.AUTHORITY, new Bundle(), period);
        }

        public void unsetPeriodicSync() {
            AccountManager accountManager = AccountManager.get(getActivity());
            Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
            ContentResolver.removePeriodicSync(account, EvernoteContract.AUTHORITY, new Bundle());
        }

        public void setSyncAutomatically() {
            AccountManager accountManager = AccountManager.get(getActivity());
            Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
            ContentResolver.setSyncAutomatically(account, EvernoteContract.AUTHORITY, true);
        }

        public void unsetSyncAutomatically() {
            AccountManager accountManager = AccountManager.get(getActivity());
            Account account = accountManager.getAccountsByType(EvernoteAccount.TYPE)[0];
            ContentResolver.setSyncAutomatically(account, EvernoteContract.AUTHORITY, false);
        }
    }
}
