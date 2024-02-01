package com.peternaggschga.gwent.data;

import static org.valid4j.Assertive.require;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * A facade class managing public access to the data layer.
 * The contained functions mostly redirect requests to package-private DAO methods in RowDao and UnitDao.
 * Some functions implement slightly more complex behavior by chaining multiple DAO calls,
 * e.g. #reset().
 */
public class UnitRepository {
    /**
     * Defines the AppDatabase that is used as a data source by this repository.
     * Is provided by dependency injection in #UnitRepository().
     */
    @NonNull
    private final AppDatabase database;

    /**
     * Constructor of a UnitRepository.
     * Depends on the given AppDatabase as a data source.
     * Three attack rows are initialized, if not yet done so, via #initializeRows().
     *
     * @param database AppDatabase that is injected for the repository.
     * @see #initializeRows()
     */
    public UnitRepository(@NonNull AppDatabase database) {
        this.database = database;
        initializeRows().blockingAwait();
    }

    /**
     * Adds one attack row for each RowType asynchronously.
     * If an attack row already exists, it is not inserted again.
     *
     * @return A Completable tracking operation status.
     */
    private Completable initializeRows() {
        Completable result = Completable.complete();
        for (RowType row : RowType.values()) {
            result = result.andThen(database.rows().insertRow(new RowEntity(row)));
        }
        return result;
    }

    /**
     * Resets the board asynchronously by removing all units and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling #initializeRows().
     * Method is a wrapper for #reset(UnitEntity).
     *
     * @return A Completable tracking operation status.
     * @see #reset(UnitEntity)
     */
    public Completable reset() {
        return reset(null);
    }

    /**
     * Resets the board asynchronously by removing all units but the given one and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling #initializeRows().
     *
     * @param keptUnit UnitEntity that should be kept.
     * @return A Completable tracking operation status.
     * @see #initializeRows()
     */
    public Completable reset(@Nullable UnitEntity keptUnit) {
        Completable result = database.rows().clearRows().andThen(initializeRows());
        if (keptUnit != null) {
            result = result.andThen(insertUnit(keptUnit));
        }
        return result;
    }

    /**
     * Adds the given UnitEntity asynchronously.
     *
     * @param unit UnitEntity that should be added.
     * @return A Completable tracking operation status.
     */
    private Completable insertUnit(@NonNull UnitEntity unit) {
        return database.units().insertUnit(unit);
    }

    /**
     * Adds a unit with the given stats to the given row asynchronously.
     *
     * @param epic    Boolean representing whether card is #epic.
     * @param damage  Non-negative value representing the #damage of the card.
     * @param ability Ability representing the #ability of the card.
     * @param squad   Integer representing the #squad of a card that has the Ability#BINDING #ability.
     * @param row     RowType representing the combat type of the card.
     * @return A Completable tracking operation status.
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero or if ability is Ability#BINDING and squad is null or less than zero or if ability is not Ability#BINDING and squad is not null.
     */
    public Completable insertUnit(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability, @IntRange(from = 0) @Nullable Integer squad, @NonNull RowType row) {
        require(damage >= 0);
        require((ability != Ability.BINDING && squad == null) || (ability == Ability.BINDING && squad != null && squad >= 0));
        return database.units().insertUnit(epic, damage, ability, squad, row);
    }

    /**
     * Adds a number of units with the given stats to the given row asynchronously.
     * Essentially calls #insertUnit(boolean, int, Ability, Integer, RowType) multiple times.
     *
     * @param epic    Boolean representing whether card is #epic.
     * @param damage  Non-negative value representing the #damage of the card.
     * @param ability Ability representing the #ability of the card.
     * @param squad   Integer representing the #squad of a card that has the Ability#BINDING #ability.
     * @param row     RowType representing the combat type of the card.
     * @param number  Integer representing the number of units to be added.
     * @return A Completable tracking operation status.
     * @see #insertUnit(boolean, int, Ability, Integer, RowType)
     */
    public Completable insertUnit(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability, @IntRange(from = 0) @Nullable Integer squad, @NonNull RowType row, int number) {
        Completable result = Completable.complete();
        for (int i = 0; i < number; i++) {
            result = result.andThen(insertUnit(epic, damage, ability, squad, row));
        }
        return result;
    }

    /**
     * Flips RowEntity#weather of the given attack row asynchronously.
     *
     * @param row RowEntity#id where the weather should be updated.
     * @return A Completable tracking operation status.
     */
    public Completable switchWeather(@NonNull RowType row) {
        return database.rows().updateWeather(row);
    }

    /**
     * Returns the value of RowEntity#weather for the given attack row asynchronously.
     *
     * @param row RowEntity#id where the weather is queried.
     * @return A Single tracking operation status and returning the value.
     */
    public Single<Boolean> isWeather(@NonNull RowType row) {
        return database.rows().isWeather(row);
    }

    /**
     * Sets RowEntity#weather to `false` asynchronously for all attack rows.
     *
     * @return A Completable tracking operation status.
     */
    public Completable clearWeather() {
        return database.rows().clearWeather();
    }

    /**
     * Flips RowEntity#horn of the given attack row asynchronously.
     *
     * @param row RowEntity#id where the horn status should be updated.
     * @return A Completable tracking operation status.
     */
    public Completable switchHorn(@NonNull RowType row) {
        return database.rows().updateHorn(row);
    }

    /**
     * Returns the value of RowEntity#horn for the given attack row asynchronously.
     *
     * @param row RowEntity#id where the horn status is queried.
     * @return A Single tracking operation status and returning the value.
     */
    public Single<Boolean> isHorn(@NonNull RowType row) {
        return database.rows().isHorn(row);
    }

    /**
     * Removes the given units from the game asynchronously.
     *
     * @param units List of units to be removed.
     * @return A Completable tracking operation status.
     */
    public Completable delete(@NonNull Collection<UnitEntity> units) {
        return database.units().deleteUnits(units);
    }

    /**
     * Copies the given units asynchronously.
     *
     * @param units List of UnitEntity elements that should be copied.
     * @return A Completable tracking operation status.
     */
    public Completable copy(@NonNull Collection<UnitEntity> units) {
        Completable result = Completable.complete();
        for (UnitEntity unit : units) {
            result = result.andThen(insertUnit(unit.isEpic(), unit.getDamage(), unit.getAbility(), unit.getSquad(), unit.getRow()));
        }
        return result;
    }

    /**
     * Counts the units in the given attack row asynchronously.
     *
     * @param row RowEntity#id where the units are counted.
     * @return A Single tracking operation status and returning the value.
     * @see #countUnits()
     */
    public Single<Integer> countUnits(@NonNull RowType row) {
        return database.units().countUnits(row);
    }

    /**
     * Counts the units in all attack rows asynchronously.
     *
     * @return A Single tracking operation status and returning the value.
     * @see #countUnits(RowType)
     */
    public Single<Integer> countUnits() {
        return database.units().countUnits();
    }

    /**
     * Returns the units in the given attack row asynchronously.
     *
     * @param row RowEntity#id where the units have been placed.
     * @return A Single tracking operation status and returning the value.
     * @see #getUnits()
     */
    public Single<List<UnitEntity>> getUnits(@NonNull RowType row) {
        return database.units().getUnits(row);
    }

    /**
     * Returns the units in the given attack row asynchronously.
     *
     * @return A Single tracking operation status and returning the value.
     * @see #getUnits(RowType)
     */
    public Single<List<UnitEntity>> getUnits() {
        return database.units().getUnits();
    }
}
