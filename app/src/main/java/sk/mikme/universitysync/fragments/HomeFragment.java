package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.TabPageIndicator;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.adapters.HomePagerAdapter;
import sk.mikme.universitysync.provider.User;


/**
 * Created by fic on 20.9.2014.
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "homeFragment";

    //private FragmentListener mListener;

    private ViewPager mHomePager;
    private HomePagerAdapter mHomePagerAdapter;
    private TabPageIndicator mTabIndicator;
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
        NotesFragment notesFragment = (NotesFragment) mHomePagerAdapter.getItem(0);
        notesFragment.setSelectionArgs(mUser);
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
//        if (getArguments() != null) {
//            mUser = getArguments().getParcelable(User.TABLE_NAME);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mHomePager = (ViewPager) view.findViewById(R.id.home_pager);
        mHomePagerAdapter = new HomePagerAdapter(getChildFragmentManager(), mUser);
        mHomePager.setAdapter(mHomePagerAdapter);
        mTabIndicator = (TabPageIndicator) view.findViewById(R.id.titles);
        mTabIndicator.setViewPager(mHomePager);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener.onHideTabs();
        //mListener = null;
    }

    public boolean onBackPressed() {
        boolean popped = false;
        Fragment fragment = mHomePagerAdapter.getItem(mHomePager.getCurrentItem());
        if (fragment instanceof NotesFragment)
            popped = ((NotesFragment) fragment).onBackPressed();
        return popped;
    }
}
