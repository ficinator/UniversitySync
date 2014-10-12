package sk.mikme.universitysync.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.User;
import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 6.10.2014.
 */
public class NotesFragment extends Fragment {

    private boolean mIsTwoPane;
    private Parcelable mSelectionArgs;
    private boolean mHasDetail;
    private View mDetailView;

    public NotesFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        mIsTwoPane = view instanceof LinearLayout;
        mDetailView = view.findViewById(R.id.detail);

        NoteListFragment listFragment = getListFragment();
        if (listFragment == null)
            setListFragment(NoteListFragment.newInstance(mSelectionArgs));

        NoteDetailFragment fragment = getDetailFragment();
        if (fragment != null)
            setDetailFragment(fragment);
        else
            mHasDetail = false;

        if (mIsTwoPane && !mHasDetail)
            mDetailView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes, menu);
    }

    public void onPrepareOptionsMenu (Menu menu) {
        if (!mHasDetail) {
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.add:
                getListFragment().showNoteManageFragment();
                return true;
            case R.id.edit:
                getDetailFragment().showNoteManageFragment();
                return true;
            case R.id.delete:
                getDetailFragment().deleteNote();
                return true;
            case R.id.sync:
                SyncAdapter.syncCurrentUserNotes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public NoteListFragment getListFragment() {
        return (NoteListFragment) getChildFragmentManager().findFragmentByTag(NoteListFragment.TAG);
    }

    public void setListFragment(NoteListFragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.list, fragment, NoteListFragment.TAG).commit();
    }

    public NoteDetailFragment getDetailFragment() {
        return (NoteDetailFragment) getChildFragmentManager().findFragmentByTag(NoteDetailFragment.TAG);
    }

    public void setDetailFragment(NoteDetailFragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        fm.popBackStackImmediate(NoteDetailFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().remove(fragment).commit();
        fm.executePendingTransactions();

        FragmentTransaction transaction = fm.beginTransaction();
        mHasDetail = true;
        if (mIsTwoPane) {
            mDetailView.setVisibility(View.VISIBLE);
            transaction.replace(R.id.detail, fragment, NoteDetailFragment.TAG);
        } else {
            transaction.addToBackStack(NoteDetailFragment.TAG);
            transaction.replace(R.id.list, fragment, NoteDetailFragment.TAG);
        }
        transaction.commit();
        getActivity().supportInvalidateOptionsMenu();
    }

    public void removeDetailFragment() {
        FragmentManager fm = getChildFragmentManager();
        fm.popBackStackImmediate(NoteDetailFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //fm.beginTransaction().remove(getDetailFragment()).commit();

        mHasDetail = false;
        if (mIsTwoPane)
            mDetailView.setVisibility(View.GONE);
        getActivity().supportInvalidateOptionsMenu();
    }

    public void setSelectionArgs(Parcelable args) {
        mSelectionArgs = args;
    }

    public boolean onBackPressed() {
        boolean popped = getChildFragmentManager().popBackStackImmediate();
        if (popped) {
            mHasDetail = false;
            getActivity().supportInvalidateOptionsMenu();
        }
        return popped;
    }
}
