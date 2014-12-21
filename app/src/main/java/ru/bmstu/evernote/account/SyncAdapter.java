package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;
import ru.bmstu.evernote.provider.database.tables.ResourcesTable;
import ru.bmstu.evernote.provider.database.tables.TransactionsTable;

import static ru.bmstu.evernote.provider.database.tables.TransactionsTable.Method;
import static ru.bmstu.evernote.provider.database.tables.TransactionsTable.Type;

/**
 * Created by Ivan on 10.12.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private ContentResolver mContentResolver;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public SyncAdapter(Context context) {
        super(context, true);

        mContentResolver = context.getContentResolver();
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        try {
            EvernoteSession evernoteSession = EvernoteSession.initInstance(getContext(), EvernoteSession.EvernoteService.SANDBOX);
            AccountManager accountManager = AccountManager.get(getContext());
            String authToken = accountManager.blockingGetAuthToken(account, account.type, false);
            NoteStore.Client noteStoreClient = evernoteSession.getClientFactory().getNoteStoreClient();
            SyncState syncState = noteStoreClient.getSyncState(authToken);
            long updateCount = syncState.getUpdateCount();
            long fullSyncBefore = syncState.getFullSyncBefore();
            long lastSyncTime = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_SYNC_TIME));
            long lastUpdateCount = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_UPDATED_COUNT));

            if (fullSyncBefore > lastSyncTime) {
                fullSync(noteStoreClient,authToken, updateCount, contentProviderClient);
            } else if (updateCount == lastUpdateCount) {
                sendChanges();
            } else {
                incrementalSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void incrementalSync() {

    }

    private void sendChanges() {

    }

    private void fullSync(NoteStore.Client noteStoreClient, String authToken, long updateCount, ContentProviderClient contentProviderClient) throws TException, EDAMUserException, EDAMSystemException {
        int afterUSN = 0;
        int maxEntries = 10;
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotebooks(true);
        filter.setIncludeNotes(true);
        filter.setIncludeResources(true);
        List<SyncChunk> syncChunkList = new LinkedList<>();


        SyncChunk syncChunk = null;
        int chunkHighUSN = 0;
        do {
            syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
            chunkHighUSN = syncChunk.getChunkHighUSN();
            afterUSN = chunkHighUSN;
            processSyncChunk(authToken, syncChunk, contentProviderClient);
        } while (chunkHighUSN < updateCount);

        new Integer(4).toString();
    }

    private void processSyncChunk(String authToken, SyncChunk syncChunk, ContentProviderClient contentProviderClient) {
        List<Notebook> notebooks = syncChunk.getNotebooks();
        if (notebooks != null) {
            for (Notebook notebook : notebooks) {
                try {
                    String name = notebook.getName();
                    String guid = notebook.getGuid();
                    long usn = notebook.getUpdateSequenceNum();
                    long created = notebook.getServiceCreated();
                    long updated = notebook.getServiceUpdated();
                    insertNotebook(contentProviderClient, name, guid, usn, created, updated);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class Transaction {
        private long _id;
        private Method method;
        private Type type;
        private long id;

        private Transaction(long _id, Method method, Type type, long id) {
            this._id = _id;
            this.method = method;
            this.type = type;
            this.id = id;
        }

        public long get_id() {
            return _id;
        }

        public Method getMethod() {
            return method;
        }

        public Type getType() {
            return type;
        }

        public long getId() {
            return id;
        }
    }

    private class Transactions {
        private Map<Long, Transaction> transactions = new HashMap<>();
        private NoteStore.Client noteStoreClient;
        private String authToken;
        public Transactions(Cursor cursor) {
            while (cursor.moveToNext()) {
                long _id = cursor.getLong(cursor.getColumnIndex(TransactionsTable._ID));
                Method method = Method.values()[cursor.getInt(cursor.getColumnIndex(TransactionsTable.METHOD))];
                Type type = Type.values()[cursor.getInt(cursor.getColumnIndex(TransactionsTable.TYPE))];
                long id = cursor.getLong(cursor.getColumnIndex(TransactionsTable.ID));
                transactions.put(_id, new Transaction(_id, method, type, id));
            }
        }

        public Transaction getTransaction() {
            return null;
        }
    }


    private boolean insertNotebook(ContentProviderClient contentProviderClient, String name, String guid, long usn, long created, long updated) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotebooksTable.NAME, name);
        contentValues.put(NotebooksTable.GUID, guid);
        contentValues.put(NotebooksTable.USN, usn);
        contentValues.put(NotebooksTable.CREATED, created);
        contentValues.put(NotebooksTable.UPDATED, updated);
        contentValues.put(NotebooksTable.IS_LOCALLY_DELETED, 0);
        Uri result = contentProviderClient.insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
        return result != null;
    }


    private boolean insertNote(ContentProviderClient contentProviderClient, String title, String guid, long usn, long created, long updated, long notebooksId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NotesTable.TITLE, title);
        contentValues.put(NotesTable.GUID, guid);
        contentValues.put(NotesTable.USN, usn);
        contentValues.put(NotesTable.CREATED, created);
        contentValues.put(NotesTable.UPDATED, updated);
        contentValues.put(NotesTable.NOTEBOOKS_ID, notebooksId);
        contentValues.put(NotesTable.IS_LOCALLY_DELETED, 0);
        Uri result = contentProviderClient.insert(EvernoteContentProvider.NOTEBOOKS_URI, contentValues);
        return result != null;
    }


    private boolean insertResource(ContentProviderClient contentProviderClient, String guid, String resource, String mimeType, long notesId) throws RemoteException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResourcesTable.GUID, guid);
        contentValues.put(ResourcesTable.PATH_TO_RESOURCE, resource);
        contentValues.put(ResourcesTable.MIME_TYPE, mimeType);
        contentValues.put(ResourcesTable.NOTES_ID, notesId);
        contentValues.put(ResourcesTable.IS_LOCALLY_DELETED, 0);
        Uri result = contentProviderClient.insert(EvernoteContentProvider.NOTES_URI, contentValues);
        return result != null;
    }


    private boolean updateNotebook(ContentProviderClient contentProviderClient, String name, long usn, long updated, long notebooksId) throws RemoteException {
        ContentValues values = new ContentValues();
        values.put(NotebooksTable.NAME, name);
        values.put(NotebooksTable.USN, usn);
        values.put(NotebooksTable.UPDATED, updated);
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = contentProviderClient.update(notebookUri, values, null, null);
        return result != 0;
    }

    private boolean updateNote(ContentProviderClient contentProviderClient, String title, long usn, long updated, long notesId) throws RemoteException {
        ContentValues values = new ContentValues();
        values.put(NotesTable.TITLE, title);
        values.put(NotesTable.USN, usn);
        values.put(NotesTable.UPDATED, updated);
        Uri notesUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTES_URI, notesId);
        int result = contentProviderClient.update(notesUri, values, null, null);
        return result != 0;
    }


    private boolean deleteNotebookFromDatabase(ContentProviderClient contentProviderClient, long notebooksId) throws RemoteException {
        Uri notebookUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notebooksId);
        int result = contentProviderClient.delete(notebookUri, null, null);
        return result != 0;
    }


    private boolean deleteNoteFromDatabase(ContentProviderClient contentProviderClient, long notesId) throws RemoteException {
        Uri noteUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, notesId);
        int result = contentProviderClient.delete(noteUri, null, null);
        return result != 0;
    }


    private boolean deleteResourceFromDatabase(ContentProviderClient contentProviderClient, long resourcesId) throws RemoteException {
        Uri resourceUri = ContentUris.withAppendedId(EvernoteContentProvider.NOTEBOOKS_URI, resourcesId);
        int result = contentProviderClient.delete(resourceUri, null, null);
        return result != 0;
    }
}