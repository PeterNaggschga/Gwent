package com.peternaggschga.gwent.domain.damage;

import java.util.Map;
import java.util.Objects;

class BondDamageCalculatorDecorator extends DamageCalculatorDecorator {
    private final Map<Integer, Integer> idToSquad;

    BondDamageCalculatorDecorator(DamageCalculator component, Map<Integer, Integer> idToSquad) {
        super(component);
        this.idToSquad = idToSquad;
    }

    @Override
    public int calculateDamage(int id, int damage) {
        int componentDamage = component.calculateDamage(id, damage);
        return idToSquad.containsKey(id) ? componentDamage * Objects.requireNonNull(idToSquad.get(id)) : componentDamage;
    }
}
