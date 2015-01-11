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
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;
import ru.bmstu.evernote.provider.database.tables.ResourcesTable;

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
        contentValues.put(NotebooksTable.NAME, name);
        contentValues.put(NotebooksTable.CREATED, currentTime);
        contentValues.put(NotebooksTable.UPDATED, currentTime);
        contentValues.put(NotebooksTable.IS_LOCALLY_DELETED, 0);
        Uri result = getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI_TRANSACT, contentValues);
        return result != null;
    }

    @Override
    public boolean insertNote(String title, String content, long notebooksId) {
        ContentValues contentValues = new ContentValues();
        Long currentTime = new Date().getTime();
        contentValues.put(NotesTable.TITLE, title);
        contentValues.put(NotesTable.CREATED, currentTime);
        contentValues.put(NotesTable.UPDATED, currentTime);
        contentValues.put(NotesTable.NOTEBOOKS_ID, notebooksId);
        contentValues.put(NotesTable.IS_LOCALLY_DELETED, 0);
        contentValues.put(NotesTable.CONTENT, content);
        Uri result = getContentResolver().insert(EvernoteContentProvider.NOTES_URI_TRANSACT, contentValues);
        return result != null;
    }

    @Override
    public boolean insertResource(long notesId, String filename) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResourcesTable.FILENAME, filename);
        contentValues.put(ResourcesTable.MIME_TYPE, Constants.EDAM_MIME_TYPE_DEFAULT);
        contentValues.put(ResourcesTable.NOTES_ID, notesId);
        contentValues.put(ResourcesTable.IS_LOCALLY_DELETED, 0);
        Uri result = getContentResolver().insert(EvernoteContentProvider.RESOURCES_URI_TRANSACT, contentValues);
        return result != null;
    }

    @Override
    public boolean updateNotebook(long notebooksId, String name) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(NotebooksTable.NAME, name);
        values.put(NotebooksTable.UPDATED, currentTime);
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI_TRANSACT, notebooksId);
        int result = getContentResolver().update(notebookUri, values, null, null);
        return result != 0;
    }

    @Override
    public boolean updateNote(String title, String content, long notebooksId) {
        ContentValues values = new ContentValues();
        Long currentTime = new Date().getTime();
        values.put(NotesTable.TITLE, title);
        values.put(NotesTable.UPDATED, currentTime);
        values.put(NotesTable.CONTENT, content);
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI_TRANSACT, notebooksId);
        int result = getContentResolver().update(notesUri, values, null, null);
        return result != 0;
    }


    @Override
    public boolean deleteNotebook(long notebooksId) {
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI_TRANSACT, notebooksId);
        int result = getContentResolver().delete(notebookUri, null, null);
        return result != 0;
    }

    @Override
    public boolean deleteNote(long notesId) {
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI_TRANSACT, notesId);
        int result = getContentResolver().delete(notesUri, null, null);
        return result != 0;
    }

    @Override
    public boolean deleteResource(long resourcesId) {
        Uri resourcesUri = ContentUris.withAppendedId(EvernoteContentProvider.RESOURCES_URI_TRANSACT, resourcesId);
        int result = getContentResolver().delete(resourcesUri, null, null);
        return result != 0;
    }
}