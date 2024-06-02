package com.peternaggschga.gwent.ui.main;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.dialogs.ChangeFactionDialog;
import com.peternaggschga.gwent.ui.dialogs.CoinFlipDialog;
import com.peternaggschga.gwent.ui.dialogs.OverlayDialog;
import com.peternaggschga.gwent.ui.dialogs.addcard.AddCardDialog;
import com.peternaggschga.gwent.ui.dialogs.cards.ShowUnitsDialog;
import com.peternaggschga.gwent.ui.introduction.IntroductionActivity;
import com.peternaggschga.gwent.ui.settings.SettingsActivity;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Function;

/**
 * An {@link AppCompatActivity} that is called on startup and that encapsulates the main view onto the game board.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * {@link CompositeDisposable} used to store all {@link io.reactivex.rxjava3.disposables.Disposable}s,
     * this activity might create to allow for their disposal in {@link #onDestroy()}.
     */
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * {@link SoundManager} used for sound effects on certain events.
     */
    private SoundManager soundManager;

    /**
     * {@link SharedPreferences.OnSharedPreferenceChangeListener} that is called when faction-layout,
     * i.e., the {@link androidx.preference.Preference} at the key defined by {@link com.peternaggschga.gwent.ui.main.FactionSwitchListener#THEME_PREFERENCE_KEY},
     * is changed.
     * Reference must be kept (even if not used) to avoid garbage collection of the registered listener
     * (see <a href="https://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)">here</a> for more information).
     */
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener factionSwitchListener;

    /**
     * {@link GameBoardViewModel} holding the ui state of this activity.
     */
    private GameBoardViewModel gameBoardViewModel;

    /**
     * Sets the theme and layout, initializes {@link #soundManager}, {@link #gameBoardViewModel}, and {@link #factionSwitchListener}
     * and sets listeners for some menu buttons.
     * If the application is started for the first time (as tracked by the preference at key {@link R.string#preference_first_use_key})
     * the {@link IntroductionActivity} is called first.
     * The theme is set according to the preference at the key specified by {@link FactionSwitchListener#THEME_PREFERENCE_KEY}.
     * The layout is set to {@link R.layout#activity_main}.
     * {@link android.widget.Button.OnClickListener}s are set for the buttons referenced by
     * {@link R.id#factionButton}, {@link R.id#coinButton}, and {@link R.id#settingsButton}.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        if (soundManager == null) {
            soundManager = new SoundManager(this);
        }

        if (gameBoardViewModel == null) {
            disposables.add(
                    GwentApplication.getRepository(this)
                            .map(repository -> GameBoardViewModel.getModel(MainActivity.this, repository))
                            .subscribe(gameBoardViewModel -> {
                                this.gameBoardViewModel = gameBoardViewModel;
                                initializeViewModel();
                            })
            );
        }

        if (factionSwitchListener == null) {
            factionSwitchListener = FactionSwitchListener.getListener(getWindow());
            preferences.registerOnSharedPreferenceChangeListener(factionSwitchListener);
        }

        findViewById(R.id.factionButton).setOnClickListener(v -> inflateFactionPopup());
        findViewById(R.id.coinButton).setOnClickListener(v -> inflateCoinFlipPopup());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    /**
     * Called when the application is resumed after a pause or on startup.
     * Sets the background image according to the preference at the key referenced by {@link R.string#preference_key_design}.
     */
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

    /**
     * Called when the focus of this activities {@link android.view.Window} changes.
     * Hides system ui and sets flags to keep the screen on when the window is in focus for more than 250 ms.
     * @param hasFocus Whether the window of this activity has focus.
     *
     */
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

    /**
     * Called when the activity is destroyed.
     * Disposes and clears all {@link io.reactivex.rxjava3.disposables.Disposable}s in {@link #disposables}.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
        disposables.clear();
    }

    /**
     * Initializes the {@link View}s in this activity to communicate with the {@link #gameBoardViewModel}.
     * Sets {@link android.widget.Button.OnClickListener} for the weather-, horn-, and card-views of each row
     * as well as listeners for the reset-, weather- and burn-buttons.
     */
    private void initializeViewModel() {
        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int rowId = 0; rowId < rowIds.length; rowId++) {
            RowType row = RowType.values()[rowId];
            ConstraintLayout rowLayout = findViewById(rowIds[rowId]);

            ImageView weather = rowLayout.findViewById(R.id.weatherView);
            ImageView horn = rowLayout.findViewById(R.id.hornView);
            ConstraintLayout cards = rowLayout.findViewById(R.id.cardView);

            weather.setOnClickListener(v -> disposables.add(
                    gameBoardViewModel.onWeatherViewPressed(row).subscribe(weatherActivated -> {
                        if (weatherActivated) {
                            soundManager.playWeatherSound(row);
                        }
                    })
            ));
            horn.setOnClickListener(v -> disposables.add(
                    gameBoardViewModel.onHornViewPressed(row).subscribe(hornActivated -> {
                        if (hornActivated) {
                            soundManager.playHornSound();
                        }
                    })
            ));
            cards.setOnClickListener(v -> disposables.add(
                    GwentApplication.getRepository(this)
                            .flatMap(repository -> repository.countUnits(row))
                            .map(count -> count == 0)
                            .flatMap((Function<Boolean, Single<? extends OverlayDialog>>) rowEmpty ->
                                    rowEmpty
                                            ? Single.just(new AddCardDialog(MainActivity.this, row))
                                            : ShowUnitsDialog.getDialog(MainActivity.this, row))
                            .subscribe(Dialog::show)
            ));

            final RowUiStateObserver observer = RowUiStateObserver.getObserver(row,
                    rowLayout.findViewById(R.id.pointView),
                    weather,
                    horn,
                    rowLayout.findViewById(R.id.cardCountView));
            disposables.add(gameBoardViewModel.getRowUiState(row).subscribe(observer));
        }

        ImageButton reset = findViewById(R.id.resetButton);
        ImageButton weather = findViewById(R.id.weatherButton);
        ImageButton burn = findViewById(R.id.burnButton);

        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView),
                reset,
                weather,
                burn);
        disposables.add(gameBoardViewModel.getMenuUiState().subscribe(observer));

        reset.setOnClickListener(v -> disposables.add(
                gameBoardViewModel.onResetButtonPressed(this)
                        .subscribe(playSound -> {
                            if (playSound) {
                                soundManager.playResetSound();
                            }
                        })
        ));
        weather.setOnClickListener(v -> {
            disposables.add(gameBoardViewModel.onWeatherButtonPressed().subscribe());
            soundManager.playClearWeatherSound();
        });
        burn.setOnClickListener(v -> disposables.add(
                gameBoardViewModel.onBurnButtonPressed(MainActivity.this).subscribe(aBoolean -> {
                if (aBoolean) {
                    soundManager.playBurnSound();
                }
                })
        ));
    }

    /**
     * Shows a new {@link ChangeFactionDialog} enabling the user to choose the preferred theme.
     * The decision is saved in the preference at the key {@link FactionSwitchListener#THEME_PREFERENCE_KEY}
     * and if the user opted to reset on faction switch
     * (i.e., the preference at {@link R.string#preference_key_faction_reset} is true),
     * {@link GameBoardViewModel#onFactionSwitchReset(Context)} is called.
     */
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
                        gameBoardViewModel.onFactionSwitchReset(this).subscribe(playSound -> {
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

    /**
     * Shows a new {@link CoinFlipDialog} and plays a coin-flip sound using {@link SoundManager#playCoinSound()}.
     */
    private void inflateCoinFlipPopup() {
        new CoinFlipDialog(this).show();
        soundManager.playCoinSound();
    }
}
