package com.peternaggschga.gwent.domain.damage;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * A DamageCalculator class responsible for calculating the horn buff if necessary,
 * i.e., if there are units with the com.peternaggschga.gwent.data.Ability#HORN ability
 * that are not the unit the damage is calculated for
 * or if there is a commander's horn in this row.
 * Acts as a ConcreteDecorator in the implemented decorator pattern and should decorate a
 * WeatherDamageCalculator, a BondDamageCalculatorDecorator or a MoralDamageCalculatorDecorator.
 */
class HornDamageCalculatorDecorator extends DamageCalculatorDecorator {
    /**
     * A List of Integers
     * containing the ids of all units with the com.peternaggschga.gwent.data.Ability#HORN ability.
     * If a commander's horn is in the respective row, #unitIds contains ``null``.
     */
    private final List<Integer> unitIds;

    /**
     * Constructor of a HornDamageCalculatorDecorator.
     * Should only be called by DamageCalculatorBuilder.
     * The given damage calculator should be of type WeatherDamageCalculator,
     * BondDamageCalculatorDecorator or MoralDamageCalculatorDecorator for correct damage calculation.
     * If the respective row has a commander's horn, ``null`` must be an element of the given List.
     *
     * @param component DamageCalculator that is being decorated by this decorator.
     * @param unitIds   List of Integers
     *                  representing ids of units with the com.peternaggschga.gwent.data.Ability#HORN ability.
     * @see DamageCalculatorBuilder
     */
    HornDamageCalculatorDecorator(@NonNull DamageCalculator component, @NonNull List<Integer> unitIds) {
        super(component);
        this.unitIds = unitIds;
    }

    /**
     * Decides whether the unit with the given id is affected by the horn buff.
     *
     * @param id Integer representing the UnitEntity#id of the unit that is being evaluated.
     * @return A Boolean value defining whether the horn buff affects the given unit.
     */
    private boolean doubleDamage(int id) {
        return unitIds.contains(null) || !(unitIds.isEmpty() || unitIds.contains(id)) || unitIds.size() > 1;
    }

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Returns given damage times two if the unit is buffed by a commander's horn.
     * @see #doubleDamage(int)
     * @param id     Integer representing the UnitEntity#id of the unit whose (de-)buff damage is calculated.
     * @param damage Integer representing the base-damage of the unit whose (de-)buff damage is calculated.
     * @return Integer representing the (de-)buffed damage of the unit.
     * @throws IllegalArgumentException When damage is negative.
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be greater or equal to 0 but is " + damage + ".");
        }
        return (doubleDamage(id) ? 2 : 1) * component.calculateDamage(id, damage);
    }

    /**
     * Calculates whether the unit with the given id is shown as Color#BUFFED,
     * Color#DEBUFFED, or Color#DEFAULT.
     * Units are shown as Color#BUFFED when they are affected by a horn buff,
     * otherwise their Color is defined by #component.
     *
     * @param id Integer representing the UnitEntity#id of the unit buff status is calculated.
     * @return Color representing whether the unit is buffed, de-buffed or not affected.
     * @see Color
     * @see #doubleDamage(int)
     */
    @Override
    public Color isBuffed(int id) {
        return doubleDamage(id) ? BUFFED : component.isBuffed(id);
    }
}
