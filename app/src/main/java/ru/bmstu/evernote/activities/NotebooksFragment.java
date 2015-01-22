package ru.bmstu.evernote.activities;


import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotebooksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private CursorAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Object mStatusChangeListener;

    private final String[] from = new String[]{Notebooks.NAME};
    private final int[] to = new int[]{R.id.name};

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
        View v = inflater.inflate(R.layout.notebook_item, container, false);
        TextView name = (TextView) v.findViewById(R.id.name);
        Typeface regular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        name.setTypeface(regular);
        return inflater.inflate(R.layout.fragment_notebooks, null, false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(getActivity(), ItemDetailActivity.class);
        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                Notebooks.CONTENT_URI,
                Notebooks.ALL_COLUMNS_PROJECTION,
                Notebooks.NOT_DELETED_SELECTION,
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