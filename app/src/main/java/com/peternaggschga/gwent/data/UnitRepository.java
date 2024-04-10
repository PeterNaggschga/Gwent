package com.peternaggschga.gwent.data;

import static org.valid4j.Assertive.require;

import android.database.Observable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.RowType;

import java.util.Collection;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.BiPredicate;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A facade class managing public access to the data layer.
 * The contained functions mostly redirect requests to package-private DAO methods in RowDao and UnitDao.
 * Some functions implement slightly more complex behavior by chaining multiple DAO calls,
 * e.g., #reset().
 * Extends Flowable to allow the registration of Observer implementors using #registerObserver().
 * Can be used by ViewModel classes.
 *
 * @see Flowable
 * @todo Add testing that every non-read operations call #notifyObservers().
 * @todo Check which methods can be dropped.
 * @todo Add method to get non-epic units and replace stream.filter() calls.
 * @todo Add method to get units of a certain ability and replace stream.filter() calls.
 * @todo Add caching proxy.
 */
public class UnitRepository extends Observable<Observer> {
    /**
     * BiPredicate used to check whether two Lists of UnitEntity are the same.
     *
     * @see Flowable#distinctUntilChanged(BiPredicate)
     * @see #getUnitsFlowable(RowType)
     * @see #getUnitsFlowable()
     */
    private static final BiPredicate<List<UnitEntity>, List<UnitEntity>> UNIT_LIST_COMPARATOR = (list1, list2) -> {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int index = 0; index < list1.size(); index++) {
            if (list1.get(index).getId() != list2.get(index).getId()) {
                return false;
            }
        }
        return true;
    };

    /**
     * Defines the AppDatabase that is used as a data source by this repository.
     * Is provided by dependency injection in #UnitRepository().
     */
    @NonNull
    private final AppDatabase database;

    /**
     * Constructor of a UnitRepository.
     * Depends on the given AppDatabase as a data source.
     * Should only be called by #getRepository().
     *
     * @param database AppDatabase that is injected for the repository.
     * @see #getRepository(AppDatabase)
     */
    private UnitRepository(@NonNull AppDatabase database) {
        this.database = database;
    }

    /**
     * Factory method for the UnitRepository class.
     * Creates a new UnitRepository managing the given AppDatabase.
     * Also initializes one RowEntity per RowType using #initializeRows().
     * @see #initializeRows()
     * @param database AppDatabase managed and initialized by the returned UnitRepository.
     * @return A Single emitting the created UnitRepository when initialization is finished.
     */
    @NonNull
    public static Single<UnitRepository> getRepository(@NonNull AppDatabase database) {
        UnitRepository repository = new UnitRepository(database);
        return repository.initializeRows()
                .andThen(Single.just(repository))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Notifies all Observer objects in #mObservers using Observer#update().
     * @see Observer#update()
     * @return A Completable tracking operation status.
     */
    @NonNull
    private Completable notifyObservers() {
        Completable result = Completable.complete();
        for (Observer observer : mObservers) {
            result = result.andThen(observer.update());
        }
        return result;
    }

    /**
     * Adds one attack row for each RowType.
     * If an attack row already exists, it is not inserted again.
     *
     * @return A Completable tracking operation status.
     */
    @NonNull
    private Completable initializeRows() {
        Completable result = Completable.complete();
        for (RowType row : RowType.values()) {
            result = result.andThen(database.rows().insertRow(new RowEntity(row)));
        }
        return result;
    }

    /**
     * Resets the board by removing all units and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling #initializeRows().
     * Notifies observers by calling #notifyObservers().
     * Method is a wrapper for #reset(UnitEntity).
     *
     * @return A Completable tracking operation status.
     * @see #reset(UnitEntity)
     */
    @NonNull
    public Completable reset() {
        return reset(null);
    }

    /**
     * Resets the board by removing all units but the given one and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling #initializeRows().
     * Notifies observers by calling #notifyObservers().
     *
     * @param keptUnit UnitEntity that should be kept.
     * @return A Completable tracking operation status.
     * @see #initializeRows()
     * @see #notifyObservers()
     */
    @NonNull
    public Completable reset(@Nullable UnitEntity keptUnit) {
        Completable result = database.rows().clearRows().andThen(initializeRows());
        if (keptUnit != null) {
            result = result.andThen(insertUnit(keptUnit));
        }
        return result.andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Adds the given UnitEntity.
     *
     * @param unit UnitEntity that should be added.
     * @return A Completable tracking operation status.
     * @todo See if this can be removed.
     */
    @NonNull
    private Completable insertUnit(@NonNull UnitEntity unit) {
        return database.units().insertUnit(unit);
    }

    /**
     * Adds a unit with the given stats to the given row.
     *
     * @param epic    Boolean representing whether card is #epic.
     * @param damage  Non-negative value representing the #damage of the card.
     * @param ability Ability representing the #ability of the card.
     * @param squad   Integer representing the #squad of a card that has the Ability#BINDING #ability.
     * @param row     RowType representing the combat type of the card.
     * @return A Completable tracking operation status.
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero or if ability is Ability#BINDING and squad is null or less than zero or if ability is not Ability#BINDING and squad is not null.
     */
    @NonNull
    private Completable insertUnit(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability,
                                   @IntRange(from = 0) @Nullable Integer squad, @NonNull RowType row) {
        require(damage >= 0);
        require((ability != Ability.BINDING && squad == null) || (ability == Ability.BINDING && squad != null && squad >= 0));
        return database.units().insertUnit(epic, damage, ability, squad, row)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Adds a number of units with the given stats to the given row.
     * Notifies observers by calling #notifyObservers().
     * Essentially calls #insertUnit(boolean, int, Ability, Integer, RowType) multiple times.
     *
     * @param epic    Boolean representing whether card is #epic.
     * @param damage  Non-negative value representing the #damage of the card.
     * @param ability Ability representing the #ability of the card.
     * @param squad   Integer representing the #squad of a card that has the Ability#BINDING #ability.
     * @param row     RowType representing the combat type of the card.
     * @param number  Integer representing the number of units to be added.
     * @return A Completable tracking operation status.
     * @see #notifyObservers()
     * @see #insertUnit(boolean, int, Ability, Integer, RowType)
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero or if ability is Ability#BINDING and squad is null or less than zero
     * or if ability is not Ability#BINDING and squad is not null.
     */
    @NonNull
    public Completable insertUnit(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability,
                                  @IntRange(from = 0) @Nullable Integer squad, @NonNull RowType row,
                                  @IntRange(from = 0) int number) {
        Completable result = Completable.complete();
        for (int i = 0; i < number; i++) {
            result = result.andThen(insertUnit(epic, damage, ability, squad, row));
        }
        return result.andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Flips RowEntity#weather of the given attack row.
     * Notifies observers by calling #notifyObservers().
     *
     * @see #notifyObservers()
     * @param row RowEntity#id where the weather should be updated.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable switchWeather(@NonNull RowType row) {
        return database.rows().updateWeather(row).andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the value of RowEntity#weather for the given attack row.
     * @see #isWeatherFlowable(RowType)
     * @param row RowEntity#id where the weather is queried.
     * @return A Single tracking operation status and returning the value.
     */
    @NonNull
    public Single<Boolean> isWeather(@NonNull RowType row) {
        return database.rows().isWeather(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the latest value of RowEntity#weather for the given attack row.
     *
     * @param row RowEntity#id where the weather is queried.
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #isWeather(RowType)
     */
    @NonNull
    public Flowable<Boolean> isWeatherFlowable(@NonNull RowType row) {
        return database.rows()
                .isWeatherFlowable(row)
                .onBackpressureLatest()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Sets RowEntity#weather to `false` for all attack rows.
     * Notifies observers by calling #notifyObservers().
     *
     * @see #notifyObservers()
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable clearWeather() {
        return database.rows().clearWeather().andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Flips RowEntity#horn of the given attack row.
     * Notifies observers by calling #notifyObservers().
     * @see #notifyObservers()
     * @param row RowEntity#id where the horn status should be updated.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable switchHorn(@NonNull RowType row) {
        return database.rows().updateHorn(row).andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the value of RowEntity#horn for the given attack row.
     * @see #isHornFlowable(RowType)
     * @param row RowEntity#id where the horn status is queried.
     * @return A Single tracking operation status and returning the value.
     */
    @NonNull
    public Single<Boolean> isHorn(@NonNull RowType row) {
        return database.rows().isHorn(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the values of RowEntity#horn for the given attack row.
     *
     * @param row RowEntity#id where the horn status is queried.
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #isHorn(RowType)
     */
    @NonNull
    public Flowable<Boolean> isHornFlowable(@NonNull RowType row) {
        return database.rows()
                .isHornFlowable(row)
                .onBackpressureLatest()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Removes the given units from the game.
     * Notifies observers by calling #notifyObservers().
     * @see #notifyObservers()
     * @param units List of units to be removed.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable delete(@NonNull Collection<UnitEntity> units) {
        return database.units().deleteUnits(units).andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Removes the unit with the given id from the game.
     * Notifies observers by calling #notifyObservers().
     *
     * @param id Integer representing the unit that should be deleted.
     * @return A Completable tracking operation status.
     * @todo Add testing.
     * @see #notifyObservers()
     */
    @NonNull
    public Completable delete(int id) {
        return database.units().deleteUnit(id).andThen(notifyObservers()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Copies the unit with the given id.
     * Notifies observers by calling #notifyObservers().
     * @see #notifyObservers()
     * @param id Integer representing the unit that should be copied.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable copy(int id) {
        return getUnit(id).flatMapCompletable(unit ->
                        insertUnit(unit.isEpic(), unit.getDamage(), unit.getAbility(), unit.getSquad(), unit.getRow()))
                .andThen(notifyObservers())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Counts the units in the given attack row.
     * @see #countUnitsFlowable(RowType)
     * @param row RowEntity#id where the units are counted.
     * @return A Single tracking operation status and returning the value.
     * @see #countUnits()
     */
    @NonNull
    public Single<Integer> countUnits(@NonNull RowType row) {
        return database.units().countUnits(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable counting the units in the given attack row.
     *
     * @param row RowEntity#id where the units are counted.
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #countUnits(RowType)
     * @see #countUnits()
     */
    @NonNull
    public Flowable<Integer> countUnitsFlowable(@NonNull RowType row) {
        return database.units()
                .countUnitsFlowable(row)
                .onBackpressureLatest()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Counts the units in all attack rows.
     *
     * @return A Single tracking operation status and returning the value.
     * @see #countUnits(RowType)
     * @see #countUnitsFlowable()
     */
    @NonNull
    public Single<Integer> countUnits() {
        return database.units().countUnits().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable counting the units in all attack rows.
     *
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #countUnitsFlowable(RowType)
     * @see #countUnits()
     */
    @NonNull
    public Flowable<Integer> countUnitsFlowable() {
        return database.units()
                .countUnitsFlowable()
                .onBackpressureLatest()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the unit with the given id.
     *
     * @param id Integer representing the queried unit.
     * @return A Single tracking operation status and returning the value.
     * @todo Add testing.
     */
    @NonNull
    public Single<UnitEntity> getUnit(int id) {
        return database.units().getUnit(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the units in the given attack row.
     * @see #getUnitsFlowable(RowType)
     * @param row RowEntity#id where the units have been placed.
     * @return A Single tracking operation status and returning the value.
     * @see #getUnits()
     */
    @NonNull
    public Single<List<UnitEntity>> getUnits(@NonNull RowType row) {
        return database.units().getUnits(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the units in the given attack row.
     *
     * @param row RowEntity#id where the units have been placed.
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #getUnits(RowType)
     * @see #getUnits()
     */
    @NonNull
    public Flowable<List<UnitEntity>> getUnitsFlowable(@NonNull RowType row) {
        return database.units()
                .getUnitsFlowable(row)
                .onBackpressureLatest()
                .distinctUntilChanged(UNIT_LIST_COMPARATOR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the units in the given attack row.
     *
     * @return A Single tracking operation status and returning the value.
     * @see #getUnits(RowType)
     * @see #getUnitsFlowable()
     */
    @NonNull
    public Single<List<UnitEntity>> getUnits() {
        return database.units().getUnits().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the units in the given attack row.
     *
     * @return A Flowable emitting the values.
     * @todo Add testing.
     * @see #getUnits()
     * @see #getUnits(RowType)
     */
    @NonNull
    public Flowable<List<UnitEntity>> getUnitsFlowable() {
        return database.units()
                .getUnitsFlowable()
                .onBackpressureLatest()
                .distinctUntilChanged(UNIT_LIST_COMPARATOR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
