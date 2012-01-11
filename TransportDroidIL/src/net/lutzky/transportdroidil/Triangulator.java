package net.lutzky.transportdroidil;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

public class Triangulator implements LocationListener {
	final Context context;
	Location bestLocation = null;
	
	public Triangulator(Context context) {
		this.context = context;
	}
	
	/**
	 * Tries to get the best location known in the given amount of time.
	 * @param timeout time to wait for location in ms.
	 * @param listener callback object. Only onLocationChanged method is called, and it is called once.
	 */
	public void getLocation(int timeout, final LocationListener listener) {
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		for (String provider : locationManager.getProviders(true)) {
			onLocationChanged(locationManager.getLastKnownLocation(provider));
			locationManager.requestLocationUpdates(provider, 0, MIN_DISTANCE, this);
		}
		
		Handler stopUpdates = new Handler();
		stopUpdates.postDelayed(new Runnable() {
			@Override
			public void run() {
				locationManager.removeUpdates(Triangulator.this);
				listener.onLocationChanged(bestLocation);
			}
		}, timeout);
	}
	
	private static final float MIN_DISTANCE = 500; // meters
	
	// Code from http://developer.android.com/guide/topics/location/obtaining-user-location.html

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	    
	    if (location == null)
	    	return false;

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, bestLocation))
			bestLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
