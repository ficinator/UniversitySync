package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Keyword;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.NoteKeyword;

/**
 * Created by fic on 6.10.2014.
 */
public class NoteDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final String ARG_NOTE_ID = "note_id";
    public static final String TAG = "noteDetailFragment";
    private static final int LOADER_NOTE = 0;
    private static final int LOADER_KEYWORDS = 1;
    private static final int LOADER_REFERENCES = 2;

    private int mNoteId;
    private Note mNote;
    private TextView mTitleView;
    private TextView mContentView;
    private TextView mKeywordsView;

    public static NoteDetailFragment newInstance(int noteId) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NOTE_ID, noteId);
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

        if (getArguments() != null) {
            mNoteId = getArguments().getInt(ARG_NOTE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mContentView = (TextView) view.findViewById(R.id.content);
        mKeywordsView = (TextView) view.findViewById(R.id.keywords);

        getLoaderManager().initLoader(LOADER_NOTE, null, this);

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

    public void showNoteManageFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG) == null) {
            NoteManageFragment fragment = NoteManageFragment.newInstance(mNote);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.addToBackStack(TAG);
            fragment.show(transaction, TAG);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case LOADER_NOTE:
                return new CursorLoader(getActivity(),
                        Note.URI.buildUpon().appendPath(Integer.toString(mNoteId)).build(),
                        Note.PROJECTION, null, null, null);
            case LOADER_KEYWORDS:
                return new CursorLoader(getActivity(),
                        NoteKeyword.URI.buildUpon()
                                .appendPath(Note.PATH)
                                .appendPath(Integer.toString(mNote.getId()))
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
            case LOADER_NOTE:
                if (cursor.moveToFirst()) {
                    mNote = new Note(cursor);
                    mTitleView.setText(mNote.getTitle());
                    mContentView.setText(Html.fromHtml(mNote.getContent()));
                    getLoaderManager().initLoader(LOADER_KEYWORDS, null, this);
                }
                break;
            case LOADER_KEYWORDS:
                mNote.getKeywords().clear();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext())
                    mNote.getKeywords().add(new NoteKeyword(cursor).getKeyword());
                mKeywordsView.setText(mNote.getKeywordsString());
                break;
            case LOADER_REFERENCES:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        switch (cursorLoader.getId()) {
            case LOADER_NOTE:
                mTitleView.setText("");
                mContentView.setText("");
                break;
            case LOADER_KEYWORDS:
                mKeywordsView.setText("");
                break;
            case LOADER_REFERENCES:
                break;
        }
    }

    public void deleteNote() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getString(R.string.delete_string, mNote.getTitle()))
                .setMessage(R.string.delete_note_sure)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity activity = getActivity();
                        try {
                            mNote.delete(activity.getApplicationContext());
                            ((NotesFragment) getParentFragment()).removeDetailFragment();
                            Toast.makeText(activity.getApplicationContext(),
                                    activity.getResources().getString(R.string.delete_note_succ, mNote.getTitle()),
                                    Toast.LENGTH_SHORT).show();
                        } catch (RemoteException e) {
                            Toast.makeText(activity.getApplicationContext(),
                                    activity.getResources().getString(R.string.delete_note_err, mNote.getTitle()),
                                    Toast.LENGTH_SHORT).show();
                        } catch (OperationApplicationException e) {
                            Toast.makeText(activity.getApplicationContext(),
                                    activity.getResources().getString(R.string.delete_note_err, mNote.getTitle()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
