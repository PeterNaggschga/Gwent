package com.peternaggschga.gwent.ui.introduction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * @todo Documentation
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {
    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public int getItemCount() {
        return PlaceholderFragment.PAGES_COUNT;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PlaceholderFragment.newInstance(position);
    }
}
