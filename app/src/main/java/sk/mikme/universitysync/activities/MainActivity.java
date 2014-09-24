package sk.mikme.universitysync.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.adapters.DrawerListAdapter;
import sk.mikme.universitysync.drawer.DrawerItem;
import sk.mikme.universitysync.drawer.DrawerUserItem;
import sk.mikme.universitysync.provider.User;
import sk.mikme.universitysync.sync.AccountService;
import sk.mikme.universitysync.sync.Session;
import sk.mikme.universitysync.sync.SyncAdapter;


public class MainActivity extends ActionBarActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQEST_LOGIN = 0;
    private static final int LOADER_USER = 0;
    private static final int LOADER_GROUPS = 1;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private List<DrawerItem> mDrawerItems;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLeftDrawer();

        Session session = Session.load(getApplicationContext());
        Account account = AccountService.findAccountBySession(getApplicationContext(), session);
        if (account != null) {
            SyncAdapter.setSession(session);
            SyncAdapter.setAccount(account);
            showCurrentUser();
        }
        else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, REQEST_LOGIN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showCurrentUser() {
        SyncAdapter.syncCurrentUser();
        getSupportLoaderManager().initLoader(LOADER_USER, null, this);
    }

    private void showUserGroups() {
        SyncAdapter.syncUserGroups();
        getSupportLoaderManager().initLoader(LOADER_GROUPS, null, this);
    }

    private void initLeftDrawer() {
        //mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.email, R.string.password) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setTitle("Oh Shit");
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setTitle(R.string.app_name);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerItems = new ArrayList<DrawerItem>();
        //mSideMenuListView.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setAdapter(new DrawerListAdapter(getApplicationContext(), mDrawerItems));
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.menu_sync).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                    String authToken = data.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                    if (accountName == null || accountType == null || authToken == null) {
                        //TODO: wrong credentials
                    }
                    else {
                        getSupportLoaderManager().initLoader(LOADER_USER, null, this);
                    }
                }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case LOADER_USER:
                return new CursorLoader(this,
                        User.URI.buildUpon()
                                .appendPath(Integer.toString(SyncAdapter.getSession().getUserId()))
                                .build(),
                        User.PROJECTION,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_USER:
                while (cursor.moveToNext()) {
                    mDrawerItems.add(new DrawerUserItem(new User(cursor)));
                }
        }
        ((DrawerListAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        getSupportLoaderManager().destroyLoader(LOADER_USER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mDrawerItems.clear();
        ((DrawerListAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
    }
}
