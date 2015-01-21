package ru.bmstu.evernote.provider.database;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import java.util.Date;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;
import static ru.bmstu.evernote.provider.database.EvernoteContract.Notes;
import static ru.bmstu.evernote.provider.database.EvernoteContract.StateDeleted;
import static ru.bmstu.evernote.provider.database.EvernoteContract.StateSyncRequired;

public class ContentProviderHelperService extends Service implements IClientAPI {
    private final IBinder mBinder = new ContentProviderHelperBinder();

    public ContentProviderHelperService() {
    }

    public class ContentProviderHelperBinder extends Binder {
        public IClientAPI getClientApiService() {
            return ContentProviderHelperService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean insertNotebook(String name) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.CREATED, currentTime);
        contentValues.put(Notebooks.UPDATED, currentTime);
        contentValues.put(Notebooks.STATE_DELETED, StateDeleted.FALSE.ordinal());
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.PENDING.ordinal());
        Uri result = getContentResolver().insert(Notebooks.CONTENT_URI, contentValues);
        return result != null;
    }

    @Override
    public boolean insertNote(String title, String content, long notebooksId) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CONTENT, content);
        contentValues.put(Notes.CREATED, currentTime);
        contentValues.put(Notes.UPDATED, currentTime);
        contentValues.put(Notes.NOTEBOOKS_ID, notebooksId);
        contentValues.put(Notes.STATE_DELETED, StateDeleted.FALSE.ordinal());
        contentValues.put(Notes.STATE_SYNC_REQUIRED, StateSyncRequired.PENDING.ordinal());
        Uri result = getContentResolver().insert(Notes.CONTENT_URI, contentValues);
        return result != null;
    }

    @Override
    public boolean updateNotebook(long notebooksId, String name) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.UPDATED, currentTime);
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.PENDING.ordinal());
        Uri notebookUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notebooksId);
        int result = getContentResolver().update(notebookUri, contentValues, null, null);
        return result != 0;
    }

    @Override
    public boolean updateNote(String title, String content, long notebooksId) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CONTENT, content);
        contentValues.put(Notes.UPDATED, currentTime);
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.PENDING.ordinal());
        Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notebooksId);
        int result = getContentResolver().update(notesUri, contentValues, null, null);
        return result != 0;
    }


    @Override
    public boolean deleteNotebook(long notebooksId) {
        Uri notebookUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notebooksId);
        int result = getContentResolver().delete(notebookUri, null, null);
        return result != 0;
    }

    @Override
    public boolean deleteNote(long notesId) {
        Uri notesUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notesId);
        int result = getContentResolver().delete(notesUri, null, null);
        return result != 0;
    }
}