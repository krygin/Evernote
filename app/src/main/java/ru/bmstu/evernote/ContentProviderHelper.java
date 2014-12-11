package ru.bmstu.evernote;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;

import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.Notebooks;

/**
 * Created by Ivan on 11.12.2014.
 */
public class ContentProviderHelper implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int NOTEBOOKS_LOADER_ID = 0;

    private CursorAdapter mAdapter;
    private Context mContext;

    public ContentProviderHelper(Context context,CursorAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
        ((Activity)mContext).getLoaderManager().initLoader(NOTEBOOKS_LOADER_ID, null, this);
    }

    public void requery() {
        ((Activity)mContext).getLoaderManager().restartLoader(NOTEBOOKS_LOADER_ID, null, this);
    }

    public void insert(String name, String guid) {
        ContentValues values = new ContentValues();
        values.put(Notebooks.NAME, name);
        values.put(Notebooks.GUID, guid);
        values.put(Notebooks.CREATED, 0);
        values.put(Notebooks.UPDATED, 1);
        values.put(Notebooks.USN, 2);
        mContext.getContentResolver().insert(EvernoteContentProvider.NOTEBOOKS_URI, values);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext, EvernoteContentProvider.NOTEBOOKS_URI, Notebooks.ALL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
