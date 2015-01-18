package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;
import static ru.bmstu.evernote.provider.database.EvernoteContract.Notes;
import static ru.bmstu.evernote.provider.database.EvernoteContract.Resources;

/**
 * Created by Ivan on 10.12.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context) {
        super(context, true);
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
                sendChanges(getContext(), databaseHelper, noteStoreClient, authToken);
            } else {
                incrementalSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fullSync(NoteStore.Client noteStoreClient, String authToken, long updateCount, DatabaseHelper databaseHelper) throws TException, EDAMUserException, EDAMSystemException, RemoteException, EDAMNotFoundException {
        int afterUSN = 0;
        int maxEntries = 10;
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotebooks(true);
        filter.setIncludeNotes(true);
        filter.setIncludeResources(true);
        filter.setIncludeExpunged(true);

        SyncChunk syncChunk;
        int chunkHighUSN;
        do {
            syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
            chunkHighUSN = syncChunk.getChunkHighUSN();
            afterUSN = chunkHighUSN;
            processSyncChunk(getContext(), syncChunk, databaseHelper);
        } while (chunkHighUSN < updateCount);

        sendChanges(getContext(), databaseHelper, noteStoreClient, authToken);
    }

    private void processSyncChunk(Context context, SyncChunk syncChunk, DatabaseHelper databaseHelper) throws RemoteException {
        List<String> expungedNotebooks = syncChunk.getExpungedNotebooks();
        List<String> expungedNotes = syncChunk.getExpungedNotes();
        List<Notebook> notebooks = syncChunk.getNotebooks();
        List<Note> notes = syncChunk.getNotes();
        List<Resource> resources = syncChunk.getResources();
        if (expungedNotes != null) {
            for (String guid : expungedNotes) {
                deleteNoteWithSpecifiedGuid(context, databaseHelper, guid);
            }
        }
        if (expungedNotebooks != null) {
            for (String guid : expungedNotebooks) {
                deleteNotebookWithSpecifiedGuid(context, databaseHelper, guid);
            }
        }
        if (notebooks != null) {
            for (Notebook notebook: notebooks) {
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
            for (Note note: notes) {
                String title = note.getTitle();
                String content = note.getContent();
                String guid = note.getGuid();
                long created = note.getCreated();
                long updated = note.getUpdated();
                long deleted = note.getDeleted();
                if (deleted != 0) {
                    databaseHelper.deleteNotebookWithSpecifiedGuid(guid);
                } else {

                }
            }

        }
        if (resources != null) {

        }
    }

    private void deleteNotebookWithSpecifiedGuid(Context context, DatabaseHelper databaseHelper, String guid) throws RemoteException {
        Cursor notebook = databaseHelper.getNotebookWithSpecifiedGuid(guid);
        int count = notebook.getCount();
        switch (count) {
            case 0:
                break;
            case 1:
                notebook.moveToFirst();
                long notebooksId = notebook.getLong(notebook.getColumnIndex(Notebooks._ID));
                Cursor notes = databaseHelper.getNotesWithSpecifiedNotebooksId(notebooksId);
                while (notes.moveToNext()) {
                    long notesId = notes.getLong(notes.getColumnIndex(Notes._ID));
                    Cursor resources = databaseHelper.getResourcesWithSpecifiedNotesId(notesId);
                    while (resources.moveToNext()) {
                        long resourceId = resources.getLong(resources.getColumnIndex(Resources._ID));
                        context.deleteFile(((Long)resourceId).toString());
                    }
                }
                databaseHelper.deleteNotebook(notebooksId);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void deleteNoteWithSpecifiedGuid(Context context, DatabaseHelper databaseHelper, String guid) throws RemoteException {
        Cursor cursor = databaseHelper.getNoteWithSpecifiedGuid(guid);
        int count = cursor.getCount();
        switch (count) {
            case 0:
                break;
            case 1:
                cursor.moveToFirst();
                long notesId = cursor.getLong(cursor.getColumnIndex(Notes._ID));
                Cursor deletedNoteResourcesCursor = databaseHelper.getResourcesWithSpecifiedNotesId(notesId);
                while (deletedNoteResourcesCursor.moveToNext()) {
                    long resourceId = deletedNoteResourcesCursor.getLong(deletedNoteResourcesCursor.getColumnIndex(Resources._ID));
                    context.deleteFile(((Long)resourceId).toString());
                }
                databaseHelper.deleteNote(notesId);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void incrementalSync() {

    }

    private void sendChanges(Context context, DatabaseHelper databaseHelper, NoteStore.Client noteStoreClient, String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        deleteNotebooks(context, databaseHelper, noteStoreClient, authToken);


        sendNotebooks(databaseHelper, noteStoreClient, authToken);
    }

    private void sendNotebooks(DatabaseHelper databaseHelper, NoteStore.Client noteStoreClient, String authToken) throws RemoteException, TException, EDAMUserException, EDAMSystemException, EDAMNotFoundException {
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

    private void deleteNotebooks(Context context, DatabaseHelper databaseHelper, NoteStore.Client noteStoreClient, String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Cursor notebooksCursor = databaseHelper.getDeletedNotebooks();
        while (notebooksCursor.moveToNext()) {
            String notebooksGuid = notebooksCursor.getString(notebooksCursor.getColumnIndex(Notebooks.GUID));
            long notebooksId = notebooksCursor.getLong(notebooksCursor.getColumnIndex(Notebooks._ID));
            Cursor notesCursor = databaseHelper.getNotesWithSpecifiedNotebooksId(notebooksId);
            while (notesCursor.moveToNext()) {
                long notesId = notesCursor.getLong(notesCursor.getColumnIndex(Notes._ID));
                Cursor resourcesCursor = databaseHelper.getResourcesWithSpecifiedNotesId(notesId);
                while (resourcesCursor.moveToNext()) {
                    long resourceId = resourcesCursor.getLong(resourcesCursor.getColumnIndex(Resources._ID));
                    context.deleteFile(((Long)resourceId).toString());
                }
            }
            noteStoreClient.deleteNote(authToken, notebooksGuid);
            databaseHelper.deleteNotebook(notebooksId);
        }
    }

    private void deleteNotes(Context context, ContentProviderClient contentProviderClient, NoteStore.Client noteStoreClient, String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Cursor deletedNotesCursor = contentProviderClient.query(Notes.CONTENT_URI, Notes.ALL_COLUMNS_PROJECTION, Notes.DELETED_SELECTION, null, null);
        while (deletedNotesCursor.moveToNext()) {
            String notesGuid = deletedNotesCursor.getString(deletedNotesCursor.getColumnIndex(Notes.GUID));
            long notesId = deletedNotesCursor.getLong(deletedNotesCursor.getColumnIndex(Notes._ID));
            Cursor deletedNotesResourcesCursor = contentProviderClient.query(Resources.CONTENT_URI, Resources.ALL_COLUMNS_PROJECTION, Resources.WITH_SPECIFIED_NOTES_ID_SELECTION, new String[]{((Long)notesId).toString()}, null);
            while (deletedNotesResourcesCursor.moveToNext()) {
                long resourceId = deletedNotesResourcesCursor.getLong(deletedNotesResourcesCursor.getColumnIndex(Resources._ID));
                context.deleteFile(((Long)resourceId).toString());
            }
            noteStoreClient.deleteNote(authToken, notesGuid);
            Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notesId);
            contentProviderClient.delete(notesUri, null, null);
        }
    }
    private void deleteResources(Context context, ContentProviderClient contentProviderClient, NoteStore.Client noteStoreClient, String authToken) throws RemoteException, EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Cursor deletedResourcesCursor = contentProviderClient.query(Resources.CONTENT_URI, Resources.ALL_COLUMNS_PROJECTION, Resources.DELETED_SELECTION, null, null);
        Map<String, List<String>> notesToResourcesGuids = new HashMap<>();
        Set<Long> resourcesSet = new HashSet<>();
        while (deletedResourcesCursor.moveToNext()) {
            long notesId = deletedResourcesCursor.getLong(deletedResourcesCursor.getColumnIndex(Resources.NOTES_ID));
            long resourceId = deletedResourcesCursor.getLong(deletedResourcesCursor.getColumnIndex(Resources._ID));
            Uri notesUri = ContentUris.withAppendedId(Notes.CONTENT_URI, notesId);
            Cursor noteCursor = contentProviderClient.query(notesUri, Notes.ALL_COLUMNS_PROJECTION, null, null, null);
            String resourcesGuid = deletedResourcesCursor.getString(deletedResourcesCursor.getColumnIndex(Resources.GUID));
            String notesGuid = noteCursor.getString(noteCursor.getColumnIndex(Notes.GUID));
            List<String> resourcesGuids = notesToResourcesGuids.get(notesGuid);
            if (resourcesGuids == null) {
                resourcesGuids = new LinkedList<>();
                notesToResourcesGuids.put(notesGuid, resourcesGuids);
            }
            resourcesGuids.add(resourcesGuid);
            resourcesSet.add(resourceId);
        }
        for (Map.Entry<String, List<String>> entry : notesToResourcesGuids.entrySet()) {
            String noteGuid = entry.getKey();
            List<String> resourcesGuids = entry.getValue();
            Note note = noteStoreClient.getNote(authToken, noteGuid, false, false, false, false);
            List<Resource> resources = note.getResources();
            for (Resource resource : resources) {
                for (String guid : resourcesGuids) {
                    if (resource.getGuid().equals(guid))
                        resources.remove(resource);
                }
            }
            noteStoreClient.updateNote(authToken, note);
        }
        for (Long id: resourcesSet) {
            context.deleteFile(id.toString());
            Uri resourceUri = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
            contentProviderClient.delete(resourceUri, null, null);
        }
    }
}