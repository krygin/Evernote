package ru.bmstu.evernote;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.util.Date;

import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.Notebooks;
import ru.bmstu.evernote.provider.database.tables.Notes;

public class ContentProviderHelperService extends Service {
    private final IBinder mBinder = new ContentProviderHelperBinder();

    public ContentProviderHelperService() {
    }

    public class ContentProviderHelperBinder extends Binder {
        ContentProviderHelperService getService() {
            return ContentProviderHelperService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Uri insertNotebook(String name) {
        ContentValues contentValues = new ContentValues();
        Long createTime = new Date().getTime();
        Long updateTime = new Date().getTime();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.CREATED, createTime);
        contentValues.put(Notebooks.UPDATED, updateTime);
        return getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
    }

    public Uri insertNote(String title, Long notebooksId) {
        ContentValues contentValues = new ContentValues();
        Long createTime = new Date().getTime();
        Long updateTime = new Date().getTime();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CREATED, createTime);
        contentValues.put(Notes.UPDATED, updateTime);
        contentValues.put(Notes.NOTEBOOKS_ID, notebooksId);
        return getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
    }
}