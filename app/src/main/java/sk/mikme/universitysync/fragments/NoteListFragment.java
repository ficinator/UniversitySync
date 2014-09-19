package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
import sk.mikme.universitysync.provider.Note;
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

    private SimpleCursorAdapter mAdapter;
    private Menu mOptionsMenu;

    // TODO: Rename and change types of parameters
    public static NoteListFragment newInstance(String param1, String param2) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        SyncAdapter.createSyncAccount(activity);
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
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                Note.URI,
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
                SyncAdapter.triggerRefresh(Note.TABLE_NAME);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
