package com.peternaggschga.gwent.domain.damage;

import static org.valid4j.Assertive.require;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * A DamageCalculator class responsible for calculating the moral buff if necessary,
 * i.e., if there are units with the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability
 * that are not the unit the damage is calculated for.
 * Acts as a ConcreteDecorator in the implemented decorator pattern and must decorate a
 * WeatherDamageCalculator or a BondDamageCalculatorDecorator.
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
     * The given damage calculator must be of type WeatherDamageCalculator or BondDamageCalculatorDecorator.
     *
     * @param component DamageCalculator that is being decorated by this decorator.
     * @param unitIds   List of Integers
     *                  representing ids of units with the com.peternaggschga.gwent.data.Ability#MORAL_BOOST ability.
     * @see DamageCalculatorBuilder
     */
    MoralDamageCalculatorDecorator(@NonNull DamageCalculator component, @NonNull List<Integer> unitIds) {
        super(component);
        require(component.getClass() == WeatherDamageCalculator.class || component.getClass() == BondDamageCalculatorDecorator.class);
        require(!unitIds.contains(null));
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
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        require(damage >= 0);
        int componentDamage = component.calculateDamage(id, damage) + unitIds.size();
        return unitIds.contains(id) ? componentDamage - 1 : componentDamage;
    }
}
