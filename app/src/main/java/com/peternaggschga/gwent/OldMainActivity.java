package com.peternaggschga.gwent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Deprecated
public class OldMainActivity extends AppCompatActivity {
    private static final String FILE_NAME_BACKUP_MELEE_ROW = "melee.json";
    private static final String FILE_NAME_BACKUP_RANGE_ROW = "range.json";
    private static final String FILE_NAME_BACKUP_SIEGE_ROW = "siege.json";
    final private Set<Row> allRows = new HashSet<>();
    final private Set<Unit> copyUnits = new HashSet<>();
    final private Set<Unit> deleteUnits = new HashSet<>();
    private SharedPreferences sharedPreferences;
    private SoundPool soundPool;
    private PopupWindow popupWindow;
    private NumberPicker epicPicker;
    private NumberPicker dmgPicker;
    private NumberPicker abilityPicker;
    private TextView bindingPickerLabel;
    private NumberPicker bindingPicker;
    private NumberPicker numberPicker;
    private Row meleeRow;
    private Row rangeRow;
    private Row siegeRow;
    private ImageView backgroundImageView;
    private ImageView firstRowBackgroundPointBall;
    private ImageView firstRowPointBall;
    private TextView firstRowPointView;
    private ImageView firstRowBackgroundWeatherImageView;
    private ImageView firstRowWeatherImageView;
    private ImageView firstRowBackgroundHornImageView;
    private ImageView firstRowHornImageView;
    private ImageView firstRowBackgroundCardImageView;
    private ImageView firstRowCardImageView;
    private TextView firstRowCardCount;
    private ImageView secondRowBackgroundPointBall;
    private ImageView secondRowPointBall;
    private TextView secondRowPointView;
    private ImageView secondRowBackgroundWeatherImageView;
    private ImageView secondRowWeatherImageView;
    private ImageView secondRowBackgroundHornImageView;
    private ImageView secondRowHornImageView;
    private ImageView secondRowBackgroundCardImageView;
    private ImageView secondRowCardImageView;
    private TextView secondRowCardCount;
    private ImageView thirdRowBackgroundPointBall;
    private ImageView thirdRowPointBall;
    private TextView thirdRowPointView;
    private ImageView thirdRowBackgroundWeatherImageView;
    private ImageView thirdRowWeatherImageView;
    private ImageView thirdRowBackgroundHornImageView;
    private ImageView thirdRowHornImageView;
    private ImageView thirdRowBackgroundCardImageView;
    private ImageView thirdRowCardImageView;
    private TextView thirdRowCardCount;
    private ImageView backgroundOverallPointBall;
    private ImageView overallPointBall;
    private TextView overallPointView;
    private ImageButton factionButton;
    private ImageButton resetButton;
    private ImageButton weatherButton;
    private ImageButton burnButton;
    private ImageView coinButton;
    private ImageButton settingsButton;

