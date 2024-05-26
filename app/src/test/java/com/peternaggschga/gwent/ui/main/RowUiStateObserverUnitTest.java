package com.peternaggschga.gwent.ui.main;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.widget.ImageView;
import android.widget.TextView;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.reactivex.rxjava3.core.Completable;

@RunWith(MockitoJUnitRunner.class)
public class RowUiStateObserverUnitTest {
    private static final RowUiState NEGATIVE_TEST_STATE = new RowUiState(0, false, false, 0);
    private static final RowUiState POSITIVE_TEST_STATE = new RowUiState(1, true, true, 1);
    private RowUiStateObserver testObserver;
    private TextView damageView;
    private ImageView weatherView;
    private ImageView hornView;
    private TextView unitView;

    @Before
    public void initMocks() {
        damageView = mock(TextView.class);
        weatherView = mock(ImageView.class);
        hornView = mock(ImageView.class);
        unitView = mock(TextView.class);
        testObserver = RowUiStateObserver.getObserver(RowType.MELEE, damageView, weatherView, hornView, unitView);
    }

    @Test
    public void onChangedQueriesStateFields() {
        RowUiState mockState = mock(RowUiState.class);
        try (MockedStatic<ImageViewSwitchAnimator> switchAnimator = mockStatic(ImageViewSwitchAnimator.class)) {
            switchAnimator.when(() -> ImageViewSwitchAnimator.animatedSwitch(any(), anyInt()))
                    .then((Answer<Completable>) invocation -> {
                        ((ImageView) invocation.getArgument(0)).setImageResource(invocation.getArgument(1));
                        return Completable.complete();
                    });
            testObserver.accept(mockState);
            verify(mockState, atLeastOnce()).getDamage();
            verify(mockState, atLeastOnce()).isWeather();
            verify(mockState, atLeastOnce()).isHorn();
            verify(mockState, atLeastOnce()).getUnits();
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
            for (RowUiState state : new RowUiState[]{NEGATIVE_TEST_STATE, POSITIVE_TEST_STATE}) {
                testObserver.accept(state);
                verify(damageView, atLeastOnce()).setText(String.valueOf(state.getDamage()));
                verify(weatherView, atLeastOnce()).setImageResource(state.isWeather() ? R.drawable.frost_weather : R.drawable.good_weather);
                verify(hornView, atLeastOnce()).setImageResource(state.isHorn() ? R.drawable.horn : R.drawable.horn_grey);
                verify(unitView, atLeastOnce()).setText(String.valueOf(state.getUnits()));
            }
        }
    }
}
