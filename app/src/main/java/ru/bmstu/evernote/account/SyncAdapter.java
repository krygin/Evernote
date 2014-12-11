package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncState;
import com.evernote.thrift.transport.TTransport;

import java.io.IOException;

import ru.bmstu.evernote.data.TEvernoteHttpClient;

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
            EvernoteSession evernoteSession = EvernoteSession.getInstance();
            AccountManager accountManager = AccountManager.get(getContext());
            String authToken = accountManager.blockingGetAuthToken(account, account.type, false);
            NoteStore.Client noteStoreClient = evernoteSession.getClientFactory().getNoteStoreClient();
            SyncState syncState = noteStoreClient.getSyncState(authToken);
            long lastSyncTime = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_SYNC_TIME));
            long lastUpdateCount = Long.parseLong(accountManager.getUserData(account, EvernoteAccount.EXTRA_LAST_UPDATED_COUNT));
            long updateCount = syncState.getUpdateCount();
            long fullSyncBefore = syncState.getFullSyncBefore();

            if (fullSyncBefore > lastSyncTime) {
                fullSync();
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

    private void fullSync() {

    }
}