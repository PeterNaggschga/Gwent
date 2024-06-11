package com.peternaggschga.gwent.domain.damage;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEBUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEFAULT;

import androidx.annotation.IntRange;

/**
 * A DamageCalculator class responsible for calculating the weather debuff if necessary,
 * i.e., if #weather is true.
 * Acts as the ConcreteComponent in the implemented decorator pattern
 * and is therefore always at the end of the decorator chain.
 */
class WeatherDamageCalculator implements DamageCalculator {
    /**
     * Defines whether the weather debuff should be applied when calling #calculateDamage().
     */
    private final boolean weather;

    /**
     * Constructor of a WeatherDamageCalculator.
     * Should only be called by DamageCalculatorBuilder.
     *
     * @param weather Boolean defining whether the weather debuff should be applied.
     * @see DamageCalculatorBuilder
     */
    WeatherDamageCalculator(boolean weather) {
        this.weather = weather;
    }

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Returns the given damage if #weather is false or 1.
     *
     * @param id     Integer representing the UnitEntity#id of the unit whose (de-)buff damage is calculated.
     * @param damage Integer representing the base-damage of the unit whose (de-)buff damage is calculated.
     * @return Integer representing the (de-)buffed damage of the unit.
     * @throws org.valid4j.errors.RequireViolation When damage is negative.
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        // TODO: assert damage >= 0);
        if (damage == 0) {
            return damage;
        }
        return weather ? 1 : damage;
    }

    /**
     * Calculates whether the unit with the given id is shown as Color#BUFFED,
     * Color#DEBUFFED, or Color#DEFAULT.
     * Returns Color#DEBUFFED if #weather is true or else Color#DEFAULT.
     *
     * @param id Integer representing the UnitEntity#id of the unit buff status is calculated.
     * @return Color representing whether the unit is buffed, de-buffed or not affected.
     * @see Color
     */
    @Override
    public Color isBuffed(int id) {
        return weather ? DEBUFFED : DEFAULT;
    }
}
