package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Completable;

/**
 * A factory class providing a Dialog asking whether the Ability#REVENGE ability should be activated
 * and uses the given Callbacks in #getRevengeDialog().
 *
 * @todo Restructure as an adapter class for AlertDialog.Builder (see ResetAlertDialogBuilderAdapter)
 * @see Ability#REVENGE
 * @see #getRevengeDialog(Context, DialogInterface.OnClickListener, DialogInterface.OnClickListener)
 */
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

    /**
     * Creates a Dialog
     * asking whether the Ability#REVENGE ability of a removed UnitEntity should be activated.
     * The onPositiveClickListener should call #insertAvengers().
     *
     * @param context                 Context of the created Dialog.
     * @param onPositiveClickListener DialogInterface.OnClickListener which is called when the positive button is clicked.
     * @param onNegativeClickListener DialogInterface.OnClickListener which is called when the negative button is clicked.
     * @return A Dialog asking about the activation of the Ability#REVENGE ability.
     * @see #insertAvengers(UnitRepository, int)
     */
    @NonNull
    public static Dialog getRevengeDialog(@NonNull Context context, @NonNull DialogInterface.OnClickListener onPositiveClickListener,
                                          @NonNull DialogInterface.OnClickListener onNegativeClickListener) {
        return new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alertDialog_revenge_title)
                .setMessage(R.string.alertDialog_revenge_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_revenge_positive, onPositiveClickListener)
                .setNegativeButton(R.string.alertDialog_revenge_negative, onNegativeClickListener)
                .create();
    }

    /**
     * Inserts numberOfAvengers avenger units into the given UnitRepository.
     * The inserted UnitEntity objects have the attributes defined in #AVENGER_EPIC,
     * #AVENGER_DAMAGE, #AVENGER_ABILITY, #AVENGER_SQUAD, and #AVENGER_ROW.
     *
     * @param repository       UnitRepository where avengers are inserted.
     * @param numberOfAvengers Integer defining how many avengers are inserted.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public static Completable insertAvengers(@NonNull UnitRepository repository, @IntRange(from = 0) int numberOfAvengers) {
        return repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, numberOfAvengers);
    }
}
