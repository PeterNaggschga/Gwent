package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class BondDamageCalculatorDecoratorUnitTest {
    private static final int TESTING_DEPTH = 500;
    private static final int TESTING_DAMAGE = 5;
    private final WeatherDamageCalculator component = Mockito.mock(WeatherDamageCalculator.class);

    @Before
    public void initMock() {
        when(component.calculateDamage(anyInt(), anyInt())).thenReturn(TESTING_DAMAGE);
    }

    @Test
    public void constructorAssertsNoNullValueInMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, null);
        try {
            new BondDamageCalculatorDecorator(component, map);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void constructorAssertsPositiveValueInMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 0);
        try {
            new BondDamageCalculatorDecorator(component, map);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageEmptyMapReturnsInput() {
        BondDamageCalculatorDecorator decorator = new BondDamageCalculatorDecorator(component, Collections.emptyMap());
        for (int i = 0; i < TESTING_DEPTH; i++) {
            assertThat(decorator.calculateDamage(i, 0)).isEqualTo(TESTING_DAMAGE);
        }
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
        try {
            new BondDamageCalculatorDecorator(component, Collections.emptyMap()).calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageMultipliesSizeOfSquadToDamage() {
        // noinspection unchecked cast
        Map<Integer, Integer> map = (Map<Integer, Integer>) Mockito.mock(Map.class);
        when(map.containsKey(anyInt())).thenReturn(true);
        BondDamageCalculatorDecorator decorator = new BondDamageCalculatorDecorator(component, map);
        for (int i = 1; i <= TESTING_DEPTH; i++) {
            when(map.get(anyInt())).thenReturn(i);
            assertThat(decorator.calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * i);
        }
    }

    @Test
    public void calculateDamageReturnsDefaultWhenNoSquad() {
        // noinspection unchecked cast
        Map<Integer, Integer> map = (Map<Integer, Integer>) Mockito.mock(Map.class);
        when(map.containsKey(anyInt())).thenReturn(false);
        BondDamageCalculatorDecorator decorator = new BondDamageCalculatorDecorator(component, map);
        assertThat(decorator.calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE);
    }
}