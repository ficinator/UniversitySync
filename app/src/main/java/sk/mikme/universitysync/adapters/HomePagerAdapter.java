package sk.mikme.universitysync.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.fragments.NotesFragment;
import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 1.10.2014.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = { "Notes", "Folders", "Files" };

    private FragmentManager mFragmentManager;
    private User mUser;

    public HomePagerAdapter(FragmentManager fm, User user) {
        super(fm);
        mFragmentManager = fm;
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
            default:
                return new FakeFragment();

        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position].toUpperCase();
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_fake, container, false);
        }
    }
}
