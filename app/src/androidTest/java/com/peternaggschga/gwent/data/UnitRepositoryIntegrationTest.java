package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.RowType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.valid4j.errors.RequireViolation;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UnitRepositoryIntegrationTest {
    private static final int TESTING_DEPTH = 50;
    private AppDatabase database;
    private UnitRepository repository;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).build();
        repository = new UnitRepository(database);
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    private void insertDummys() {
        for (RowType row : RowType.values()) {
            for (int dummyNumber = 0; dummyNumber < 5; dummyNumber++) {
                database.units().insertUnit(false, dummyNumber, Ability.values()[dummyNumber / 2], null, row)
                        .blockingAwait();
            }
        }
    }

    @Test
    public void constructorAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository = new UnitRepository(null);
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void constructorInitializesRows() {
        for (RowType row : RowType.values()) {
            database.rows().isWeather(row).test().assertValue(false);
        }
    }

    @Test
    public void constructorDoesntResetDatabase() {
        insertDummys();
        repository = new UnitRepository(database);
        assertThat(database.units().countUnits().blockingGet()).isGreaterThan(0);
    }

    @Test
    public void resetNullDeletesUnits() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (RowType row : RowType.values()) {
                database.units().insertUnit(false, 5, Ability.NONE, null, row).blockingAwait();
            }
        }
        repository.reset().andThen(database.units().countUnits()).test().assertValue(0);
    }

    @Test
    public void resetKeepsUnit() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (RowType row : RowType.values()) {
                database.units().insertUnit(false, 5, Ability.NONE, null, row).blockingAwait();
            }
        }
        //noinspection OptionalGetWithoutIsPresent
        UnitEntity unit = database.units().getUnits().blockingGet().stream().findAny().get();
        repository.reset(unit).andThen(database.units().countUnits()).test().assertValue(1);
    }

    @Test
    public void resetRemovesEffects() {
        for (RowType row : RowType.values()) {
            database.rows().updateHorn(row).andThen(database.rows().updateHorn(row)).blockingAwait();
        }
        repository.reset().blockingAwait();
        for (RowType row : RowType.values()) {
            database.rows().isHorn(row).test().assertValue(false);
            database.rows().isWeather(row).test().assertValue(false);
        }
    }

    @Test
    public void insertUnitAddsUnits() {
        for (int unitNumber = 0; unitNumber < TESTING_DEPTH; unitNumber++) {
            repository.insertUnit(unitNumber % 5 == 0,
                    unitNumber % 10,
                    Ability.values()[unitNumber % 3],
                    null,
                    RowType.values()[unitNumber % 3]).blockingAwait();
        }
        database.units().countUnits().test().assertValue(TESTING_DEPTH);
    }

    @Test
    public void insertUnitNumberAddsUnits() {
        repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, TESTING_DEPTH)
                .andThen(database.units().countUnits())
                .test()
                .assertValue(TESTING_DEPTH);
    }

    @Test
    public void insertUnitAssertsNonNegativeDamage() {
        try {
            repository.insertUnit(false, -1, Ability.NONE, null, RowType.MELEE).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            repository.insertUnit(false, -1, Ability.NONE, null, RowType.MELEE, 5).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void insertUnitAllowsZeroDamage() {
        try {
            repository.insertUnit(false, 0, Ability.NONE, null, RowType.MELEE)
                    .andThen(repository.insertUnit(false, 0, Ability.NONE, null, RowType.MELEE, 5))
                    .blockingAwait();
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void insertUnitAssertsSquadNullForOtherAbilities() {
        try {
            repository.insertUnit(false, 0, Ability.NONE, 0, RowType.MELEE).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            repository.insertUnit(false, 0, Ability.NONE, 0, RowType.MELEE, 5).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void insertUnitAssertsSquadNonNegativeForBinding() {
        try {
            repository.insertUnit(false, 0, Ability.BINDING, -1, RowType.MELEE).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            repository.insertUnit(false, 0, Ability.BINDING, -1, RowType.MELEE, 5).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void insertUnitAllowsZeroForBinding() {
        try {
            repository.insertUnit(false, 0, Ability.BINDING, 0, RowType.MELEE)
                    .andThen(repository.insertUnit(false, 0, Ability.BINDING, 0, RowType.MELEE, 5))
                    .blockingAwait();
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void insertUnitAssertsSquadNonNullForBinding() {
        try {
            repository.insertUnit(false, 0, Ability.BINDING, null, RowType.MELEE).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            repository.insertUnit(false, 0, Ability.BINDING, null, RowType.MELEE, 5).blockingAwait();
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void insertUnitAssertsRowNotNull() {
        try {
            //noinspection DataFlowIssue
            repository.insertUnit(false, 5, Ability.NONE, null, null).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            //noinspection DataFlowIssue
            repository.insertUnit(false, 5, Ability.NONE, null, null, 5).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void switchWeatherSwitchesWeather() {
        for (RowType row : RowType.values()) {
            repository.switchWeather(row)
                    .andThen(database.rows().isWeather(row))
                    .test()
                    .assertValue(true);
            repository.switchWeather(row)
                    .andThen(database.rows().isWeather(row))
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void switchWeatherAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.switchWeather(null).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isWeatherReturnsWeather() {
        for (RowType row : RowType.values()) {
            repository.isWeather(row).test().assertValue(false);
            database.rows().updateWeather(row)
                    .andThen(repository.isWeather(row))
                    .test()
                    .assertValue(true);
        }
    }

    @Test
    public void isWeatherAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.isWeather(null).test().dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void clearWeatherClearsWeather() {
        for (RowType row : RowType.values()) {
            database.rows().updateWeather(row).blockingAwait();
        }
        repository.clearWeather().blockingAwait();
        for (RowType row : RowType.values()) {
            database.rows().isWeather(row).test().assertValue(false);
        }
    }

    @Test
    public void switchHornSwitchesHorn() {
        for (RowType row : RowType.values()) {
            repository.switchHorn(row)
                    .andThen(database.rows().isHorn(row))
                    .test()
                    .assertValue(true);
            repository.switchHorn(row)
                    .andThen(database.rows().isHorn(row))
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void switchHornAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.switchHorn(null).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isHornReturnsHorn() {
        for (RowType row : RowType.values()) {
            repository.isHorn(row).test().assertValue(false);
            database.rows().updateHorn(row)
                    .andThen(repository.isHorn(row))
                    .test()
                    .assertValue(true);
        }
    }

    @Test
    public void isHornAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.isHorn(null).test().dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void deleteDeletesUnits() {
        insertDummys();

        List<UnitEntity> units = database.units().getUnits().blockingGet();
        repository.delete(units)
                .andThen(database.units().countUnits())
                .test()
                .assertValue(0);
    }

    @Test
    public void deleteAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.delete(null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void copyCopiesUnits() {
        insertDummys();

        List<UnitEntity> units = database.units().getUnits().blockingGet();
        repository.copy(units)
                .andThen(database.units().countUnits())
                .test()
                .assertValue(units.size() * 2);
    }

    @Test
    public void copyAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.copy(null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void countUnitsCountsCorrectly() {
        for (int rowNumber = 0; rowNumber < RowType.values().length; rowNumber++) {
            RowType row = RowType.values()[rowNumber];
            repository.countUnits(row)
                    .test()
                    .assertValue(0);
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                repository.countUnits()
                        .test()
                        .assertValue(rowNumber * 5 + unitNumber);
                database.units().insertUnit(false, unitNumber, Ability.values()[unitNumber / 2], null, row)
                        .andThen(repository.countUnits(row))
                        .test()
                        .assertValue(unitNumber + 1);
            }
            repository.countUnits()
                    .test()
                    .assertValue((rowNumber + 1) * 5);
        }

    }

    @Test
    public void countUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.countUnits(null)
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getUnitsReturnsAllUnits() {
        for (RowType row : RowType.values()) {
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                assertThat(
                        database.units().insertUnit(false, unitNumber, Ability.values()[unitNumber / 2], null, row)
                                .andThen(repository.getUnits(row))
                                .blockingGet())
                        .hasSize(unitNumber + 1);
            }
            assertThat(repository.getUnits(row).blockingGet()).hasSize(5);
        }
        assertThat(repository.getUnits().blockingGet()).hasSize(RowType.values().length * 5);
    }

    @Test
    public void getUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository.getUnits(null)
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }
}
