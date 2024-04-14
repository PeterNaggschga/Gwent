package com.peternaggschga.gwent.ui.dialogs.cards;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEBUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEFAULT;

import android.content.Context;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.domain.cases.DamageCalculatorUseCase;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A factory class responsible for creating CardUiState objects from UnitEntity objects.
 * @see CardUiState
 */
public class CardUiStateFactory {
    /**
     * Boolean defining the status of the weather debuff in the row
     * for which CardUiState objects are created.
     */
    private final boolean weather;

    /**
     * Boolean defining the status of the horn buff in the row
     * for which CardUiState objects are created.
     */
    private final boolean horn;

    /**
     * Map containing a color integer for each Color value possible,
     * i.e., Color#DEFAULT, Color#BUFFED, and Color#DEBUFFED.
     */
    @NonNull
    private final Map<Color, Integer> damageTextColors = new HashMap<>(Color.values().length);

    /**
     * Constructor of a CardUiStateFactory for a row with the given weather and horn
     * (de-)buff values.
     * The Context parameter is used to retrieve the color values saved in #damageTextColors.
     *
     * @param context Context object used to get colors.
     * @param weather Boolean defining whether the weather debuff is active.
     * @param horn    Boolean defining whether the horn buff is active.
     * @see Context#getColor(int)
     */
    public CardUiStateFactory(@NonNull Context context, boolean weather, boolean horn) {
        this.weather = weather;
        this.horn = horn;
        damageTextColors.put(DEFAULT, context.getColor(R.color.color_damage_textColor));
        damageTextColors.put(BUFFED, context.getColor(R.color.color_damage_textColor_buffed));
        damageTextColors.put(DEBUFFED, context.getColor(R.color.color_damage_textColor_debuffed));
    }

    /**
     * Creates a List of CardUiState objects from the given List of UnitEntity objects.
     * Basically calls #createCardUiState(UnitEntity, DamageCalculator) for each given UnitEntity.
     * @see #createCardUiState(UnitEntity, DamageCalculator)
     * @param units List of UnitEntity objects that are converted to CardUiState objects.
     * @return A List of CardUiState objects from the given UnitEntity objects.
     */
    @NonNull
    public List<CardUiState> createCardUiState(@NonNull Collection<UnitEntity> units) {
        DamageCalculator calculator = DamageCalculatorUseCase.getDamageCalculator(weather, horn, units);
        return units.stream()
                .map(unit -> createCardUiState(unit, calculator))
                .collect(Collectors.toList());
    }

    /**
     * Creates a CardUiState from the given UnitEntity.
     * @see #createCardUiState(Collection)
     * @param unit UnitEntity that is converted to a CardUiState.
     * @param calculator DamageCalculator used to calculate damage and Color of the given UnitEntity.
     * @return A CardUiState obtained from the given UnitEntity.
     */
    @NonNull
    public CardUiState createCardUiState(@NonNull UnitEntity unit, @NonNull DamageCalculator calculator) {
        int damageBackgroundId = R.drawable.icon_damage_background;
        int damage = unit.calculateDamage(calculator);

        if (unit.isEpic()) {
            switch (damage) {
                case 0:
                default:
                    damageBackgroundId = R.drawable.icon_epic_damage_0;
                    break;
                case 7:
                    damageBackgroundId = R.drawable.icon_epic_damage_7;
                    break;
                case 8:
                    damageBackgroundId = R.drawable.icon_epic_damage_8;
                    break;
                case 10:
                    damageBackgroundId = R.drawable.icon_epic_damage_10;
                    break;
                case 11:
                    damageBackgroundId = R.drawable.icon_epic_damage_11;
                    break;
                case 15:
                    damageBackgroundId = R.drawable.icon_epic_damage_15;
            }
            damage = CardUiState.UNUSED;
        }

        int damageColor = Objects.requireNonNull(damageTextColors.get(unit.isBuffed(calculator)));

        int abilityImage;
        switch (unit.getAbility()) {
            default:
            case NONE:
                abilityImage = CardUiState.UNUSED;
                break;
            case HORN:
                abilityImage = R.drawable.icon_horn;
                break;
            case REVENGE:
                abilityImage = R.drawable.icon_revenge;
                break;
            case BINDING:
                abilityImage = R.drawable.icon_binding;
                break;
            case MORAL_BOOST:
                abilityImage = R.drawable.icon_moral_boost;
        }

        return new CardUiState(unit.getId(), damageBackgroundId, damage, damageColor, abilityImage, unit.getSquad());
    }
}
