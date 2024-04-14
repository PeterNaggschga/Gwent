package com.peternaggschga.gwent.ui.dialogs.cards;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.valid4j.errors.RequireViolation;

@RunWith(JUnit4.class)
public class CardUiStateUnitTest {
    @Test
    public void diffCallbackAreItemsTheSameComparesIds() {
        CardUiState state1 = new CardUiState(1, 0, 0, 0, CardUiState.UNUSED, null);
        CardUiState state2 = new CardUiState(1, 0, 0, 0, CardUiState.UNUSED, null);
        assertThat(CardUiState.DIFF_CALLBACK.areItemsTheSame(state1, state2)).isTrue();

        state2 = new CardUiState(2, 0, 0, 0, CardUiState.UNUSED, null);
        assertThat(CardUiState.DIFF_CALLBACK.areItemsTheSame(state1, state2)).isFalse();
    }

    @Test
    public void diffCallbackAreContentsTheSameUsesEquals() {
        CardUiState state1 = new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        CardUiState state2 = new CardUiState(1, 0, 0, 0, CardUiState.UNUSED, null);

        assertThat(CardUiState.DIFF_CALLBACK.areContentsTheSame(state1, state2)).isEqualTo(state1.equals(state2));

        state2 = new CardUiState(1, 0, 1, 0, CardUiState.UNUSED, null);
        assertThat(CardUiState.DIFF_CALLBACK.areContentsTheSame(state1, state2)).isEqualTo(state1.equals(state2));
    }

    @Test
    public void constructorAssertsNonNegativeDamage() {
        try {
            new CardUiState(0, 0, -1, 0, CardUiState.UNUSED, null);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void constructorAllowsZeroDamage() {
        try {
            new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        } catch (RequireViolation ignored) {
            fail();
        }
    }

    @Test
    public void constructorAssertsPositiveSquad() {
        try {
            new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, 0);
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, 1);
        } catch (RequireViolation ignored) {
            fail();
        }
    }

    @Test
    public void constructorAllowsNullSquad() {
        try {
            new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        } catch (RequireViolation ignored) {
            fail();
        }
    }

    @Test
    public void constructorSetsEmptySquadString() {
        CardUiState state = new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        assertThat(state.getSquadString()).isEmpty();
    }

    @Test
    public void showAbilityReturnsTrueWhenAbilityImageIsDefined() {
        CardUiState state = new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        assertThat(state.showAbility()).isFalse();

        state = new CardUiState(0, 0, 0, 0, 0, null);
        assertThat(state.showAbility()).isTrue();
    }

    @Test
    public void showAbilityReturnsTrueWhenAbilityImageAndSquadStringAreDefined() {
        for (boolean ability : new boolean[]{false, true}) {
            for (boolean squad : new boolean[]{false, true}) {
                CardUiState state = new CardUiState(0, 0, 0, 0, ability ? 0 : CardUiState.UNUSED, squad ? 1 : null);
                assertThat(state.showSquad()).isEqualTo(ability && squad);
            }
        }
    }

    @Test
    public void equalsDoesNotCompareUnitId() {
        CardUiState state1 = new CardUiState(0, 0, 0, 0, CardUiState.UNUSED, null);
        CardUiState state2 = new CardUiState(1, 0, 0, 0, CardUiState.UNUSED, null);
        assertThat(state1.equals(state2)).isTrue();
    }
}
