package com.peternaggschga.gwent.domain.damage;

import java.util.Map;
import java.util.Objects;

class BondDamageCalculatorDecorator extends DamageCalculatorDecorator {
    private final Map<Integer, Integer> idToSquad;

    BondDamageCalculatorDecorator(DamageCalculator component, Map<Integer, Integer> idToSquadSize) {
        super(component);
        this.idToSquad = idToSquadSize;
    }

    @Override
    public int calculateDamage(int id, int damage) {
        int componentDamage = component.calculateDamage(id, damage);
        return idToSquad.containsKey(id) ? componentDamage * Objects.requireNonNull(idToSquad.get(id)) : componentDamage;
    }
}
