package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.Arrays;
import java.util.Collections;

@RunWith(Enclosed.class)
public class UnitEntityUnitTest {
    @RunWith(JUnit4.class)
    public static class ConstructorTests {
        @Test
        public void constructorAssertsNonNegativeDamage() {
            try {
                new UnitEntity(false, -1, Ability.NONE, null, RowType.MELEE);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void constructorAllowsZeroDamage() {
            try {
                new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE);
            } catch (RequireViolation ignored) {
                fail();
            }
        }

        @Test
        public void constructorAssertsSquadNullForOtherAbilities() {
            try {
                new UnitEntity(false, 0, Ability.NONE, 0, RowType.MELEE);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void constructorAssertsSquadNonNegativeForBinding() {
            try {
                new UnitEntity(false, 0, Ability.BINDING, -1, RowType.MELEE);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void constructorAllowsZeroForBinding() {
            try {
                new UnitEntity(false, 0, Ability.BINDING, 0, RowType.MELEE);
            } catch (RequireViolation ignored) {
                fail();
            }
        }

        @Test
        public void constructorAssertsSquadNonNullForBinding() {
            try {
                new UnitEntity(false, 0, Ability.BINDING, null, RowType.MELEE);
                fail();
            } catch (RequireViolation ignored) {
            }
        }
    }

    @RunWith(JUnit4.class)
    public static class SetDamageTests {
        @Test
        public void setDamageAssertsNonNegative() {
            try {
                new UnitEntity(false, -1, Ability.NONE, null, RowType.MELEE);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void setDamageAllowsZero() {
            try {
                new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE);
            } catch (RequireViolation ignored) {
                fail();
            }
        }
    }

    @RunWith(JUnit4.class)
    public static class SetSquadTests {
        private final UnitEntity testEntity = new UnitEntity(false, 0, Ability.BINDING, 0, RowType.MELEE);

        @Test
        public void setSquadAssertsSquadNullForOtherAbilities() {
            UnitEntity testEntity = new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE);
            try {
                testEntity.setSquad(0);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void setSquadAssertsSquadNonNegativeForBinding() {
            try {
                testEntity.setSquad(-1);
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void setSquadAllowsZeroForBinding() {
            try {
                testEntity.setSquad(0);
            } catch (RequireViolation ignored) {
                fail();
            }
        }

        @Test
        public void setSquadAssertsSquadNonNullForBinding() {
            try {
                testEntity.setSquad(null);
                fail();
            } catch (RequireViolation ignored) {
            }
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class CalculateDamageTests {
        @Test
        public void epicUnitsDoNotCallCalculator() {
            DamageCalculator calculator = mock(DamageCalculator.class);
            new UnitEntity(true, 5, Ability.NONE, null, RowType.MELEE).calculateDamage(calculator);
            verify(calculator, never()).calculateDamage(anyInt(), anyInt());
        }

        @Test
        public void nonEpicUnitsCallCalculator() {
            int damage = 5;
            DamageCalculator calculator = mock(DamageCalculator.class);
            new UnitEntity(false, damage, Ability.NONE, null, RowType.MELEE).calculateDamage(calculator);
            verify(calculator, only()).calculateDamage(anyInt(), eq(damage));
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class IsBuffedTests {
        @Test
        public void epicUnitsDoNotCallCalculator() {
            DamageCalculator calculator = mock(DamageCalculator.class);
            new UnitEntity(true, 5, Ability.NONE, null, RowType.MELEE).isBuffed(calculator);
            verify(calculator, never()).calculateDamage(anyInt(), anyInt());
        }

        @Test
        public void nonEpicUnitsCallCalculator() {
            DamageCalculator calculator = mock(DamageCalculator.class);
            new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE).isBuffed(calculator);
            verify(calculator, only()).isBuffed(anyInt());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class ToStringTests {
        @Mock
        UnitEntity mockUnit;

        @Before
        public void initMock() {
            when(mockUnit.toString(any())).thenCallRealMethod();
            when(mockUnit.getRow()).thenReturn(RowType.MELEE);
            when(mockUnit.getAbility()).thenReturn(Ability.NONE);
        }

        @Test
        public void selectsCorrectRowString() {
            Context context = ApplicationProvider.getApplicationContext();
            when(mockUnit.getRow()).thenReturn(RowType.MELEE);
            assertThat(mockUnit.toString(context)).startsWith(context.getString(R.string.unit_toString_melee));
            when(mockUnit.getRow()).thenReturn(RowType.RANGE);
            assertThat(mockUnit.toString(context)).startsWith(context.getString(R.string.unit_toString_range));
            when(mockUnit.getRow()).thenReturn(RowType.SIEGE);
            assertThat(mockUnit.toString(context)).startsWith(context.getString(R.string.unit_toString_siege));
        }

        @Test
        public void selectsCorrectEpicString() {
            Context context = ApplicationProvider.getApplicationContext();
            when(mockUnit.isEpic()).thenReturn(true);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.unit_toString_epic));
            when(mockUnit.isEpic()).thenReturn(false);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.unit_toString_unit));
        }

        @Test
        public void selectsCorrectAbilityString() {
            Context context = ApplicationProvider.getApplicationContext();
            when(mockUnit.getAbility()).thenReturn(Ability.NONE);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.unit_toString_ability_none));
            when(mockUnit.getAbility()).thenReturn(Ability.HORN);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.add_picker_ability_horn));
            when(mockUnit.getAbility()).thenReturn(Ability.MORAL_BOOST);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.add_picker_ability_moralBoost));
            when(mockUnit.getAbility()).thenReturn(Ability.BINDING);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.add_picker_ability_binding));
            when(mockUnit.getAbility()).thenReturn(Ability.REVENGE);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.add_picker_ability_revenge));
        }

