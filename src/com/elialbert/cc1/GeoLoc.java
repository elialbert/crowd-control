package com.elialbert.cc1;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GeoLoc implements LocationListener {
	
	private crowdcontrol parent;
	
	public GeoLoc(crowdcontrol cc) {
		this.parent = cc; //get a dynamic ref to the crowdcontrol class
		}

	@Override
	public void onLocationChanged(Location location) {
		this.parent.errtitleString = "";
		if (this.parent.paused == 0)
			this.parent.updateLoc(location); //referring to crowdcontrol, update the location
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		this.parent.errtitleString = "Please Enable GPS";
		this.parent.setTitle(this.parent.titleString + ", " + this.parent.errtitleString);
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		this.parent.errtitleString = "";
		this.parent.setTitle(this.parent.titleString + ", " + this.parent.errtitleString);		
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	
	}
}
