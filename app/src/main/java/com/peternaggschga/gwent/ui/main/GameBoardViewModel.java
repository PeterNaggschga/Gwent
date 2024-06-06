package com.peternaggschga.gwent.ui.main;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.BurnDialogUseCase;
import com.peternaggschga.gwent.domain.cases.DamageCalculatorUseCase;
import com.peternaggschga.gwent.domain.cases.ResetDialogUseCase;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * An AndroidViewModel class responsible for encapsulating
 * and offering state of views in activity_main.xml, i.e., that show the overall game board.
 * Click events on the rows and the menu are handled also.
 */
public class GameBoardViewModel extends AndroidViewModel {
    /**
     * ViewModelInitializer used by androidx.lifecycle.ViewModelProvider.Factory to instantiate the class.
     *
     * @see androidx.lifecycle.ViewModelProvider.Factory#from(ViewModelInitializer[])
     */
    @NonNull
    private static final ViewModelInitializer<GameBoardViewModel> INITIALIZER = new ViewModelInitializer<>(
            GameBoardViewModel.class,
            creationExtras -> {
                GwentApplication app = (GwentApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new GameBoardViewModel(app);
            });

    /**
     * Constructor of a GameBoardViewModel object.
     * Should only be called in #initializer.
     *
     * @param application GwentApplication that uses this AndroidViewModel.
     * @see #INITIALIZER
     */
    private GameBoardViewModel(@NonNull GwentApplication application) {
        super(application);
    }

    /**
     * A map structure containing the Flowable objects emitting the RowUiState for each row.
     * Initialized in #getModel().
     * @see #getRowUiState(RowType)
     * @see RowUiState
     */
    @NonNull
    private final Map<RowType, Flowable<RowUiState>> rowUiStates = new HashMap<>(RowType.values().length);

    /**
     * Flowable emitting the MenuUiState for the right-hand side menu.
     * Initialized in #getModel().
     * @see #getMenuUiState()
     * @see MenuUiState
     */
    @NonNull
    private Flowable<MenuUiState> menuUiState = Flowable.empty();

    /**
     * Factory method of a GameBoardViewModel.
     * Creates a new GameBoardViewModel for the given owner and initializes #rowUiStates and #menuUiState.
     *
     * @param owner      ViewModelStoreOwner instantiating the GameBoardViewModel.
     * @param repository UnitRepository where Flowables are retrieved.
     * @return A new GameBoardViewModel instance.
     * @see ViewModelProvider#ViewModelProvider(ViewModelStoreOwner, ViewModelProvider.Factory)
     */
    @NonNull
    public static GameBoardViewModel getModel(@NonNull ViewModelStoreOwner owner,
                                              @NonNull UnitRepository repository) {
        GameBoardViewModel result = new ViewModelProvider(owner, ViewModelProvider.Factory.from(INITIALIZER))
                .get(GameBoardViewModel.class);

        for (RowType row : RowType.values()) {
            result.rowUiStates.put(row,
                    Flowable.combineLatest(repository.isWeatherFlowable(row),
                            repository.isHornFlowable(row),
                            repository.getUnitsFlowable(row),
                            (weather, horn, units) -> {
                                DamageCalculator calculator = DamageCalculatorUseCase.getDamageCalculator(weather, horn, units);
                                int damage = units.stream()
                                        .map(unitEntity -> unitEntity.calculateDamage(calculator))
                                        .reduce(0, Integer::sum);
                                return new RowUiState(damage, weather, horn, units.size());
                            })
                            .distinctUntilChanged()
                            .onBackpressureLatest()
                            .debounce(10, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
            );
        }

        result.menuUiState = Flowable.combineLatest(result.rowUiStates.values(), (Object[] rowUiStates) -> {
            int damage = 0;
            boolean reset = false;
            boolean weather = false;
            boolean burn = false;
            for (Object state : rowUiStates) {
                RowUiState rowUiState = (RowUiState) state;
                damage += rowUiState.getDamage();
                reset |= rowUiState.isHorn();
                weather |= rowUiState.isWeather();
                burn |= rowUiState.getUnits() != 0;
            }
            reset |= weather || burn;
            return new MenuUiState(damage, reset, weather, burn);
        }).distinctUntilChanged().onBackpressureLatest();

        return result;
    }

    /**
     * Returns the UnitRepository used by the parent GwentApplication.
     * Basically a wrapper for GwentApplication#getRepository(Context).
     *
     * @return A Single emitting the UnitRepository instance.
     * @see GwentApplication#getRepository(Context)
     */
    private Single<UnitRepository> getRepository() {
        return GwentApplication.getRepository(getApplication());
    }

    /**
     * Returns a Flowable object emitting RowUiState for the given row.
     * @param row RowType defining the row for which the state is queried.
     * @return A Flowable object for the state of the given row.
     * @see RowUiState
     * @see #rowUiStates
     */
    @NonNull
    public Flowable<RowUiState> getRowUiState(@NonNull RowType row) {
        return Objects.requireNonNull(rowUiStates.get(row));
    }

    /**
     * Returns a Flowable object emitting MenuUiState.
     *
     * @return A Flowable object for the state of the menu.
     * @see MenuUiState
     * @see #menuUiState
     */
    @NonNull
    public Flowable<MenuUiState> getMenuUiState() {
        return menuUiState;
    }

    /**
     * Updates the weather debuff of the given row.
     * Flips between good and bad weather.
     * @param row RowType defining the affected row.
     * @return A Single emitting a Boolean defining the weather status of the row after the operation.
     * @see UnitRepository#switchWeather(RowType)
     */
    @NonNull
    public Single<Boolean> onWeatherViewPressed(@NonNull RowType row) {
        return getRepository().flatMap(repository ->
                repository.switchWeather(row)
                        .andThen(repository.isWeather(row)));
    }

    /**
     * Updates the horn buff of the given row.
     * Flips between on and off.
     * @param row RowType defining the affected row.
     * @return A Single emitting a Boolean defining the horn status of the row after the operation.
     * @see UnitRepository#switchHorn(RowType)
     */
    public Single<Boolean> onHornViewPressed(@NonNull RowType row) {
        return getRepository().flatMap(repository ->
                repository.switchHorn(row)
                        .andThen(repository.isHorn(row)));
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * Should only be called by the button's View.OnClickListener.
     * Wrapper for #reset().
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see #reset(Context, ResetDialogUseCase.Trigger)
     * @see ResetDialogUseCase.Trigger#BUTTON_CLICK
     */
    @NonNull
    public Single<Boolean> onResetButtonPressed(@NonNull Context context) {
        return reset(context, ResetDialogUseCase.Trigger.BUTTON_CLICK);
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * Should only be called when the faction has been switched.
     * Wrapper for #reset().
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see #reset(Context, ResetDialogUseCase.Trigger)
     */
    @NonNull
    public Single<Boolean> onFactionSwitchReset(@NonNull Context context) {
        return reset(context, ResetDialogUseCase.Trigger.FACTION_SWITCH);
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @param trigger {@link com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.Trigger} defining which action triggered the reset.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see #reset(Context, ResetDialogUseCase.Trigger)
     */
    @NonNull
    private Single<Boolean> reset(@NonNull Context context, @NonNull ResetDialogUseCase.Trigger trigger) {
        return ResetDialogUseCase.reset(context, trigger);
    }

    /**
     * Clears all weather effects.
     * Should only be called by the View.OnClickListener of the weather button.
     * @return A Completable tracking operation status.
     * @see UnitRepository#clearWeather()
     */
    @NonNull
    public Completable onWeatherButtonPressed() {
        return getRepository().flatMapCompletable(UnitRepository::clearWeather);
    }

    /**
     * Clears units with the most damage.
     * May inflate a warning dialog depending on the user's preferences and a Toast
     * informing the user about the burned units.
     * Should only be called by the View.OnClickListener of the burn button.
     * @param context Context
     * @return A Single emitting a Boolean defining whether the units were actually removed.
     * @see BurnDialogUseCase#burn(Context)
     */
    @NonNull
    public Single<Boolean> onBurnButtonPressed(@NonNull Context context) {
        return BurnDialogUseCase.burn(context);
    }
}
