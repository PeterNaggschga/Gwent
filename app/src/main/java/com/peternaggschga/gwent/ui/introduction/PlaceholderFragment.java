package com.peternaggschga.gwent.ui.introduction;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.peternaggschga.gwent.R;

/**
 * A placeholder fragment containing a simple view.
 * @todo Documentation
 */
public class PlaceholderFragment extends Fragment {

    public static final int PAGES_COUNT = 5;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int[] stringIds = {R.string.introduction_page1, R.string.introduction_page2, R.string.introduction_page3, R.string.introduction_page4, R.string.introduction_page5};
    private static final int[] imageIds = {R.drawable.introduction_1, R.drawable.introduction_2, R.drawable.introduction_3, R.drawable.introduction_4, R.drawable.introduction_5};

    @NonNull
    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

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
