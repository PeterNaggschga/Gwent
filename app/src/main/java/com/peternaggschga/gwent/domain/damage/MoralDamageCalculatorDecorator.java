package com.peternaggschga.gwent.domain.damage;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * A DamageCalculator class responsible for calculating the moral buff if necessary,
 * i.e., if there are units with the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability
 * that are not the unit the damage is calculated for.
 * Acts as a ConcreteDecorator in the implemented decorator pattern and should decorate a
 * WeatherDamageCalculator or a BondDamageCalculatorDecorator for correct damage calculation.
 */
class MoralDamageCalculatorDecorator extends DamageCalculatorDecorator {
    /**
     * A List of Integers
     * containing the ids of all units with the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability.
     */
    private final List<Integer> unitIds;

    /**
     * Constructor of a MoralDamageCalculatorDecorator.
     * Should only be called by DamageCalculatorBuilder.
     * The given damage calculator should be of type WeatherDamageCalculator or BondDamageCalculatorDecorator for correct damage calculation.
     * The given List must not contain ``null``.
     *
     * @param component DamageCalculator that is being decorated by this decorator.
     * @param unitIds   List of Integers
     *                  representing ids of units with the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability.
     * @throws IllegalArgumentException When unitIds contains null values.
     * @see DamageCalculatorBuilder
     */
    MoralDamageCalculatorDecorator(@NonNull DamageCalculator component, @NonNull List<Integer> unitIds) {
        super(component);
        if (unitIds.contains(null)) {
            throw new IllegalArgumentException("List<Integer> unitIds must not contain null values.");
        }
        this.unitIds = unitIds;
    }

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Returns the given damage plus the number of moral boosts.
     * If the unit itself has the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability,
     * it is boosted one time less.
     *
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
        int componentDamage = component.calculateDamage(id, damage) + unitIds.size();
        return unitIds.contains(id) ? componentDamage - 1 : componentDamage;
    }

    /**
     * Calculates whether the unit with the given id is shown as Color#BUFFED,
     * Color#DEBUFFED, or Color#DEFAULT.
     * Units are shown as Color#BUFFED when they are affected by a moral boost buff,
     * otherwise their Color is defined by #component.
     *
     * @param id Integer representing the UnitEntity#id of the unit buff status is calculated.
     * @return Color representing whether the unit is buffed, de-buffed or not affected.
     * @see Color
     */
    @Override
    public Color isBuffed(int id) {
        return (!unitIds.isEmpty() && (!unitIds.contains(id) || (unitIds.size() > 1)))
                ? BUFFED
                : component.isBuffed(id);
    }
}
