package edu.uwm.cs.util;

import java.util.Observer;

/**
 * An ADT that keeps track of 1-based indices being selected in a GUI.
 * It is used to implement whether something is selected or not.
 */
public interface SelectionModel {

	/**
	 * Clear the selection and then select a single index (if > 0)
	 * @param i index to select (if positive)
	 */
	public abstract void setSelected(int i);

	/**
	 * Clear the selection.  Afterwards nothing is selected.
	 */
	public abstract void clearSelection();

	/**
	 * Return the lowest selected index, if any, or else 0.
	 * @return lowest selected index (1 based).
	 */
	public abstract int getSelected();

	/**
	 * Return whether this index is selected.
	 * @param index to examine, must be > 0 (negative gets exception, 0 false)
	 * @return whether this index is selected.
	 */
	public abstract boolean isSelected(int index);

	/**
	 * Return the number of selected indices.
	 * @return number selected.
	 */
	public abstract int numSelected();

	/**
	 * @see {@link java.util.Observable#addObserver}
	 */
	public void addObserver(Observer o);

	/**
	 * @see {@link java.util.Observable#deleteObserver}
	 */
	public void deleteObserver(Observer o);
}