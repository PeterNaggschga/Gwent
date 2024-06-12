package com.peternaggschga.gwent.ui.introduction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * A {@link FragmentStateAdapter} that returns an {@link IntroductionFragment} corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {
    /**
     * Constructor of a SectionsPagerAdapter for the given {@link FragmentActivity}.
     *
     * @param fragmentActivity {@link FragmentActivity} that uses this adapter.
     */
    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * Essentially queries {@link IntroductionFragment#PAGES_COUNT}.
     * @return An {@link Integer} defining the number of pages.
     */
    @Override
    public int getItemCount() {
        return IntroductionFragment.PAGES_COUNT;
    }

    /**
     * Provides a new {@link IntroductionFragment} associated with the specified position.
     * @param position {@link Integer} defining the page that is queried.
     * @return A {@link Fragment} that will be shown at the specified position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return IntroductionFragment.newInstance(position);
    }
}
