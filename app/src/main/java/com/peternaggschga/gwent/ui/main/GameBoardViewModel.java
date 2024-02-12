package com.peternaggschga.gwent.ui.main;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.BurnUnitsUseCase;
import com.peternaggschga.gwent.domain.cases.RowStateUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GameBoardViewModel extends ViewModel {
    public static final ViewModelInitializer<GameBoardViewModel> initializer = new ViewModelInitializer<>(
            GameBoardViewModel.class,
            creationExtras -> {
                GwentApplication app = (GwentApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new GameBoardViewModel(app.getApplicationContext(), app.getRepository());
            });

    @NonNull
    private final UnitRepository repository;
    @NonNull
    private final Map<RowType, MutableLiveData<RowUiState>> rowUiStates = new HashMap<>();
    private MediatorLiveData<MenuUiState> menuUiState;
    @NonNull
    @SuppressWarnings("FieldCanBeLocal")
    private final SharedPreferences.OnSharedPreferenceChangeListener changeListener;
    private boolean showWarnings;

    private GameBoardViewModel(@NonNull Context context, @NonNull UnitRepository repository) {
        this.repository = repository;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String warningPrefKey = context.getString(R.string.preference_key_warning);
        showWarnings = pref.getBoolean(warningPrefKey, context.getResources().getBoolean(R.bool.warning_preference_default));
        changeListener = (sharedPreferences, key) -> {
            if (warningPrefKey.equals(key)) {
                showWarnings = sharedPreferences.getBoolean(key, showWarnings);
            }
        };
        pref.registerOnSharedPreferenceChangeListener(changeListener);
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

    public Completable updateUi() {
        return updateUiState().subscribeOn(Schedulers.io());
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

    @NonNull
    public Single<Boolean> onWeatherViewPressed(@NonNull RowType row) {
        return repository.switchWeather(row)
                .andThen(updateUiState(row))
                .andThen(repository.isWeather(row))
                .subscribeOn(Schedulers.io());
    }

    public Completable onHornViewPressed(@NonNull RowType row) {
        return repository.switchHorn(row)
                .andThen(updateUiState(row))
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> onResetButtonPressed() {
        // TODO: Warnung
        return repository.reset()
                .andThen(updateUiState())
                .andThen(Single.just(true))
                .subscribeOn(Schedulers.io());
    }

    public Completable onWeatherButtonPressed() {
        return repository.clearWeather()
                .andThen(updateUiState())
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> onBurnButtonPressed() {
        // TODO: Warnung
        return new BurnUnitsUseCase(repository)
                .removeBurnUnits()
                .andThen(updateUiState())
                .andThen(Single.just(true))
                .subscribeOn(Schedulers.io());
    }
}
