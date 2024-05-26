package com.peternaggschga.gwent.ui.main;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.peternaggschga.gwent.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.reactivex.rxjava3.core.Completable;

@RunWith(MockitoJUnitRunner.class)
public class MenuUiStateObserverUnitTest {
    private static final MenuUiState NEGATIVE_TEST_STATE = new MenuUiState(0, false, false, false);
    private static final MenuUiState POSITIVE_TEST_STATE = new MenuUiState(1, true, true, true);
    private final TextView damageView = mock(TextView.class);
    private ImageButton resetButton;
    private ImageButton weatherButton;
    private ImageButton burnButton;
    private MenuUiStateObserver testObserver;

    @Before
    public void initMocks() {
        resetButton = mock(ImageButton.class);
        weatherButton = mock(ImageButton.class);
        burnButton = mock(ImageButton.class);
        testObserver = new MenuUiStateObserver(damageView, resetButton, weatherButton, burnButton);
    }

    @Test
    public void onChangedQueriesStateFields() {
        MenuUiState mockState = mock(MenuUiState.class);
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .then((Answer<Completable>) invocation -> {
                        ((ImageView) invocation.getArgument(0)).setImageResource(invocation.getArgument(1));
                        return Completable.complete();
                    });
            testObserver.accept(mockState);
            verify(mockState, atLeastOnce()).getDamage();
            verify(mockState, atLeastOnce()).isReset();
            verify(mockState, atLeastOnce()).isWeather();
            verify(mockState, atLeastOnce()).isBurn();
        }
    }

    @Test
    public void onChangedUpdatesViews() {
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .then((Answer<Completable>) invocation -> {
                        ((ImageView) invocation.getArgument(0)).setImageResource(invocation.getArgument(1));
                        return Completable.complete();
                    });
            for (MenuUiState state : new MenuUiState[]{NEGATIVE_TEST_STATE, POSITIVE_TEST_STATE}) {
                testObserver.accept(state);
                verify(damageView, atLeastOnce()).setText(String.valueOf(state.getDamage()));
                verify(resetButton, atLeastOnce()).setImageResource(state.isReset() ? R.drawable.icon_reset : R.drawable.icon_reset_grey);
                verify(resetButton, atLeastOnce()).setClickable(state.isReset());
                verify(weatherButton, atLeastOnce()).setImageResource(state.isWeather() ? R.drawable.icon_weather : R.drawable.icon_weather_grey);
                verify(weatherButton, atLeastOnce()).setClickable(state.isWeather());
                verify(burnButton, atLeastOnce()).setImageResource(state.isBurn() ? R.drawable.icon_burn : R.drawable.icon_burn_grey);
                verify(burnButton, atLeastOnce()).setClickable(state.isBurn());
            }
        }
    }
}
