package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MoralDamageCalculatorDecoratorUnitTest {
    private static final int TESTING_DEPTH = 500;
    private static final int TESTING_DAMAGE = 5;
    private final WeatherDamageCalculator component = new WeatherDamageCalculator(false);

    @Test
    public void constructorAssertsComponentType() {
        try {
            new MoralDamageCalculatorDecorator(component, Collections.emptyList());
            new MoralDamageCalculatorDecorator(new BondDamageCalculatorDecorator(component, Collections.emptyMap()), Collections.emptyList());
        } catch (Exception ignored) {
            fail();
        }
        try {
            new MoralDamageCalculatorDecorator(new HornDamageCalculatorDecorator(component, Collections.emptyList()), Collections.emptyList());
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            new MoralDamageCalculatorDecorator(new MoralDamageCalculatorDecorator(component, Collections.emptyList()), Collections.emptyList());
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageEmptyListReturnsInput() {
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, Collections.emptyList());
        for (int i = 0; i < TESTING_DEPTH; i++) {
            assertThat(decorator.calculateDamage(i, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE);
        }
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
        try {
            component.calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageAddsSizeOfListToDamage() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, list);
        for (int i = 1; i <= TESTING_DEPTH; i++) {
            when(list.size()).thenReturn(i);
            assertThat(decorator.calculateDamage(i, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE + i);
        }
    }

    @Test
    public void calculateDamageDoesNotAddUnitItself() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(TESTING_DEPTH);
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, TESTING_DAMAGE)).isEqualTo(TESTING_DAMAGE + TESTING_DEPTH - 1);
    }
}
