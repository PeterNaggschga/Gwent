package com.peternaggschga.gwent.ui.main;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;

import com.peternaggschga.gwent.RowType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MenuUpdateObserverUnitTest {
    private Map<RowType, MutableLiveData<RowUiState>> rowObservers;
    private MutableLiveData<MenuUiState> menuObserver;

    @Before
    public void initRowObservers() {
        rowObservers = new HashMap<>(3);
        for (RowType row : RowType.values()) {
            //noinspection unchecked
            rowObservers.put(row, (MutableLiveData<RowUiState>) mock(MutableLiveData.class));
        }
        //noinspection unchecked
        menuObserver = (MutableLiveData<MenuUiState>) mock(MutableLiveData.class);
    }

    @Test
    public void getObserverAssertsRowObserverForEachRow() {
        rowObservers.remove(RowType.MELEE);
        try {
            //noinspection unchecked
            MenuUpdateObserver.getObserver(RowType.MELEE,
                    (MutableLiveData<MenuUiState>) mock(MutableLiveData.class),
                    rowObservers);
            fail();
        } catch (RequireViolation ignored) {
        }
        //noinspection unchecked
        rowObservers.put(null, (MutableLiveData<RowUiState>) mock(MutableLiveData.class));
        try {
            //noinspection unchecked
            MenuUpdateObserver.getObserver(RowType.MELEE,
                    (MutableLiveData<MenuUiState>) mock(MutableLiveData.class),
                    rowObservers);
            fail();
        } catch (RequireViolation ignored) {
        }
        rowObservers.remove(null);
        rowObservers.put(RowType.MELEE, null);
        try {
            //noinspection unchecked
            MenuUpdateObserver.getObserver(RowType.MELEE,
                    (MutableLiveData<MenuUiState>) mock(MutableLiveData.class),
                    rowObservers);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void getObserverFiltersRowObservers() {
        MenuUpdateObserver.getObserver(RowType.MELEE, menuObserver, rowObservers).onChanged(null);
        verify(rowObservers.get(RowType.RANGE), atLeastOnce()).getValue();
        verify(rowObservers.get(RowType.SIEGE), atLeastOnce()).getValue();


        initRowObservers();
        MenuUpdateObserver.getObserver(RowType.RANGE, menuObserver, rowObservers).onChanged(null);
        verify(rowObservers.get(RowType.MELEE), atLeastOnce()).getValue();
        verify(rowObservers.get(RowType.SIEGE), atLeastOnce()).getValue();

        initRowObservers();
        MenuUpdateObserver.getObserver(RowType.SIEGE, menuObserver, rowObservers).onChanged(null);
        verify(rowObservers.get(RowType.MELEE), atLeastOnce()).getValue();
        verify(rowObservers.get(RowType.RANGE), atLeastOnce()).getValue();
    }

    @Test
    public void onChangeAllowsNullValues() {
        for (MutableLiveData<RowUiState> rowObserver : rowObservers.values()) {
            when(rowObserver.getValue()).thenReturn(null);
        }
        MenuUpdateObserver.getObserver(RowType.MELEE, menuObserver, rowObservers).onChanged(null);
        verify(menuObserver, atLeastOnce()).postValue(new MenuUiState(0, false, false, false));
    }

    @Test
    public void onChangedOnlyPostsValueIfChanged() {
        MenuUpdateObserver observer = MenuUpdateObserver.getObserver(RowType.MELEE, menuObserver, rowObservers);
        when(menuObserver.getValue()).thenReturn(null);
        observer.onChanged(null);
        when(menuObserver.getValue()).thenReturn(new MenuUiState(0, false, false, false));
        observer.onChanged(null);
        verify(menuObserver, times(1)).postValue(any());
    }
}
