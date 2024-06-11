package com.peternaggschga.gwent.ui.introduction;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.peternaggschga.gwent.R;

/**
 * A {@link Fragment} containing an {@link ImageView} and a {@link TextView} conveying introductory information.
 */
public class IntroductionFragment extends Fragment {
    /**
     * {@link Integer} constant defining the number of possible pages represented by this fragment.
     * Is equivalent to the length of {@link #stringIds} and {@link #imageIds}.
     */
    public static final int PAGES_COUNT = 5;
    /**
     * {@link String} constant defining the argument key used to communicate the requested page to the fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    /**
     * {@link Integer} array constant containing the resource ids for all introductory images.
     */
    private static final int[] imageIds = {R.drawable.introduction_1, R.drawable.introduction_2,
            R.drawable.introduction_3, R.drawable.introduction_4, R.drawable.introduction_5};
    /**
     * {@link Integer} array constant containing the resource ids for all introductory texts.
     */
    private static final int[] stringIds = {R.string.introduction_page1, R.string.introduction_page2,
            R.string.introduction_page3, R.string.introduction_page4, R.string.introduction_page5};

    /**
     * Creates a new {@link IntroductionFragment} showing the page defined by the given index.
     * Factory method of {@link IntroductionFragment}.
     * @param index {@link Integer} defining the page that the created fragment shows.
     * @return An {@link IntroductionFragment} showing the page with the given index.
     * @throws IllegalArgumentException When the given index is not in [0, {@link #PAGES_COUNT} - 1].
     */
    @NonNull
    public static IntroductionFragment newInstance(@IntRange(from = 0, to = PAGES_COUNT - 1) int index) {
        if (index < 0 || index >= PAGES_COUNT) {
            throw new IllegalArgumentException("Index must be in [0, PAGES_COUNT - 1] but is " + index + ".");
        }
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_SECTION_NUMBER, index);
        IntroductionFragment fragment = new IntroductionFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Inflates the {@link R.layout#fragment_introduction} layout and sets text and image.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return A {@link View} that is the root of the newly inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int page = getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;

        View root = inflater.inflate(R.layout.fragment_introduction, container, false);

        TextView textView = root.findViewById(R.id.introduction_textView);
        textView.setText(Html.fromHtml(getString(stringIds[page]), Html.FROM_HTML_MODE_LEGACY));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        ((ImageView) root.findViewById(R.id.introduction_imageView)).setImageResource(imageIds[page]);
        return root;
    }
}
