package com.peternaggschga.gwent.ui.main;

import static org.valid4j.Assertive.require;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.data.RowType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An observer class responsible for creating and posting MenuUiState when notified,
 * i.e., when the state of the observed row changes.
 *
 * @see MenuUiState
 */
public class MenuUpdateObserver implements Observer<RowUiState> {
    /**
     * A MutableLiveData object that is updated by this observer.
     */
    @NonNull
    private final MutableLiveData<MenuUiState> menuObserver;

    /**
     * A List of two LiveData objects issuing the state of the rows not observed by this observer.
     */
    @NonNull
    private final List<LiveData<RowUiState>> rowStates;

    /**
     * Constructor of a MenuUpdateObserver.
     * Should only be called by factory method #getObserver()
     * @param menuObserver MutableLiveData object that updates are pushed to.
     * @param rowStates List of two LiveData objects where the state of other rows is queried from.
     * @throws org.valid4j.errors.RequireViolation When rowStates does not contain exactly two values.
     * @see #getObserver(RowType, MutableLiveData, Map)
     */
    private MenuUpdateObserver(@NonNull MutableLiveData<MenuUiState> menuObserver, @NonNull List<LiveData<RowUiState>> rowStates) {
        require(rowStates.size() == 2);
        this.menuObserver = menuObserver;
        this.rowStates = rowStates;
    }

    /**
     * Returns a new MenuUpdateObserver for the given row.
     * Factory method for MenuUpdateObserver.
     *
     * @param row          RowType defining which row is being observed.
     * @param menuObserver MutableLiveData object that updates are pushed to.
     * @param rowObservers Map from RowType to the MutableLiveData object associated with that row.
     * @return A MenuUpdateObserver for the given row.
     * @throws org.valid4j.errors.RequireViolation When rowObservers does not contain non-null values for each RowType.
     * @see MenuUpdateObserver#MenuUpdateObserver(MutableLiveData, List)
     */
    @NonNull
    public static MenuUpdateObserver getObserver(@NonNull RowType row,
                                                 @NonNull MutableLiveData<MenuUiState> menuObserver,
                                                 @NonNull Map<RowType, MutableLiveData<RowUiState>> rowObservers) {
        require(rowObservers.size() == RowType.values().length);
        require(!rowObservers.containsKey(null));
        require(!rowObservers.containsValue(null));
        return new MenuUpdateObserver(menuObserver,
                rowObservers.values()
                        .stream()
                        .filter(rowUiStateMutableLiveData -> rowUiStateMutableLiveData != rowObservers.get(row))
                        .collect(Collectors.toList()));
    }

    /**
     * Creates a new MenuUiState based on the given RowUiState and the objects
     * issued by the LiveData in #rowStates and posts it to #menuObserver.
     * Only posts new state if it is actually different from the value saved before.
     * @param rowUiState RowUiState representing the updated state of the observed row.
     */
    @Override
    public void onChanged(RowUiState rowUiState) {
        int damage = 0;
        boolean reset = false;
        boolean weather = false;
        boolean burn = false;
        List<RowUiState> states = Arrays.asList(rowUiState, rowStates.get(0).getValue(), rowStates.get(1).getValue());
        for (RowUiState state :
                states.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())) {
            damage += state.getDamage();
            reset |= state.isHorn();
            weather |= state.isWeather();
            burn |= state.getUnits() != 0;
        }
        reset |= weather || burn;
        MenuUiState newState = new MenuUiState(damage, reset, weather, burn);
        if (!newState.equals(menuObserver.getValue())) {
            menuObserver.postValue(newState);
        }
    }
}
