package ru.bmstu.evernote.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ru.bmstu.evernote.R;

/**
 * Created by Sun on 27.12.2014.
 */
public class ItemDetailActivity extends ActionBarActivity {

    private long notesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Title");
        // toolbar.setSubtitle("Subtitle");

        // Show the Up button in the action bar.
        //.getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            notesId = getIntent().getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0);
            Bundle arguments = new Bundle();
            arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, notesId);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_edit:
                DialogFragment dialogFragment = new EditNoteDialog();
                Bundle bundle = new Bundle();
                bundle.putLong(ItemDetailFragment.ARG_ITEM_ID, notesId);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getFragmentManager(), "Remove notebook dialog");
                break;
            case R.id.action_discard:
                new RemoveNoteDialog().show(getFragmentManager(), "Remove note dialog");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