        @Test
        public void selectsNoSquadStringWhenNotBindingUnit() {
            Context context = ApplicationProvider.getApplicationContext();
            when(mockUnit.getSquad()).thenReturn(1);
            for (Ability ability : Ability.values()) {
                if (ability == Ability.BINDING) {
                    continue;
                }
                when(mockUnit.getAbility()).thenReturn(ability);
                assertThat(mockUnit.toString(context)).doesNotContain(context.getString(R.string.unit_toString_squad, 1));
            }
        }

        @Test
        public void selectsSquadStringWhenBindingUnit() {
            Context context = ApplicationProvider.getApplicationContext();
            when(mockUnit.getAbility()).thenReturn(Ability.BINDING);
            when(mockUnit.getSquad()).thenReturn(1);
            assertThat(mockUnit.toString(context)).contains(context.getString(R.string.unit_toString_squad, 1));
        }
    }

    @RunWith(AndroidJUnit4.class)
    public static class CollectionToStringTests {
        private static final String MOCK_UNIT_DESCRIPTION = "MockUnit";

        @Test
        public void assertsNonEmptyCollection() {
            try {
                UnitEntity.collectionToString(ApplicationProvider.getApplicationContext(), Collections.emptyList());
                fail();
            } catch (RequireViolation ignored) {
            }
        }

        @Test
        public void returnsUnitToStringForSingularUnit() {
            UnitEntity mockUnit = mock(UnitEntity.class);
            when(mockUnit.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION);
            assertThat(UnitEntity.collectionToString(ApplicationProvider.getApplicationContext(),
                    Collections.singleton(mockUnit)))
                    .isEqualTo(MOCK_UNIT_DESCRIPTION);
        }

        @Test
        public void accumulatesUsingWord() {
            Context context = ApplicationProvider.getApplicationContext();
            UnitEntity mockUnit1 = mock(UnitEntity.class);
            when(mockUnit1.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION + "1");
            UnitEntity mockUnit2 = mock(UnitEntity.class);
            when(mockUnit2.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION + "2");

            assertThat(UnitEntity.collectionToString(context, Arrays.asList(mockUnit1, mockUnit2)))
                    .isEqualTo(context.getString(R.string.unit_collection_toString_accumulation_word,
                            MOCK_UNIT_DESCRIPTION + "1",
                            MOCK_UNIT_DESCRIPTION + "2"));
        }

        @Test
        public void accumulatesUsingComma() {
            Context context = ApplicationProvider.getApplicationContext();
            UnitEntity mockUnit1 = mock(UnitEntity.class);
            when(mockUnit1.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION + "1");
            UnitEntity mockUnit2 = mock(UnitEntity.class);
            when(mockUnit2.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION + "2");
            UnitEntity mockUnit3 = mock(UnitEntity.class);
            when(mockUnit3.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION + "3");

            assertThat(UnitEntity.collectionToString(context, Arrays.asList(mockUnit1, mockUnit2, mockUnit3)))
                    .startsWith(context.getString(R.string.unit_collection_toString_accumulation_symbol,
                            MOCK_UNIT_DESCRIPTION + "1",
                            MOCK_UNIT_DESCRIPTION + "3"));
        }

        @Test
        public void sameDescriptionIsReusedViaTimesSymbol() {
            Context context = ApplicationProvider.getApplicationContext();
            UnitEntity mockUnit = mock(UnitEntity.class);
            when(mockUnit.toString(any())).thenReturn(MOCK_UNIT_DESCRIPTION);

            assertThat(UnitEntity.collectionToString(context, Arrays.asList(mockUnit, mockUnit)))
                    .isEqualTo(context.getString(R.string.unit_toString_multiplicity, 2, MOCK_UNIT_DESCRIPTION));
        }
    }
}
