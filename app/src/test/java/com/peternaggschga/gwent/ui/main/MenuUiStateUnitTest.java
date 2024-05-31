package com.peternaggschga.gwent.ui.main;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.valid4j.errors.RequireViolation;

@RunWith(JUnit4.class)
public class MenuUiStateUnitTest {
    @Test
    public void constructorAssertsNonNegativeDamage() {
        try {
            new MenuUiState(-1, false, false, false);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroDamage() {
        try {
            new MenuUiState(0, false, false, false);
        } catch (RequireViolation ignored) {
            fail();
        }
    }
}
