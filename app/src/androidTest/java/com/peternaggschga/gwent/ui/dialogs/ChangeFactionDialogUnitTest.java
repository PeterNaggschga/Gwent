package com.peternaggschga.gwent.ui.dialogs;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;
import static org.junit.Assert.fail;
import static java.lang.Thread.sleep;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ChangeFactionDialogUnitTest {
    private final Map<Integer, Integer> selectThemeMap = new HashMap<>(8);
    private ChangeFactionDialog dialog;

    @Before
    public void initThemeMap() {
        selectThemeMap.put(R.id.monsterCardView, THEME_MONSTER);
        selectThemeMap.put(R.id.monsterButton, THEME_MONSTER);
        selectThemeMap.put(R.id.nilfgaardCardView, THEME_NILFGAARD);
        selectThemeMap.put(R.id.nilfgaardButton, THEME_NILFGAARD);
        selectThemeMap.put(R.id.northernKingdomsCardView, THEME_NORTHERN_KINGDOMS);
        selectThemeMap.put(R.id.northernKingdomsButton, THEME_NORTHERN_KINGDOMS);
        selectThemeMap.put(R.id.scoiataelCardView, THEME_SCOIATAEL);
        selectThemeMap.put(R.id.scoiataelButton, THEME_SCOIATAEL);
    }

    public void initDialog(@NonNull ChangeFactionDialog.Callback callback) throws InterruptedException {
        new Handler(Looper.getMainLooper()).post(() -> dialog = new ChangeFactionDialog(ApplicationProvider.getApplicationContext(), callback));
        sleep(50);
    }

    @Test
    public void onBackgroundClickCancelsDialog() throws InterruptedException {
        initDialog(theme -> fail());
        final boolean[] canceled = {false};
        dialog.setOnCancelListener(dialog -> canceled[0] = true);
        dialog.findViewById(R.id.factionBackground).callOnClick();
        sleep(10);
        assertThat(canceled[0]).isTrue();
    }

    @Test
    public void onSelectCancelsDialog() throws InterruptedException {
        for (Integer id : selectThemeMap.keySet()) {
            initDialog(theme -> {
            });
            final boolean[] canceled = {false};
            dialog.setOnCancelListener(dialog -> canceled[0] = true);
            dialog.findViewById(id).callOnClick();
            sleep(10);
            assertThat(canceled[0]).isTrue();
        }
    }

    @Test
    public void onSelectCallsCallback() throws InterruptedException {
        for (Map.Entry<Integer, Integer> entry : selectThemeMap.entrySet()) {
            final boolean[] callback = {false};
            initDialog(theme -> callback[0] = theme == entry.getValue());
            dialog.findViewById(entry.getKey()).callOnClick();
            sleep(10);
            assertThat(callback[0]).isTrue();
        }
    }
}
