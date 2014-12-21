package ru.bmstu.evernote.activities;


import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotebooksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private CursorAdapter mAdapter;

    private final String[] from = new String[]{NotebooksTable._ID, NotebooksTable.NAME, NotebooksTable.GUID, NotebooksTable.CREATED, NotebooksTable.UPDATED, NotebooksTable.USN};
    private final int[] to = new int[]{R.id.id, R.id.name, R.id.guid, R.id.created, R.id.updated, R.id.usn };

    public NotebooksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.notebook_item, null, from, to, 0);
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notebooks, null, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                EvernoteContentProvider.NOTEBOOKS_URI,
                NotebooksTable.ALL_COLUMNS,
                NotebooksTable.NOT_DELETED_CONDITION,
                null,
                null);
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