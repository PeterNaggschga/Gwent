package com.peternaggschga.gwent;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RuleActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_TAG = "rule_type";
    public static final int PREFERENCE_KEY_GENERAL = R.string.preference_rules_general_title;
    public static final int PREFERENCE_KEY_COURSE = R.string.preference_rules_course_title;
    public static final int PREFERENCE_KEY_FRACTIONS = R.string.preference_rules_factions_title;
    public static final int PREFERENCE_KEY_COMMANDER = R.string.preference_rules_commander_title;
    public static final int PREFERENCE_KEY_CARDS = R.string.preference_rules_cards_title;
    public static final int PREFERENCE_KEY_CARD_ABILITIES = R.string.preference_rules_card_abilities_title;
    public static final int PREFERENCE_KEY_SPECIAL_CARDS = R.string.preference_rules_special_cards_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        int key = getIntent().getIntExtra(INTENT_EXTRA_TAG, PREFERENCE_KEY_GENERAL);

        setSupportActionBar(findViewById(R.id.rulesToolbar));
        MainActivity.hideSystemUI(getWindow());

        TextView textView = findViewById(R.id.rulesTextView);

        switch (key) {
            case PREFERENCE_KEY_GENERAL:
                textView.setText(Html.fromHtml(getString(R.string.rules_general_text)));
                break;
            case PREFERENCE_KEY_COURSE:
                textView.setText(Html.fromHtml(getString(R.string.rules_course_text)));
                break;
            case PREFERENCE_KEY_FRACTIONS:
                textView.setText(Html.fromHtml(getString(R.string.rules_factions_text)));
                break;
            case PREFERENCE_KEY_COMMANDER:
                textView.setText(Html.fromHtml(getString(R.string.rules_commander_text)));
                break;
            case PREFERENCE_KEY_CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_cards_text)));
                break;
            case PREFERENCE_KEY_CARD_ABILITIES:
                textView.setText(Html.fromHtml(getString(R.string.rules_card_abilities_text)));
                break;
            case PREFERENCE_KEY_SPECIAL_CARDS:
                textView.setText(Html.fromHtml(getString(R.string.rules_special_cards_text)));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(key);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
