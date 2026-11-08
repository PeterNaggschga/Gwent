package com.peternaggschga.gwent.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.domain.cases.TransformBerserkersUseCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A facade class managing public access to the data layer.
 * The contained functions mostly redirect requests to package-private DAO methods in RowDao and UnitDao.
 * Some functions implement slightly more complex behavior by chaining multiple DAO calls,
 * e.g., .reset().
 */
public class UnitRepository {
    /**
     * Preference storing the selected leader's weather effect between application launches.
     */
    private static final String KING_BRAN_PREFERENCE_KEY = "kingBran";

    /**
     * Defines the AppDatabase that is used as a data source by this repository.
     * Is provided by dependency injection in .UnitRepository().
     */
    @NonNull
    private final AppDatabase database;

    /**
     * Application context, never an Activity context, used for notifications and preferences.
     */
    @NonNull
    private final Context context;

    /**
     * Constructor of a UnitRepository.
     * Depends on the given AppDatabase as a data source.
     * Should only be called by .getRepository().
     *
     * @param database AppDatabase that is injected for the repository.
     * @param context Context whose application context is retained for transformation notifications.
     * @see .getRepository(AppDatabase, Context)
     */
    private UnitRepository(@NonNull AppDatabase database, @NonNull Context context) {
        this.database = database;
        this.context = Objects.requireNonNull(context.getApplicationContext());
    }

