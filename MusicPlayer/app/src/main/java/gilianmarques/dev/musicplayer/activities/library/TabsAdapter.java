package gilianmarques.dev.musicplayer.activities.library;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class TabsAdapter extends FragmentPagerAdapter {

    private final List<Fragment> listFragments = new ArrayList<>();
    private final List<String> listFragmentsTitle = new ArrayList<>();

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    public void add(Fragment frag, String title) {
        this.listFragments.add(frag);
        this.listFragmentsTitle.add(title);

    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    @Override
    public int getCount() {
        return listFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return listFragmentsTitle.get(position);
    }


}