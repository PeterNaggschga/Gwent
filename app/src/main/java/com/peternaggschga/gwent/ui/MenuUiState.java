package com.peternaggschga.gwent.ui;

import androidx.annotation.IntRange;

public class MenuUiState {
    @IntRange(from = 0)
    private final int damage;

    private final boolean reset;

    private final boolean weather;

    private final boolean burn;

    public MenuUiState(int damage, boolean reset, boolean weather, boolean burn) {
        this.damage = damage;
        this.reset = reset;
        this.weather = weather;
        this.burn = burn;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isReset() {
        return reset;
    }

    public boolean isWeather() {
        return weather;
    }

    public boolean isBurn() {
        return burn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuUiState)) return false;
        MenuUiState that = (MenuUiState) o;
        return damage == that.damage && reset == that.reset && weather == that.weather && burn == that.burn;
    }
}
