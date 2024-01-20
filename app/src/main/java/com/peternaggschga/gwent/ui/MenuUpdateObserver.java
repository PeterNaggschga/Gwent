package com.peternaggschga.gwent.ui;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MenuUpdateObserver implements Observer<RowUiState> {
    private final MediatorLiveData<MenuUiState> menuObserver;
    private final List<MutableLiveData<RowUiState>> rowStates = new ArrayList<>();

    public MenuUpdateObserver(MediatorLiveData<MenuUiState> menuObserver, MutableLiveData<RowUiState> rowState1, MutableLiveData<RowUiState> rowState2) {
        this.menuObserver = menuObserver;
        rowStates.add(rowState1);
        rowStates.add(rowState2);
    }

    @Override
    public void onChanged(RowUiState rowUiState) {
        List<RowUiState> states = Arrays.asList(rowUiState, rowStates.get(0).getValue(), rowStates.get(1).getValue());
        int damage = 0;
        boolean reset = false;
        boolean weather = false;
        boolean burn = false;
        for (RowUiState state : states.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            damage += state.getDamage();
            reset |= state.isHorn();
            weather |= state.isWeather();
            burn |= state.getUnits() != 0;
        }
        reset |= weather || burn;
        MenuUiState newState = new MenuUiState(damage, reset, weather, burn);
        if (!newState.equals(menuObserver.getValue())) {
            menuObserver.postValue(newState);
        }
    }
}
