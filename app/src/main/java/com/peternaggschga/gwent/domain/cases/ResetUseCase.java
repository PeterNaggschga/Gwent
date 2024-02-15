package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @todo Review function accessibility
 */
public class ResetUseCase {
    public static final int TRIGGER_BUTTON_CLICK = 0;
    public static final int TRIGGER_FACTION_SWITCH = 1;

    @NonNull
    private final UnitRepository repository;
    @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH)
    private final int trigger;
    private final boolean monsterReset;

    public ResetUseCase(@NonNull UnitRepository repository,
                        @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger,
                        boolean monsterReset) {
        this.repository = repository;
        this.trigger = trigger;
        this.monsterReset = monsterReset && trigger != TRIGGER_FACTION_SWITCH;
    }

    @NonNull
    private static Maybe<UnitEntity> getRandomUnit(@NonNull UnitRepository repository) {
        return Maybe.create(emitter -> {
            List<UnitEntity> units = repository.getUnits()
                    .blockingGet()
                    .stream()
                    .filter(unit -> !unit.isEpic())
                    .collect(Collectors.toList());
            if (units.isEmpty()) {
                emitter.onComplete();
            } else {
                emitter.onSuccess(units.get(new Random().nextInt(units.size())));
            }
        });
    }

    @NonNull
    public static Maybe<UnitEntity> reset(@NonNull UnitRepository repository, boolean keepUnit) {
        if (!keepUnit) {
            return repository.reset(null).andThen(Maybe.empty());
        }
        return Maybe.create(emitter -> {
            UnitEntity keptUnit = getRandomUnit(repository).blockingGet();
            repository.reset(keptUnit).blockingAwait();
            if (keptUnit == null) {
                emitter.onComplete();
            } else {
                emitter.onSuccess(keptUnit);
            }
        });
    }

    private static void showKeptUnitToast(@NonNull Context context, @NonNull UnitEntity unit) {
        ContextCompat.getMainExecutor(context).execute(() -> Toast.makeText(context,
                        context.getString(R.string.alertDialog_factionreset_monster_toast_keep, unit.toString()),
                        Toast.LENGTH_LONG)
                .show());
    }

    @NonNull
    public Maybe<UnitEntity> reset(boolean keepUnit) {
        return reset(repository, keepUnit);
    }

    @NonNull
    public Completable reset() {
        return Completable.fromMaybe(reset(repository, false));
    }

    @NonNull
    public Single<Boolean> showMonsterDialog() {
        return Single.concat(Single.just(monsterReset),
                Single.create(emitter -> emitter.onSuccess(repository.getUnits()
                        .blockingGet()
                        .stream()
                        .anyMatch(unit -> !unit.isEpic()))
                )).all(bool -> bool);
    }

    @NonNull
    public Single<AlertDialog> getWarningDialog(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        return Single.create(dialogEmitter -> {
            Consumer<AlertDialog.Builder> builderCompleter = builder -> {
                builder.setTitle(R.string.alertDialog_reset_title)
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

    @NonNull
    private Single<AlertDialog.Builder> getButtonClickDialogBuilder(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        return Single.create(emitter -> {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context).setMessage(R.string.alertDialog_reset_msg_default)
                    .setCancelable(true)
                    .setOnCancelListener(dialog -> resetEmitter.onSuccess(false));

            if (showMonsterDialog().blockingGet()) {
                final boolean[] keepUnit = new boolean[]{true};
                builder.setMultiChoiceItems(new CharSequence[]{context.getString(R.string.alertDialog_reset_checkbox)},
                                keepUnit,
                                (dialog, which, isChecked) -> keepUnit[which] = isChecked)
                        .setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                            // noinspection CheckResult, ResultOfMethodCallIgnored
                            reset(keepUnit[0]).subscribe(unitEntity -> showKeptUnitToast(context, unitEntity));
                        });
            } else {
                builder.setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    reset().subscribe(() -> resetEmitter.onSuccess(true));
                });
            }
            emitter.onSuccess(builder);
        });
    }

    @NonNull
    private Single<AlertDialog.Builder> getFactionSwitchDialogBuilder(@NonNull Context context, @NonNull SingleEmitter<Boolean> resetEmitter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.alertDialog_reset_msg_faction_switch)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    reset().subscribe(() -> resetEmitter.onSuccess(true));
                });
        return Single.just(builder);
    }
}
