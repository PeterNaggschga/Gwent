package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.IntRange;

import java.util.Collection;

/**
 * An interface for a class capable of calculating the
 * (de-)buffed damage of a unit for a given UnitEntity#id and UnitEntity#damage.
 * Is used to implement the visitor design pattern,
 * see com.peternaggschga.gwent.data.UnitEntity#calculateDamage()
 * The respective visitors are created as a decorator hierarchy,
 * see DamageCalculatorBuildDirector#getCalculator().
 * @see com.peternaggschga.gwent.data.UnitEntity#calculateDamage(DamageCalculator)
 * @see DamageCalculatorBuildDirector#getCalculator(boolean, boolean, Collection)
 */
public interface DamageCalculator {
    /**
     * Calculates whether the unit with the given id is shown as Color#BUFFED,
     * Color#DEBUFFED, or Color#DEFAULT.
     * Units are shown as Color#DEFAULT
     * when they are not affected by any damage changing buffs or de-buffs.
     * Units are shown as Color#DEBUFFED when they are only affected by the weather de-buff.
     * Units are shown as Color#BUFFED when they are affected by any damage-increasing buff.
     *
     * @param id Integer representing the UnitEntity#id of the unit buff status is calculated.
     * @return Color representing whether the unit is buffed, de-buffed or not affected.
     * @see Color
     */
    Color isBuffed(int id);

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Calculation is defined by the underlying decorator structure
     * implemented via DamageCalculatorDecorator and WeatherDamageCalculator.
     *
     * @param id     Integer representing the UnitEntity#id of the unit whose (de-)buff damage is calculated.
     * @param damage Integer representing the base-damage of the unit whose (de-)buff damage is calculated.
     * @return Integer representing the (de-)buffed damage of the unit.
     * @see DamageCalculatorDecorator
     * @see WeatherDamageCalculator
     */
    int calculateDamage(int id, @IntRange(from = 0) int damage);

    /**
     * An enum containing values representing whether a unit is buffed,
     * de-buffed or not affected by status effects.
     */
    enum Color {
        /**
         * The unit is not affected by status effects.
         */
        DEFAULT,
        /**
         * The unit is buffed by status effects.
         */
        BUFFED,
        /**
         * The unit is de-buffed by weather effects.
         */
        DEBUFFED
    }
}
