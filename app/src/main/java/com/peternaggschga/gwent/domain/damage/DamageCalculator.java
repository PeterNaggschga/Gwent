package com.peternaggschga.gwent.domain.damage;

import java.util.Collection;

/**
 * An interface for a class capable of calculating the
 * (de-)buffed damage of a unit for a given UnitEntity#id and UnitEntity#damage.
 * Is used to implement the visitor design pattern,
 * see com.peternaggschga.gwent.data.UnitEntity#calculateDamage()
 * The respective visitors are created as a decorator hierarchy,
 * see DamageCalculatorBuildDirector#getCalculator().
 *
 * @see com.peternaggschga.gwent.data.UnitEntity#calculateDamage(DamageCalculator)
 * @see DamageCalculatorBuildDirector#getCalculator(boolean, boolean, Collection)
 */
public interface DamageCalculator {
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
    int calculateDamage(int id, int damage);
}
