package ru.bmstu.evernote.activities;



import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Arrays;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.EvernoteContentProvider;
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;

/**
 * A simple {@link Fragment} subclass.
 *
 */
//public class NotesFragment extends Fragment {
//
//
//    public NotesFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_notes, container, false);
//    }
//
//
//}
public class NotesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private CursorAdapter mAdapter;

    private final String[] from = new String[]{NotesTable._ID, NotesTable.TITLE, NotesTable.GUID, NotesTable.CREATED, NotesTable.UPDATED, NotesTable.USN, NotesTable.NOTEBOOKS_ID};
    private final int[] to = new int[]{R.id.id, R.id.name, R.id.guid, R.id.created, R.id.updated, R.id.usn, R.id.notebook_id };

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.note_item, null, from, to, 0);
        Log.e("","@!(#*$^!&(*@^#$%)(!&@^#$(*&!@#^$(!*&#$^!(@#*&$");
        Log.e("","!@$)(#*%&^!@(*&#^$!*(@#&$^(!@*#&$^(!#@*&$^!(*&");
//        System.out.println(Arrays.toString(from));
        Log.e("","@!(#*$^!&(*@^#$%)(!&@^#$(*&!@#^$(!*&#$^!(@#*&$");
        Log.e("","!@$)(#*%&^!@(*&#^$!*(@#&$^(!@*#&$^(!#@*&$^!(*&");
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, null, false);
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
                EvernoteContentProvider.NOTES_URI,
                NotesTable.ALL_COLUMNS,
                NotesTable.NOT_DELETED_CONDITION,
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
