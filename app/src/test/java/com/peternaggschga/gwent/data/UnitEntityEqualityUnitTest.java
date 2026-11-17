package com.peternaggschga.gwent.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class UnitEntityEqualityUnitTest {
    private UnitEntity unit(int id, boolean epic, int damage, Ability ability, Integer squad, RowType row) {
        UnitEntity result = new UnitEntity(epic, damage, ability, squad, row);
        result.setId(id);
        return result;
    }

    @Test
    public void equalIdentityAndStatsHaveEqualHashes() {
        UnitEntity first = unit(7, false, 8, Ability.BINDING, 2, RowType.RANGE);
        UnitEntity second = unit(7, false, 8, Ability.BINDING, 2, RowType.RANGE);
        UnitEntity third = unit(7, false, 8, Ability.BINDING, 2, RowType.RANGE);
        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(1, new HashSet<>(Arrays.asList(first, second, third)).size());
        assertNotEquals(null, first);
        assertNotEquals("not a unit", first);
    }

    @Test
    public void eachPersistedFieldParticipatesInEquality() {
        UnitEntity original = unit(7, false, 8, Ability.BINDING, 2, RowType.RANGE);
        List<UnitEntity> different = Arrays.asList(
                unit(8, false, 8, Ability.BINDING, 2, RowType.RANGE),
                unit(7, true, 8, Ability.BINDING, 2, RowType.RANGE),
                unit(7, false, 9, Ability.BINDING, 2, RowType.RANGE),
                unit(7, false, 8, Ability.BINDING, 3, RowType.RANGE),
                unit(7, false, 8, Ability.BINDING, 2, RowType.MELEE));
        for (UnitEntity other : different) {
            assertNotEquals(original, other);
            assertNotEquals(other, original);
        }
        assertNotEquals(unit(7, false, 8, Ability.NONE, null, RowType.MELEE),
                unit(7, false, 8, Ability.MORAL_BOOST, null, RowType.MELEE));
    }

    @Test
    public void nullSquadsAreHandledAndStatCopiesDoNotMutateOriginal() {
        UnitEntity original = unit(7, false, 2, Ability.BERSERKER, null, RowType.MELEE);
        UnitEntity identical = unit(7, false, 2, Ability.BERSERKER, null, RowType.MELEE);
        assertEquals(original, identical);
        assertEquals(original.hashCode(), identical.hashCode());
        UnitEntity transformed = original.withStats(14, Ability.MORAL_BOOST, null);
        assertEquals(original.getId(), transformed.getId());
        assertNotEquals(original, transformed);
        assertEquals(original, identical);
        assertSame(original.getAbility(), Ability.BERSERKER);
    }

    @Test
    public void copyRetainsAllStatsButStartsWithUnsetIdentity() {
        UnitEntity original = unit(7, true, 8, Ability.BINDING, UnitEntity.BERSERKER_SQUAD, RowType.RANGE);
        UnitEntity copied = original.copy();
        assertNotSame(original, copied);
        assertEquals(0, copied.getId());
        assertEquals(original.isEpic(), copied.isEpic());
        assertEquals(original.getDamage(), copied.getDamage());
        assertEquals(original.getAbility(), copied.getAbility());
        assertEquals(original.getSquad(), copied.getSquad());
        assertEquals(original.getRow(), copied.getRow());
        copied.setDamage(10);
        copied.setId(9);
        assertEquals(8, original.getDamage());
        assertEquals(7, original.getId());
    }

    @Test
    public void withStatsRetainsIdentityAndValidatesNewStatsWithoutMutatingOriginal() {
        UnitEntity original = unit(7, false, 8, Ability.BINDING, 2, RowType.RANGE);
        UnitEntity changed = original.withStats(14, Ability.MORAL_BOOST, null);
        assertNotSame(original, changed);
        assertEquals(7, changed.getId());
        assertEquals(RowType.RANGE, changed.getRow());
        assertEquals(14, changed.getDamage());
        assertEquals(Ability.MORAL_BOOST, changed.getAbility());
        assertNull(changed.getSquad());
        assertThrows(IllegalArgumentException.class, () -> original.withStats(-1, Ability.NONE, null));
        assertThrows(IllegalArgumentException.class, () -> original.withStats(8, Ability.BINDING, null));
        assertThrows(IllegalArgumentException.class, () -> original.withStats(8, Ability.BINDING, 0));
        assertThrows(IllegalArgumentException.class, () -> original.withStats(8, Ability.NONE, 2));
        assertEquals(8, original.getDamage());
        assertEquals(Ability.BINDING, original.getAbility());
        assertEquals(Integer.valueOf(2), original.getSquad());
    }

    @Test
    public void defaultListComparisonSuppressesEqualSnapshotsButEmitsChangedStats() {
        UnitEntity original = unit(7, false, 2, Ability.BERSERKER, null, RowType.MELEE);
        UnitEntity identical = unit(7, false, 2, Ability.BERSERKER, null, RowType.MELEE);
        List<UnitEntity> before = Collections.singletonList(original);
        List<UnitEntity> after = Collections.singletonList(original.withStats(14, Ability.MORAL_BOOST, null));
        Flowable.just(before, Collections.singletonList(identical), after).distinctUntilChanged()
                .test().assertResult(before, after);
    }
}
