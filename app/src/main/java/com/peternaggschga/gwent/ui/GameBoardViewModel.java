package com.peternaggschga.gwent.ui;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.RowStateUseCase;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;

public class GameBoardViewModel extends ViewModel {
    public static final ViewModelInitializer<GameBoardViewModel> initializer = new ViewModelInitializer<>(
            GameBoardViewModel.class,
            creationExtras -> {
                GwentApplication app = (GwentApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new GameBoardViewModel(app.getRepository());
            });

    private final UnitRepository repository;
    private final Map<RowType, MutableLiveData<RowUiState>> rowUiStates = new HashMap<>();
    private MediatorLiveData<MenuUiState> menuUiState;

    private GameBoardViewModel(UnitRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<RowUiState> getRowUiState(@NonNull RowType row) {
        if (!rowUiStates.containsKey(row)) {
            return rowUiStates.put(row, new MutableLiveData<>());
        }
        return rowUiStates.get(row);
    }

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

    public Completable updateUiState() {
        Completable result = Completable.complete();
        for (RowType row : RowType.values()) {
            result = result.andThen(updateUiState(row));
        }
        return result;
    }

    public Completable updateUiState(@NonNull RowType row) {
        return Completable.create(emitter -> {
            getRowUiState(row).postValue(RowStateUseCase.getRowState(repository, row).blockingGet());
            emitter.onComplete();
        });

    }

    public Completable onWeatherViewPressed(@NonNull RowType row) {
        return repository.switchWeather(row)
                .andThen(updateUiState(row));
    }

    public Completable onHornViewPressed(@NonNull RowType row) {
        return repository.switchHorn(row)
                .andThen(updateUiState(row));
    }

    public Completable onResetButtonPressed() {
        // TODO: Warnung
        return repository.reset()
                .andThen(updateUiState());
    }

    public Completable onWeatherButtonPressed() {
        return repository.clearWeather()
                .andThen(updateUiState());
    }

    public void onBurnButtonPressed() {
        // TODO: Warnung
        // TODO: Ausführung
        // TODO: State updaten
    }
}
