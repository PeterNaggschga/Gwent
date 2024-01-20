package com.peternaggschga.gwent.domain.damage;

class WeatherDamageCalculator implements DamageCalculator {
    private final boolean weather;

    WeatherDamageCalculator(boolean weather) {
        this.weather = weather;
    }

    @Override
    public int calculateDamage(int id, int damage) {
        return weather ? 1 : damage;
    }
}
