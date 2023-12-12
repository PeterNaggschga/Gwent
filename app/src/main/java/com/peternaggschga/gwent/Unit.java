package com.peternaggschga.gwent;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Unit {

    private static final String JSON_KEY_BASE_AD = "baseAD";
    private static final String JSON_KEY_EPIC = "epic";
    private static final String JSON_KEY_JASKIER = "jaskier";
    private static final String JSON_KEY_REVENGE = "revenge";
    private static final String JSON_KEY_BINDING = "binding";
    private static final String JSON_KEY_MORAL_BOOST = "moralBoost";
    private static final String JSON_KEY_ROW = "row";

    private final int baseAD;
    private final boolean epic;
    private final boolean jaskier;
    private final boolean revenge;
    private final int binding;
    private final boolean moralBoost;
    private int buffAD = -1;
    private boolean buffed = false;
    private int row = Row.ROW_ALL;

    public Unit(@NonNull JSONObject jsonObject) throws JSONException {
        this.baseAD = jsonObject.getInt(JSON_KEY_BASE_AD);
        this.epic = jsonObject.getBoolean(JSON_KEY_EPIC);
        this.jaskier = jsonObject.getBoolean(JSON_KEY_JASKIER);
        this.revenge = jsonObject.getBoolean(JSON_KEY_REVENGE);
        this.binding = jsonObject.getInt(JSON_KEY_BINDING);
        this.moralBoost = jsonObject.getBoolean(JSON_KEY_MORAL_BOOST);
        this.row = jsonObject.getInt(JSON_KEY_ROW);
        if (this.epic) {
            this.buffAD = baseAD;
        }
    }

    public Unit(int baseAD, boolean epic, boolean jaskier, boolean revenge, int binding, boolean moralBoost) {
        this.baseAD = baseAD;
        this.epic = epic;
        this.jaskier = jaskier;
        this.revenge = revenge;
        this.binding = binding;
        this.moralBoost = moralBoost;
        if (epic) {
            this.buffAD = baseAD;
        }
    }

    public Unit(@NonNull Unit unit) {
        this.baseAD = unit.getBaseAD();
        this.epic = unit.isEpic();
        this.jaskier = unit.isJaskier();
        this.revenge = unit.isRevenge();
        this.binding = unit.getBinding();
        this.moralBoost = unit.isMoralBoost();
        this.row = unit.getRow();
        this.buffed = unit.isBuffed();
        if (unit.isEpic()) {
            this.buffAD = this.baseAD;
        }
    }

    public int getBaseAD() {
        return baseAD;
    }

    public int getBuffAD() {
        return buffAD;
    }

    public void setBuffAD(int buffAD) {
        this.buffAD = buffAD;
    }

    public boolean isEpic() {
        return epic;
    }

    public boolean isJaskier() {
        return jaskier;
    }

    public boolean isRevenge() {
        return revenge;
    }

    public int getBinding() {
        return binding;
    }

    public boolean isMoralBoost() {
        return moralBoost;
    }

    public boolean isBuffed() {
        return buffed;
    }

    public void setBuffed(boolean buffed) {
        this.buffed = buffed;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        return new JSONObject().put(JSON_KEY_BASE_AD, this.baseAD)
                .put(JSON_KEY_EPIC, this.epic)
                .put(JSON_KEY_JASKIER, this.jaskier)
                .put(JSON_KEY_REVENGE, this.revenge)
                .put(JSON_KEY_BINDING, this.binding)
                .put(JSON_KEY_MORAL_BOOST, this.moralBoost)
                .put(JSON_KEY_ROW, this.row);
    }

    @NonNull
    public String toString(@NonNull Context context, @Nullable Object object) {
        Set<String> arguments = object == null ? Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(context).getStringSet("unit_string", new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.preference_unit_string_defaultValues))))) : new HashSet<>(Arrays.asList(object.toString().replace("[", "").replace("]", "").split(", ")));
        boolean type = false;
        boolean baseAD = false;
        boolean buffAD = false;
        boolean ability = false;
        String[] values = context.getResources().getStringArray(R.array.preference_unit_string_values);
        for (String argument : arguments) {
            if (argument.equals(values[0])) {
                type = true;
            } else if (argument.equals(values[1])) {
                baseAD = true;
            } else if (argument.equals(values[2])) {
                buffAD = true;
            } else if (argument.equals(values[3])) {
                ability = true;
            }
        }
        StringBuilder builder = new StringBuilder();
        if (type) {
            switch (this.row) {
                case Row.ROW_MELEE:
                    builder.append(context.getString(R.string.unit_toString_melee));
                    break;
                case Row.ROW_RANGE:
                    builder.append(context.getString(R.string.unit_toString_range));
                    break;
                case Row.ROW_SIEGE:
                    builder.append(context.getString(R.string.unit_toString_siege));
            }
        } else {
            builder.append(context.getString(R.string.unit_toString_unit));
        }
        if (baseAD || buffAD || (ability && (this.jaskier || this.revenge || this.moralBoost || this.binding != 0))) {
            String[] abilities = context.getResources().getStringArray(R.array.popup_add_card_ability_values);
            builder.append(" (");
            if (ability && (this.jaskier || this.revenge || this.moralBoost || this.binding != 0)) {
                if (this.jaskier) {
                    builder.append(abilities[2]);
                } else if (this.revenge) {
                    builder.append(abilities[3]);
                } else if (this.moralBoost) {
                    builder.append(abilities[1]);
                } else {
                    builder.append(abilities[4]);
                }
                if (baseAD || buffAD) {
                    builder.append(", ");
                }
            }
            if (baseAD) {
                builder.append(this.baseAD);
                if (buffAD && this.buffed) {
                    builder.append("→");
                    builder.append(this.buffAD);
                }
            } else if (buffAD) {
                builder.append(this.buffAD);
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
