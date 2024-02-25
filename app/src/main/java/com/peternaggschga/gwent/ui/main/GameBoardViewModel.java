package com.peternaggschga.gwent.ui.main;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;
import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_BUTTON_CLICK;
import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_FACTION_SWITCH;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.IntRange;
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
import com.peternaggschga.gwent.domain.cases.BurnDialogUseCase;
import com.peternaggschga.gwent.domain.cases.ResetDialogUseCase;
import com.peternaggschga.gwent.domain.cases.RowStateUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

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
        return updateUiState();
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
            // noinspection CheckResult, ResultOfMethodCallIgnored
            RowStateUseCase.getRowState(repository, row)
                    .subscribe(rowUiState -> {
                        if (!rowUiState.equals(rowState.getValue())) {
                            rowState.postValue(rowUiState);
                        }
                        emitter.onComplete();
                    });
        });

    }

    @NonNull
    public Single<Boolean> onWeatherViewPressed(@NonNull RowType row) {
        return repository.switchWeather(row)
                .andThen(updateUiState(row))
                .andThen(repository.isWeather(row));
    }

    public Single<Boolean> onHornViewPressed(@NonNull RowType row) {
        return repository.switchHorn(row)
                .andThen(updateUiState(row))
                .andThen(repository.isHorn(row));
    }

    @NonNull
    public Single<Boolean> onResetButtonPressed(@NonNull Context context) {
        return reset(context, TRIGGER_BUTTON_CLICK);
    }

    @NonNull
    public Single<Boolean> onFactionSwitchReset(@NonNull Context context) {
        return reset(context, TRIGGER_FACTION_SWITCH);
    }

    @NonNull
    private Single<Boolean> reset(@NonNull Context context,
                                  @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        return ResetDialogUseCase.reset(context, repository, trigger);
    }

    public Completable onWeatherButtonPressed() {
        return repository.clearWeather()
                .andThen(updateUiState());
    }

    public Single<Boolean> onBurnButtonPressed(@NonNull Context context) {
        // TODO: Warnung
        return new BurnDialogUseCase(context, repository)
                .removeBurnUnits()
                .andThen(updateUiState())
                .andThen(Single.just(true));
    }
}
