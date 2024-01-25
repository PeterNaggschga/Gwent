package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

class DamageCalculatorBuilder {
    private DamageCalculator calculator;

    void setWeather(boolean weather) {
        calculator = new WeatherDamageCalculator(weather);
    }

    void setBond(@NonNull Map<Integer, Integer> idToSquad) {
        calculator = new BondDamageCalculatorDecorator((WeatherDamageCalculator) calculator, idToSquad);
    }

    void setMoral(@NonNull List<Integer> unitIds) {
        calculator = new MoralDamageCalculatorDecorator(calculator, unitIds);
    }

    void setHorn(@NonNull List<Integer> unitIds) {
        calculator = new HornDamageCalculatorDecorator(calculator, unitIds);
    }

    public DamageCalculator getResult() {
        return calculator;
    }
}
