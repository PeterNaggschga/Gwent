package com.peternaggschga.gwent.domain.cases;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResetAlertDialogBuilderAdapterUnitTest {
    @Test
    public void callbackDefaultMethodCallsImplementation() {
        for (boolean resetDecision : new boolean[]{false, true}) {
            final boolean[] test = {!resetDecision, true};
            ResetAlertDialogBuilderAdapter.Callback callback = (decision, keepUnit) -> {
                test[0] = decision;
                test[1] = keepUnit;
            };
            callback.reset(resetDecision);
            assertThat(test[0]).isEqualTo(resetDecision);
            assertThat(test[1]).isFalse();
        }
    }
}
