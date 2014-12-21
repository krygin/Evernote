package ru.bmstu.evernote.provider.database;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.evernote.edam.limits.Constants;

import java.util.Date;

import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.Notebooks;
import ru.bmstu.evernote.provider.database.tables.Notes;
import ru.bmstu.evernote.provider.database.tables.Resources;

public class ContentProviderHelperService extends Service implements IClientAPI, ISyncAdapterAPI {
    private final IBinder mBinder = new ContentProviderHelperBinder();

    public ContentProviderHelperService() {
    }

    public class ContentProviderHelperBinder extends Binder {
        public IClientAPI getClientApiService() {
            return ContentProviderHelperService.this;
        }

        public ISyncAdapterAPI getSyncAdapterApiService() {
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
        contentValues.put(Notebooks.IS_LOCALLY_DELETED, 0);
        Uri result = getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
        return result != null;
    }

    @Override
    public boolean insertNote(String title, long notebooksId) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CREATED, currentTime);
        contentValues.put(Notes.UPDATED, currentTime);
        contentValues.put(Notes.NOTEBOOKS_ID, notebooksId);
        contentValues.put(Notes.IS_LOCALLY_DELETED, 0);
        Uri result = getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
        return result != null;
    }

    @Override
    public boolean updateNotebook(long notebooksId, String name) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(Notebooks.NAME, name);
        values.put(Notebooks.UPDATED, currentTime);
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = getContentResolver().update(notebookUri, values, null, null);
        return result != 0;
    }

    @Override
    public boolean updateNote(String title, long notesId) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(Notes.TITLE, title);
        values.put(Notes.UPDATED, currentTime);
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI, notesId);
        int result = getContentResolver().update(notesUri, values, null, null);
        return result != 0;
    }

    @Override
    public boolean deleteNote(long notesId) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(Notes.UPDATED, currentTime);
        values.put(Notes.IS_LOCALLY_DELETED, 1);
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI, notesId);
        int result = getContentResolver().update(notesUri, values, null, null);
        return result != 0;
    }

    @Override
    public boolean deleteNotebook(long notebooksId) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(Notebooks.UPDATED, currentTime);
        values.put(Notebooks.IS_LOCALLY_DELETED, 1);
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = getContentResolver().update(notebookUri, values, null, null);
        return result != 0;
    }

    @Override
    public boolean insertResource(long notesId, String resource) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Resources.PATH_TO_RESOURCE, resource);
        contentValues.put(Resources.MIME_TYPE, Constants.EDAM_MIME_TYPE_DEFAULT);
        contentValues.put(Resources.NOTES_ID, notesId);
        contentValues.put(Resources.IS_LOCALLY_DELETED, 0);
        Uri result = getContentResolver().insert(EvernoteContentProvider.NOTES_URI, contentValues);
        return result != null;
    }

    @Override
    public boolean deleteResource(long resourcesId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Resources.IS_LOCALLY_DELETED, 1);
        Uri resourcesUri = ContentUris.withAppendedId(EvernoteContentProvider.RESOURCES_URI, resourcesId);
        int result = getContentResolver().update(resourcesUri, contentValues, null, null);
        return result != 0;
    }
}