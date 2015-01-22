package ru.bmstu.evernote.activities;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.SettingsHelper;

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
                        SettingsHelper.setSyncAutomatically(getActivity());
                    } else {
                        SettingsHelper.unsetPeriodicSync(getActivity());
                        SettingsHelper.unsetSyncAutomatically(getActivity());
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    break;
                case "sync_period":
                    Long period = Long.parseLong(sharedPreferences.getString(s, "0"));
                    SettingsHelper.setPeriodicSync(getActivity(),period);
                    break;
                default:
                    break;
            }
        }
    }
}
