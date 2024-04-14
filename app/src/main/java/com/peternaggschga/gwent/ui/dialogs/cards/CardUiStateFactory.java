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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @todo Documentation
 * @todo Add testing.
 */
public class CardUiStateFactory {
    private final boolean weather;
    private final boolean horn;
    @NonNull
    private final Map<Color, Integer> damageTextColors = new HashMap<>(Color.values().length);

    public CardUiStateFactory(@NonNull Context context, boolean weather, boolean horn) {
        this.weather = weather;
        this.horn = horn;
        damageTextColors.put(DEFAULT, context.getColor(R.color.color_damage_textColor));
        damageTextColors.put(BUFFED, context.getColor(R.color.color_damage_textColor_buffed));
        damageTextColors.put(DEBUFFED, context.getColor(R.color.color_damage_textColor_debuffed));
    }

    @NonNull
    public List<CardUiState> createCardUiState(@NonNull List<UnitEntity> units) {
        DamageCalculator calculator = DamageCalculatorUseCase.getDamageCalculator(weather, horn, units);
        List<CardUiState> result = new ArrayList<>(units.size());
        for (UnitEntity unit : units) {
            result.add(createCardUiState(unit, calculator));
        }
        return result;
    }

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
