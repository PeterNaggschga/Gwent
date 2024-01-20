package com.peternaggschga.gwent.domain.damage;

import java.util.List;

class MoralDamageCalculatorDecorator extends DamageCalculatorDecorator {
    private final List<Integer> unitIds;

    MoralDamageCalculatorDecorator(DamageCalculator component, List<Integer> unitIds) {
        super(component);
        this.unitIds = unitIds;
    }

    @Override
    public int calculateDamage(int id, int damage) {
        int componentDamage = component.calculateDamage(id, damage) + unitIds.size();
        return unitIds.contains(id) ? componentDamage - 1 : componentDamage;
    }
}
