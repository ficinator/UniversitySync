package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Keyword;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.NoteKeyword;

/**
 * Created by fic on 6.10.2014.
 */
public class NoteDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String ARG_NOTE = "note";
    public static final String TAG = "noteFragment";
    private static final int LOADER_KEYWORDS = 0;
    private static final int LOADER_REFERENCES = 1;

    private Note mNote;
    private TextView mKeywordsView;

    public static NoteDetailFragment newInstance(Note note) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    public NoteDetailFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //SyncAdapter.triggerRefresh(Note.TABLE_NAME);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

        if (getArguments() != null) {
            mNote = getArguments().getParcelable(ARG_NOTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);

        TextView idView = (TextView) view.findViewById(R.id.id);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView contentView = (TextView) view.findViewById(R.id.content);

        idView.setText(Integer.toString(mNote.getNoteId()));
        titleView.setText(mNote.getTitle());
        contentView.setText(Html.fromHtml(mNote.getContent()));

        mKeywordsView = (TextView) view.findViewById(R.id.keywords);
        getLoaderManager().initLoader(LOADER_KEYWORDS, null, this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //mOptionsMenu = menu;
        //inflater.inflate(R.menu.main, menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            // If the user clicks the "Refresh" button.
//            case R.id.menu_sync:
//                //SyncAdapter.triggerRefresh(Group.TABLE_NAME);
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    public void setNote(Note note) {
        this.mNote = note;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case LOADER_KEYWORDS:
                return new CursorLoader(getActivity(),
                        NoteKeyword.URI.buildUpon()
                                .appendPath(Note.PATH)
                                .appendPath(Integer.toString(mNote.getNoteId()))
                                .build(),
                        NoteKeyword.PROJECTION, null, null, Keyword.COLUMN_NAME_NAME + " asc");
            case LOADER_REFERENCES:
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case LOADER_KEYWORDS:
                mNote.getKeywords().clear();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext())
                    mNote.getKeywords().add(new NoteKeyword(cursor).getKeyword().getName());
                mKeywordsView.setText(mNote.getKeywordsString());
                break;
            case LOADER_REFERENCES:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_KEYWORDS:
                mKeywordsView.setText("");
                break;
            case LOADER_REFERENCES:
                break;
        }
    }
}
