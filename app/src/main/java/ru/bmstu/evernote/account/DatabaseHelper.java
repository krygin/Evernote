package ru.bmstu.evernote.account;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.Date;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;
import static ru.bmstu.evernote.provider.database.EvernoteContract.Notes;
import static ru.bmstu.evernote.provider.database.EvernoteContract.StateDeleted;
import static ru.bmstu.evernote.provider.database.EvernoteContract.StateSyncRequired;

/**
 * Created by Ivan on 23.12.2014.
 */
public class DatabaseHelper {
    private final ContentProviderClient contentProviderClient;

    public DatabaseHelper(ContentProviderClient contentProviderClient) {
        this.contentProviderClient = contentProviderClient;
    }

    public Uri insertNotebook(String name, String guid, long created, long updated, long usn) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.GUID, guid);
        contentValues.put(Notebooks.CREATED, created);
        contentValues.put(Notebooks.UPDATED, updated);
        contentValues.put(Notebooks.USN, usn);
        contentValues.put(Notebooks.STATE_DELETED, StateDeleted.FALSE.ordinal());
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        return contentProviderClient.insert(Notebooks.CONTENT_URI, contentValues);
    }

    public Uri insertNotebookWithConflictResolution(String name, String guid, long created, long updated, long usn) throws RemoteException {
        Cursor cursor = getNotebookWithSpecifiedName(name);
        int count = cursor.getCount();
        switch (count) {
            case 0:
                break;
            case 1:
                int i = 2;
                while (getNotebookWithSpecifiedName(name + "(" + i + ")").getCount() != 0)
                    i++;
                cursor.moveToNext();
                long id = cursor.getLong(cursor.getColumnIndex(Notebooks._ID));
                String newName = name + "(" + i + ")";
                Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, id);
                ContentValues contentValues = new ContentValues();
                contentValues.put(Notebooks.NAME, newName);
                contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.PENDING.ordinal());
                contentValues.put(Notebooks.UPDATED, new Date().getTime());
                contentProviderClient.update(notebooksUri, contentValues, null, null);
                break;
            default:
                throw new IllegalStateException();
        }
        return insertNotebook(name, guid, created, updated, usn);
    }

    public void updateNotebook(String name, long updated, long usn, long notebooksId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.UPDATED, updated);
        contentValues.put(Notebooks.USN, usn);
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notebooksId);
        contentProviderClient.update(notebooksUri, contentValues, null, null);
    }

    public Cursor getNotebookWithSpecifiedGuid(String notebooksGuid) throws RemoteException {
        return contentProviderClient.query(Notebooks.CONTENT_URI, Notebooks.ALL_COLUMNS_PROJECTION, Notebooks.WITH_SPECIFIED_GUID_SELECTION, new String[]{notebooksGuid}, null);
    }

    public Cursor getNotebookWithSpecifiedName(String name) throws RemoteException {
        return contentProviderClient.query(Notebooks.CONTENT_URI, Notebooks.ALL_COLUMNS_PROJECTION, Notebooks.WITH_SPECIFIED_NAME_SELECTION, new String[]{name}, null);
    }

    public Cursor getNoteWithSpecifiedGuid(String notesGuid) throws RemoteException {
        return contentProviderClient.query(Notes.CONTENT_URI, Notes.ALL_COLUMNS_PROJECTION, Notes.WITH_SPECIFIED_GUID_SELECTION, new String[]{notesGuid}, null);
    }

    public int deleteNotebookWithSpecifiedGuid(String guid) throws RemoteException {
        return contentProviderClient.delete(Notebooks.CONTENT_URI, Notebooks.WITH_SPECIFIED_GUID_SELECTION, new String[]{guid});
    }

    public int deleteNote(long notesId) throws RemoteException {
        Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notesId);
        return contentProviderClient.delete(notesUri, null, null);
    }

    public Cursor getDeletedNotebooks() throws RemoteException {
        return contentProviderClient.query(Notebooks.CONTENT_URI, Notebooks.ALL_COLUMNS_PROJECTION, Notebooks.DELETED_SELECTION, null, null);
    }

    public Cursor getNotesWithSpecifiedNotebooksId(long notebooksId) throws RemoteException {
        return contentProviderClient.query(Notes.CONTENT_URI, Notes.ALL_COLUMNS_PROJECTION, Notes.WITH_SPECIFIED_NOTEBOOKS_ID_SELECTION, new String[]{((Long) notebooksId).toString()}, null);
    }

    public int deleteNotebook(long notebooksId) throws RemoteException {
        Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notebooksId);
        return contentProviderClient.delete(notebooksUri, null, null);
    }

    public Cursor getChangedNotebooks() throws RemoteException {
        return contentProviderClient.query(Notebooks.CONTENT_URI, Notebooks.ALL_COLUMNS_PROJECTION, Notebooks.NOT_SYNCED_SELECTION, null, null);
    }

    public void updateNotebook(String guid, String name, long created, long updated, long usn, long id) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notebooks.NAME, name);
        contentValues.put(Notebooks.GUID, guid);
        contentValues.put(Notebooks.CREATED, created);
        contentValues.put(Notebooks.UPDATED, updated);
        contentValues.put(Notebooks.USN, usn);
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, id);
        contentProviderClient.update(notebooksUri, contentValues, null, null);

    }

    public void updateNotebook(long usn, long id) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notebooks.USN, usn);
        contentValues.put(Notebooks.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, id);
        contentProviderClient.update(notebooksUri, contentValues, null, null);
    }

    public long insertNote(String title, String content, String guid, long created, long updated, long usn, long notebooksId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CONTENT, content);
        contentValues.put(Notes.GUID, guid);
        contentValues.put(Notes.CREATED, created);
        contentValues.put(Notes.UPDATED, updated);
        contentValues.put(Notes.USN, usn);
        contentValues.put(Notes.NOTEBOOKS_ID, notebooksId);
        contentValues.put(Notes.STATE_DELETED, StateDeleted.FALSE.ordinal());
        contentValues.put(Notes.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri result = contentProviderClient.insert(Notes.CONTENT_URI, contentValues);
        return Long.parseLong(result.getLastPathSegment());
    }

    public void updateNote(String title, String content, long updated, long usn, long notesId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CONTENT, content);
        contentValues.put(Notes.UPDATED, updated);
        contentValues.put(Notes.USN, usn);
        contentValues.put(Notes.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notesId);
        contentProviderClient.update(notesUri, contentValues, null, null);
    }


    public Cursor getDeletedNotes() throws RemoteException {
        return contentProviderClient.query(Notes.CONTENT_URI, Notes.ALL_COLUMNS_PROJECTION, Notes.DELETED_SELECTION, null, null);
    }

    public Cursor getNote(long notesId) throws RemoteException {
        Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notesId);
        return contentProviderClient.query(notesUri, Notes.ALL_COLUMNS_PROJECTION, null, null, null);
    }

    public Cursor getChangedNotes() throws RemoteException {
        return contentProviderClient.query(Notes.CONTENT_URI, Notes.ALL_COLUMNS_PROJECTION, Notes.NOT_SYNCED_SELECTION, null, null);
    }

    public Cursor getNotebook(long notebooksId) throws RemoteException {
        Uri notebooksUri = ContentUris.withAppendedId(Notebooks.CONTENT_URI, notebooksId);
        return contentProviderClient.query(notebooksUri, Notebooks.ALL_COLUMNS_PROJECTION, null, null, null);
    }

    public int deleteNoteWithSpecifiedGuid(String guid) throws RemoteException {
        return contentProviderClient.delete(Notes.CONTENT_URI, Notes.WITH_SPECIFIED_GUID_SELECTION, new String[]{guid});
    }

    public int deleteNotes() throws RemoteException {
        return contentProviderClient.delete(Notes.CONTENT_URI, Notes.DELETED_SELECTION, null);
    }

    public int deleteNotebooks() throws RemoteException {
        return contentProviderClient.delete(Notebooks.CONTENT_URI, Notebooks.DELETED_SELECTION, null);
    }

    public void updateNote(String guid, String title, String content, long updated, long created, long usn, long id) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Notes.GUID, guid);
        contentValues.put(Notes.TITLE, title);
        contentValues.put(Notes.CONTENT, content);
        contentValues.put(Notes.UPDATED, updated);
        contentValues.put(Notes.CREATED, created);
        contentValues.put(Notes.USN, usn);
        contentValues.put(Notes.STATE_DELETED, StateDeleted.FALSE.ordinal());
        contentValues.put(Notes.STATE_SYNC_REQUIRED, StateSyncRequired.SYNCED.ordinal());
        Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
        contentProviderClient.update(notesUri, contentValues, null, null);
    }
}