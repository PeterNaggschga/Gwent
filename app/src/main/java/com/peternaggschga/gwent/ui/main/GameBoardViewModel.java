package com.peternaggschga.gwent.ui.main;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.BurnUnitsUseCase;
import com.peternaggschga.gwent.domain.cases.RowStateUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GameBoardViewModel extends ViewModel {
    public static final ViewModelInitializer<GameBoardViewModel> initializer = new ViewModelInitializer<>(
            GameBoardViewModel.class,
            creationExtras -> {
                GwentApplication app = (GwentApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new GameBoardViewModel(app.getRepository());
            });

    @NonNull
    private final UnitRepository repository;
    @NonNull
    private final Map<RowType, MutableLiveData<RowUiState>> rowUiStates = new HashMap<>();
    private MediatorLiveData<MenuUiState> menuUiState;

    private GameBoardViewModel(@NonNull UnitRepository repository) {
        this.repository = repository;
    }

    private static void runOnUiThread(@NonNull Completable toBeRun) {
        toBeRun.subscribeOn(Schedulers.io()).subscribe();
    }

    @NonNull
    public MutableLiveData<RowUiState> getRowUiState(@NonNull RowType row) {
        rowUiStates.putIfAbsent(row, new MutableLiveData<>());
        return Objects.requireNonNull(rowUiStates.get(row));
    }

    @NonNull
    public MutableLiveData<MenuUiState> getMenuUiState() {
        if (menuUiState == null) {
            menuUiState = new MediatorLiveData<>();
            // initialize observers in rowUiStates if not yet done so
            if (rowUiStates.size() < RowType.values().length) {
                getRowUiState(RowType.MELEE);
                getRowUiState(RowType.RANGE);
                getRowUiState(RowType.SIEGE);
            }
            // add rowUiStates as sources
            for (RowType row : RowType.values()) {
                menuUiState.addSource(getRowUiState(row),
                        MenuUpdateObserver.getObserver(row, menuUiState, rowUiStates));
            }
        }
        return menuUiState;
    }

    public void updateUi() {
        runOnUiThread(updateUiState());
    }

    @NonNull
    private Completable updateUiState() {
        Completable result = Completable.complete();
        for (RowType row : RowType.values()) {
            result = result.andThen(updateUiState(row));
        }
        return result;
    }

    @NonNull
    private Completable updateUiState(@NonNull RowType row) {
        return Completable.create(emitter -> {
            MutableLiveData<RowUiState> rowState = getRowUiState(row);
            RowUiState state = RowStateUseCase.getRowState(repository, row).blockingGet();
            if (!state.equals(rowState.getValue())) {
                rowState.postValue(state);
            }
            emitter.onComplete();
        });

    }

    public void onWeatherViewPressed(@NonNull RowType row) {
        runOnUiThread(repository.switchWeather(row)
                .andThen(updateUiState(row)));
    }

    public void onHornViewPressed(@NonNull RowType row) {
        runOnUiThread(repository.switchHorn(row)
                .andThen(updateUiState(row)));
    }

    public void onResetButtonPressed() {
        // TODO: Warnung
        runOnUiThread(repository.reset()
                .andThen(updateUiState()));
    }

    public void onWeatherButtonPressed() {
        runOnUiThread(repository.clearWeather()
                .andThen(updateUiState()));
    }

    public void onBurnButtonPressed() {
        // TODO: Warnung
        runOnUiThread(new BurnUnitsUseCase(repository)
                .removeBurnUnits()
                .andThen(updateUiState()));
    }
}
