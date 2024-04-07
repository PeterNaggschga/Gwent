package com.peternaggschga.gwent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.ui.dialogs.ChangeFactionDialog;
import com.peternaggschga.gwent.ui.dialogs.CoinFlipDialog;
import com.peternaggschga.gwent.ui.dialogs.cards.ShowUnitsDialog;
import com.peternaggschga.gwent.ui.main.FactionSwitchListener;
import com.peternaggschga.gwent.ui.main.GameBoardViewModel;
import com.peternaggschga.gwent.ui.main.MenuUiStateObserver;
import com.peternaggschga.gwent.ui.main.RowUiStateObserver;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

/**
 * @todo Documentation
 */
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

        // noinspection CheckResult, ResultOfMethodCallIgnored
        ((GwentApplication) getApplicationContext()).getRepository()
                .map(repository -> GameBoardViewModel.getModel(MainActivity.this, repository))
                .subscribe(gameBoardViewModel -> {
                    gameBoard = gameBoardViewModel;
                    initializeViewModel();
                });

        factionSwitchListener = FactionSwitchListener.getListener(getWindow());
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
            ConstraintLayout cards = rowLayout.findViewById(R.id.cardView);

            weather.setOnClickListener(v -> {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                gameBoard.onWeatherViewPressed(row).subscribe(aBoolean -> {
                    if (aBoolean) {
                        soundManager.playWeatherSound(row);
                    }
                });
            });
            horn.setOnClickListener(v -> {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                gameBoard.onHornViewPressed(row).subscribe(aBoolean -> {
                    if (aBoolean) {
                        soundManager.playHornSound();
                    }
                });
            });
            cards.setOnClickListener(v -> new ShowUnitsDialog(MainActivity.this, row).show());

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
            gameBoard.onResetButtonPressed(this)
                    .subscribe(playSound -> {
                        if (playSound) {
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
            gameBoard.onBurnButtonPressed(MainActivity.this).subscribe(aBoolean -> {
                if (aBoolean) {
                    soundManager.playBurnSound();
                }
            });
        });
        gameBoard.update().subscribe();
    }

    private void inflateFactionPopup() {
        new ChangeFactionDialog(this, theme -> {
            if (resetOnFactionSwitch) {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                gameBoard.onFactionSwitchReset(this)
                        .subscribe(playSound -> {
                            if (playSound) {
                                soundManager.playResetSound();
                            }
                        });
            }
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putInt(FactionSwitchListener.THEME_PREFERENCE_KEY, theme)
                    .apply();
        }).show();
    }

    private void inflateCoinFlipPopup() {
        new CoinFlipDialog(this).show();
        soundManager.playCoinSound();
    }
}
