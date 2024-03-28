package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

/**
 * An abstract DamageCalculator
 * that is used as a superclass for the decorator classes implementing the damage calculation.
 */
abstract class DamageCalculatorDecorator implements DamageCalculator {
    /**
     * A DamageCalculator which is decorated by this decorator.
     */
    protected DamageCalculator component;

    /**
     * Constructor of a DamageCalculatorDecorator.
     *
     * @param component DamageCalculator that is being decorated by this decorator.
     */
    DamageCalculatorDecorator(@NonNull DamageCalculator component) {
        this.component = component;
    }
}
