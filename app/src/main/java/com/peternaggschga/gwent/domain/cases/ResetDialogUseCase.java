package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * A use case class responsible for the execution of the reset operation.
 *
 * @todo Move dialog stuff here.
 */
public class ResetDialogUseCase {
    /**
     * Context where the warning Dialog is shown.
     */
    @NonNull
    private final Context context;

    /**
     * UnitRepository where units are burned.
     */
    @NonNull
    private final UnitRepository repository;

    /**
     * Integer constant representing that a reset was triggered by a click on the reset button.
     */
    public static final int TRIGGER_BUTTON_CLICK = 0;

    /**
     * Integer constant representing that a reset was triggered by a faction switch.
     * Only relevant if faction reset is activated, i.e.,
     * the SharedPreference referenced by R.string#preference_key_faction_reset.
     *
     * @see R.string#preference_key_faction_reset
     */
    public static final int TRIGGER_FACTION_SWITCH = 1;

    /**
     * Integer representing what triggered this use case.
     * Either #TRIGGER_BUTTON_CLICK or #TRIGGER_FACTION_SWITCH.
     *
     * @see #TRIGGER_BUTTON_CLICK
     * @see #TRIGGER_FACTION_SWITCH
     */
    @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH)
    private final int trigger;

    /**
     * Boolean representing whether the monster ability should be presented, i.e.,
     * if the user should be able to opt for keeping a random unit.
     */
    private final boolean monsterReset;

    /**
     * Constructor of a ResetDialogUseCase for the given UnitRepository and trigger.
     * When monsterTheme is true and the trigger is not #TRIGGER_FACTION_SWITCH,
     * then #monsterReset is initialized as true.
     *
     * @param repository   UnitRepository where the reset that is reset.
     * @param trigger      Integer representing what triggered this reset, i.e.,
     *                     either #TRIGGER_BUTTON_CLICK or #TRIGGER_FACTION_SWITCH.
     * @param monsterTheme Boolean defining whether the current theme is set to the monster theme.
     */
    public ResetDialogUseCase(@NonNull Context context, @NonNull UnitRepository repository,
                              @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger,
                              boolean monsterTheme) {
        this.context = context;
        this.repository = repository;
        this.trigger = trigger;
        this.monsterReset = monsterTheme && trigger != TRIGGER_FACTION_SWITCH;
    }

    /**
     * Selects a random unit that is not epic.
     * If all units are epic or if there are no units at all, an empty Maybe is returned.
     *
     * @return A Maybe emitting the selected unit or nothing if no unit could be selected.
     */
    @NonNull
    private Maybe<UnitEntity> getRandomUnit() {
        return repository.getUnits().flatMapMaybe(units -> {
            units = units.stream()
                    .filter(unit -> !unit.isEpic())
                    .collect(Collectors.toList());
            return units.isEmpty() ? Maybe.empty() : Maybe.just(units.get(new Random().nextInt(units.size())));

        });
    }

    /**
     * Resets the repository. If keepUnit is true, the returned Maybe emits the kept UnitEntity.
     *
     * @param keepUnit Boolean defining whether a single UnitEntity should be kept.
     * @return A Maybe emitting the kept UnitEntity or nothing if keepUnit is false.
     */
    @NonNull
    private Maybe<UnitEntity> reset(boolean keepUnit) {
        if (!keepUnit) {
            return ResetRepositoryUseCase.reset(context, repository).andThen(Maybe.empty());
        }
        return getRandomUnit().concatMap(unit -> ResetRepositoryUseCase
                .reset(context, repository, unit)
                .andThen(unit == null ? Maybe.empty() : Maybe.just(unit)));
    }

    /**
     * Resets the repository. Wrapper function for #reset(boolean).
     *
     * @return A Completable tracking operation status.
     * @see #reset(boolean)
     */
    @NonNull
    public Completable reset() {
        return Completable.fromMaybe(reset(false));
    }

    /**
     * Checks whether the monster reset dialog should be shown.
     * Is true when #monsterReset is true and at least one non-epic unit exists.
     *
     * @return A Single emitting a Boolean deciding whether the monster dialog should be shown.
     */
    @NonNull
    public Single<Boolean> showMonsterDialog() {
        return Single.concat(Single.just(monsterReset),
                        repository.getUnits().map(units -> units.stream()
                                .anyMatch(unit -> !unit.isEpic())))
                .all(bool -> bool);
    }

    /**
     * Creates an AlertDialog warning the user of the upcoming reset.
     * When the user agrees to perform a reset,
     * the resetEmitter is called to emit true, otherwise false.
     * The appearance of the created AlertDialog depends on #trigger and #monsterReset.
     *
     * @param context      Context of the created AlertDialog.
     * @param resetEmitter SingleEmitter that emits whether a reset is performed or not.
     * @return A Single emitting the created AlertDialog.
     * @see #getFactionSwitchDialogBuilder(Context, SingleEmitter)
     * @see #getButtonClickDialogBuilder(Context, SingleEmitter)
     */
    @NonNull
    public Single<AlertDialog> getWarningDialog(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        return Single.create(dialogEmitter -> {
            Consumer<AlertDialog.Builder> builderCompleter = builder -> {
                builder.setTitle(R.string.alertDialog_reset_title)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setNegativeButton(R.string.alertDialog_reset_negative, (dialog, which) -> resetEmitter.onSuccess(false));
                dialogEmitter.onSuccess(builder.create());
            };

            switch (trigger) {
                case TRIGGER_FACTION_SWITCH:
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    getFactionSwitchDialogBuilder(context, resetEmitter).subscribe(builderCompleter);
                    break;
                case TRIGGER_BUTTON_CLICK:
                default:
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    getButtonClickDialogBuilder(context, resetEmitter).subscribe(builderCompleter);
            }
        });
    }

    /**
     * Creates an AlertDialog.Builder object for a reset triggered by a click on the reset button,
     * i.e., #trigger is #TRIGGER_BUTTON_CLICK.
     *
     * @param context      Context of the created AlertDialog.Builder.
     * @param resetEmitter SingleEmitter that emits whether a reset is performed or not.
     * @return A Single emitting the created AlertDialog.Builder.
     * @see #getWarningDialog(Context, SingleEmitter)
     */
    @NonNull
    private Single<AlertDialog.Builder> getButtonClickDialogBuilder(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        return showMonsterDialog().map(showMonsterDialog -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setMessage(R.string.alertDialog_reset_msg_default)
                    .setCancelable(true)
                    .setOnCancelListener(dialog -> resetEmitter.onSuccess(false));

            if (showMonsterDialog) {
                View checkBoxView = View.inflate(context, R.layout.alertdialog_checkbox, null);
                builder.setView(checkBoxView)
                        .setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                            CheckBox checkBox = checkBoxView.findViewById(R.id.alertDialog_checkbox);
                            // noinspection CheckResult, ResultOfMethodCallIgnored
                            reset(checkBox.isChecked()).subscribe(
                                    unitEntity -> {
                                        Toast.makeText(context,
                                                        context.getString(R.string.alertDialog_factionreset_monster_toast_keep, unitEntity.toString(context)),
                                                        Toast.LENGTH_LONG)
                                                .show();
                                        resetEmitter.onSuccess(true);
                                    },
                                    Throwable::printStackTrace,
                                    () -> resetEmitter.onSuccess(true));
                        });
            } else {
                builder.setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    reset().subscribe(() -> resetEmitter.onSuccess(true));
                });
            }
            return builder;
        });
    }

    /**
     * Creates an AlertDialog.Builder object for a reset triggered by a faction switch,
     * i.e., #trigger is #TRIGGER_FACTION_SWITCH.
     *
     * @param context      Context of the created AlertDialog.Builder.
     * @param resetEmitter SingleEmitter that emits whether a reset is performed or not.
     * @return A Single emitting the created AlertDialog.Builder.
     * @see #getWarningDialog(Context, SingleEmitter)
     */
    @NonNull
    private Single<AlertDialog.Builder> getFactionSwitchDialogBuilder(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(R.string.alertDialog_reset_msg_faction_switch)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    reset().subscribe(() -> resetEmitter.onSuccess(true));
                });
        return Single.just(builder);
    }
}
