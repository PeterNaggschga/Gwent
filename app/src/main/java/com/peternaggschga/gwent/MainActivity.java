package com.peternaggschga.gwent;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.getListener;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.ui.dialogs.ChangeFactionDialog;
import com.peternaggschga.gwent.ui.dialogs.CoinFlipDialog;
import com.peternaggschga.gwent.ui.dialogs.cards.ShowUnitsDialog;
import com.peternaggschga.gwent.ui.main.GameBoardViewModel;
import com.peternaggschga.gwent.ui.main.MenuUiStateObserver;
import com.peternaggschga.gwent.ui.main.RowUiStateObserver;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * @todo Documentation
 */
public class MainActivity extends AppCompatActivity {
    private SoundManager soundManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener factionSwitchListener;
    /**
     * @todo Initialize ViewModel only once in #onCreate().
     */
    private GameBoardViewModel gameBoard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getBoolean(getString(R.string.preference_first_use_key), true)) {
            startActivity(new Intent(this, IntroductionActivity.class));
        }

        switch (preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL)) {
            case THEME_MONSTER:
                setTheme(R.style.MonsterTheme);
                break;
            case THEME_NILFGAARD:
                setTheme(R.style.NilfgaardTheme);
                break;
            case THEME_NORTHERN_KINGDOMS:
                setTheme(R.style.NorthernKingdomsTheme);
                break;
            case THEME_SCOIATAEL:
                setTheme(R.style.ScoiataelTheme);
        }

        setContentView(R.layout.activity_main);

        soundManager = new SoundManager(this);

        disposables.add(
                GwentApplication.getRepository(this)
                        .map(repository -> GameBoardViewModel.getModel(MainActivity.this, repository))
                        .subscribe(gameBoardViewModel -> {
                            gameBoard = gameBoardViewModel;
                            initializeViewModel();
                        })
        );

        factionSwitchListener = getListener(getWindow());
        preferences.registerOnSharedPreferenceChangeListener(factionSwitchListener);

        findViewById(R.id.factionButton).setOnClickListener(v -> inflateFactionPopup());
        findViewById(R.id.coinButton).setOnClickListener(v -> inflateCoinFlipPopup());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set background image according to preferences
        int backgroundImageKey = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.preference_key_design),
                                getString(R.string.design_preference_default))
        );
        ImageView backgroundImage = findViewById(R.id.backgroundImageView);
        if (backgroundImageKey != 0) {
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageResource(new int[]{
                    R.drawable.background_geralt,
                    R.drawable.background_ciri,
                    R.drawable.background_jaskier,
                    R.drawable.background_yennefer,
                    R.drawable.background_eredin
            }[backgroundImageKey - 1]);
        } else {
            backgroundImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!hasWindowFocus()) {
                    return;
                }

                // hide system UI
                WindowInsetsControllerCompat windowController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                windowController.hide(WindowInsetsCompat.Type.systemBars());
                windowController.hide(WindowInsetsCompat.Type.tappableElement());
                windowController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

                // keep screen on
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }, 250);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
        disposables.clear();
    }

    private void initializeViewModel() {
        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int rowId = 0; rowId < rowIds.length; rowId++) {
            RowType row = RowType.values()[rowId];
            ConstraintLayout rowLayout = findViewById(rowIds[rowId]);

            ImageView weather = rowLayout.findViewById(R.id.weatherView);
            ImageView horn = rowLayout.findViewById(R.id.hornView);
            ConstraintLayout cards = rowLayout.findViewById(R.id.cardView);

            weather.setOnClickListener(v -> disposables.add(
                    gameBoard.onWeatherViewPressed(row).subscribe(weatherActivated -> {
                        if (weatherActivated) {
                            soundManager.playWeatherSound(row);
                        }
                    })
            ));
            horn.setOnClickListener(v -> disposables.add(
                    gameBoard.onHornViewPressed(row).subscribe(hornActivated -> {
                        if (hornActivated) {
                            soundManager.playHornSound();
                        }
                    })
            ));
            cards.setOnClickListener(v -> disposables.add(
                    ShowUnitsDialog.getDialog(MainActivity.this, row).subscribe(Dialog::show)
            ));

            final RowUiStateObserver observer = RowUiStateObserver.getObserver(row,
                    rowLayout.findViewById(R.id.pointView),
                    weather,
                    horn,
                    rowLayout.findViewById(R.id.cardCountView));
            disposables.add(gameBoard.getRowUiState(row).subscribe(observer));
        }

        ImageButton reset = findViewById(R.id.resetButton);
        ImageButton weather = findViewById(R.id.weatherButton);
        ImageButton burn = findViewById(R.id.burnButton);

        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView),
                reset,
                weather,
                burn);
        disposables.add(gameBoard.getMenuUiState().subscribe(observer));

        reset.setOnClickListener(v -> disposables.add(
                gameBoard.onResetButtonPressed(this)
                        .subscribe(playSound -> {
                            if (playSound) {
                                soundManager.playResetSound();
                            }
                        })
        ));
        weather.setOnClickListener(v -> {
            disposables.add(gameBoard.onWeatherButtonPressed().subscribe());
            soundManager.playClearWeatherSound();
        });
        burn.setOnClickListener(v -> disposables.add(
                gameBoard.onBurnButtonPressed(MainActivity.this).subscribe(aBoolean -> {
                if (aBoolean) {
                    soundManager.playBurnSound();
                }
                })
        ));
    }

    private void inflateFactionPopup() {
        new ChangeFactionDialog(this, theme -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            if (preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL) == theme) {
                return;
            }

            boolean resetOnFactionSwitch = preferences.getBoolean(
                    getString(R.string.preference_key_faction_reset),
                    getResources().getBoolean(R.bool.faction_reset_preference_default)
            );
            if (resetOnFactionSwitch) {
                disposables.add(
                        gameBoard.onFactionSwitchReset(this).subscribe(playSound -> {
                            if (playSound) {
                                soundManager.playResetSound();
                            }
                        })
                );
            }
            preferences.edit()
                    .putInt(THEME_PREFERENCE_KEY, theme)
                    .apply();
        }).show();
    }

    private void inflateCoinFlipPopup() {
        new CoinFlipDialog(this).show();
        soundManager.playCoinSound();
    }
}
