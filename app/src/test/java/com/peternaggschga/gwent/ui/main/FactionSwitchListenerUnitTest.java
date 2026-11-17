package com.peternaggschga.gwent.ui.main;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SKELLIGE;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.getListener;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

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
    private ImageView mockMardroemeView;
    private TextView mockUnitView;
    private ImageButton mockFactionButton;
    private FactionSwitchListener testListener;
    private SharedPreferences mockPreferences;

    @Before
    public void initMocks() {
        Theme mockTheme = mock(Theme.class);
        lenient().when(mockTheme.obtainStyledAttributes(any())).thenReturn(mock(TypedArray.class));

        mockContext = mock(Context.class);
        lenient().when(mockContext.getTheme()).thenReturn(mockTheme);

        mockBallView = mock(ImageView.class);
        mockCardView = mock(ImageView.class);
        mockMardroemeView = mock(ImageView.class);
        mockUnitView = mock(TextView.class);
        mockFactionButton = mock(ImageButton.class);
        when(mockFactionButton.getContext()).thenReturn(mockContext);

        ConstraintLayout mockRowLayout = mock(ConstraintLayout.class);
        when(mockRowLayout.findViewById(R.id.pointBall)).thenReturn(mockBallView);
        when(mockRowLayout.findViewById(R.id.cardsImage)).thenReturn(mockCardView);
        when(mockRowLayout.findViewById(R.id.cardCountView)).thenReturn(mockUnitView);
        when(mockRowLayout.findViewById(R.id.mardroemeView)).thenReturn(mockMardroemeView);

        Window mockWindow = mock(Window.class);
        when(mockWindow.findViewById(R.id.firstRow)).thenReturn(mockRowLayout);
        when(mockWindow.findViewById(R.id.secondRow)).thenReturn(mockRowLayout);
        when(mockWindow.findViewById(R.id.thirdRow)).thenReturn(mockRowLayout);

        when(mockWindow.findViewById(R.id.overallPointBall)).thenReturn(mockBallView);
        when(mockWindow.findViewById(R.id.factionButton)).thenReturn(mockFactionButton);

        mockPreferences = mock(SharedPreferences.class);
        when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(THEME_SCOIATAEL);
        try (MockedStatic<PreferenceManager> preferences = mockStatic(PreferenceManager.class)) {
            preferences.when(() -> PreferenceManager.getDefaultSharedPreferences(mockContext))
                    .thenReturn(mockPreferences);
            testListener = getListener(mockWindow);
        }
        when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(THEME_MONSTER);
    }

    @Test
    public void onSharedPreferenceChangeSetsTheme() {
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .then((Answer<Completable>) invocation -> {
                        ((ImageView) invocation.getArgument(0)).setImageResource(invocation.getArgument(1));
                        return Completable.complete();
                    });
            for (int i : new int[]{0, 1, 2, 3, THEME_SKELLIGE, THEME_MONSTER}) {
                clearInvocations(mockMardroemeView);
                when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(i);
                testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
                verify(mockContext, atLeast(i)).setTheme(anyInt());
                verify(mockMardroemeView, times(3)).setVisibility(i == THEME_SKELLIGE ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Test
    public void clearingPreferencesRestoresDefaultFaction() {
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .thenReturn(Completable.complete());
            when(mockPreferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)).thenReturn(THEME_SKELLIGE);
            testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
            clearInvocations(mockContext, mockPreferences, mockMardroemeView);

            // A missing preference returns the supplied default after Editor.clear().
            when(mockPreferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL))
                    .thenAnswer(invocation -> invocation.getArgument(1));
            testListener.onSharedPreferenceChanged(mockPreferences, null);

            verify(mockPreferences).getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL);
            verify(mockContext).setTheme(R.style.ScoiataelTheme);
            verify(mockMardroemeView, times(3)).setVisibility(View.GONE);

            clearInvocations(mockContext, mockMardroemeView);
            testListener.onSharedPreferenceChanged(mockPreferences, null);
            verify(mockContext, never()).setTheme(anyInt());
            verify(mockMardroemeView, never()).setVisibility(anyInt());
        }
    }

    @Test
    public void selectingCurrentFactionDoesNothing() {
        when(mockPreferences.getInt(eq(THEME_PREFERENCE_KEY), anyInt())).thenReturn(THEME_SCOIATAEL);
        testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
        verify(mockContext, never()).setTheme(anyInt());
        verify(mockMardroemeView, never()).setVisibility(anyInt());
        verify(mockBallView, never()).setImageResource(anyInt());
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
            clearInvocations(mockContext, mockMardroemeView, mockBallView, mockFactionButton);
            testListener.onSharedPreferenceChanged(mockPreferences, THEME_PREFERENCE_KEY);
            verify(mockContext, never()).setTheme(anyInt());
            verify(mockMardroemeView, never()).setVisibility(anyInt());
            verify(mockBallView, never()).setImageResource(anyInt());
            verify(mockFactionButton, never()).setImageResource(anyInt());
        }
    }
}
