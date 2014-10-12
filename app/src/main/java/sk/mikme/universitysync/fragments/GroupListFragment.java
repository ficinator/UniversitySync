package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Group;

/**
 * Created by fic on 19.9.2014.
 */
public class GroupListFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            Group.COLUMN_NAME_NAME,
            Group.COLUMN_NAME_UNIVERSITY
    };
    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            R.id.name,
            R.id.university};

    private SimpleCursorAdapter mAdapter;
    private Menu mOptionsMenu;

    // TODO: Rename and change types of parameters
    public static GroupListFragment newInstance(String param1, String param2) {
        GroupListFragment fragment = new GroupListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //SyncAdapter.triggerRefresh(Note.TABLE_NAME);
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
                R.layout.item_group,
                null,
                FROM_COLUMNS,
                TO_FIELDS,
                0);
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
                Group.URI,
                Group.PROJECTION,
                null,
                null,
                Group.COLUMN_NAME_NAME + " asc");
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
        inflater.inflate(R.menu.notes, menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.sync:
                //SyncAdapter.triggerRefresh(Group.TABLE_NAME);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
