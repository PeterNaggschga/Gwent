package com.peternaggschga.gwent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.ui.dialogs.ChangeFactionDialog;
import com.peternaggschga.gwent.ui.main.FactionSwitchListener;
import com.peternaggschga.gwent.ui.main.GameBoardViewModel;
import com.peternaggschga.gwent.ui.main.MenuUiStateObserver;
import com.peternaggschga.gwent.ui.main.RowUiStateObserver;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private SoundManager soundManager;
    private GameBoardViewModel gameBoard;
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener factionSwitchListener;
    private boolean resetOnFactionSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(FactionSwitchListener.THEME_PREFERENCE_KEY,
                        FactionSwitchListener.THEME_SCOIATAEL)) {
            case FactionSwitchListener.THEME_MONSTER:
                setTheme(R.style.MonsterTheme);
                break;
            case FactionSwitchListener.THEME_NILFGAARD:
                setTheme(R.style.NilfgaardTheme);
                break;
            case FactionSwitchListener.THEME_NORTHERN_KINGDOMS:
                setTheme(R.style.NorthernKingdomsTheme);
                break;
            case FactionSwitchListener.THEME_SCOIATAEL:
                setTheme(R.style.ScoiataelTheme);
        }

        setContentView(R.layout.activity_main);

        soundManager = new SoundManager(this);

        Schedulers.io().scheduleDirect(() -> {
            gameBoard = new ViewModelProvider(MainActivity.this,
                    ViewModelProvider.Factory.from(GameBoardViewModel.initializer)
            ).get(GameBoardViewModel.class);
            new Handler(Looper.getMainLooper()).post(MainActivity.this::initializeViewModel);
        });

        factionSwitchListener = FactionSwitchListener.getFactionSwitchObserver(getWindow());
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(factionSwitchListener);

        findViewById(R.id.factionButton).setOnClickListener(v -> inflateFactionPopup());
        findViewById(R.id.coinButton).setOnClickListener(v -> inflateCoinFlipPopup());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetOnFactionSwitch = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.preference_key_faction_reset),
                        getResources().getBoolean(R.bool.faction_reset_preference_default));
    }

    private void initializeViewModel() {
        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int rowId = 0; rowId < rowIds.length; rowId++) {
            RowType row = RowType.values()[rowId];
            ConstraintLayout rowLayout = findViewById(rowIds[rowId]);

            ImageView weather = rowLayout.findViewById(R.id.weatherView);
            ImageView horn = rowLayout.findViewById(R.id.hornView);

            weather.setOnClickListener(v -> {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                gameBoard.onWeatherViewPressed(row).subscribe(aBoolean -> {
                    if (aBoolean) {
                        soundManager.playWeatherSound(row);
                    }
                });
            });
            horn.setOnClickListener(v -> {
                gameBoard.onHornViewPressed(row).subscribe();
                soundManager.playHornSound();
            });

            final RowUiStateObserver observer = RowUiStateObserver.getObserver(row,
                    rowLayout.findViewById(R.id.pointView),
                    weather,
                    horn,
                    rowLayout.findViewById(R.id.cardCountView));
            gameBoard.getRowUiState(row).observe(this, observer);
        }

        ImageButton reset = findViewById(R.id.resetButton);
        ImageButton weather = findViewById(R.id.weatherButton);
        ImageButton burn = findViewById(R.id.burnButton);

        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView),
                reset,
                weather,
                burn);
        gameBoard.getMenuUiState().observe(this, observer);

        reset.setOnClickListener(v -> {
            // noinspection CheckResult, ResultOfMethodCallIgnored
            gameBoard.onResetButtonPressed().subscribe(aBoolean -> {
                if (aBoolean) {
                    soundManager.playResetSound();
                }
            });
        });
        weather.setOnClickListener(v -> {
            gameBoard.onWeatherButtonPressed().subscribe();
            soundManager.playClearWeatherSound();
        });
        burn.setOnClickListener(v -> {
            // noinspection CheckResult, ResultOfMethodCallIgnored
            gameBoard.onBurnButtonPressed().subscribe(aBoolean -> {
                if (aBoolean) {
                    soundManager.playBurnSound();
                }
            });
        });

        gameBoard.updateUi().subscribe();
    }

    private void inflateFactionPopup() {
        // TODO reset
        // TODO warnung
        new ChangeFactionDialog(this, theme -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (resetOnFactionSwitch) {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                gameBoard.onResetButtonPressed().subscribe(aBoolean -> preferences.edit()
                        .putInt(FactionSwitchListener.THEME_PREFERENCE_KEY, theme)
                        .apply());
            } else {
                preferences.edit()
                        .putInt(FactionSwitchListener.THEME_PREFERENCE_KEY, theme)
                        .apply();
            }
        }).show();
    }

    private void inflateCoinFlipPopup() {
        soundManager.playCoinSound();
        Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_SHORT).show();
        // TODO
    }
}
