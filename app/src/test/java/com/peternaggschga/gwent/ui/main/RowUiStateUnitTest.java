package com.peternaggschga.gwent.ui.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.peternaggschga.gwent.data.RowBuffState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RowUiStateUnitTest {
    @Test
    public void constructorAssertsNonNegativeDamage() {
        try {
            new RowUiState(-1, false, RowBuffState.NONE, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroDamage() {
        try {
            new RowUiState(0, false, RowBuffState.NONE, 1);
        } catch (IllegalArgumentException ignored) {
            fail();
        }
    }

    @Test
    public void constructorAssertsNonNegativeUnits() {
        try {
            new RowUiState(1, false, RowBuffState.NONE, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroUnits() {
        try {
            new RowUiState(1, false, RowBuffState.NONE, 0);
        } catch (IllegalArgumentException ignored) {
            fail();
        }
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    public void constructorRejectsNullBuffState() {
        assertThrows(NullPointerException.class, () -> new RowUiState(0, false, null, 0));
    }

    @Test
    public void constructorAllowsMutuallyExclusiveEffects() {
        for (RowBuffState buffState : RowBuffState.values()) {
            RowUiState state = new RowUiState(0, false, buffState, 0);
            assertEquals(buffState == RowBuffState.HORN, state.isHorn());
            assertEquals(buffState == RowBuffState.MARDROEME, state.isMardroeme());
            assertFalse(state.isHorn() && state.isMardroeme());
        }
    }

    @Test
    public void mardroemeIsExposedAndParticipatesInEquality() {
        RowUiState active = new RowUiState(1, false, RowBuffState.MARDROEME, 1);
        assertTrue(active.isMardroeme());
        assertEquals(new RowUiState(1, false, RowBuffState.MARDROEME, 1), active);
        assertNotEquals(new RowUiState(1, false, RowBuffState.NONE, 1), active);
        assertNotEquals(new RowUiState(1, false, RowBuffState.HORN, 1), active);
    }
}
