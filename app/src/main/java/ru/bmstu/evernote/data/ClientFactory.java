package ru.bmstu.evernote.data;

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.protocol.TProtocol;
import com.evernote.thrift.transport.TTransport;
import com.evernote.thrift.transport.TTransportException;

import java.io.File;

import ru.bmstu.evernote.account.EvernoteSession;

/**
 * Created by Ivan on 10.12.2014.
 */
public class ClientFactory {

    private String mUserAgent;
    private File mTempDir;

    public ClientFactory(String userAgent, File tempDir) {
        mUserAgent = userAgent;
        mTempDir = tempDir;
    }


    public UserStore.Client getUserStoreClient() throws TTransportException {
        TTransport transport = new TEvernoteHttpClient("https://sandbox.evernote.com/edam/user", mUserAgent, mTempDir);
        TProtocol protocol = new TBinaryProtocol(transport);
        return new UserStore.Client(protocol);
    }

    public NoteStore.Client getNoteStoreClient() throws TTransportException {
        String noteStoreUrl = EvernoteSession.getInstance().getNoteStoreUrl();
        TTransport transport = new TEvernoteHttpClient(noteStoreUrl, mUserAgent, mTempDir);
        TProtocol protocol = new TBinaryProtocol(transport);
        return new NoteStore.Client(protocol);
    }
}