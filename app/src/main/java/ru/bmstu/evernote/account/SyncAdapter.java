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
import com.evernote.thrift.TException;

import java.util.List;

import ru.bmstu.evernote.EvernoteUtil;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;
import static ru.bmstu.evernote.provider.database.EvernoteContract.Notes;

/**
 * Created by Ivan on 10.12.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private DatabaseHelper databaseHelper;
    private NoteStore.Client noteStoreClient;
    private Context context;

    public SyncAdapter(Context context) {
        super(context, true);
        this.context = context;
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        try {
            databaseHelper = new DatabaseHelper(contentProviderClient);
            EvernoteSession evernoteSession = EvernoteSession.initInstance(getContext(), EvernoteSession.EvernoteService.SANDBOX);
            AccountManager accountManager = AccountManager.get(getContext());
            String authToken = accountManager.blockingGetAuthToken(account, account.type, false);
            noteStoreClient = evernoteSession.getClientFactory().getNoteStoreClient();
            SyncState syncState = noteStoreClient.getSyncState(authToken);
            long updateCount = syncState.getUpdateCount();
            long fullSyncBefore = syncState.getFullSyncBefore();
            long lastSyncTime = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_SYNC_TIME));
            long lastUpdateCount = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_UPDATED_COUNT));
            if (fullSyncBefore > lastSyncTime) {
                fullSync(authToken, updateCount);
            } else if (updateCount == lastUpdateCount) {
                sendChanges(authToken);
            } else {
                incrementalSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fullSync(String authToken, long updateCount) throws TException, EDAMUserException, EDAMSystemException, RemoteException, EDAMNotFoundException {
        int afterUSN = 0;
        int maxEntries = 10;
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotebooks(true);
        filter.setIncludeNotes(true);
        filter.setIncludeExpunged(true);

        SyncChunk syncChunk;
        int chunkHighUSN;
        do {
            syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
            chunkHighUSN = syncChunk.getChunkHighUSN();
            afterUSN = chunkHighUSN;
            processSyncChunk(syncChunk, authToken);
        } while (chunkHighUSN < updateCount);
        sendChanges(authToken);
    }

    private void processSyncChunk(SyncChunk syncChunk, String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        List<String> expungedNotebooks = syncChunk.getExpungedNotebooks();
        List<String> expungedNotes = syncChunk.getExpungedNotes();
        List<Notebook> notebooks = syncChunk.getNotebooks();
        List<Note> notes = syncChunk.getNotes();
        if (expungedNotes != null) {
            for (String guid : expungedNotes) {
                databaseHelper.deleteNoteWithSpecifiedGuid(guid);
            }
        }
        if (expungedNotebooks != null) {
            for (String guid : expungedNotebooks) {
                databaseHelper.deleteNotebookWithSpecifiedGuid(guid);
            }
        }
        if (notebooks != null) {
            for (Notebook notebook : notebooks) {
                String guid = notebook.getGuid();
                String name = notebook.getName();
                long created = notebook.getServiceCreated();
                long updated = notebook.getServiceUpdated();
                long usn = notebook.getUpdateSequenceNum();
                Cursor cursor = databaseHelper.getNotebookWithSpecifiedGuid(guid);
                int count = cursor.getCount();
                switch (count) {
                    case 0:
                        databaseHelper.insertNotebookWithConflictResolution(name, guid, created, updated, usn);
                        break;
                    case 1:
                        cursor.moveToFirst();
                        long id = cursor.getLong(cursor.getColumnIndex(Notebooks._ID));
                        long localNotebooksUsn = cursor.getLong(cursor.getColumnIndex(Notebooks.USN));
                        if (usn > localNotebooksUsn) {
                            databaseHelper.updateNotebook(name, updated, usn, id);
                        }
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
        if (notes != null) {
            for (Note note : notes) {
                String title = note.getTitle();
                String guid = note.getGuid();
                String notebooksGuid = note.getNotebookGuid();
                long created = note.getCreated();
                long updated = note.getUpdated();
                long deleted = note.getDeleted();
                long usn = note.getUpdateSequenceNum();
                String content = noteStoreClient.getNoteContent(authToken, guid);
                long notesId;
                if (deleted != 0) {
                    databaseHelper.deleteNotebookWithSpecifiedGuid(guid);
                } else {
                    Cursor notesCursor = databaseHelper.getNoteWithSpecifiedGuid(guid);
                    int count = notesCursor.getCount();
                    switch (count) {
                        case 0:
                            Cursor notebooksCursor = databaseHelper.getNotebookWithSpecifiedGuid(notebooksGuid);
                            notebooksCursor.moveToFirst();
                            long notebooksId = notebooksCursor.getLong(notebooksCursor.getColumnIndex(Notebooks._ID));
                            databaseHelper.insertNote(title, content, guid, created, updated, usn, notebooksId);
                            break;
                        case 1:
                            notesCursor.moveToFirst();
                            notesId = notesCursor.getLong(notesCursor.getColumnIndex(Notes._ID));
                            databaseHelper.updateNote(title, content, updated, usn, notesId);
                            break;
                        case 2:
                            throw new IllegalStateException();
                    }
                }
            }

        }
    }


    private void incrementalSync() {

    }

    private void sendChanges(String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        databaseHelper.deleteNotes();
        databaseHelper.deleteNotebooks();

        sendNotebooks(authToken);
        sendNotes(authToken);
    }

    private void sendNotes(String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Cursor notes = databaseHelper.getChangedNotes();
        while (notes.moveToNext()) {
            long id = notes.getLong(notes.getColumnIndex(Notes._ID));
            long usn = notes.getLong(notes.getColumnIndex(Notes.USN));
            String title = notes.getString(notes.getColumnIndex(Notes.TITLE));
            String content = notes.getString(notes.getColumnIndex(Notes.CONTENT));
            String guid = notes.getString(notes.getColumnIndex(Notes.GUID));
            long notebooksId = notes.getLong(notes.getColumnIndex(Notes.NOTEBOOKS_ID));
            Cursor notebook = databaseHelper.getNotebook(notebooksId);
            notebook.moveToFirst();
            String notebooksGuid = notebook.getString(notebook.getColumnIndex(Notebooks.GUID));
            Note note = new Note();
            note.setNotebookGuid(notebooksGuid);
            note.setTitle(title);
            note.setContent(EvernoteUtil.NOTE_PREFIX + content + EvernoteUtil.NOTE_SUFFIX);
            note.setGuid(guid);
            Note serversNote;
            if (usn == 0) {
                serversNote = noteStoreClient.createNote(authToken, note);
            } else {
                serversNote = noteStoreClient.updateNote(authToken, note);
            }
            String serversGuid = serversNote.getGuid();
            String serversTitle = serversNote.getTitle();
            String serverContent = serversNote.getContent();
            long serversUSN = serversNote.getUpdateSequenceNum();
            long serversCreated = serversNote.getCreated();
            long serversUpdated = serversNote.getUpdated();
            databaseHelper.updateNote(serversGuid, serversTitle, serverContent, serversUpdated, serversCreated, serversUSN, id);
        }
    }

    private void sendNotebooks(String authToken) throws RemoteException, TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
        Cursor notebooks = databaseHelper.getChangedNotebooks();
        while (notebooks.moveToNext()) {
            long id = notebooks.getLong(notebooks.getColumnIndex(Notebooks._ID));
            long usn = notebooks.getLong(notebooks.getColumnIndex(Notebooks.USN));
            String name = notebooks.getString(notebooks.getColumnIndex(Notebooks.NAME));
            String guid = notebooks.getString(notebooks.getColumnIndex(Notebooks.GUID));
            Notebook notebook = new Notebook();
            notebook.setName(name);
            notebook.setGuid(guid);
            if (usn == 0) {
                Notebook serversNotebook = noteStoreClient.createNotebook(authToken, notebook);
                String serversGuid = serversNotebook.getGuid();
                String serverName = serversNotebook.getName();
                long serversUSN = serversNotebook.getUpdateSequenceNum();
                long serversCreated = serversNotebook.getServiceCreated();
                long serversUpdated = serversNotebook.getServiceUpdated();
                databaseHelper.updateNotebook(serversGuid, serverName, serversCreated, serversUpdated, serversUSN, id);
            } else {
                long serversUSN = noteStoreClient.updateNotebook(authToken, notebook);
                databaseHelper.updateNotebook(serversUSN, id);
            }
        }
    }

}