package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MoralDamageCalculatorDecoratorUnitTest {
    private static final int TESTING_DEPTH = 500;
    private static final int TESTING_DAMAGE = 5;
    private final WeatherDamageCalculator component = Mockito.mock(WeatherDamageCalculator.class);

    @Before
    public void initMock() {
        when(component.calculateDamage(anyInt(), anyInt())).thenReturn(TESTING_DAMAGE);
    }

    @Test
    public void constructorAssertsNoNullInList() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        try {
            new MoralDamageCalculatorDecorator(component, list);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageEmptyListReturnsInput() {
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, Collections.emptyList());
        for (int id = 0; id < TESTING_DEPTH; id++) {
            assertThat(decorator.calculateDamage(id, 0)).isEqualTo(TESTING_DAMAGE);
        }
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
        try {
            new MoralDamageCalculatorDecorator(component, Collections.emptyList()).calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void calculateDamageAddsSizeOfListToDamage() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, list);
        for (int moralUnitNumber = 1; moralUnitNumber <= TESTING_DEPTH; moralUnitNumber++) {
            when(list.size()).thenReturn(moralUnitNumber);
            assertThat(decorator.calculateDamage(moralUnitNumber, 0)).isEqualTo(TESTING_DAMAGE + moralUnitNumber);
        }
    }

    @Test
    public void calculateDamageDoesNotAddUnitItself() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(TESTING_DEPTH);
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(TESTING_DAMAGE + TESTING_DEPTH - 1);
    }

    @Test
    public void isBuffedEmptyListCallsComponent() {
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, Collections.emptyList());
        for (int id = 0; id < TESTING_DEPTH; id++) {
            decorator.isBuffed(id);
            verify(component).isBuffed(id);
        }
    }

    @Test
    public void isBuffedReturnsBuffedWhenUnitIsBuffed() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(TESTING_DEPTH);
        MoralDamageCalculatorDecorator decorator = new MoralDamageCalculatorDecorator(component, list);
        assertThat(decorator.isBuffed(0)).isEqualTo(BUFFED);
    }
}
