package sk.mikme.universitysync.fragments;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Group;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.Provider;
import sk.mikme.universitysync.provider.User;
import sk.mikme.universitysync.sync.SyncAdapter;

public class NoteListFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            Note.COLUMN_NAME_NOTE_ID,
            Note.COLUMN_NAME_DATE
    };
    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            R.id.title,
            R.id.date};
    private static final String SELECTION = "selection";

    private SimpleCursorAdapter mAdapter;
    private Menu mOptionsMenu;
    private Parcelable mSelectionArgs;
    private Object mSyncObserverHandle;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteListFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mSelectionArgs != null)
            getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.item_note,
                null,
                FROM_COLUMNS,
                TO_FIELDS,
                0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                switch (i) {
                    case Note.COLUMN_USER_ID:
                        ((TextView) view).setText(Integer.toString(cursor.getInt(i)));
                        return true;
                    case Note.COLUMN_DATE:
                        // Convert timestamp to human-readable date
                        Time t = new Time();
                        t.set(cursor.getLong(i));
                        ((TextView) view).setText(t.format("%Y-%m-%d %H:%M"));
                        return true;
                    default:
                        return false;
                }
            }
        });
        setListAdapter(mAdapter);
        //setEmptyText();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mSyncStatusObserver.onStatusChanged(0);
//
//        // Watch for sync state changes
//        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
//                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
//        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mSyncObserverHandle != null) {
//            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
//            mSyncObserverHandle = null;
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri uri = Note.URI;
        if (mSelectionArgs instanceof User) {
            uri = uri.buildUpon()
                    .appendPath(User.PATH)
                    .appendPath(Integer.toString(((User) mSelectionArgs).getUserId()))
                    .build();
        }
        else if (mSelectionArgs instanceof Group) {
            //TODO:
        }
        return new CursorLoader(getActivity(),
                uri,
                Note.PROJECTION,
                null,
                null,
                Note.COLUMN_NAME_DATE + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.main, menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_sync:
                SyncAdapter.syncCurrentUserNotes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSelectionArgs(Parcelable selectionArgs) {
        mSelectionArgs = selectionArgs;

        if (isAdded())
            getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Set the state of the Sync button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param syncing True if an active sync is occuring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setSyncActionButtonState(boolean syncing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem syncItem = mOptionsMenu.findItem(R.id.menu_sync);
        if (syncItem != null) {
            if (syncing) {
                syncItem.setActionView(R.layout.actionbar_sync_progress);
            } else {
                syncItem.setActionView(null);
            }
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = SyncAdapter.getAccount();
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setSyncActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(account, Provider.AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(account, Provider.AUTHORITY);
                    setSyncActionButtonState(syncActive || syncPending);
                }
            });
        }
    };
}
