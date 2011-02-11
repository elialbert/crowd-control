package com.test.cc1;

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
		this.parent.updateLoc(location); //referring to crowdcontrol, update the location
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	
	}
}
