package sk.mikme.universitysync.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.fragments.NoteListFragment;

/**
 * Created by fic on 1.10.2014.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    private FragmentManager mFragmentManager;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        String tag = makeFragmentName(R.id.home_pager, position);
        Fragment fragment = mFragmentManager.findFragmentByTag(tag);
        if (fragment != null)
            return fragment;
        switch (position) {
            case 0:
                return new NoteListFragment();
            default:
                return new FakeFragment();

        }
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
