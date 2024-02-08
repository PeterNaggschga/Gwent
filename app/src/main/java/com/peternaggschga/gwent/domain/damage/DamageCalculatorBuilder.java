package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

/**
 * A builder class responsible for creating a decorator structure of DamageCalculator classes with the given parameters.
 * Should only be used by DamageCalculatorBuildDirector.
 * Calls to #setWeather(), #setBond(),
 * #setMoral() or #setHorn() should be in that exact order to create a correct DamageCalculator.
 * Every function call is optional, i.e. every function should be called once or not at all.
 *
 * @see DamageCalculatorBuildDirector
 * @see DamageCalculator
 * @see DamageCalculatorDecorator
 */
class DamageCalculatorBuilder {
    /**
     * The DamageCalculator instance that is being built by this DamageCalculatorBuilder.
     * Starts as a WeatherDamageCalculator, since this is always at the end of the decorator chain.
     */
    @NonNull
    private DamageCalculator calculator = new WeatherDamageCalculator(false);

    /**
     * Sets the weather debuff for the calculation. Should be called first or never.
     *
     * @param weather Boolean defining whether the weather debuff is active.
     */
    void setWeather(boolean weather) {
        calculator = new WeatherDamageCalculator(weather);
    }

    /**
     * Sets the tight bond buff for the calculation. Should be called after #setWeather() or never.
     *
     * @param idToSquad Map mapping the ids of all units with the com.peternaggschga.gwent.Ability#BINDING ability to the respective squad size.
     */
    void setBond(@NonNull Map<Integer, Integer> idToSquad) {
        calculator = new BondDamageCalculatorDecorator(calculator, idToSquad);
    }

    /**
     * Sets the moral boost buff for the calculation. Should be called after #setBond() or never.
     *
     * @param unitIds List of Integers
     *                representing ids of units with the com.peternaggschga.gwent.Ability#MORAL_BOOST ability.
     */
    void setMoral(@NonNull List<Integer> unitIds) {
        calculator = new MoralDamageCalculatorDecorator(calculator, unitIds);
    }

    /**
     * Sets the commander's horn buff for the calculation.
     * Should be called after #setMoral() or never.
     *
     * @param unitIds List of Integers
     *                representing ids of units with the com.peternaggschga.gwent.Ability#HORN ability.
     */
    void setHorn(@NonNull List<Integer> unitIds) {
        calculator = new HornDamageCalculatorDecorator(calculator, unitIds);
    }

    /**
     * Returns the current state of the built DamageCalculator, i.e. #calculator.
     *
     * @return A DamageCalculator that has been built by this builder.
     */
    @NonNull
    DamageCalculator getResult() {
        return calculator;
    }
}
