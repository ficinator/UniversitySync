package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.adapters.HomePagerAdapter;
import sk.mikme.universitysync.provider.User;
import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 20.9.2014.
 */
public class HomeFragment extends Fragment {

    //private static final String[] TAB_TITLES = { "Notes", "Articles", "Files" };

    //private FragmentListener mListener;

    private ViewPager mHomePager;
    private HomePagerAdapter mHomePagerAdapter;
    private User mUser;

    public static HomeFragment newInstance(User user) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        //args.putParcelable(User.TABLE_NAME, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setUser(User user) {
        mUser = user;
        NoteListFragment fragment = (NoteListFragment) mHomePagerAdapter.getItem(0);
        fragment.setSelectionArgs(user);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (FragmentListener) activity;
            //mListener.onShowTabs(TAB_TITLES, getTabListener());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mUser = getArguments().getParcelable(User.TABLE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mHomePager = (ViewPager) view.findViewById(R.id.home_pager);
        mHomePagerAdapter = new HomePagerAdapter(getChildFragmentManager());
        mHomePager.setAdapter(mHomePagerAdapter);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener.onHideTabs();
        //mListener = null;
    }
}
