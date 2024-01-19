package com.peternaggschga.gwent.ui;

import androidx.annotation.IntRange;

public class RowUiState {
    @IntRange(from = 0)
    private final int damage;

    private final boolean weather;

    private final boolean horn;

    @IntRange(from = 0)
    private final int units;

    public RowUiState(int damage, boolean weather, boolean horn, int units) {
        this.damage = damage;
        this.weather = weather;
        this.horn = horn;
        this.units = units;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isWeather() {
        return weather;
    }

    public boolean isHorn() {
        return horn;
    }

    public int getUnits() {
        return units;
    }
}
