package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.os.Parcel;

/**
 * Created by Ivan on 08.12.2014.
 */
public class EvernoteAccount extends Account {

    public static final String TYPE = "ru.bmstu.evernote.account";
    public static final String TOKEN_FULL_ACCESS = "ru.bmstu.evetnote.account.FULL_ACCESS";

    public static final String EXTRA_NOTE_STORE_URL = "ru.bmstu.evernote.extra.NOTE_STORE_URL";
    public static final String EXTRA_WEB_API_URL_PREFIX = "ru.bmstu.evernote.extra.WEB_API_URL_PREFIX";
    public static final String EXTRA_LAST_UPDATED_COUNT = "ru.bmstu.evernote.extra.LAST_UPDATED_COUNT";
    public static final String EXTRA_LAST_SYNC_TIME = "ru.bmstu.evernote.extra.LAST_SYNC_TIME";

    public EvernoteAccount(String name) {
        super(name, TYPE);
    }

    public EvernoteAccount(Parcel in) {
        super(in);
    }
}