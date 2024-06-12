package com.peternaggschga.gwent.ui.main;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RowUiStateUnitTest {
    @Test
    public void constructorAssertsNonNegativeDamage() {
        try {
            new RowUiState(-1, false, false, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroDamage() {
        try {
            new RowUiState(0, false, false, 1);
        } catch (IllegalArgumentException ignored) {
            fail();
        }
    }

    @Test
    public void constructorAssertsNonNegativeUnits() {
        try {
            new RowUiState(1, false, false, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroUnits() {
        try {
            new RowUiState(1, false, false, 0);
        } catch (IllegalArgumentException ignored) {
            fail();
        }
    }
}