    public static void ImageViewAnimatedChange(Context context, @NonNull final ImageView view, @NonNull final ImageView backgroundView, @DrawableRes final int resId) {
        final Animation anim_out = AnimationUtils.loadAnimation(context, R.anim.popup_hide);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                backgroundView.setImageResource(resId);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setImageResource(resId);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim_out);
    }

    public static void hideSystemUI(@NonNull Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

    }

    public static void keepScreenOn(@NonNull Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("firstUse", true)) {
            startActivity(new Intent(this, OnboardingSupportActivity.class));
        }
        inflateFactionLayout(true);
        initViews();

        try {
            allRows.add(meleeRow = retrieveRow(FILE_NAME_BACKUP_MELEE_ROW, Row.ROW_MELEE));
            allRows.add(rangeRow = retrieveRow(FILE_NAME_BACKUP_RANGE_ROW, Row.ROW_RANGE));
            allRows.add(siegeRow = retrieveRow(FILE_NAME_BACKUP_SIEGE_ROW, Row.ROW_SIEGE));
        } catch (@NonNull IOException | JSONException e) {
            e.printStackTrace();
            allRows.clear();
            allRows.add(meleeRow = new Row(Row.ROW_MELEE));
            allRows.add(rangeRow = new Row(Row.ROW_RANGE));
            allRows.add(siegeRow = new Row(Row.ROW_SIEGE));
        }

        checkSidebarButtons();
        setRowImages();

        factionButton.setOnClickListener(this::inflateFactionPopup);
        resetButton.setOnClickListener(view -> {
            final boolean monster = THEME.MONSTER.ordinal() == sharedPreferences.getInt("faction", THEME.SCOIATAEL.ordinal());
            AlertDialog.Builder builder = getAlertDialogBuilder();
            if (sharedPreferences.getBoolean("warnings", true)) {
                //final View checkBoxView = View.inflate(OldMainActivity.this, R.layout.alertdialog_checkbox, null);
                if (monster) {
                    //builder.setView(checkBoxView);
                }
                builder.setTitle(R.string.alertDialog_reset_title)
                        .setMessage(R.string.alertDialog_reset_msg_default)
                        .setPositiveButton(R.string.alertDialog_reset_positive, (dialogInterface, i) -> {
                            if (monster) {
                                /*CheckBox checkBox = checkBoxView.findViewById(R.id.alertDialog_checkbox);
                                resetAll(checkBox.isChecked());*/
                            } else {
                                resetAll(false);
                            }
                        });
                builder.create().show();
            } else if (monster) {
                /*builder.setTitle(R.string.alertDialog_monster_title)
                        .setMessage(R.string.alertDialog_monster_msg)
                        .setPositiveButton(R.string.alertDialog_monster_positive, (dialogInterface, i) -> resetAll(true))
                        .setNegativeButton(R.string.alertDialog_monster_negative, (dialogInterface, i) -> resetAll(false));*/
                builder.create().show();
            } else {
                resetAll(false);
            }
        });
        weatherButton.setOnClickListener(view -> {
            if (sharedPreferences.getBoolean("sound_weather", true)) {
                playSound(R.raw.weather_good);
            }
            resetWeather();
        });
        burnButton.setOnClickListener(view -> {
            final List<Unit> burnUnits = getBurnUnits();
            int burnedRevenge = 0;
            for (Unit unit : burnUnits) {
                if (unit.isRevenge()) {
                    burnedRevenge++;
                }
            }
            if (sharedPreferences.getBoolean("warnings", true)) {
                Map<String, Integer> unitStrings = new HashMap<>();
                for (Unit unit : burnUnits) {
                    String unitString = unit.toString(getApplicationContext(), null);
                    if (unitStrings.containsKey(unitString)) {
                        Integer currentValue = unitStrings.get(unitString);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && currentValue != null) {
                            unitStrings.replace(unitString, currentValue, currentValue + 1);
                        } else if (currentValue != null) {
                            unitStrings.remove(unitString);
                            unitStrings.put(unitString, currentValue + 1);
                        }
                    } else {
                        unitStrings.put(unitString, 1);
                    }
                }
                StringBuilder stringBuilder = new StringBuilder();
                int k = unitStrings.size();
                for (Map.Entry<String, Integer> entry : unitStrings.entrySet()) {
                    if (unitStrings.size() != 1 || entry.getValue() != 1) {
                        stringBuilder.append(entry.getValue());
                        stringBuilder.append("x ");
                    }
                    stringBuilder.append(entry.getKey());
                    if (k > 2) {
                        stringBuilder.append(", ");
                        k--;
                    } else if (k == 2) {
                        stringBuilder.append(" und ");
                        k--;
                    }
                }
                String msg = getString(R.string.alertDialog_burn_msg, stringBuilder.toString());
                AlertDialog.Builder alertDialogBuilder = getAlertDialogBuilder();
                final int finalBurnedRevenge = burnedRevenge;
                alertDialogBuilder.setTitle(R.string.alertDialog_burn_title)
                        .setMessage(msg)
                        .setPositiveButton(R.string.alertDialog_burn_title, (dialogInterface, i) -> burn(burnUnits, finalBurnedRevenge));
                alertDialogBuilder.create().show();
            } else {
                burn(burnUnits, burnedRevenge);
            }
        });
        coinButton.setOnClickListener(this::inflateCoinflipPopup);
        settingsButton.setOnClickListener(view -> startActivity(new Intent(OldMainActivity.this, SettingsActivity.class)));
        firstRowWeatherImageView.setOnClickListener(view -> changeWeather((ImageView) view, meleeRow));
        secondRowWeatherImageView.setOnClickListener(view -> changeWeather((ImageView) view, rangeRow));
        thirdRowWeatherImageView.setOnClickListener(view -> changeWeather((ImageView) view, siegeRow));
        firstRowHornImageView.setOnClickListener(view -> changeHorn(meleeRow));
        secondRowHornImageView.setOnClickListener(view -> changeHorn(rangeRow));
        thirdRowHornImageView.setOnClickListener(view -> changeHorn(siegeRow));
        firstRowCardImageView.setOnClickListener(view -> inflateCardPopup(meleeRow, view));
        secondRowCardImageView.setOnClickListener(view -> inflateCardPopup(rangeRow, view));
        thirdRowCardImageView.setOnClickListener(view -> inflateCardPopup(siegeRow, view));
    }

    private void initViews() {
        backgroundImageView = findViewById(R.id.backgroundImageView);
        View firstRow = findViewById(R.id.firstRow);
        View secondRow = findViewById(R.id.secondRow);
        View thirdRow = findViewById(R.id.thirdRow);
        //firstRowBackgroundPointBall = firstRow.findViewById(R.id.backgroundPointBall);
        firstRowPointBall = firstRow.findViewById(R.id.pointBall);
        firstRowPointView = firstRow.findViewById(R.id.pointView);
        //firstRowBackgroundWeatherImageView = firstRow.findViewById(R.id.backgroundWeatherImage);
        firstRowWeatherImageView = firstRow.findViewById(R.id.weatherView);
        //firstRowBackgroundHornImageView = firstRow.findViewById(R.id.backgroundHornImage);
        firstRowHornImageView = firstRow.findViewById(R.id.hornView);
        //firstRowBackgroundCardImageView = firstRow.findViewById(R.id.backgroundCardsImage);
        firstRowCardImageView = firstRow.findViewById(R.id.cardsImage);
        firstRowCardCount = firstRow.findViewById(R.id.cardCountView);
        //secondRowBackgroundPointBall = secondRow.findViewById(R.id.backgroundPointBall);
        secondRowPointBall = secondRow.findViewById(R.id.pointBall);
        secondRowPointView = secondRow.findViewById(R.id.pointView);
        //secondRowBackgroundWeatherImageView = secondRow.findViewById(R.id.backgroundWeatherImage);
        secondRowWeatherImageView = secondRow.findViewById(R.id.weatherView);
        //secondRowBackgroundHornImageView = secondRow.findViewById(R.id.backgroundHornImage);
        secondRowHornImageView = secondRow.findViewById(R.id.hornView);
        //secondRowBackgroundCardImageView = secondRow.findViewById(R.id.backgroundCardsImage);
        secondRowCardImageView = secondRow.findViewById(R.id.cardsImage);
        secondRowCardCount = secondRow.findViewById(R.id.cardCountView);
        //thirdRowBackgroundPointBall = thirdRow.findViewById(R.id.backgroundPointBall);
        thirdRowPointBall = thirdRow.findViewById(R.id.pointBall);
        thirdRowPointView = thirdRow.findViewById(R.id.pointView);
        //thirdRowBackgroundWeatherImageView = thirdRow.findViewById(R.id.backgroundWeatherImage);
        thirdRowWeatherImageView = thirdRow.findViewById(R.id.weatherView);
        //thirdRowBackgroundHornImageView = thirdRow.findViewById(R.id.backgroundHornImage);
        thirdRowHornImageView = thirdRow.findViewById(R.id.hornView);
        //thirdRowBackgroundCardImageView = thirdRow.findViewById(R.id.backgroundCardsImage);
        thirdRowCardImageView = thirdRow.findViewById(R.id.cardsImage);
        thirdRowCardCount = thirdRow.findViewById(R.id.cardCountView);
        //backgroundOverallPointBall = findViewById(R.id.backgroundOverallPointBall);
        overallPointBall = findViewById(R.id.overallPointBall);
        overallPointView = findViewById(R.id.overallPointView);
        factionButton = findViewById(R.id.factionButton);
        resetButton = findViewById(R.id.resetButton);
        weatherButton = findViewById(R.id.weatherButton);
        burnButton = findViewById(R.id.burnButton);
        coinButton = findViewById(R.id.coinButton);
        settingsButton = findViewById(R.id.settingsButton);
    }

    private void inflateFactionPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_faction, (ViewGroup) getWindow().getDecorView(), false);
        popupView.findViewById(R.id.monsterButton).setOnClickListener(view1 -> changeTheme(THEME.MONSTER));
        popupView.findViewById(R.id.nilfgaardButton).setOnClickListener(view12 -> changeTheme(THEME.NILFGAARD));
        popupView.findViewById(R.id.northernKingdomsButton).setOnClickListener(view13 -> changeTheme(THEME.NORTHERN_KINGDOMS));
        popupView.findViewById(R.id.scoiataelButton).setOnClickListener(view14 -> changeTheme(THEME.SCOIATAEL));
        inflatePopup(view, popupView, true);
    }

    private void changeHorn(@NonNull Row row) {
        boolean horn = row.isHorn();
        if (!horn && sharedPreferences.getBoolean("sound_horn", true)) {
            playSound(R.raw.horn);
        }
        row.setHorn(!horn);
        ImageView imageView;
        ImageView backgroundImageView;
        switch (row.getType()) {
            case Row.ROW_MELEE:
                imageView = firstRowHornImageView;
                backgroundImageView = firstRowBackgroundHornImageView;
                break;
            case Row.ROW_RANGE:
                imageView = secondRowHornImageView;
                backgroundImageView = secondRowBackgroundHornImageView;
                break;
            default:
                imageView = thirdRowHornImageView;
                backgroundImageView = thirdRowBackgroundHornImageView;
        }
        ImageViewAnimatedChange(this, imageView, backgroundImageView, horn ? R.drawable.horn_grey : R.drawable.horn);
        checkSidebarButtons();
    }

    private void resetHorns() {
        for (Row row : allRows) {
            row.setHorn(false);
        }
        ImageViewAnimatedChange(OldMainActivity.this, firstRowHornImageView, firstRowBackgroundHornImageView, R.drawable.horn_grey);
        ImageViewAnimatedChange(OldMainActivity.this, secondRowHornImageView, secondRowBackgroundHornImageView, R.drawable.horn_grey);
        ImageViewAnimatedChange(OldMainActivity.this, thirdRowHornImageView, thirdRowBackgroundHornImageView, R.drawable.horn_grey);
    }

    private void changeWeather(@NonNull ImageView view, @NonNull Row row) {
        int badWeatherImageResourceId;
        int badWeatherSoundResourceId;
        ImageView backgroundView;
        switch (row.getType()) {
            case Row.ROW_MELEE:
                badWeatherImageResourceId = R.drawable.frost_weather;
                backgroundView = firstRowBackgroundWeatherImageView;
                badWeatherSoundResourceId = R.raw.weather_frost;
                break;
            case Row.ROW_RANGE:
                badWeatherImageResourceId = R.drawable.fog_weather;
                backgroundView = secondRowBackgroundWeatherImageView;
                badWeatherSoundResourceId = R.raw.weather_fog;
                break;
            default:
                badWeatherImageResourceId = R.drawable.rain_weather;
                backgroundView = thirdRowBackgroundWeatherImageView;
                badWeatherSoundResourceId = R.raw.weather_rain;
        }
        ImageViewAnimatedChange(this, view, backgroundView, row.isWeather() ? R.drawable.good_weather : badWeatherImageResourceId);
        if (sharedPreferences.getBoolean("sound_weather", true) && !row.isWeather()) {
            playSound(badWeatherSoundResourceId);
        }

        row.setWeather(!row.isWeather());
        checkSidebarButtons();
    }

    private void resetWeather() {
        for (Row row : allRows) {
            row.setWeather(false);
        }
        ImageViewAnimatedChange(OldMainActivity.this, firstRowWeatherImageView, firstRowBackgroundWeatherImageView, R.drawable.good_weather);
        ImageViewAnimatedChange(OldMainActivity.this, secondRowWeatherImageView, secondRowBackgroundWeatherImageView, R.drawable.good_weather);
        ImageViewAnimatedChange(OldMainActivity.this, thirdRowWeatherImageView, thirdRowBackgroundWeatherImageView, R.drawable.good_weather);
        checkSidebarButtons();
    }

    private void checkSidebarButtons() {
        if (meleeRow.isWeather() || rangeRow.isWeather() || siegeRow.isWeather()) {
            weatherButton.setEnabled(true);
            weatherButton.setImageResource(R.drawable.icon_weather);
        } else {
            weatherButton.setEnabled(false);
            weatherButton.setImageResource(R.drawable.icon_weather_grey);
        }

        if (meleeRow.isWeather() ||
                rangeRow.isWeather() ||
                siegeRow.isWeather() ||
                meleeRow.isHorn() ||
                rangeRow.isHorn() ||
                siegeRow.isHorn() ||
                meleeRow.getCardCount() > 0 ||
                rangeRow.getCardCount() > 0 ||
                siegeRow.getCardCount() > 0) {
            resetButton.setEnabled(true);
            resetButton.setImageResource(R.drawable.icon_reset);
        } else {
            resetButton.setEnabled(false);
            resetButton.setImageResource(R.drawable.icon_reset_grey);
        }

        if (getBurnUnits().size() > 0) {
            burnButton.setEnabled(true);
            burnButton.setImageResource(R.drawable.icon_burn);
        } else {
            burnButton.setEnabled(false);
            burnButton.setImageResource(R.drawable.icon_burn_grey);
        }

        updateRows();
    }

    private void resetAll(boolean keepRdmUnit) {
        if (sharedPreferences.getBoolean("sound_reset", true)) {
            playSound(R.raw.reset);
        }
        int revengeCount = 0;
        if (keepRdmUnit) {
            HashSet<Row> fullClear = new HashSet<>();
            for (Row row : allRows) {
                if (row.isRevenge()) {
                    revengeCount += row.getJaskierMoralBoosterRevengeCount()[2];
                }
                if (row.getCardCount() == 0 || row.isEpicOnly()) {
                    fullClear.add(row);
                }
            }
            for (Row row : fullClear) {
                allRows.remove(row);
            }
            int size = allRows.size();
            while (size > 1) {
                int randomItem = new Random().nextInt(size);
                int i = 0;
                for (Row row : allRows) {
                    if (i == randomItem) {
                        fullClear.add(row);
                        break;
                    }
                    i++;
                }
                for (Row row : fullClear) {
                    allRows.remove(row);
                }
                size = allRows.size();
            }
            for (Row row : allRows) {
                row.clear(true);
                Unit keepUnit = row.getAllUnits().get(0);
                Toast.makeText(this, getString(R.string.alertDialog_factionreset_monster_toast_keep, keepUnit.toString(this, null)), Toast.LENGTH_LONG).show();
                if (keepUnit.isRevenge()) {
                    revengeCount--;
                }
            }
            if (allRows.size() == 0) {
                //Toast.makeText(this, R.string.alertDialog_factionreset_monster_toast_nokeep, Toast.LENGTH_LONG).show();
            }
            for (Row row : fullClear) {
                row.clear(false);
                allRows.add(row);
            }
        } else {
            for (Row row : allRows) {
                if (row.isRevenge()) {
                    revengeCount += row.getJaskierMoralBoosterRevengeCount()[2];
                }
                row.clear(false);
            }
        }
        resetHorns();
        resetWeather();
        inflateRevengeDialog(revengeCount);
        updateRows();
    }

    private void inflateRevengeDialog(final int revengeUnits) {
        if (revengeUnits > 0) {
            AlertDialog.Builder builder = getAlertDialogBuilder();
            builder.setTitle(R.string.alertDialog_revenge_title)
                    .setMessage(R.string.alertDialog_revenge_msg)
                    .setPositiveButton(R.string.alertDialog_revenge_positive, (dialogInterface, i) -> {
                        for (int j = 0; j < revengeUnits; j++) {
                            meleeRow.addUnit(new Unit(8, false, false, false, 0, false));
                            updateRows();
                            checkSidebarButtons();
                        }
                    });
            builder.create().show();
        }
    }

    private void burn(@NonNull List<Unit> burnUnits, int revengeUnits) {
        if (sharedPreferences.getBoolean("sound_burn", true)) {
            playSound(R.raw.burn);
        }
        for (Unit unit : burnUnits) {
            switch (unit.getRow()) {
                case Row.ROW_MELEE:
                    meleeRow.removeUnit(unit);
                    break;
                case Row.ROW_RANGE:
                    rangeRow.removeUnit(unit);
                    break;
                case Row.ROW_SIEGE:
                    siegeRow.removeUnit(unit);
            }
        }
        inflateRevengeDialog(revengeUnits);
        checkSidebarButtons();
    }

    @NonNull
    private List<Unit> getBurnUnits() {
        HashSet<Unit> allUnits = new HashSet<>();
        int maxDmg = -1;
        for (Row row : allRows) {
            for (Unit unit : row.getAllUnits()) {
                int buffAD = unit.getBuffAD();
                if (!unit.isEpic() && buffAD >= maxDmg) {
                    allUnits.add(unit);
                    maxDmg = buffAD;
                }
            }
        }
        List<Unit> burnUnits = new ArrayList<>();
        for (Unit unit : allUnits) {
            if (unit.getBuffAD() == maxDmg) {
                burnUnits.add(unit);
            }
        }
        return burnUnits;
    }

    @NonNull
    private AlertDialog.Builder getAlertDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OldMainActivity.this);
        builder.setIconAttribute(android.R.attr.alertDialogIcon)
                .setNeutralButton(R.string.alertDialog_cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        return builder;
    }

    private void inflatePopup(View view, @NonNull View popupView, boolean dismissOnTouch) {
        ViewGroup.LayoutParams layoutParams = findViewById(R.id.constraintLayout).getLayoutParams();
        popupWindow = new PopupWindow(popupView, layoutParams.width, layoutParams.height, true);
        popupWindow.setAnimationStyle(R.style.popUpAnimation);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        if (dismissOnTouch) {
            popupView.setOnTouchListener((view1, motionEvent) -> {
                view1.performClick();
                popupWindow.dismiss();
                return true;
            });
        }
    }

    private void inflateFactionLayout(boolean init) {
        THEME theme = THEME.values()[sharedPreferences.getInt("faction", THEME.SCOIATAEL.ordinal())];
        switch (theme) {
            case MONSTER:
                setTheme(R.style.MonsterTheme);
                break;
            case NILFGAARD:
                setTheme(R.style.NilfgaardTheme);
                break;
            case NORTHERN_KINGDOMS:
                setTheme(R.style.NorthernKingdomsTheme);
                break;
            default:
                setTheme(R.style.ScoiataelTheme);
        }
        if (init) {
            setContentView(R.layout.activity_main);
        } else {
            int factionButtonResourceId;
            int ballImageResourceId;
            int rowCardResourceId;
            int textColorResourceId;
            switch (theme) {
                case MONSTER:
                    factionButtonResourceId = R.drawable.icon_round_monster;
                    ballImageResourceId = R.drawable.ball_red;
                    rowCardResourceId = R.drawable.card_monster_landscape_free;
                    textColorResourceId = R.color.color_text_monster;
                    break;
                case NILFGAARD:
                    factionButtonResourceId = R.drawable.icon_round_nilfgaard;
                    ballImageResourceId = R.drawable.ball_grey;
                    rowCardResourceId = R.drawable.card_nilfgaard_landscape_free;
                    textColorResourceId = R.color.color_text_nilfgaard;
                    break;
                case NORTHERN_KINGDOMS:
                    factionButtonResourceId = R.drawable.icon_round_northern_kingdoms;
                    ballImageResourceId = R.drawable.ball_blue;
                    rowCardResourceId = R.drawable.card_northern_kingdoms_landscape_free;
                    textColorResourceId = R.color.color_text_northern_kingdoms;
                    break;
                default:
                    factionButtonResourceId = R.drawable.icon_round_scoiatael;
                    ballImageResourceId = R.drawable.ball_green;
                    rowCardResourceId = R.drawable.card_scoiatael_landscape_free;
                    textColorResourceId = R.color.color_text_scoiatael;
            }
            factionButton.setImageResource(factionButtonResourceId);
            ImageViewAnimatedChange(this, firstRowPointBall, firstRowBackgroundPointBall, ballImageResourceId);
            ImageViewAnimatedChange(this, secondRowPointBall, secondRowBackgroundPointBall, ballImageResourceId);
            ImageViewAnimatedChange(this, thirdRowPointBall, thirdRowBackgroundPointBall, ballImageResourceId);
            ImageViewAnimatedChange(this, firstRowCardImageView, firstRowBackgroundCardImageView, rowCardResourceId);
            ImageViewAnimatedChange(this, secondRowCardImageView, secondRowBackgroundCardImageView, rowCardResourceId);
            ImageViewAnimatedChange(this, thirdRowCardImageView, thirdRowBackgroundCardImageView, rowCardResourceId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                firstRowCardCount.setTextColor(getColor(textColorResourceId));
                secondRowCardCount.setTextColor(getColor(textColorResourceId));
                thirdRowCardCount.setTextColor(getColor(textColorResourceId));
            } else {
                firstRowCardCount.setTextColor(getResources().getColor(textColorResourceId));
                secondRowCardCount.setTextColor(getResources().getColor(textColorResourceId));
                thirdRowCardCount.setTextColor(getResources().getColor(textColorResourceId));
            }
            ImageViewAnimatedChange(this, overallPointBall, backgroundOverallPointBall, ballImageResourceId);
        }
    }

    private void inflateAddCardPopup(@NonNull final Row row, View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_add_card, (ViewGroup) getWindow().getDecorView(), false);
        epicPicker = popupView.findViewById(R.id.popup_add_card_epic_picker);
        dmgPicker = popupView.findViewById(R.id.popup_add_card_dmg_picker);
        abilityPicker = popupView.findViewById(R.id.popup_add_card_ability_picker);
        bindingPickerLabel = popupView.findViewById(R.id.popup_add_card_binding_picker_label);
        bindingPicker = popupView.findViewById(R.id.popup_add_card_binding_picker);
        numberPicker = popupView.findViewById(R.id.popup_add_card_number_picker);
        epicPicker.setMinValue(0);
        epicPicker.setMaxValue(1);
        epicPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_epic_values));
        dmgPicker.setMinValue(0);
        dmgPicker.setMaxValue(18);
        dmgPicker.setValue(5);
        abilityPicker.setMinValue(0);
        abilityPicker.setMaxValue(4);
        abilityPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_ability_values));
        bindingPicker.setMinValue(1);
        bindingPicker.setMaxValue(3);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(5);

        epicPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            boolean epic = i1 == 1;
            dmgPicker.setDisplayedValues(null);
            dmgPicker.setMinValue(0);
            dmgPicker.setMaxValue(epic ? 5 : 18);
            dmgPicker.setValue(epic ? 3 : 5);
            dmgPicker.setEnabled(epic);
            if (epic) {
                dmgPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_dmg_values_epic));

                abilityPicker.setMinValue(0);
                abilityPicker.setMaxValue(1);
                abilityPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_ability_values_epic));

                bindingPicker.setVisibility(View.GONE);
                bindingPickerLabel.setVisibility(View.GONE);
            } else {
                abilityPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_ability_values));
                abilityPicker.setMinValue(0);
                abilityPicker.setMaxValue(4);
            }
            int ability = abilityPicker.getValue();
            abilityPicker.setValue(ability == 1 ? ability : 0);
            if (ability == 1) {
                dmgPicker.setMinValue(0);
                dmgPicker.setMaxValue(1);
                dmgPicker.setValue(0);
                dmgPicker.setDisplayedValues(getResources().getStringArray(i1 == 0 ? R.array.popup_add_card_dmg_values_moralboost_normal : R.array.popup_add_card_dmg_values_moralboost_epic));
            }
        });
        abilityPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            switch (i) {
                case 1:
                    boolean epic = !(epicPicker.getValue() == 0);
                    dmgPicker.setDisplayedValues(null);
                    dmgPicker.setMinValue(0);
                    dmgPicker.setMaxValue(epic ? 4 : 18);
                    dmgPicker.setValue(epic ? 3 : 5);
                    if (epic) {
                        dmgPicker.setDisplayedValues(getResources().getStringArray(R.array.popup_add_card_dmg_values_epic));
                    }
                    dmgPicker.setEnabled(true);
                    break;
                case 2:
                case 3:
                    dmgPicker.setEnabled(true);
                    dmgPicker.setValue(5);
                    break;
                case 4:
                    bindingPicker.setVisibility(View.GONE);
                    bindingPickerLabel.setVisibility(View.GONE);
            }

            switch (i1) {
                case 1:
                    dmgPicker.setMinValue(0);
                    boolean epic = !(epicPicker.getValue() == 0);
                    dmgPicker.setMaxValue(epic ? 1 : 4);
                    dmgPicker.setValue(1);
                    dmgPicker.setDisplayedValues(getResources().getStringArray(epic ? R.array.popup_add_card_dmg_values_moralboost_epic : R.array.popup_add_card_dmg_values_moralboost_normal));
                    break;
                case 2:
                    dmgPicker.setValue(2);
                    dmgPicker.setEnabled(false);
                    break;
                case 3:
                    dmgPicker.setValue(0);
                    dmgPicker.setEnabled(false);
                    break;
                case 4:
                    bindingPicker.setVisibility(View.VISIBLE);
                    bindingPickerLabel.setVisibility(View.VISIBLE);
                    int binding = bindingPicker.getValue();
                    List<Unit> bindingList = row.getBindingUnits(binding);
                    Toast.makeText(OldMainActivity.this, getString(R.string.popUp_add_card_binding_count, binding, bindingList.size()), Toast.LENGTH_SHORT).show();
                    if (bindingList.size() > 0) {
                        dmgPicker.setValue(bindingList.get(0).getBaseAD());
                        dmgPicker.setEnabled(false);
                    }
            }
        });
        bindingPicker.setOnValueChangedListener((numberPicker, i, i1) -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (i1 == bindingPicker.getValue()) {
                        List<Unit> bindingList = row.getBindingUnits(i1);
                        Toast.makeText(OldMainActivity.this, getString(R.string.popUp_add_card_binding_count, i1, bindingList.size()), Toast.LENGTH_SHORT).show();
                        if (bindingList.size() > 0) {
                            dmgPicker.setValue(bindingList.get(0).getBaseAD());
                            dmgPicker.setEnabled(false);
                        } else {
                            dmgPicker.setEnabled(true);
                        }
                    }
                });
            }
        }, 250));

        popupView.findViewById(R.id.popup_add_card_save_button).setOnClickListener(view12 -> {
            boolean epic = epicPicker.getValue() == 1;
            boolean moralBoost = abilityPicker.getValue() == 1;
            boolean jaskier = abilityPicker.getValue() == 2;
            boolean revenge = abilityPicker.getValue() == 3;
            int binding = abilityPicker.getValue() == 4 ? bindingPicker.getValue() : 0;
            int damage = 0;
            if (moralBoost) {
                if (epic) {
                    damage = dmgPicker.getValue() == 0 ? 8 : 10;
                } else {
                    switch (dmgPicker.getValue()) {
                        case 0:
                            damage = 1;
                            break;
                        case 1:
                            damage = 6;
                            break;
                        case 2:
                            damage = 10;
                            break;
                        case 3:
                            damage = 12;
                            break;
                        case 4:
                            damage = 14;
                    }
                }
            } else {
                if (epic) {
                    switch (dmgPicker.getValue()) {
                        case 1:
                            damage = 7;
                            break;
                        case 2:
                            damage = 8;
                            break;
                        case 3:
                            damage = 10;
                            break;
                        case 4:
                            damage = 11;
                            break;
                        case 5:
                            damage = 15;
                    }
                } else {
                    damage = dmgPicker.getValue();
                }
            }
            Unit unit = new Unit(damage, epic, jaskier, revenge, binding, moralBoost);
            for (int i = 0; i < numberPicker.getValue(); i++) {
                row.addUnit(new Unit(unit));
            }
            playAddCardSound(epic, row.getType());
            checkSidebarButtons();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.popup_add_card_cancel_button).setOnClickListener(view1 -> popupWindow.dismiss());

        inflatePopup(view, popupView, false);
    }

    private void inflateCoinflipPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;

        if (sharedPreferences.getBoolean("sound_coin", true)) {
            playSound(R.raw.coin);
        }

        Random random = new Random();
        int randomInt = random.nextInt(100);
        switch (randomInt) {
            case 1:
                popupView = inflater.inflate(R.layout.popup_coin_terry, (ViewGroup) getWindow().getDecorView(), false);
                break;
            case 2:
                popupView = inflater.inflate(R.layout.popup_coin_stewie, (ViewGroup) getWindow().getDecorView(), false);
                break;
            case 3:
                popupView = inflater.inflate(R.layout.popup_coin_vin, (ViewGroup) getWindow().getDecorView(), false);
                break;
            default:
                popupView = inflater.inflate(R.layout.popup_coin_normal, (ViewGroup) getWindow().getDecorView(), false);
                if (random.nextBoolean()) {
                    ImageView coinImageView = popupView.findViewById(R.id.popup_coin_normal_coinView);
                    TextView coinTextView = popupView.findViewById(R.id.popup_coin_normal_textView);
                    coinImageView.setImageResource(R.drawable.coin_lose);
                    coinTextView.setText(R.string.popUp_coin_normal_lose);
                }
        }
        inflatePopup(view, popupView, true);
    }

    private void inflateCardPopup(@NonNull final Row row, View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ConstraintLayout popupLayout = (ConstraintLayout) inflater.inflate(R.layout.popup_cards, (ViewGroup) getWindow().getDecorView(), false);
        LinearLayout cardsList = popupLayout.findViewById(R.id.cardsList).findViewById(R.id.cardListLayout);
        for (final Unit unit : row.getAllUnits()) {
            final View cardView = inflater.inflate(R.layout.card, cardsList, false);
            TextView damageView = cardView.findViewById(R.id.damageView);
            if (unit.isEpic()) {
                switch (unit.getBaseAD()) {
                    case 0:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_0);
                        break;
                    case 7:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_7);
                        break;
                    case 8:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_8);
                        break;
                    case 10:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_10);
                        break;
                    case 11:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_11);
                        break;
                    case 15:
                        damageView.setBackgroundResource(R.drawable.icon_epic_damage_15);
                }
            } else {
                damageView.setText(String.valueOf(unit.getBuffAD()));
                if (row.isWeather() && !unit.isBuffed()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        damageView.setTextColor(getColor(R.color.color_damage_textColor_debuffed));
                    } else {
                        damageView.setTextColor(getResources().getColor(R.color.color_damage_textColor_debuffed));
                    }
                } else if (unit.isBuffed()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        damageView.setTextColor(getColor(R.color.color_damage_textColor_buffed));
                    } else {
                        damageView.setTextColor(getResources().getColor(R.color.color_damage_textColor_buffed));
                    }
                }
            }
            ImageView abilityView = cardView.findViewById(R.id.abilityView);
            if (unit.getBinding() != 0) {
                TextView bindingView = cardView.findViewById(R.id.bindingView);
                bindingView.setText(String.valueOf(unit.getBinding()));
                bindingView.setVisibility(View.VISIBLE);
                abilityView.setVisibility(View.VISIBLE);
            } else if (unit.isMoralBoost()) {
                abilityView.setImageResource(R.drawable.icon_moral_boost);
                abilityView.setVisibility(View.VISIBLE);
            } else if (unit.isJaskier()) {
                abilityView.setImageResource(R.drawable.icon_horn);
                abilityView.setVisibility(View.VISIBLE);
            } else if (unit.isRevenge()) {
                abilityView.setImageResource(R.drawable.icon_revenge);
                abilityView.setVisibility(View.VISIBLE);
            }

            final ImageView copyButtonBackground = cardView.findViewById(R.id.copyButtonBackground);
            cardView.findViewById(R.id.copyButton).setOnClickListener(view1 -> {
                boolean contained = copyUnits.contains(unit);
                ImageViewAnimatedChange(OldMainActivity.this, (ImageView) view1, copyButtonBackground, contained ? R.drawable.icon_copy_grey : R.drawable.icon_copy);
                if (contained) {
                    copyUnits.remove(unit);
                } else {
                    copyUnits.add(unit);
                }
            });
            final ImageView deleteButtonBackground = cardView.findViewById(R.id.deleteButtonBackground);
            cardView.findViewById(R.id.deleteButton).setOnClickListener(view12 -> {
                boolean contained = deleteUnits.contains(unit);
                ImageViewAnimatedChange(OldMainActivity.this, (ImageView) view12, deleteButtonBackground, contained ? R.drawable.icon_delete_grey : R.drawable.icon_delete);
                if (contained) {
                    deleteUnits.remove(unit);
                } else {
                    deleteUnits.add(unit);
                }
            });
            cardsList.addView(cardView);
        }
        cardsList.findViewById(R.id.popup_cards_add).setOnClickListener(view13 -> {
            popupWindow.dismiss();
            inflateAddCardPopup(row, view13);
        });
        popupLayout.findViewById(R.id.popup_cards_cancel_button).setOnClickListener(view14 -> {
            copyUnits.clear();
            deleteUnits.clear();
            popupWindow.dismiss();
        });
        popupLayout.findViewById(R.id.popup_cards_save_button).setOnClickListener(view15 -> {
            int revengeUnits = 0;
            for (Unit unit : deleteUnits) {
                if (unit.isRevenge()) {
                    revengeUnits++;
                }
                row.removeUnit(unit);
            }
            inflateRevengeDialog(revengeUnits);
            boolean epic = false;
            for (Unit unit : copyUnits) {
                row.addUnit(new Unit(unit));
                if (unit.isEpic()) {
                    epic = true;
                }
            }
            if (copyUnits.size() > 0) {
                playAddCardSound(epic, row.getType());
            } else if (deleteUnits.size() > 0 && sharedPreferences.getBoolean("sound_reset", true)) {
                playSound(R.raw.reset);
            }
            deleteUnits.clear();
            copyUnits.clear();
            checkSidebarButtons();
            popupWindow.dismiss();
        });
        inflatePopup(view, popupLayout, false);
    }

    private void changeTheme(final THEME theme) {
        if (sharedPreferences.getInt("faction", THEME.SCOIATAEL.ordinal()) != theme.ordinal()) {
            if (sharedPreferences.getBoolean("factionReset", false) && sharedPreferences.getBoolean("warnings", true) && resetButton.isEnabled()) {
                AlertDialog.Builder builder = getAlertDialogBuilder();
                builder.setTitle(R.string.alertDialog_reset_title)
                        .setMessage(R.string.alertDialog_reset_msg_faction_switch)
                        .setCancelable(false)
                        .setPositiveButton(R.string.alertDialog_reset_positive, (dialogInterface, i) -> {
                            resetAll(false);
                            sharedPreferences.edit().putInt("faction", theme.ordinal()).apply();
                            inflateFactionLayout(false);
                            popupWindow.dismiss();
                        })
                        .setNegativeButton(R.string.alertDialog_reset_negative, (dialogInterface, i) -> {
                            sharedPreferences.edit().putInt("faction", theme.ordinal()).apply();
                            inflateFactionLayout(false);
                            popupWindow.dismiss();
                        });
                builder.create().show();
            } else if (sharedPreferences.getBoolean("factionReset", false) && resetButton.isEnabled()) {
                resetAll(false);
                sharedPreferences.edit().putInt("faction", theme.ordinal()).apply();
                inflateFactionLayout(false);
                popupWindow.dismiss();
            } else {
                sharedPreferences.edit().putInt("faction", theme.ordinal()).apply();
                inflateFactionLayout(false);
                popupWindow.dismiss();
            }
        } else {
            popupWindow.dismiss();
        }
    }

    private void playAddCardSound(boolean epic, int rowType) {
        if (sharedPreferences.getBoolean("sound_cards", true)) {
            if (epic) {
                playSound(R.raw.card_epic);
            } else {
                switch (rowType) {
                    case Row.ROW_MELEE:
                        playSound(R.raw.card_melee);
                        break;
                    case Row.ROW_RANGE:
                        playSound(R.raw.card_range);
                        break;
                    case Row.ROW_SIEGE:
                        playSound(R.raw.card_siege);
                }
            }
        }
    }

    private void playSound(@RawRes int soundResourceId) {
        if (sharedPreferences.getBoolean("sound_all", true)) {
            final int soundId = soundPool.load(getApplicationContext(), soundResourceId, 1);
            soundPool.setOnLoadCompleteListener((soundPool, i, i1) -> soundPool.play(soundId, 1, 1, 1, 0, 1));
        }
    }

    @Override
    protected void onResume() {
        updateBackground();
        if (sharedPreferences.getBoolean("sound_all", true)) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_GAME).build();
            soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        }
        super.onResume();
    }

    private void updateBackground() {
        int backgroundKey = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("design", "0")));
        int[] imageResIds = {R.drawable.background_geralt, R.drawable.background_ciri, R.drawable.background_jaskier, R.drawable.background_yennefer, R.drawable.background_eredin};
        backgroundImageView.setImageResource(backgroundKey > 1 ? imageResIds[backgroundKey - 1] : imageResIds[0]);
        backgroundImageView.setVisibility(backgroundKey == 0 ? View.GONE : View.VISIBLE);
    }

    private void setRowImages() {
        for (Row row : allRows) {
            ImageView weatherView;
            int weatherImageId;
            ImageView hornView;
            switch (row.getType()) {
                case Row.ROW_MELEE:
                    weatherView = firstRowWeatherImageView;
                    hornView = firstRowHornImageView;
                    weatherImageId = R.drawable.frost_weather;
                    break;
                case Row.ROW_RANGE:
                    weatherView = secondRowWeatherImageView;
                    hornView = secondRowHornImageView;
                    weatherImageId = R.drawable.fog_weather;
                    break;
                default:
                    weatherView = thirdRowWeatherImageView;
                    hornView = thirdRowHornImageView;
                    weatherImageId = R.drawable.rain_weather;
            }
            if (row.isWeather()) {
                weatherView.setImageResource(weatherImageId);
            }
            if (row.isHorn()) {
                hornView.setImageResource(R.drawable.horn);
            }
        }
    }

    private void updateRows() {
        int meleeDmg = meleeRow.getOverallDamage();
        int rangeDmg = rangeRow.getOverallDamage();
        int siegeDmg = siegeRow.getOverallDamage();
        firstRowPointView.setText(String.valueOf(meleeDmg));
        firstRowCardCount.setText(String.valueOf(meleeRow.getCardCount()));
        secondRowPointView.setText(String.valueOf(rangeDmg));
        secondRowCardCount.setText(String.valueOf(rangeRow.getCardCount()));
        thirdRowPointView.setText(String.valueOf(siegeDmg));
        thirdRowCardCount.setText(String.valueOf(siegeRow.getCardCount()));
        overallPointView.setText(String.valueOf(meleeDmg + rangeDmg + siegeDmg));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI(getWindow());
            keepScreenOn(getWindow());
        }
    }

    @Override
    protected void onPause() {
        try {
            saveRows();
        } catch (@NonNull JSONException | IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void saveRows() throws JSONException, IOException {
        for (Row row : allRows) {
            String filename;
            switch (row.getType()) {
                case Row.ROW_MELEE:
                    filename = FILE_NAME_BACKUP_MELEE_ROW;
                    break;
                case Row.ROW_RANGE:
                    filename = FILE_NAME_BACKUP_RANGE_ROW;
                    break;
                default:
                    filename = FILE_NAME_BACKUP_SIEGE_ROW;
            }
            FileOutputStream outputStream = this.openFileOutput(filename, MODE_PRIVATE);
            outputStream.write(row.toJson().toString().getBytes());
            outputStream.close();
        }
    }

    @NonNull
    private Row retrieveRow(String filename, int rowType) throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.openFileInput(filename)));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line);
        }
        bufferedReader.close();
        return new Row(new JSONObject(builder.toString()), rowType);
    }

    public enum THEME {
        MONSTER, NILFGAARD, NORTHERN_KINGDOMS, SCOIATAEL
    }
}