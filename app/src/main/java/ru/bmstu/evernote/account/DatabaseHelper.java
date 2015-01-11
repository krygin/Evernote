package ru.bmstu.evernote.account;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;
import ru.bmstu.evernote.provider.database.tables.ResourcesTable;
import ru.bmstu.evernote.provider.database.tables.TransactionsTable;

/**
 * Created by Ivan on 23.12.2014.
 */
public class DatabaseHelper {
    private final ContentProviderClient contentProviderClient;

    public DatabaseHelper(ContentProviderClient contentProviderClient) {
        this.contentProviderClient = contentProviderClient;
    }

    public boolean insertNotebook(String name, String guid, long usn, long created, long updated) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotebooksTable.NAME, name);
        contentValues.put(NotebooksTable.GUID, guid);
        contentValues.put(NotebooksTable.USN, usn);
        contentValues.put(NotebooksTable.CREATED, created);
        contentValues.put(NotebooksTable.UPDATED, updated);
        contentValues.put(NotebooksTable.IS_LOCALLY_DELETED, 0);
        Uri result = null;
        try {
            result = contentProviderClient.insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
        } catch (Exception e) {
            return false;
        }
        return result != null;
    }


    public long insertNote(String title, String content, String guid, long usn, long created, long updated, long notebooksId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotesTable.TITLE, title);
        contentValues.put(NotesTable.CONTENT, content);
        contentValues.put(NotesTable.GUID, guid);
        contentValues.put(NotesTable.USN, usn);
        contentValues.put(NotesTable.CREATED, created);
        contentValues.put(NotesTable.UPDATED, updated);
        contentValues.put(NotesTable.NOTEBOOKS_ID, notebooksId);
        contentValues.put(NotesTable.IS_LOCALLY_DELETED, 0);
        Uri result = contentProviderClient.insert(EvernoteContentProvider.NOTES_URI, contentValues);
        return Long.parseLong(result.getLastPathSegment());
    }


    public long insertResource(String guid, String filename, String mimeType, long notesId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResourcesTable.GUID, guid);
        contentValues.put(ResourcesTable.FILENAME, filename);
        contentValues.put(ResourcesTable.MIME_TYPE, mimeType);
        contentValues.put(ResourcesTable.NOTES_ID, notesId);
        contentValues.put(ResourcesTable.IS_LOCALLY_DELETED, 0);
        Uri result = contentProviderClient.insert(EvernoteContentProvider.RESOURCES_URI, contentValues);
        return Long.parseLong(result.getLastPathSegment());
    }


    public boolean updateNotebook(String name, long usn, long updated, long notebooksId) throws RemoteException {
        ContentValues values = new ContentValues();
        values.put(NotebooksTable.NAME, name);
        values.put(NotebooksTable.USN, usn);
        values.put(NotebooksTable.UPDATED, updated);
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = contentProviderClient.update(notebookUri, values, null, null);
        return result != 0;
    }

    public boolean updateNote(String title, String content, long usn, long updated, long notesId) throws RemoteException {
        ContentValues values = new ContentValues();
        values.put(NotesTable.TITLE, title);
        values.put(NotesTable.CONTENT, content);
        values.put(NotesTable.USN, usn);
        values.put(NotesTable.UPDATED, updated);
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI, notesId);
        int result = contentProviderClient.update(notesUri, values, null, null);
        return result != 0;
    }


    public boolean deleteNotebookFromDatabase(long notebooksId) throws RemoteException {
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = contentProviderClient.delete(notebookUri, null, null);
        return result != 0;
    }


    public boolean deleteNoteFromDatabase(long notesId) throws RemoteException {
        Uri noteUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notesId);
        int result = contentProviderClient.delete(noteUri, null, null);
        return result != 0;
    }


    public boolean deleteResourceFromDatabase(long resourcesId) throws RemoteException {
        Uri resourceUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, resourcesId);
        int result = contentProviderClient.delete(resourceUri, null, null);
        return result != 0;
    }

    public boolean resolveNotebooksNamesConflict(String name) throws RemoteException {
        int i = 2;
        Cursor cursor = contentProviderClient.query(EvernoteContentProvider.NOTEBOOKS_URI, new String[]{NotebooksTable._ID},
                NotebooksTable.NAME + "='" + name + "'", null, null);
        if (cursor.getCount() != 1)
            return false;
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(NotebooksTable._ID));
        Uri uri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, id);
        while (true) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NotebooksTable.NAME, name + "(" + i + ")");
            try {
                contentProviderClient.update(uri, contentValues, null, null);
                break;
            } catch (Exception ignored) {
                i++;
            }
        }
        return true;
    }


    public Cursor getNotebookByGuid(String guid) throws RemoteException {
        Cursor cursor = contentProviderClient.query(EvernoteContentProvider.NOTEBOOKS_URI, NotebooksTable.ALL_COLUMNS,
                NotebooksTable.GUID + "='" + guid + "'", null, null);
        return cursor;
    }

    public boolean deleteNotebookTransactionIfExistsAndNotDelete(long notebooksId) throws RemoteException {
        int deleted = 0;
        Cursor notebookInTransactionsTable = contentProviderClient.query(EvernoteContentProvider.TRANSACTION_URI, TransactionsTable.ALL_COLUMNS,
                TransactionsTable.TYPE + "=" + TransactionsTable.Type.NOTEBOOK.ordinal() + " AND " + TransactionsTable.ID + "=" + TransactionsTable.ID + "=" + notebooksId, null, null);
        if (notebookInTransactionsTable.getCount() > 0) {
            notebookInTransactionsTable.moveToFirst();
            if (TransactionsTable.Method.values()[notebookInTransactionsTable.getInt(notebookInTransactionsTable.getColumnIndex(TransactionsTable.METHOD))].equals(TransactionsTable.Method.CREATE_OR_UPDATE)) {
                long transactionId = notebookInTransactionsTable.getLong(notebookInTransactionsTable.getColumnIndex(TransactionsTable._ID));
                Uri transaction = ContentUris.withAppendedId(EvernoteContentProvider.TRANSACTION_URI, transactionId);
                deleted = contentProviderClient.delete(transaction, null, null);
            }
        }
        return deleted == 1;
    }

    public Cursor getResourceByGuid(String guid) throws RemoteException {
        Cursor cursor = contentProviderClient.query(EvernoteContentProvider.RESOURCES_URI, ResourcesTable.ALL_COLUMNS,
                ResourcesTable.GUID + "='" + guid + "'", null, null);
        return cursor;
    }

    public Cursor getNoteByGuid(String guid) throws RemoteException {
        Cursor cursor = contentProviderClient.query(EvernoteContentProvider.NOTES_URI, NotesTable.ALL_COLUMNS,
                NotesTable.GUID + "='" + guid + "'", null, null);
        return cursor;
    }
}