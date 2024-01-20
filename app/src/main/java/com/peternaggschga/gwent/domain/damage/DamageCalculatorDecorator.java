package com.peternaggschga.gwent.domain.damage;

abstract class DamageCalculatorDecorator implements DamageCalculator {
    protected DamageCalculator component;

    DamageCalculatorDecorator(DamageCalculator component) {
        this.component = component;
    }
}
