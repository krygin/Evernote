package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.thrift.TException;

import java.util.LinkedList;
import java.util.List;

import ru.bmstu.evernote.FileProcessor;
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;

/**
 * Created by Ivan on 10.12.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final Context mContext;

    public SyncAdapter(Context context) {
        super(context, true);
        mContext = context;
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(contentProviderClient);
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
                fullSync(noteStoreClient, authToken, updateCount, databaseHelper);
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

    private void fullSync(NoteStore.Client noteStoreClient, String authToken, long updateCount, DatabaseHelper databaseHelper) throws TException, EDAMUserException, EDAMSystemException, RemoteException, EDAMNotFoundException {
        int afterUSN = 0;
        int maxEntries = 10;
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotebooks(true);
        filter.setIncludeNotes(true);
        filter.setIncludeNoteResources(true);
        List<SyncChunk> syncChunkList = new LinkedList<>();


        SyncChunk syncChunk = null;
        int chunkHighUSN = 0;
        do {
            syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
            chunkHighUSN = syncChunk.getChunkHighUSN();
            afterUSN = chunkHighUSN;
            processSyncChunk(authToken, noteStoreClient, syncChunk, databaseHelper);
        } while (chunkHighUSN < updateCount);
    }

    private void processSyncChunk(String authToken, NoteStore.Client noteStoreClient, SyncChunk syncChunk, DatabaseHelper databaseHelper) throws RemoteException, EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        List<Notebook> notebooks = syncChunk.getNotebooks();
        processNotebooksFromChunk(databaseHelper, notebooks);
        List<Note> notes = syncChunk.getNotes();
        processNotesFromChunk(authToken, noteStoreClient, databaseHelper, notes);
    }

    private boolean processNotebooksFromChunk(DatabaseHelper databaseHelper, List<Notebook> notebooks) throws RemoteException {
        if (notebooks == null) {
            return true;
        }
        for (Notebook notebook : notebooks) {
            String guid = notebook.getGuid();
            String name = notebook.getName();
            long usn = notebook.getUpdateSequenceNum();
            long created = notebook.getServiceCreated();
            long updated = notebook.getServiceUpdated();
            Cursor cursor = databaseHelper.getNotebookByGuid(guid);
            int count = cursor.getCount();
            switch (count) {
                case 0:
                    if (!databaseHelper.insertNotebook(name, guid, usn, created, updated)) {
                        databaseHelper.resolveNotebooksNamesConflict(name);
                        databaseHelper.insertNotebook(name, guid, usn, created, updated);
                    }
                    break;
                case 1:
                    cursor.moveToFirst();
                    long notebooksId = cursor.getLong(cursor.getColumnIndex(NotebooksTable._ID));
                    databaseHelper.updateNotebook(name, usn, updated, notebooksId);
                    databaseHelper.deleteNotebookTransactionIfExistsAndNotDelete(notebooksId);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return true;
    }

    private boolean processNotesFromChunk(String authToken, NoteStore.Client noteStoreClient, DatabaseHelper databaseHelper, List<Note> notes) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        if (notes == null)
            return true;
        for (Note note : notes) {
            String guid = note.getGuid();
            String notebooksGuid = note.getNotebookGuid();
            long usn = note.getUpdateSequenceNum();
            long created = note.getCreated();
            long updated = note.getDeleted();
            String title = note.getTitle();
            String content = noteStoreClient.getNoteContent(authToken, guid);
            Cursor noteCursor = databaseHelper.getNoteByGuid(guid);
            Cursor notebooksCursor = databaseHelper.getNotebookByGuid(notebooksGuid);
            int count = noteCursor.getCount();
            notebooksCursor.moveToFirst();
            long notebooksId = notebooksCursor.getLong(notebooksCursor.getColumnIndex(NotebooksTable._ID));
            long notesId = 0;
            switch (count) {
                case 0:
                    notesId = databaseHelper.insertNote(title, content, guid, usn, created, updated, notebooksId);
                    break;
                case 1:
                    noteCursor.moveToFirst();
                    notesId = noteCursor.getLong(noteCursor.getColumnIndex(NotesTable._ID));
                    databaseHelper.updateNote(title, content, usn, updated, notesId);
                    break;
                default:
                    throw new IllegalStateException();
            }
            FileProcessor fp = new FileProcessor(mContext);

            if (note.getResources() != null) {
                for (Resource resource: note.getResources()) {
                    String filename = resource.getAttributes().getFileName();
                    String mimeType = resource.getMime();

                    String resourceGuid = resource.getGuid();
                    Cursor resourceCursor = databaseHelper.getResourceByGuid(resourceGuid);
                    int resourcesCount = resourceCursor.getCount();
                    long resourceId = 0;
                    switch (resourcesCount) {
                        case 0:
                            byte[] data;
                            resourceId = databaseHelper.insertResource(resourceGuid, filename, mimeType, notesId);
                            data = noteStoreClient.getResourceData(authToken, resourceGuid);
                            fp.writeFile(String.valueOf(resourceId), data);
                            break;
                        case 1:
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }
            }
        }
        return true;
    }
}