package sk.mikme.universitysync.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.fragments.NoteKeywordListFragment;
import sk.mikme.universitysync.fragments.NotesFragment;
import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 1.10.2014.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    private FragmentManager mFragmentManager;
    private String[] mTabTitles;
    private User mUser;

    public HomePagerAdapter(FragmentManager fm, String[] tabTitles, User user) {
        super(fm);
        mFragmentManager = fm;
        mTabTitles = tabTitles;
        mUser = user;
    }

    @Override
    public Fragment getItem(int position) {
        String tag = makeFragmentName(R.id.home_pager, position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment != null)
            return fragment;
        switch (position) {
            case 0:
                return new NotesFragment();
            case 1:
                return new NoteKeywordListFragment();
            default:
                return new FakeFragment();

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

    @Override
    public int getCount() {
        return 3;
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    public static class FakeFragment extends Fragment {

        @Override
        public void onCreate(Bundle savedStateInstance) {
            super.onCreate(savedStateInstance);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_fake, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fake, menu);
        }
    }
}
