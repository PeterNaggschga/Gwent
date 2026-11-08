package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

/**
 * A builder class responsible for creating a decorator structure of DamageCalculator classes with the given parameters.
 * Should only be used by DamageCalculatorBuildDirector.
 * Calls to .setWeather(), .setBond(),
 * .setMoral() or .setHorn() should be in that exact order to create a correct DamageCalculator.
 * Every function call is optional, i.e. every function should be called once or not at all.
 *
 * @see DamageCalculatorBuildDirector
 * @see DamageCalculator
 * @see DamageCalculatorDecorator
 */
class DamageCalculatorBuilder {
    /**
     * The DamageCalculator instance that is being built by this DamageCalculatorBuilder.
     * Starts as a WeatherDamageCalculator; the selected weather calculator forms the base of the chain.
     */
    @NonNull
    private DamageCalculator calculator = new WeatherDamageCalculator(false);

    /**
     * Selects the weather calculator before adding other modifiers.
     * @param weather Whether weather is active.
     * @param kingBran Whether King Bran replaces the ordinary weather penalty.
     */
    void setWeather(boolean weather, boolean kingBran) {
        calculator = kingBran ? new KingBranDamageCalculator(weather) : new WeatherDamageCalculator(weather);
    }

    /**
     * Sets the tight bond buff for the calculation. Should be called after .setWeather() or never.
     *
     * @param idToSquad Map mapping the ids of all units with the com.peternaggschga.gwent.data.Ability.BINDING ability to the respective squad size.
     */
    void setBond(@NonNull Map<Integer, Integer> idToSquad) {
        calculator = new BondDamageCalculatorDecorator(calculator, idToSquad);
    }

    /**
     * Sets the moral boost buff for the calculation. Should be called after .setBond() or never.
     *
     * @param unitIds List of Integers
     *                representing ids of units with the com.peternaggschga.gwent.data.Ability.MORAL_BOOST ability.
     */
    void setMoral(@NonNull List<Integer> unitIds) {
        calculator = new MoralDamageCalculatorDecorator(calculator, unitIds);
    }

    /**
     * Sets the commander's horn buff for the calculation.
     * Should be called after .setMoral() or never.
     *
     * @param unitIds List of Integers
     *                representing ids of units with the com.peternaggschga.gwent.data.Ability.HORN ability.
     */
    void setHorn(@NonNull List<Integer> unitIds) {
        calculator = new HornDamageCalculatorDecorator(calculator, unitIds);
    }

    /**
     * Returns the current state of the built DamageCalculator, i.e. .calculator.
     *
     * @return A DamageCalculator that has been built by this builder.
     */
    @NonNull
    DamageCalculator getResult() {
        return calculator;
    }
}
