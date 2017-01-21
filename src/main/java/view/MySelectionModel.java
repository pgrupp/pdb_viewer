package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import java.util.Arrays;

/**
 * Selection model
 * Created by huson on 12/15/15.
 */
public class MySelectionModel<T> extends MultipleSelectionModel<T> {

    private final ObservableSet<Integer> selectedIndices; // the set of selected indices

    private T[] items; // need a copy of this array to map indices to objects, when required

    private int focusIndex = -1; // focus index

    private final ObservableList<Integer> unmodifiableSelectedIndices; // unmodifiable list of selected indices
    private final ObservableList<T> unmodifiableSelectedItems; // unmodifiable list of selected items

    /**
     * Constructor
     *
     * @param items 0 or more items
     */
    @SafeVarargs
    public MySelectionModel(T... items) {
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety
        selectedIndices = FXCollections.observableSet();
        selectionModeProperty().setValue(SelectionMode.MULTIPLE);

        // setup unmodifiable lists
        {
            // first setup observable array lists that listen for changes of the selectedIndices set
            final ObservableList<Integer> selectedIndicesAsList = FXCollections.observableArrayList();
            final ObservableList<T> selectedItems = FXCollections.observableArrayList();
            selectedIndices.addListener((SetChangeListener<Integer>) c -> {
                if (c.wasAdded()) {
                    selectedIndicesAsList.add(c.getElementAdded());
                    selectedItems.add(getItems()[c.getElementAdded()]);
                } else if (c.wasRemoved()) {
                    selectedIndicesAsList.remove(c.getElementRemoved());
                    selectedItems.remove(getItems()[c.getElementRemoved()]);
                }
            });
            // wrap a unmodifiable observable list around the observable arrays lists
            unmodifiableSelectedIndices = FXCollections.unmodifiableObservableList(selectedIndicesAsList);
            unmodifiableSelectedItems = FXCollections.unmodifiableObservableList(selectedItems);
        }
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return unmodifiableSelectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return unmodifiableSelectedItems;
    }

    @Override
    public void selectIndices(int index, int... indices) {
        select(index);
        for (int i : indices) {
            select(i);
        }
    }

    @Override
    public void selectAll() {
        for (int index = 0; index < items.length; index++) {
            selectedIndices.add(index);
        }
        focusIndex=-1;
    }

    @Override
    public void clearAndSelect(int index) {
        clearSelection();
        select(index);
    }

    public void clearAndSelect(T item){
        clearSelection();
        select(item);
    }

    @Override
    public void select(int index) {
        if (index >= 0 && index < items.length) {
            selectedIndices.add(index);
            focusIndex = index;
        }
    }

    @Override
    public void select(T item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                select(i);
                return;
            }
        }
    }

    @Override
    public void clearSelection(int index) {
        if (index >= 0 && index < items.length) {
            selectedIndices.remove(index);
        }
    }

    public void clearSelection(T item){
        for(int i = 0; i < items.length; i++){
            if(items[i].equals(item)) {
                clearSelection(i);
                break;
            }
        }
    }

    @Override
    public void clearSelection() {
        selectedIndices.clear();
        focusIndex = -1;
    }

    @Override
    public boolean isSelected(int index) {
        return index >= 0 && index < items.length && selectedIndices.contains(index);
    }

    public boolean isSelected(T item){
        int index = -1;
        for(int i = 0; i < items.length; i++){
            if(items[i].equals(item)) {
                return isSelected(i);
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return selectedIndices.isEmpty();
    }

    @Override
    public void selectFirst() {
        if (items.length > 0) {
            select(0);
        }
    }

    @Override
    public void selectLast() {
        if (items.length > 0) {
            select(items.length - 1);
        }
    }

    @Override
    public void selectPrevious() {
        select(focusIndex-1);
    }

    @Override
    public void selectNext() {
        select(focusIndex+1);
    }

    /**
     * get the current array of items.
     *
     * @return items
     */
    public T[] getItems() {
        return items;
    }

    /**
     * set the array of items and clear selection
     *
     * @param items
     */
    public void setItems(T[] items) {
        clearSelection();
        this.items = Arrays.copyOf(items, items.length); // use copy for safety
    }
}
