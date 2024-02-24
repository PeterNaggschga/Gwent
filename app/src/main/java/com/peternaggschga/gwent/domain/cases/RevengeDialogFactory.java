package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Completable;

class RevengeDialogFactory {
    /**
     * Boolean constant defining
     * whether the default UnitEntity summoned by the Ability#REVENGE ability is epic.
     *
     * @see Ability#REVENGE
     * @see UnitEntity#epic
     */
    private static final boolean AVENGER_EPIC = false;

    /**
     * Ability constant defining the Ability of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     * @see UnitEntity#ability
     */
    private static final Ability AVENGER_ABILITY = Ability.NONE;

    /**
     * Integer constant defining the damage of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     * @see UnitEntity#damage
     */
    private static final int AVENGER_DAMAGE = 8;

    /**
     * Integer constant defining the squad of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     * @see UnitEntity#squad
     */
    private static final Integer AVENGER_SQUAD = null;

    /**
     * RowType constant defining the row of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     * @see UnitEntity#row
     */
    private static final RowType AVENGER_ROW = RowType.MELEE;

    @NonNull
    public static Dialog getRevengeDialog(@NonNull Context context, @NonNull DialogInterface.OnClickListener onPositiveClickListener,
                                          @NonNull DialogInterface.OnClickListener onNegativeClickListener) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.alertDialog_revenge_title)
                .setMessage(R.string.alertDialog_revenge_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_revenge_positive, onPositiveClickListener)
                .setNegativeButton(R.string.alertDialog_revenge_negative, onNegativeClickListener)
                .create();
    }

    @NonNull
    public static Completable insertAvengers(@NonNull UnitRepository repository, @IntRange(from = 0) int numberOfAvengers) {
        return repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, numberOfAvengers);
    }
}
