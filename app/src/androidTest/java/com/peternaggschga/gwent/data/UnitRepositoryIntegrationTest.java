package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.RowType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.valid4j.errors.RequireViolation;

import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(AndroidJUnit4.class)
public class UnitRepositoryIntegrationTest {
    private static final int TESTING_DEPTH = 50;
    private AppDatabase database;
    private UnitRepository repository;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class)
                .build();
        repository = UnitRepository.getRepository(database).blockingGet();
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
    public void getRepositoryAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            repository = UnitRepository.getRepository(null).blockingGet();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getRepositoryInitializesRows() {
        for (RowType row : RowType.values()) {
            database.rows().isWeather(row).test().assertValue(false);
        }
    }

    @Test
    public void getRepositoryDoesNotResetDatabase() {
        insertDummys();
        repository = UnitRepository.getRepository(database).blockingGet();
        assertThat(database.units().countUnits().blockingGet()).isGreaterThan(0);
    }

    @Test
    public void resetNullDeletesUnits() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (RowType row : RowType.values()) {
                database.units().insertUnit(false, 5, Ability.NONE, null, row).blockingAwait();
            }
        }
        assertThat(repository.reset()
                .observeOn(Schedulers.io())
                .andThen(database.units().countUnits())
                .blockingGet()
        ).isEqualTo(0);
    }

    @Test
    public void resetKeepsUnit() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (RowType row : RowType.values()) {
                database.units().insertUnit(false, 5, Ability.NONE, null, row).blockingAwait();
            }
        }
        //noinspection OptionalGetWithoutIsPresent
        UnitEntity unit = database.units().getUnits().subscribeOn(Schedulers.io()).blockingGet().stream().findAny().get();
        assertThat(repository.reset(unit)
                .observeOn(Schedulers.io())
                .andThen(database.units().countUnits())
                .blockingGet()
        ).isEqualTo(1);
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
        assertThat(repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, TESTING_DEPTH)
                .observeOn(Schedulers.io())
                .andThen(database.units().countUnits())
                .blockingGet())
                .isEqualTo(TESTING_DEPTH);
    }

    @Test
    public void insertUnitAssertsNonNegativeDamage() {
        try {
            repository.insertUnit(false, -1, Ability.NONE, null, RowType.MELEE, 1).blockingAwait();
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
            repository.insertUnit(false, 0, Ability.NONE, null, RowType.MELEE, 1)
                    .andThen(repository.insertUnit(false, 0, Ability.NONE, null, RowType.MELEE, 5))
                    .blockingAwait();
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void insertUnitAssertsSquadNullForOtherAbilities() {
        try {
            repository.insertUnit(false, 0, Ability.NONE, 0, RowType.MELEE, 1).blockingAwait();
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
            repository.insertUnit(false, 0, Ability.BINDING, -1, RowType.MELEE, 1).blockingAwait();
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
            repository.insertUnit(false, 0, Ability.BINDING, 0, RowType.MELEE, 1)
                    .andThen(repository.insertUnit(false, 0, Ability.BINDING, 0, RowType.MELEE, 5))
                    .blockingAwait();
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void insertUnitAssertsSquadNonNullForBinding() {
        try {
            repository.insertUnit(false, 0, Ability.BINDING, null, RowType.MELEE, 1).blockingAwait();
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
            repository.insertUnit(false, 5, Ability.NONE, null, null, 1).blockingAwait();
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
            assertThat(repository.switchWeather(row)
                    .observeOn(Schedulers.io())
                    .andThen(database.rows().isWeather(row))
                    .blockingGet())
                    .isTrue();
            assertThat(repository.switchWeather(row)
                    .observeOn(Schedulers.io())
                    .andThen(database.rows().isWeather(row))
                    .blockingGet())
                    .isFalse();
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
            assertThat(repository.isWeather(row).blockingGet()).isFalse();
            assertThat(database.rows().updateWeather(row)
                    .andThen(repository.isWeather(row))
                    .blockingGet())
                    .isTrue();
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
            assertThat(repository.switchHorn(row)
                    .observeOn(Schedulers.io())
                    .andThen(database.rows().isHorn(row))
                    .blockingGet())
                    .isTrue();
            assertThat(repository.switchHorn(row)
                    .observeOn(Schedulers.io())
                    .andThen(database.rows().isHorn(row))
                    .blockingGet())
                    .isFalse();
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
            assertThat(repository.isHorn(row).blockingGet()).isFalse();
            assertThat(database.rows().updateHorn(row)
                    .andThen(repository.isHorn(row))
                    .blockingGet())
                    .isTrue();
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
        assertThat(repository.delete(units)
                .observeOn(Schedulers.io())
                .andThen(database.units().countUnits())
                .blockingGet())
                .isEqualTo(0);
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
        assertThat(repository.copy(units.get(0).getId())
                .observeOn(Schedulers.io())
                .andThen(database.units().countUnits())
                .blockingGet())
                .isEqualTo(units.size() + 1);
    }

    @Test
    public void countUnitsCountsCorrectly() {
        for (int rowNumber = 0; rowNumber < RowType.values().length; rowNumber++) {
            RowType row = RowType.values()[rowNumber];
            assertThat(repository.countUnits(row)
                    .blockingGet())
                    .isEqualTo(0);
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                assertThat(repository.countUnits()
                        .blockingGet())
                        .isEqualTo(rowNumber * 5 + unitNumber);
                assertThat(database.units().insertUnit(false, unitNumber, Ability.values()[unitNumber / 2], null, row)
                        .andThen(repository.countUnits(row))
                        .blockingGet())
                        .isEqualTo(unitNumber + 1);
            }
            assertThat(repository.countUnits()
                    .blockingGet())
                    .isEqualTo((rowNumber + 1) * 5);
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
