package com.peternaggschga.gwent.domain.damage;

import java.util.List;

class HornDamageCalculatorDecorator extends DamageCalculatorDecorator {
    private final List<Integer> unitIds;

    HornDamageCalculatorDecorator(DamageCalculator component, List<Integer> unitIds) {
        super(component);
        this.unitIds = unitIds;
    }

    @Override
    public int calculateDamage(int id, int damage) {
        int componentDamage = component.calculateDamage(id, damage);
        boolean doubleDamage = !unitIds.contains(id) || unitIds.contains(null) || unitIds.size() > 1;
        return doubleDamage ? componentDamage * 2 : componentDamage;
    }
}
