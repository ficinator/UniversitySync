package sk.mikme.universitysync.fragments;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.viewpagerindicator.TabPageIndicator;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.activities.MainActivity;
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
        ((MainActivity) getActivity()).setContentTitle(mUser.getFullName());
        if (mHomePagerAdapter != null)
            ((NotesFragment) mHomePagerAdapter.getItem(0)).setSelectionArgs(mUser);
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
        final String[] tabTitles = getResources().getStringArray(R.array.tab_titles);

        mHomePager = (ViewPager) view.findViewById(R.id.home_pager);
        mHomePagerAdapter = new HomePagerAdapter(getChildFragmentManager(), tabTitles, mUser);
        mHomePager.setAdapter(mHomePagerAdapter);
        if (mUser != null) {
            ((MainActivity) getActivity()).setContentTitle(mUser.getFullName());
            ((NotesFragment) mHomePagerAdapter.getItem(0)).setSelectionArgs(mUser);
        }

        mTabIndicator = (TabPageIndicator) view.findViewById(R.id.titles);
        if (mTabIndicator == null) {
            ((ActionBarActivity) getActivity()).getSupportActionBar()
                    .setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ((ActionBarActivity) getActivity()).getSupportActionBar().setListNavigationCallbacks(
                    ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                            R.array.tab_titles, R.layout.item_spinner_dropdown),
                    new ActionBar.OnNavigationListener() {
                        private String[] mTabTitles = tabTitles;
                        @Override
                        public boolean onNavigationItemSelected(int position, long itemId) {
                            mHomePager.setCurrentItem(position);
                            return true;
                        }
                    });
            mHomePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) { }
                @Override
                public void onPageSelected(int position) {
                    ((ActionBarActivity) getActivity()).getSupportActionBar()
                            .setSelectedNavigationItem(position);
                }
                @Override
                public void onPageScrollStateChanged(int i) { }
            });
        }
        else {
            mTabIndicator.setViewPager(mHomePager);
        }

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
