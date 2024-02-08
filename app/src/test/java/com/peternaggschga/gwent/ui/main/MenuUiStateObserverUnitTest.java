package com.peternaggschga.gwent.ui.main;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.widget.ImageButton;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MenuUiStateObserverUnitTest {
    private static final MenuUiState TEST_STATE = new MenuUiState(0, false, false, false);
    private final TextView damageView = mock(TextView.class);
    private MenuUiStateObserver testObserver;

    @Before
    public void initMocks() {
        ImageButton resetButton = mock(ImageButton.class);
        ImageButton weatherButton = mock(ImageButton.class);
        ImageButton burnButton = mock(ImageButton.class);
        testObserver = new MenuUiStateObserver(damageView, resetButton, weatherButton, burnButton);
    }

    @Test
    public void onChangedQueriesStateFields() {
        MenuUiState mockState = mock(MenuUiState.class);
        testObserver.onChanged(mockState);
        verify(mockState, atLeastOnce()).getDamage();
        verify(mockState, atLeastOnce()).isReset();
        verify(mockState, atLeastOnce()).isWeather();
        verify(mockState, atLeastOnce()).isBurn();
    }

    @Test
    public void onChangedUpdatesTextViews() {
        testObserver.onChanged(TEST_STATE);
        verify(damageView, atLeastOnce()).setText(String.valueOf(TEST_STATE.getDamage()));
    }
}
