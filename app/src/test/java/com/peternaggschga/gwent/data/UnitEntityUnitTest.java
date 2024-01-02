package com.peternaggschga.gwent.data;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.valid4j.errors.RequireViolation;

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
}
