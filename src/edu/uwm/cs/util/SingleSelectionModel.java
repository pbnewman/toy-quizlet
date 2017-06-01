package edu.uwm.cs.util;

import java.util.Observable;

/**
 * A selection model that handles a single item being selected.
 */
public class SingleSelectionModel extends Observable implements SelectionModel {

	private int selected;
	
	@Override
	public void setSelected(int i) {
		if (selected == i) return;
		selected = i;
		setChanged();
		notifyObservers(i);
	}

	@Override
	public void clearSelection() {
		setSelected(0);
	}

	@Override
	public int getSelected() {
		return selected;
	}

	@Override
	public boolean isSelected(int index) {
		return index == selected;
	}

	@Override
	public int numSelected() {
		return (selected > 0) ? 1 : 0;
	}

}
