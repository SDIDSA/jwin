package org.luke.gui.window.helpers;

import javafx.scene.Cursor;

public enum State {
	N(Cursor.N_RESIZE), S(Cursor.S_RESIZE), E(Cursor.E_RESIZE), W(Cursor.W_RESIZE), NE(Cursor.NE_RESIZE),
	NW(Cursor.NW_RESIZE), SE(Cursor.SE_RESIZE), SW(Cursor.SW_RESIZE), D(Cursor.DEFAULT);

	final Cursor curs;

	private State(Cursor curs) {
		this.curs = curs;
	}

	public static State stateForCoords(double x, double y, double w, double h, double min, double max) {
		boolean north = y <= max && y >= -min;
		boolean west = x <= max && x >= -min;
		boolean south = y <= h + min && y >= h - max;
		boolean east = x <= w + min && x >= w - max;
		
		State westState = west ? State.W : State.D;
		State eastState = east ? State.E : westState;
		
		State southEastState = east ? State.SE : State.S;
		State southWestState = west ? State.SW : southEastState;
		State southState = south ? southWestState : eastState;

		State northEastState = east ? State.NE : State.N;
		State northWestState = west ? State.NW : northEastState;
				
		return north ? northWestState : southState;
	}
}