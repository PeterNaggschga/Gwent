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
import org.mockito.stubbing.Answer;
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
        for (int id = 0; id < TESTING_DEPTH; id++) {
            assertThat(decorator.calculateDamage(id, 0)).isEqualTo(TESTING_DAMAGE);
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
        BondDamageCalculatorDecorator decorator = new BondDamageCalculatorDecorator(component, map);
        for (int squadSize = 1; squadSize <= TESTING_DEPTH; squadSize++) {
            when(map.getOrDefault(anyInt(), anyInt())).thenReturn(squadSize);
            assertThat(decorator.calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE * squadSize);
        }
    }

    @Test
    public void calculateDamageReturnsDefaultWhenNoSquad() {
        // noinspection unchecked cast
        Map<Integer, Integer> map = (Map<Integer, Integer>) Mockito.mock(Map.class);
        when(map.getOrDefault(anyInt(), anyInt())).then((Answer<Integer>) invocation -> invocation.getArgument(1));
        BondDamageCalculatorDecorator decorator = new BondDamageCalculatorDecorator(component, map);
        assertThat(decorator.calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE);
    }
}
