package com.peternaggschga.gwent.ui.introduction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * A FragmentStateAdapter that returns an IntroductionFragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {
    /**
     * Constructor of a SectionsPagerAdapter for the given FragmentActivity.
     *
     * @param fragmentActivity FragmentActivity that uses this adapter.
     */
    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * Essentially queries IntroductionFragmentPAGES_COUNT.
     * @return An Integer defining the number of pages.
     */
    @Override
    public int getItemCount() {
        return IntroductionFragment.PAGES_COUNT;
    }

    /**
     * Provides a new IntroductionFragment associated with the specified position.
     * @param position Integer defining the page that is queried.
     * @return A Fragment that will be shown at the specified position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return IntroductionFragment.newInstance(position);
    }
}
