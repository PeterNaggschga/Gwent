package com.peternaggschga.gwent;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class RuleActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        RULES rule = (RULES) getIntent().getSerializableExtra(INTENT_EXTRA_TAG);

        setSupportActionBar(findViewById(R.id.rulesToolbar));

        TextView textView = findViewById(R.id.rulesTextView);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        switch (Objects.requireNonNull(rule)) {
            case GENERAL:
                textView.setText(Html.fromHtml(getString(R.string.rules_general_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_general_title);
                break;
            case COURSE:
                textView.setText(Html.fromHtml(getString(R.string.rules_course_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_course_title);
                break;
            case FACTIONS:
                textView.setText(Html.fromHtml(getString(R.string.rules_factions_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_factions_title);
                break;
            case COMMANDER:
                textView.setText(Html.fromHtml(getString(R.string.rules_commander_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_commander_title);
                break;
            case CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_cards_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_cards_title);
                break;
            case CARD_ABILITIES:
                textView.setText(Html.fromHtml(getString(R.string.rules_card_abilities_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_card_abilities_title);
                break;
            case SPECIAL_CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_special_cards_text)));
                Objects.requireNonNull(actionBar).setTitle(R.string.preference_rules_special_cards_title);
        }
    }

    public static final String INTENT_EXTRA_TAG = "rule_type";

    public enum RULES {
        GENERAL, COURSE, FACTIONS, COMMANDER, CARDS, CARD_ABILITIES, SPECIAL_CARDS

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
