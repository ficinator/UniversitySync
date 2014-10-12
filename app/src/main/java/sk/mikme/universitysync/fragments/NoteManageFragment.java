package sk.mikme.universitysync.fragments;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Keyword;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.NoteKeyword;
import sk.mikme.universitysync.provider.Provider;
import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 8.10.2014.
 */
public class NoteManageFragment extends DialogFragment {
    public static final String TAG = "noteManageFragment";
    private static final String ARG_KW = "kw_";
    private static final String ARG_KW_COUNT = "kwCount";
    private static final String ARG_NOTE = "note";

    private Note mNote;
    private EditText mTitleEdit;
    private EditText mContentEdit;
    private LinearLayout mKeywordsLayout;
    private RelativeLayout mLastKeyword;

    public static NoteManageFragment newInstance(Note note) {
        NoteManageFragment fragment = new NoteManageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_NOTE, note);
        fragment.setArguments(args);
        return fragment;
    }

    public NoteManageFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mNote = getArguments().getParcelable(ARG_NOTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_note_manage, container, false);
        getDialog().setCanceledOnTouchOutside(true);

        mTitleEdit = (EditText) view.findViewById(R.id.title);
        mContentEdit = (EditText) view.findViewById(R.id.content);
        mKeywordsLayout = (LinearLayout) view.findViewById(R.id.keywords);
        mLastKeyword = (RelativeLayout) view.findViewById(R.id.last_keyword);
        ImageButton addKeywordsButton = (ImageButton) view.findViewById(R.id.add_keyword);
        Button submitButton = (Button) view.findViewById(R.id.submit);
        Button cancelButton = (Button) view.findViewById(R.id.cancel);

        addKeywordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText lastKeywordEdit = (EditText) mLastKeyword.getChildAt(0);
                inflateKeywordView(lastKeywordEdit.getText().toString());
                lastKeywordEdit.setText("");
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if (mNote != null) {
            getDialog().setTitle(getResources().getString(R.string.edit_string, mNote.getTitle()));
            mTitleEdit.setText(mNote.getTitle());
            mContentEdit.setText(mNote.getContent());
            if (savedInstanceState == null) {
                for (Keyword keyword : mNote.getKeywords())
                    inflateKeywordView(keyword.getName());
            }
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mNote.setTitle(mTitleEdit.getText().toString().trim());
                    mNote.setContent(mContentEdit.getText().toString().trim());
                    mNote.getKeywords().clear();
                    for (int i = 0; i < mKeywordsLayout.getChildCount(); i++) {
                        RelativeLayout keywordView = (RelativeLayout) mKeywordsLayout.getChildAt(i);
                        String kwString = ((EditText) keywordView.getChildAt(0)).getText().toString().trim();
                        if (!kwString.equals(""))
                            mNote.getKeywords().add(new Keyword(kwString));
                    }
                    try {
                        mNote.update(getActivity().getApplicationContext());
                        for (Keyword keyword : mNote.getKeywords())
                            keyword.setId(keyword.insert(getActivity().getApplicationContext()));
                        NoteKeyword.update(getActivity().getApplicationContext(), mNote);
                        dismiss();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.update_note_succ, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.update_note_err, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.update_note_err, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            getDialog().setTitle(R.string.new_note);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Keyword> keywords = new ArrayList<Keyword>();
                    for (int i = 0; i < mKeywordsLayout.getChildCount(); i++) {
                        RelativeLayout keywordView = (RelativeLayout) mKeywordsLayout.getChildAt(i);
                        String kwString = ((EditText) keywordView.getChildAt(0)).getText().toString().trim();
                        if (!kwString.equals(""))
                            keywords.add(new Keyword(kwString));
                    }
                    mNote = new Note(
                            SyncAdapter.getSession().getUserId(),
                            -1,
                            mTitleEdit.getText().toString().trim(),
                            mContentEdit.getText().toString().trim(),
                            keywords,
                            new ArrayList<String>());
                    try {
                        mNote.setId(mNote.insert(getActivity().getApplicationContext()));
                        for (Keyword keyword : mNote.getKeywords())
                            keyword.setId(keyword.insert(getActivity().getApplicationContext()));
                        NoteKeyword.update(getActivity().getApplicationContext(), mNote);
                        dismiss();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.add_note_succ, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.add_note_err, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getResources().getString(R.string.add_note_err, mNote.getTitle()),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return view;
    }

    private void inflateKeywordView(String keyword) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout newKeywordView = (RelativeLayout) inflater.inflate(R.layout.new_keyword, mKeywordsLayout, false);

        ((EditText) newKeywordView.getChildAt(0)).setText(keyword);
        ImageButton removeKeywordButton = (ImageButton) newKeywordView.findViewById(R.id.remove_keyword);
        removeKeywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKeywordsLayout.removeView((View) view.getParent());
            }
        });
        mKeywordsLayout.addView(newKeywordView, mKeywordsLayout.getChildCount() - 1);
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
        //inflater.inflate(R.menu.notes, menu);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(ARG_KW_COUNT, mKeywordsLayout.getChildCount());
        for (int i = 0; i < mKeywordsLayout.getChildCount(); i++) {
            EditText keywordEdit = (EditText) ((RelativeLayout) mKeywordsLayout.getChildAt(i)).getChildAt(0);
            savedInstanceState.putString(ARG_KW + i, keywordEdit.getText().toString());
        }
    }

    @Override
     public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            int kwCount = savedInstanceState.getInt(ARG_KW_COUNT);
            for (int i = 0; i < kwCount - 1; i++)
                inflateKeywordView(savedInstanceState.getString(ARG_KW + i));
            ((EditText) mLastKeyword.getChildAt(0))
                    .setText(savedInstanceState.getString(ARG_KW + (kwCount - 1)));
        }
    }
}
