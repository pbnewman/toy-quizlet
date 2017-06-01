package edu.uwm.cs.util;

import java.util.BitSet;
import java.util.Observable;

/**
 * A selection model that permits multiple selection.
 */
public class MultiSelectionModel extends Observable implements SelectionModel {
	private BitSet selected = new BitSet();
	
	public MultiSelectionModel() {}
	
	/**
	 * Change the setting of this index.
	 * @param i index to toggle, expected > 0.
	 * @return whether the index is now selected.
	 * Zero or a negative index is never selected.
	 */
	public boolean toggleSelection(int i) {
		if (i <= 0) return false;
		boolean newVal = !selected.get(i);
		selected.flip(i);
		setChanged();
		notifyObservers();
		return newVal;
	}

	/* (non-Javadoc)
	 * @see edu.uwm.cs.util.SelectionModel#setSelected(int)
	 */
	public void setSelected(int i) {
		selected.clear();
		if (i <= 0) return;
		boolean oldVal = selected.get(i);
		if (oldVal) return;
		selected.set(i);
		setChanged();
		notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see edu.uwm.cs.util.SelectionModel#clearSelection()
	 */
	public void clearSelection() {
		if (selected.size() > 0) setChanged();
		selected.clear();
		notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see edu.uwm.cs.util.SelectionModel#getSelected()
	 */
	public int getSelected() {
		if (selected.size() > 0) return selected.nextSetBit(0);
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.uwm.cs.util.SelectionModel#isSelected(int)
	 */
	public boolean isSelected(int index) {
		return selected.get(index);
	}
	
	/* (non-Javadoc)
	 * @see edu.uwm.cs.util.SelectionModel#numSelected()
	 */
	public int numSelected() {
		return selected.cardinality();
	}
}
