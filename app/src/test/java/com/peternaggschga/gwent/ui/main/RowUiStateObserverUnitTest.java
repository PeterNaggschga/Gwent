package com.peternaggschga.gwent.ui.main;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.widget.ImageView;
import android.widget.TextView;

import com.peternaggschga.gwent.data.RowType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RowUiStateObserverUnitTest {
    private static final RowUiState TEST_STATE = new RowUiState(0, false, false, 0);
    private RowUiStateObserver testObserver;
    private final TextView damageView = mock(TextView.class);
    private final TextView unitView = mock(TextView.class);

    @Before
    public void initMocks() {
        ImageView weatherView = mock(ImageView.class);
        ImageView hornView = mock(ImageView.class);
        testObserver = RowUiStateObserver.getObserver(RowType.MELEE, damageView, weatherView, hornView, unitView);
    }

    @Test
    public void onChangedQueriesStateFields() {
        RowUiState mockState = mock(RowUiState.class);
        testObserver.onChanged(mockState);
        verify(mockState, atLeastOnce()).getDamage();
        verify(mockState, atLeastOnce()).isWeather();
        verify(mockState, atLeastOnce()).isHorn();
        verify(mockState, atLeastOnce()).getUnits();
    }

    @Test
    public void onChangedUpdatesTextViews() {
        testObserver.onChanged(TEST_STATE);
        verify(damageView, atLeastOnce()).setText(String.valueOf(TEST_STATE.getDamage()));
        verify(unitView, atLeastOnce()).setText(String.valueOf(TEST_STATE.getUnits()));
    }
}
