package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncChunkFilter;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

import java.util.LinkedList;
import java.util.List;

import ru.bmstu.evernote.EvernoteUtil;

/**
 * Created by Ivan on 10.12.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    ContentResolver mContentResolver;

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
            Note note = new Note();
            note.setTitle("New note");
            note.setContent(EvernoteUtil.NOTE_PREFIX + "New note content" + EvernoteUtil.NOTE_SUFFIX);
            Note responseNote = noteStoreClient.createNote(authToken, note);

            noteStoreClient.deleteNote(authToken, responseNote.getGuid());

            if (fullSyncBefore > lastSyncTime) {
                fullSync(noteStoreClient,authToken);
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

    private void fullSync(NoteStore.Client noteStoreClient, String authToken) throws TException, EDAMUserException, EDAMSystemException {
        int afterUSN = 0;
        int maxEntries = 10;
        SyncChunkFilter filter = new SyncChunkFilter();
        filter.setIncludeNotebooks(true);
        filter.setIncludeNotes(true);
        filter.setIncludeResources(true);
        List<SyncChunk> syncChunkList = new LinkedList<>();


        SyncChunk syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
        syncChunkList.add(syncChunk);
        while (syncChunk.getChunkHighUSN() < syncChunk.getUpdateCount()) {
            afterUSN = syncChunk.getChunkHighUSN();
            syncChunk = noteStoreClient.getFilteredSyncChunk(authToken, afterUSN, maxEntries, filter);
            syncChunkList.add(syncChunk);
        }

        List<Notebook> notebooks = new LinkedList<>();
        List<Note> notes = new LinkedList<>();
        for (SyncChunk chunk: syncChunkList) {
            notebooks.addAll(chunk.getNotebooks());
            notes.addAll(chunk.getNotes());
        }


    }
}