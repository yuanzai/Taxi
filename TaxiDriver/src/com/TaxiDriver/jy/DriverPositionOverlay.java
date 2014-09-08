package com.TaxiDriver.jy;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class DriverPositionOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> pinpoints = new ArrayList<OverlayItem>();

	public DriverPositionOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public DriverPositionOverlay(Drawable m, Context context) {
		this(m);

		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return pinpoints.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return pinpoints.size();
	}

	public void insertPinpoint(OverlayItem item) {
		pinpoints.add(item);
		this.populate();
	}

	public void populatePinpoint() {
		this.populate();

	}

	@Override
	protected boolean onTap(int index) {

		return true;

	}

}
