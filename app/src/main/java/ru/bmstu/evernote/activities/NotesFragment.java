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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.EvernoteContract.Notes;

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


//    private final String[] from = new String[]{Notes._ID, Notes.TITLE, Notes.CONTENT, Notes.GUID, Notes.CREATED, Notes.UPDATED, Notes.USN, Notes.NOTEBOOKS_ID};
//    private final int[] to = new int[]{R.id.id, R.id.name, R.id.content, R.id.guid, R.id.created, R.id.updated, R.id.usn, R.id.notebook_id };

    private final String[] from = new String[]{Notes.TITLE, Notes.CONTENT};
    private final int[] to = new int[]{R.id.name, R.id.content};

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);


        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.note_item, null, from, to, 0);
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.note_item, container, false);
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView content = (TextView) v.findViewById(R.id.content);
        Typeface regular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface slabRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/RobotoSlab-Regular.ttf");
        name.setTypeface(regular);
        content.setTypeface(slabRegular);
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
                Notes.CONTENT_URI,
                Notes.ALL_COLUMNS_PROJECTION,
                Notes.NOT_DELETED_SELECTION,
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
