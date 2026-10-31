package com.peternaggschga.gwent.ui.settings;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import com.peternaggschga.gwent.R;

import java.util.Objects;

/**
 * An AppCompatActivity used to present the rules of the game.
 * Can be accessed from the SettingsActivity.
 * When called, a selected RuleSection must always be given through the calling android.content.Intent
 * at the key defined in INTENT_EXTRA_TAG.
 */
public class RuleActivity extends AppCompatActivity {
    /**
     * String constant defining the identifier where the requested RuleSection is provided
     * in the calling android.content.Intent.
     */
    public static final String INTENT_EXTRA_TAG = "rule_section";

    /**
     * Initializes the content of the selected rule.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rules);

        RuleSection section = Objects.requireNonNull(
                IntentCompat.getSerializableExtra(getIntent(), INTENT_EXTRA_TAG, RuleSection.class));

        setSupportActionBar(findViewById(R.id.rulesToolbar));

        TextView textView = findViewById(R.id.rulesTextView);

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);

        switch (section) {
            case GENERAL:
                textView.setText(Html.fromHtml(getString(R.string.rules_general_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_general_title);
                break;
            case COURSE:
                textView.setText(Html.fromHtml(getString(R.string.rules_course_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_course_title);
                break;
            case FACTIONS:
                textView.setText(Html.fromHtml(getString(R.string.rules_factions_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_factions_title);
                break;
            case COMMANDER:
                textView.setText(Html.fromHtml(getString(R.string.rules_commander_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_commander_title);
                break;
            case CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_cards_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_cards_title);
                break;
            case CARD_ABILITIES:
                textView.setText(Html.fromHtml(getString(R.string.rules_card_abilities_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_card_abilities_title);
                break;
            case SPECIAL_CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_special_cards_text),
                        Html.FROM_HTML_MODE_LEGACY));
                actionBar.setTitle(R.string.preference_rules_special_cards_title);
        }
    }

    /**
     * Called whenever a MenuItem in the options menu is selected.
     * Returns to the calling android.app.Activity when the android.R.idhome item was selected.
     * @param item MenuItem that was selected.
     *
     * @return Boolean defining whether the call has been handled.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
