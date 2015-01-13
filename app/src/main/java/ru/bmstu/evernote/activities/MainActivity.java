package ru.bmstu.evernote.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.account.EvernoteAccount;
import ru.bmstu.evernote.account.LoginActivity;
import ru.bmstu.evernote.provider.EvernoteContentProvider;

public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, SyncStatusObserver {
    private static final String LOGTAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mListView;
    private ActionBarDrawerToggle mActionBarToggle;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    private String[] mDrawerItemsViewsNames;

    private Object mStatusChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerItemsViewsNames = getResources().getStringArray(R.array.drawer_items_views_names);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mListView = (ListView) findViewById(R.id.left_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_app_logo);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setSubtitle("List view");
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.subtitle_color));
        toolbar.inflateMenu(R.menu.add_items_toolbar);
        mActionBarToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mActionBarToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mActionBarToggle);

        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, android.R.id.text1, mDrawerItemsViewsNames));
        mListView.setOnItemClickListener(new DrawerItemClickListener());
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_frame);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mStatusChangeListener = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);
        displayView(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSwipeRefreshLayout.setOnRefreshListener(null);
        ContentResolver.removeStatusChangeListener(mStatusChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static void startMainActivity(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        ctx.startActivity(intent);
        ((Activity) ctx).finish();
    }

    @Override
    public void onStatusChanged(int i) {

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                AccountManager accountManager = AccountManager.get(MainActivity.this);
                Account[] accounts = accountManager.getAccountsByType(EvernoteAccount.TYPE);
                if (accounts.length == 0) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                } else {
                    Account account = accounts[0];
                    mSwipeRefreshLayout.setRefreshing(ContentResolver.isSyncActive(account, EvernoteContentProvider.AUTHORITY));
                }
            }
        });

        Log.d(LOGTAG, Thread.currentThread().getName());
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            displayView(i);
        }
    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new NotebooksFragment();
                break;
            case 1:
                fragment = new NotesFragment();
                break;
            case 2:
                fragment = new TransactionsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mListView.setItemChecked(position, true);
            mListView.setSelection(position);
            setTitle(mDrawerItemsViewsNames[position]);
            mDrawerLayout.closeDrawer(mListView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.create_notebook:
                new CreateNotebookDialog().show(getFragmentManager(), "Create notebook dialog");
                break;
            case R.id.create_note:
                new CreateNoteDialog().show(getFragmentManager(), "Create note dialog");
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRefresh() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(EvernoteAccount.TYPE);
        if (accounts.length == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        } else {
            Account account = accounts[0];
            ContentResolver.requestSync(account, EvernoteContentProvider.AUTHORITY, new Bundle());
        }
    }
}