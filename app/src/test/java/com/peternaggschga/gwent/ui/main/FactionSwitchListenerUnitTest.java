package com.peternaggschga.gwent.ui.main;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.getListener;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.peternaggschga.gwent.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.reactivex.rxjava3.core.Completable;

@RunWith(MockitoJUnitRunner.class)
public class FactionSwitchListenerUnitTest {
    private Context mockContext;
    private ImageView mockBallView;
    private ImageView mockCardView;
    private TextView mockUnitView;
    private ImageButton mockFactionButton;
    private FactionSwitchListener testListener;
    private SharedPreferences mockPreferences;

    @Before
    public void initMocks() {
        Theme mockTheme = mock(Theme.class);
        when(mockTheme.obtainStyledAttributes(any())).thenReturn(mock(TypedArray.class));

        mockContext = mock(Context.class);
        when(mockContext.getTheme()).thenReturn(mockTheme);

        mockBallView = mock(ImageView.class);
        when(mockBallView.getContext()).thenReturn(mockContext);
        mockCardView = mock(ImageView.class);
        when(mockCardView.getContext()).thenReturn(mockContext);
        mockUnitView = mock(TextView.class);
        mockFactionButton = mock(ImageButton.class);
        when(mockFactionButton.getContext()).thenReturn(mockContext);

        ConstraintLayout mockRowLayout = mock(ConstraintLayout.class);
        when(mockRowLayout.findViewById(R.id.pointBall)).thenReturn(mockBallView);
        when(mockRowLayout.findViewById(R.id.cardsImage)).thenReturn(mockCardView);
        when(mockRowLayout.findViewById(R.id.cardCountView)).thenReturn(mockUnitView);

        Window mockWindow = mock(Window.class);
        when(mockWindow.findViewById(R.id.firstRow)).thenReturn(mockRowLayout);
        when(mockWindow.findViewById(R.id.secondRow)).thenReturn(mockRowLayout);
        when(mockWindow.findViewById(R.id.thirdRow)).thenReturn(mockRowLayout);

        when(mockWindow.findViewById(R.id.overallPointBall)).thenReturn(mockBallView);
        when(mockWindow.findViewById(R.id.factionButton)).thenReturn(mockFactionButton);

        testListener = getListener(mockWindow);

        mockPreferences = mock(SharedPreferences.class);
        when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(THEME_MONSTER);
    }

    @Test
    public void onSharedPreferenceChangeSetsTheme() {
        for (int i = THEME_MONSTER; i <= THEME_SCOIATAEL; i++) {
            when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(i);
            testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
            verify(mockContext, atLeast(i)).setTheme(anyInt());
        }
    }

    @Test
    public void onSharedPreferenceChangeUpdatesViews() {
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .then((Answer<Completable>) invocation -> {
                        ((ImageView) invocation.getArgument(0)).setImageResource(invocation.getArgument(1));
                        return Completable.complete();
                    });
            testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
            verify(mockBallView, atLeast(4)).setImageResource(anyInt());
            verify(mockCardView, atLeast(3)).setImageResource(anyInt());
            verify(mockUnitView, atLeast(3)).setTextColor(anyInt());
            verify(mockFactionButton, atLeastOnce()).setImageResource(anyInt());
        }
    }
}
