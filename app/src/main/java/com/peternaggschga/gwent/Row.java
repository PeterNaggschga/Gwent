package com.peternaggschga.gwent;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Row {
    public static final int ROW_ALL = 0;
    public static final int ROW_MELEE = 1;
    public static final int ROW_RANGE = 2;
    public static final int ROW_SIEGE = 3;

    private static final String JSON_KEY_WEATHER = "weather";
    private static final String JSON_KEY_HORN = "horn";
    private static final String JSON_KEY_JASKIER = "jaskier";
    private static final String JSON_KEY_REVENGE = "revenge";
    private static final String JSON_KEY_UNIT_ARRAY = "units";

    private final int type;
    final private List<Unit> units = new ArrayList<>();
    private boolean weather = false;
    private boolean horn = false;
    private boolean jaskier = false;
    private boolean revenge = false;
    private int overallDamage = 0;

    public Row(@NonNull JSONObject jsonObject, int type) throws JSONException, IllegalArgumentException {
        if (type == ROW_MELEE || type == ROW_SIEGE || type == ROW_RANGE) {
            this.type = type;
            this.weather = jsonObject.getBoolean(JSON_KEY_WEATHER);
            this.horn = jsonObject.getBoolean(JSON_KEY_HORN);
            this.jaskier = jsonObject.getBoolean(JSON_KEY_JASKIER);
            this.revenge = jsonObject.getBoolean(JSON_KEY_REVENGE);
            JSONArray unitArray = jsonObject.getJSONArray(JSON_KEY_UNIT_ARRAY);
            for (int i = 0; i < unitArray.length(); i++) {
                JSONObject unit = unitArray.getJSONObject(i);
                this.units.add(new Unit(unit));
            }
        } else {
            throw new IllegalArgumentException("Row-type must be 1, 2 or 3");
        }

    }

    public Row(int type) throws IllegalArgumentException {
        if (type == ROW_MELEE || type == ROW_SIEGE || type == ROW_RANGE) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("Row-type must be 1, 2 or 3");
        }
    }

    public int getType() {
        return type;
    }

    public void addUnit(@NonNull Unit unit) {
        if (unit.isJaskier()) {
            this.jaskier = true;
        }
        if (unit.isRevenge()) {
            this.revenge = true;
        }
        unit.setRow(this.type);
        this.units.add(unit);
    }

    public void removeUnit(int i) {
        Unit removeUnit = units.get(i);
        if (removeUnit.isJaskier() && getJaskierMoralBoosterRevengeCount()[0] < 2) {
            this.jaskier = false;
        }
        if (removeUnit.isRevenge() && getJaskierMoralBoosterRevengeCount()[2] < 2) {
            this.revenge = false;
        }
        this.units.remove(i);
    }

    public void removeUnit(Unit unit) {
        removeUnit(this.units.indexOf(unit));
    }

    @NonNull
    public List<Unit> getAllUnits() {
        return units;
    }

    public boolean isWeather() {
        return weather;
    }

    public void setWeather(boolean weather) {
        this.weather = weather;
    }

    public boolean isHorn() {
        return horn;
    }

    public void setHorn(boolean horn) {
        this.horn = horn;
    }

    public boolean isRevenge() {
        return revenge;
    }

    public int getOverallDamage() {
        updateOverallDamage();
        return this.overallDamage;
    }

    private void updateOverallDamage() {
        int[] jaskierMoralBoostCount = getJaskierMoralBoosterRevengeCount();
        int overallDamage = 0;
        for (Unit unit : this.units) {
            if (!unit.isEpic()) {
                boolean buffed = false;
                int buffAD = unit.getBaseAD();
                if (this.weather && buffAD > 0) {
                    buffAD = 1;
                }
                int binding = unit.getBinding();
                if (binding != 0) {
                    int factor = getBindingUnits(binding).size();
                    if (factor > 1) {
                        buffAD *= factor;
                        buffed = true;
                    }
                }
                if (jaskierMoralBoostCount[1] > 0) {
                    buffAD += unit.isMoralBoost() ? jaskierMoralBoostCount[1] - 1 : jaskierMoralBoostCount[1];
                    buffed = !unit.isMoralBoost() || jaskierMoralBoostCount[1] - 1 > 0;
                }
                if ((jaskier && !horn && (!unit.isJaskier() || (unit.isJaskier() && getJaskierMoralBoosterRevengeCount()[0] > 1))) || horn) {
                    buffAD *= 2;
                    buffed = true;
                }
                unit.setBuffed(buffed);
                unit.setBuffAD(buffAD);
            }
            overallDamage += unit.getBuffAD();
        }
        this.overallDamage = overallDamage;
    }

    @NonNull
    public List<Unit> getBindingUnits(int binding) {
        List<Unit> unitList = new ArrayList<>();
        for (Unit unit : this.units) {
            if (unit.getBinding() == binding) {
                unitList.add(unit);
            }
        }
        return unitList;
    }

    @NonNull
    public int[] getJaskierMoralBoosterRevengeCount() {
        int[] count = {0, 0, 0};
        for (Unit unit : this.units) {
            if (unit.isJaskier()) {
                count[0]++;
            } else if (unit.isMoralBoost()) {
                count[1]++;
            } else if (unit.isRevenge()) {
                count[2]++;
            }
        }
        return count;
    }

    public int getCardCount() {
        return this.units.size();
    }

    public boolean isEpicOnly() {
        boolean returnBool = true;
        for (Unit unit : this.units) {
            if (!unit.isEpic()) {
                returnBool = false;
                break;
            }
        }
        return returnBool;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONArray unitArray = new JSONArray();
        for (Unit unit : this.units) {
            unitArray.put(unit.toJson());
        }
        return new JSONObject().put(JSON_KEY_WEATHER, this.weather)
                .put(JSON_KEY_HORN, this.horn)
                .put(JSON_KEY_JASKIER, this.jaskier)
                .put(JSON_KEY_REVENGE, this.revenge)
                .put(JSON_KEY_UNIT_ARRAY, unitArray);
    }

    public void clear(boolean keepRandomUnit) {
        if (keepRandomUnit) {
            Unit randomUnit;
            do {
                randomUnit = this.units.get(new Random().nextInt(this.units.size()));
            } while (randomUnit.isEpic());
            clear(false);
            addUnit(randomUnit);
        } else {
            for (int i = getCardCount(); i > 0; i--) {
                removeUnit(i - 1);
            }
        }
    }
}
