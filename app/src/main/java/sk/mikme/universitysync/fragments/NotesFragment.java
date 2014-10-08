package sk.mikme.universitysync.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 6.10.2014.
 */
public class NotesFragment extends Fragment {
    private static final String TAG_LIST = "list";
    private static final String TAG_DETAIL = "detail";

    private boolean mIsTwoPane;
    private Parcelable mSelectionArgs;

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

        FragmentManager fm = getChildFragmentManager();

        Fragment listFragment = fm.findFragmentByTag(TAG_LIST);
        if (listFragment == null)
            setListFragment(NoteListFragment.newInstance(mSelectionArgs));

        Fragment fragment = fm.findFragmentByTag(TAG_DETAIL);
        if (fragment != null)
            setDetailFragment(fragment);

        return view;
    }

    public void setListFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.list, fragment, TAG_LIST).commit();
    }

    public void setDetailFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        fm.popBackStackImmediate(TAG_DETAIL, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().remove(fragment).commit();
        fm.executePendingTransactions();

        FragmentTransaction transaction = fm.beginTransaction();
        if (mIsTwoPane) {
            transaction.replace(R.id.detail, fragment, TAG_DETAIL);
        }
        else {
            transaction.addToBackStack(TAG_DETAIL);
            transaction.replace(R.id.list, fragment, TAG_DETAIL);
        }
        transaction.commit();
    }

    public void setSelectionArgs(Parcelable args) {
        mSelectionArgs = args;
    }

    public boolean onBackPressed() {
        return getChildFragmentManager().popBackStackImmediate();
    }
}
