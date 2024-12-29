package com.peternaggschga.gwent.ui.dialogs.addcard;

import static android.widget.NumberPicker.OnValueChangeListener;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.widget.NumberPicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class CardNumberPickerAdapterUnitTest {
    private static final int NUMBER_PICKER_VALUE = 5;
    NumberPicker mockPicker;
    OnValueChangeListener mockListener;

    @Before
    public void initMocks() {
        mockPicker = mock(NumberPicker.class);
        when(mockPicker.getValue()).thenReturn(NUMBER_PICKER_VALUE);
        mockListener = mock(OnValueChangeListener.class);
    }

    @Test
    public void getDelayedOnValueChangeListenerDelays500Milliseconds() throws InterruptedException {
        OnValueChangeListener delayed = CardNumberPickerAdapter.getDelayedOnValueChangeListener(mockListener);
        delayed.onValueChange(mockPicker, 0, NUMBER_PICKER_VALUE);
        verify(mockListener, never()).onValueChange(any(), anyInt(), anyInt());
        TimeUnit.MILLISECONDS.sleep(600);
        verify(mockListener).onValueChange(mockPicker, 0, NUMBER_PICKER_VALUE);
    }

    @Test
    public void getDelayedOnValueChangeListenerChecksValueChanged() throws InterruptedException {
        OnValueChangeListener delayed = CardNumberPickerAdapter.getDelayedOnValueChangeListener(mockListener);
        delayed.onValueChange(mockPicker, NUMBER_PICKER_VALUE, 0);
        TimeUnit.MILLISECONDS.sleep(600);
        verify(mockListener, never()).onValueChange(any(), anyInt(), anyInt());
    }
}
