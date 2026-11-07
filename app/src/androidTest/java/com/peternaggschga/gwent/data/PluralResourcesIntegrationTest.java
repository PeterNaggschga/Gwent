package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class PluralResourcesIntegrationTest {
    private Context localizedContext(String languageTag) {
        Context context = ApplicationProvider.getApplicationContext();
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(Locale.forLanguageTag(languageTag));
        return context.createConfigurationContext(configuration);
    }

    @Test
    public void squadCountsHandleZeroOneAndManyInBothLanguages() {
        for (String language : new String[]{"en", "de-DE"}) {
            Resources resources = localizedContext(language).getResources();
            for (int count : new int[]{0, 1, 3}) {
                String noun = language.equals("en")
                        ? (count == 1 ? "unit" : "units")
                        : (count == 1 ? "Einheit" : "Einheiten");
                assertThat(resources.getQuantityString(R.plurals.popUp_add_card_binding_count, count, 2, count))
                        .isEqualTo(language.equals("en") ? "Squad 2 contains " + count + " " + noun + "."
                                : "Gruppe 2 enthält " + count + " " + noun + ".");
            }
        }
    }

    @Test
    public void scorchAndAvengerTextUseTheAffectedCount() {
        for (String language : new String[]{"en", "de-DE"}) {
            Resources resources = localizedContext(language).getResources();
            for (int count : new int[]{1, 3}) {
                String scorch = resources.getQuantityString(R.plurals.alertDialog_burn_msg, count, count, "DETAILS");
                assertThat(scorch).endsWith("\nDETAILS");
                assertThat(scorch).contains(count == 1
                        ? (language.equals("en") ? "this unit" : "diese Einheit")
                        : (language.equals("en") ? "these 3 units" : "diese 3 Einheiten"));
                assertThat(resources.getQuantityString(R.plurals.alertDialog_revenge_msg, count, count))
                        .contains(count + " " + (language.equals("en")
                                ? (count == 1 ? "unit that" : "units that")
                                : (count == 1 ? "Einheit vom" : "Einheiten vom")));
                assertThat(resources.getQuantityString(R.plurals.alertDialog_revenge_positive, count))
                        .isEqualTo(language.equals("en")
                                ? (count == 1 ? "Summon avenger" : "Summon avengers")
                                : (count == 1 ? "Einen Rächer rufen" : "Rächer rufen"));
            }
        }
    }

    @Test
    public void collectionsPluralizeUnitsAndHeroesWithoutChangingSingleDescriptions() {
        for (String language : new String[]{"en", "de-DE"}) {
            Context context = localizedContext(language);
            for (boolean epic : new boolean[]{false, true}) {
                UnitEntity unit = new UnitEntity(epic, 4, Ability.NONE, null, RowType.MELEE);
                assertThat(UnitEntity.collectionToString(context, Collections.singletonList(unit)))
                        .isEqualTo(unit.toString(context));
                String description = UnitEntity.collectionToString(context, Arrays.asList(unit, unit, unit));
                assertThat(description).startsWith("3 ");
                assertThat(description).contains(language.equals("en")
                        ? (epic ? "heroes" : "units") : (epic ? "Nahkampfhelden" : "Nahkampfeinheiten"));
                UnitEntity other = new UnitEntity(epic, 5, Ability.NONE, null, RowType.RANGE);
                assertThat(UnitEntity.collectionToString(context, Arrays.asList(unit, unit, other)))
                        .contains(other.toString(context));
            }
        }
    }
}