    /**
     * Factory method for the UnitRepository class.
     * Creates a new UnitRepository managing the given AppDatabase.
     * Also initializes one RowEntity per RowType using .initializeRows().
     * @see .initializeRows()
     * @param database AppDatabase managed and initialized by the returned UnitRepository.
     * @param context Context whose application context is retained for domain notifications.
     * @return A Single emitting the created UnitRepository when initialization is finished.
     */
    @NonNull
    public static Single<UnitRepository> getRepository(@NonNull AppDatabase database, @NonNull Context context) {
        UnitRepository repository = new UnitRepository(database, context);
        return repository.initializeRows()
                .andThen(Single.just(repository))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
     * Reads the saved King Bran weather effect without involving UI context.
     *
     * @return Whether King Bran's weather effect is enabled.
     */
    public boolean isKingBran() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KING_BRAN_PREFERENCE_KEY, false);
    }

    /**
     * Saves the leader effect independently of board resets. Faction switching clears or replaces it.
     *
     * @param enabled Whether King Bran's weather effect should be enabled.
     * @return A Completable saving the preference on subscription.
     */
    @NonNull
    public Completable setKingBran(boolean enabled) {
        return Completable.fromAction(() -> PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KING_BRAN_PREFERENCE_KEY, enabled).apply());
    }

    /**
     * Observes the persisted leader effect, including its initial value.
     *
     * @return A stream releasing its preference listener when disposed.
     */
    @NonNull
    public Flowable<Boolean> isKingBranFlowable() {
        return Flowable.<Boolean>create(emitter -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.OnSharedPreferenceChangeListener listener = (changed, key) -> {
                // Editor.clear() can notify with a null key; re-read the default in that case.
                if (key == null || KING_BRAN_PREFERENCE_KEY.equals(key)) {
                    emitter.onNext(changed.getBoolean(KING_BRAN_PREFERENCE_KEY, false));
                }
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);
            emitter.setCancellable(() -> preferences.unregisterOnSharedPreferenceChangeListener(listener));
            emitter.onNext(preferences.getBoolean(KING_BRAN_PREFERENCE_KEY, false));
        }, BackpressureStrategy.LATEST).distinctUntilChanged();
    }

    /**
     * Resets the board by removing all units and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling .initializeRows().
     * Method is a wrapper for .reset(UnitEntity).
     *
     * @return A Completable tracking operation status.
     * @see .reset(UnitEntity)
     */
    @NonNull
    public Completable reset() {
        return reset(null);
    }

    /**
     * Resets the board by removing all units but the given one and resetting row status.
     * Resetting row status is equivalent to removing the old rows and calling .initializeRows().
     *
     * @param keptUnit UnitEntity that should be kept.
     * @return A Completable tracking operation status.
     * @see .initializeRows()
     */
    @NonNull
    public Completable reset(@Nullable UnitEntity keptUnit) {
        Completable result = database.rows().clearRows().andThen(initializeRows());
        if (keptUnit != null) {
            result = result.andThen(insertUnits(Collections.singletonList(keptUnit)));
        }
        return result.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Creates a unit after validating the repository's insertion constraints.
     *
     * @param epic    Boolean representing whether the card is epic.
     * @param damage  Non-negative base damage within the permitted damage values.
     * @param ability Ability of the card.
     * @param squad   Positive Tight Bond squad number, or null for other abilities.
     * @param row     Combat row of the card.
     * @return A new UnitEntity with an automatically generated ID on insertion.
     * @throws IllegalArgumentException When damage or the ability/squad combination is invalid.
     */
    @NonNull
    private UnitEntity createUnit(boolean epic, int damage, @NonNull Ability ability, @Nullable Integer squad,
                                  @NonNull RowType row) {
        Objects.requireNonNull(ability);
        Objects.requireNonNull(row);
        if (damage < 0 || damage > UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) {
            throw new IllegalArgumentException("Damage be between 0 and " + UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND + " but is " + damage + ".");
        }
        if (epic && Arrays.stream(UnitEntity.EPIC_DAMAGE_VALUES).noneMatch(integer -> integer == damage)) {
            throw new IllegalArgumentException("Damage of epic units must be in " + Arrays.toString(UnitEntity.EPIC_DAMAGE_VALUES) + " but is " + damage + ".");
        }
        if (ability != Ability.BINDING && squad != null) {
            throw new IllegalArgumentException("Squad must be null or ability must be BINDING but squad is " + squad + " and ability is " + ability + ".");
        }
        if (ability == Ability.BINDING && (squad == null || squad < 1)) {
            throw new IllegalArgumentException("Squad must not be null or less than one but squad is " + squad + ".");
        }
        if (Objects.equals(squad, UnitEntity.BERSERKER_SQUAD)) {
            throw new IllegalArgumentException("The Berserker squad is reserved for transformed units.");
        }
        return new UnitEntity(epic, damage, ability, squad, row);
    }

    /**
     * Adds a number of units with the given stats to the given row.
     * Creates the units in memory and inserts them with a single batched DAO call.
     * The central insertion method invokes the UseCase after the units have been persisted.
     * @param epic    Boolean representing whether card is .epic.
     * @param damage  Non-negative value representing the .damage of the card.
     * @param ability Ability representing the .ability of the card.
     * @param squad   Integer representing the .squad of a card that has the Ability.BINDING .ability.
     * @param row     RowType representing the combat type of the card.
     * @param number  Integer representing the number of units to be added.
     * @return A Completable tracking operation status.
     * @see .insertUnits(List)
     * @throws IllegalArgumentException When damage is less than zero or if ability is Ability.BINDING and squad is null or less than zero
     * or if ability is not Ability.BINDING and squad is not null.
     */
    @NonNull
    public Completable insertUnit(boolean epic, @IntRange(from = 0, to = UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) int damage, @NonNull Ability ability,
                                  @IntRange(from = 1) @Nullable Integer squad, @NonNull RowType row,
                                  @IntRange(from = 0) int number) {
        List<UnitEntity> units = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            units.add(createUnit(epic, damage, ability, squad, row));
        }
        return insertUnits(units);
    }

    /**
     * Central insertion path for additions, copies, and retained units during reset.
     * Inserts one batch, then passes only successfully inserted IDs to the transformation UseCase.
     *
     * @param units Units to insert. Normal card additions use insertUnit to validate their stats first.
     * @return A Completable tracking insertion, any required transformations, and notification.
     */
    @NonNull
    public Completable insertUnits(@NonNull List<UnitEntity> units) {
        return database.units().insertUnits(units)
                .map(ids -> ids.stream().filter(id -> id != -1).collect(Collectors.toList()))
                .flatMapCompletable(ids -> TransformBerserkersUseCase.transformAddedUnits(context, this, ids))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates a list of units in one batch by their existing IDs; does not insert missing IDs.
     *
     * @param units Units containing their persisted IDs and new values.
     * @return A Completable tracking the batch update.
     */
    @NonNull
    public Completable updateUnits(@NonNull List<UnitEntity> units) {
        return database.units().updateUnits(units)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Flips RowEntity.weather of the given attack row.
     *
     * @param row RowEntity.id where the weather should be updated.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable switchWeather(@NonNull RowType row) {
        return database.rows().updateWeather(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the value of RowEntity.weather for the given attack row.
     * @see .isWeatherFlowable(RowType)
     * @param row RowEntity.id where the weather is queried.
     * @return A Single tracking operation status and returning the value.
     */
    @NonNull
    public Single<Boolean> isWeather(@NonNull RowType row) {
        return database.rows().isWeather(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the latest value of RowEntity.weather for the given attack row.
     *
     * @param row RowEntity.id where the weather is queried.
     * @return A Flowable emitting the values.
     * @see .isWeather(RowType)
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
     * Sets RowEntity.weather to `false` for all attack rows.
     *
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable clearWeather() {
        return database.rows().clearWeather().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Clears Mardroeme from all attack rows without changing horn buffs or weather.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable clearMardroeme() {
        return database.rows().clearMardroeme()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Toggles RowEntity.buffState between RowBuffState.NONE and RowBuffState.HORN for the given
     * attack row. If Mardroeme is active, only clears it to NONE; a second call activates HORN.
     * @param row RowEntity.id where the horn status should be updated.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable switchHorn(@NonNull RowType row) {
        return database
                .rows()
                .getBuffState(row)
                .map(rowBuffState -> rowBuffState == RowBuffState.NONE ? RowBuffState.HORN : RowBuffState.NONE)
                .flatMapCompletable(rowBuffState -> database.rows().updateBuffState(row, rowBuffState));
    }

    /**
     * Toggles RowEntity.buffState between RowBuffState.NONE and RowBuffState.MARDROEME.
     * If the horn is active, only clears it to NONE; a second call activates MARDROEME.
     * Activating Mardroeme implicitly transforms this row's Berserkers and notifies the user.
     * @param row RowEntity.id where the Mardroeme status should be updated.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable switchMardroeme(@NonNull RowType row) {
        return database.rows().getBuffState(row)
                .map(state -> state == RowBuffState.NONE ? RowBuffState.MARDROEME : RowBuffState.NONE)
                .flatMapCompletable(state -> database.rows().updateBuffState(row, state)
                        .andThen(Completable.defer(() -> state == RowBuffState.MARDROEME
                                ? TransformBerserkersUseCase.transformRow(context, this, row)
                                : Completable.complete())))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the persisted buff state for the given attack row.
     *
     * @param row RowEntity.id where the buff state is queried.
     * @return A Single emitting NONE, HORN, or MARDROEME.
     * @see .getBuffStateFlowable(RowType)
     */
    @NonNull
    public Single<RowBuffState> getBuffState(@NonNull RowType row) {
        return database.rows().getBuffState(row)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the persisted buff state for the given attack row.
     *
     * @param row RowEntity.id where the buff state is queried.
     * @return A Flowable emitting distinct NONE, HORN, or MARDROEME values.
     * @see .getBuffState(RowType)
     */
    @NonNull
    public Flowable<RowBuffState> getBuffStateFlowable(@NonNull RowType row) {
        return database.rows().getBuffStateFlowable(row)
                .onBackpressureLatest()
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns whether RowEntity.buffState is RowBuffState.HORN for the given attack row.
     * @see .isHornFlowable(RowType)
     * @param row RowEntity.id where the horn status is queried.
     * @return A Single emitting true for HORN and false for NONE or MARDROEME.
     */
    @NonNull
    public Single<Boolean> isHorn(@NonNull RowType row) {
        return database
                .rows()
                .getBuffState(row)
                .map(rowBuffState -> rowBuffState == RowBuffState.HORN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting whether RowEntity.buffState is RowBuffState.HORN for the given attack row.
     *
     * @param row RowEntity.id where the horn status is queried.
     * @return A Flowable emitting distinct Boolean values: true for HORN, false otherwise.
     * @see .isHorn(RowType)
     */
    @NonNull
    public Flowable<Boolean> isHornFlowable(@NonNull RowType row) {
        return database
                .rows()
                .getBuffStateFlowable(row)
                .onBackpressureLatest()
                .map(rowBuffState -> rowBuffState == RowBuffState.HORN)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Removes the given units from the game.
     * @param units List of units to be removed.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable delete(@NonNull Collection<UnitEntity> units) {
        return database.units().deleteUnits(units).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Removes the unit with the given id from the game.
     *
     * @param id Integer representing the unit that should be deleted.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable delete(int id) {
        return database.units().deleteUnit(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Copies the unit with the given id.
     * @param id Integer representing the unit that should be copied.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable copy(int id) {
        return getUnit(id).flatMapCompletable(unit -> insertUnits(Collections.singletonList(unit.copy())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Counts the units in the given attack row.
     * @param row RowEntity.id where the units are counted.
     * @return A Single tracking operation status and returning the value.
     * @see .countUnits()
     */
    @NonNull
    public Single<Integer> countUnits(@NonNull RowType row) {
        return database.units().countUnits(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Counts the units in all attack rows.
     *
     * @return A Single tracking operation status and returning the value.
     * @see .countUnits(RowType)
     */
    @NonNull
    public Single<Integer> countUnits() {
        return database.units().countUnits().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the unit with the given id.
     *
     * @param id Integer representing the queried unit.
     * @return A Single tracking operation status and returning the value.
     */
    @NonNull
    public Single<UnitEntity> getUnit(int id) {
        return database.units().getUnit(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns persisted units matching the supplied IDs.
     *
     * @param ids IDs to retrieve; missing IDs are omitted.
     * @return A Single emitting matching units, or an empty list for an empty ID list.
     */
    @NonNull
    public Single<List<UnitEntity>> getUnitsByIds(@NonNull List<Long> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return database.units().getUnitsByIds(ids)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the units in the given attack row.
     * @see .getUnitsFlowable(RowType)
     * @param row RowEntity.id where the units have been placed.
     * @return A Single tracking operation status and returning the value.
     * @see .getUnits()
     */
    @NonNull
    public Single<List<UnitEntity>> getUnits(@NonNull RowType row) {
        return database.units().getUnits(row).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting the units in the given attack row.
     *
     * @param row RowEntity.id where the units have been placed.
     * @return A Flowable emitting the values.
     * @see .getUnits(RowType)
     * @see .getUnits()
     */
    @NonNull
    public Flowable<List<UnitEntity>> getUnitsFlowable(@NonNull RowType row) {
        return database.units()
                .getUnitsFlowable(row)
                .onBackpressureLatest()
                .debounce(10, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns the units in the given attack row.
     *
     * @return A Single tracking operation status and returning the value.
     * @see .getUnits(RowType)
     */
    @NonNull
    public Single<List<UnitEntity>> getUnits() {
        return database.units().getUnits().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns a Flowable emitting Boolean values defining whether there are units in the game that are not epic.
     * @return A Flowable emitting the values.
     */
    @NonNull
    public Flowable<Boolean> hasNonEpicUnitsFlowable() {
        return database.units()
                .hasNonEpicUnitsFlowable()
                .onBackpressureLatest()
                .debounce(10, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
