package com.peternaggschga.gwent.ui.main;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;
import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_BUTTON_CLICK;
import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_FACTION_SWITCH;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.BurnDialogUseCase;
import com.peternaggschga.gwent.domain.cases.ResetDialogUseCase;
import com.peternaggschga.gwent.domain.cases.RowStateUseCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * A ViewModel class responsible for encapsulating and offering state of views in activity_main.xml,
 * i.e., that show the overall game board.
 * Click events on the rows and the menu are handled also.
 */
public class GameBoardViewModel extends ViewModel {
    /**
     * ViewModelInitializer used by androidx.lifecycle.ViewModelProvider.Factory to instantiate the class.
     *
     * @see androidx.lifecycle.ViewModelProvider.Factory#from(ViewModelInitializer[])
     */
    @NonNull
    public static final ViewModelInitializer<GameBoardViewModel> initializer = new ViewModelInitializer<>(
            GameBoardViewModel.class,
            creationExtras -> {
                GwentApplication app = (GwentApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new GameBoardViewModel(app.getRepository().blockingGet());
            });

    /**
     * UnitRepository that is used to read and update state.
     */
    @NonNull
    private final UnitRepository repository;
    /**
     * A map structure containing the MutableLiveData objects emitting the RowUiState for each row.
     * MutableLiveData objects are lazily initialized when queried using #getMutableRowUiState().
     * @see #getMutableRowUiState(RowType)
     * @see RowUiState
     */
    @NonNull
    private final Map<RowType, MutableLiveData<RowUiState>> rowUiStates = new HashMap<>();
    /**
     * MediatorLiveData emitting the MenuUiState for the right-hand side menu.
     * Is lazily initialized when queried using #getMenuUiState() and can therefore be null.
     * @see #getMenuUiState()
     * @see MenuUiState
     */
    private MediatorLiveData<MenuUiState> menuUiState;

    /**
     * Constructor of a GameBoardViewModel object.
     * Should only be called in #initializer.
     *
     * @param repository UnitRepository that is used to read and update state.
     * @see #initializer
     */
    private GameBoardViewModel(@NonNull UnitRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns a MutableLiveData object emitting RowUiState for the given row.
     * Lazily initializes entries of #rowUiStates.
     * @param row RowType defining the row for which the state is queried.
     * @return A MutableLiveData object for the state of the given row.
     * @see RowUiState
     * @see #rowUiStates
     * @see #getRowUiState(RowType)
     */
    @NonNull
    private MutableLiveData<RowUiState> getMutableRowUiState(@NonNull RowType row) {
        rowUiStates.putIfAbsent(row, new MutableLiveData<>());
        return Objects.requireNonNull(rowUiStates.get(row));
    }

    /**
     * Returns a LiveData object emitting RowUiState for the given row.
     * @param row RowType defining the row for which the state is queried.
     * @return A LiveData object for the state of the given row.
     * @see RowUiState
     * @see #getMutableRowUiState(RowType)
     */
    @NonNull
    public LiveData<RowUiState> getRowUiState(@NonNull RowType row) {
        return getMutableRowUiState(row);
    }

    /**
     * Returns a LiveData object emitting MenuUiState.
     * Lazily initializes #menuUiState.
     *
     * @return A LiveData object for the state of the menu.
     * @see MenuUiState
     * @see #menuUiState
     */
    @NonNull
    public LiveData<MenuUiState> getMenuUiState() {
        if (menuUiState == null) {
            menuUiState = new MediatorLiveData<>();
            // initialize row ui states if not yet done so
            Arrays.stream(RowType.values())
                    .filter(rowType -> !rowUiStates.containsKey(rowType))
                    .forEach(this::getRowUiState);
            // add rowUiStates as sources
            for (RowType row : RowType.values()) {
                menuUiState.addSource(getRowUiState(row),
                        MenuUpdateObserver.getObserver(row, menuUiState, rowUiStates));
            }
        }
        return menuUiState;
    }

    /**
     * Updates all the state associated with the main view, i.e., rows and menu.
     * Uses #updateUi(RowType).
     * @see #updateUi(RowType)
     * @return A Completable tracking operation status.
     */
    @NonNull
    public Completable updateUi() {
        Completable result = Completable.complete();
        for (RowType row : RowType.values()) {
            result = result.andThen(updateUi(row));
        }
        return result;
    }

    /**
     * Updates the state associated with the given row.
     * @param row RowType defining the updated row.
     * @return A Completable tracking operation status.
     */
    @NonNull
    private Completable updateUi(@NonNull RowType row) {
        return RowStateUseCase.getRowState(repository, row)
                .doOnSuccess(rowUiState -> {
                    MutableLiveData<RowUiState> rowState = getMutableRowUiState(row);
                    if (!rowUiState.equals(rowState.getValue())) {
                        rowState.postValue(rowUiState);
                    }
                }).ignoreElement();
    }

    /**
     * Updates the weather debuff of the given row.
     * Flips between good and bad weather.
     * @param row RowType defining the effected row.
     * @return A Single emitting a Boolean defining the weather status of the row after the operation.
     * @see UnitRepository#switchWeather(RowType)
     */
    @NonNull
    public Single<Boolean> onWeatherViewPressed(@NonNull RowType row) {
        return repository.switchWeather(row)
                .andThen(updateUi(row))
                .andThen(repository.isWeather(row));
    }

    /**
     * Updates the horn buff of the given row.
     * Flips between on and off.
     * @param row RowType defining the effected row.
     * @return A Single emitting a Boolean defining the horn status of the row after the operation.
     * @see UnitRepository#switchHorn(RowType)
     */
    public Single<Boolean> onHornViewPressed(@NonNull RowType row) {
        return repository.switchHorn(row)
                .andThen(updateUi(row))
                .andThen(repository.isHorn(row));
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * Should only be called by the button's View.OnClickListener.
     * Wrapper for #reset().
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see #reset(Context, int)
     * @see ResetDialogUseCase#TRIGGER_BUTTON_CLICK
     */
    @NonNull
    public Single<Boolean> onResetButtonPressed(@NonNull Context context) {
        return reset(context, TRIGGER_BUTTON_CLICK);
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * Should only be called when the faction has been switched.
     * Wrapper for #reset().
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see #reset(Context, int)
     * @see ResetDialogUseCase#TRIGGER_FACTION_SWITCH
     */
    @NonNull
    public Single<Boolean> onFactionSwitchReset(@NonNull Context context) {
        return reset(context, TRIGGER_FACTION_SWITCH);
    }

    /**
     * Triggers a reset and possibly an alert dialog, depending on preferences.
     * @param context Context object used to acquire SharedPreferences and inflate Dialog views.
     * @param trigger Integer defining which action triggered the reset.
     * @return A Single emitting a Boolean defining whether a reset was actually conducted.
     * @see ResetDialogUseCase#reset(Context, UnitRepository, int)
     * @see ResetDialogUseCase#TRIGGER_BUTTON_CLICK
     * @see ResetDialogUseCase#TRIGGER_FACTION_SWITCH
     */
    @NonNull
    private Single<Boolean> reset(@NonNull Context context,
                                  @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        return ResetDialogUseCase.reset(context, repository, trigger)
                .flatMap(resetComplete -> updateUi().andThen(Single.just(resetComplete)));
    }

    /**
     * Clears all weather effects.
     * Should only be called by the View.OnClickListener of the weather button.
     * @return A Completable tracking operation status.
     * @see UnitRepository#clearWeather()
     */
    @NonNull
    public Completable onWeatherButtonPressed() {
        return repository.clearWeather()
                .andThen(updateUi());
    }

    /**
     * Clears units with the most damage.
     * May inflate a warning dialog depending on the user's preferences and a Toast
     * informing the user about the burned units.
     * Should only be called by the View.OnClickListener of the burn button.
     * @param context Context
     * @return A Single emitting a Boolean defining whether the units were actually removed.
     * @see BurnDialogUseCase#burn(Context, UnitRepository)
     */
    @NonNull
    public Single<Boolean> onBurnButtonPressed(@NonNull Context context) {
        return BurnDialogUseCase.burn(context, repository)
                .flatMap(burnComplete -> updateUi().andThen(Single.just(burnComplete)));
    }
}
