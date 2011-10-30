package net.lutzky.transportdroidil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

public class AutolocationTextView extends EnhancedTextView {
	public interface StateChangeCallback {
		public void stateHasChanged(State newState);
	}

	private final static String TAG = "AutolocationTextView";

	public static enum State { SEARCHING, FOUND, CUSTOM }

	private State state;
	private final Triangulator triangulator;
	private final Geocoder geo;
	private String addressString; // Used to communicate between threads
	private Thread geocoderThread = null;

	// Starting search mode changes the text, but that's us - not the user, so
	// don't count that as a reason to set state to CUSTOM again.
	boolean ignoreTextChange = false;

	private StateChangeCallback stateChangeListener = null;

	public State getState() {
		return state;
	}

	public AutolocationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (isInEditMode()) {
			triangulator = null;
			geo = null;
		} else {
			triangulator = new Triangulator(context);
			geo = new Geocoder(getContext(), new Locale("he"));
		}

		startSearch();
	}

	public void onStateChange(StateChangeCallback cb) {
		stateChangeListener = cb;
	}

	public void startSearch() {
		setState(State.SEARCHING);
	}

	public void markAsCustom() {
		setState(State.CUSTOM);
	}

	private void setState(State state) {
		this.state = state;
		Log.d(TAG, "Setting state to " + state);

		if (stateChangeListener != null) {
			stateChangeListener.stateHasChanged(state);
		}

		switch (state) {
		case SEARCHING:
			ignoreTextChange = true;
			setText(R.string.my_location);
			ignoreTextChange = false;
			setTextColor(getResources().getColor(R.color.auto_location));
			getLocation();
			break;
		case FOUND:
			setTextColor(getResources().getColor(R.color.auto_location));
			break;
		case CUSTOM:
			setTextColor(getResources().getColor(android.R.color.primary_text_light));
		}
	}

	private void getLocation() {
		if (triangulator == null) return;
		
		final Handler mHandler = new Handler();
		
		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				if (state == State.CUSTOM) {
					Log.i(TAG, "Found location, but state is custom, so dropping it.");
				} else {
					setText(addressString);
					setState(State.FOUND); // After setText, since it activates the onTextChanged handler, and had set state to CUSTOM.
					setSelection(addressString.length());
					Log.d(TAG, "We are at: " + addressString);
				}
				geocoderThread = null;
			}
		};
		
		final Runnable mError = new Runnable() {
			@Override
			public void run() {
				// TODO handle errors
				Log.d(TAG, "No address found for this location.");
			}
		};

		triangulator.getLocation(10 * 1000, new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onProviderDisabled(String provider) {}

			@Override
			public void onLocationChanged(final Location location) {
				Log.d(TAG, "Location changed: " + location);
				if (location == null)
					return;

				try {
					if (geocoderThread != null)
						geocoderThread.join(1000); // TODO stop the older thread somehow
				} catch (InterruptedException e) {}
				geocoderThread = new Thread() {
					@Override
					public void run() {
						try {
							List<Address> addresses;
							addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
							if (addresses.size() > 0) {
								addressString = addressToQueryString(addresses.get(0));
								mHandler.post(mUpdateResults);
							}
							else
								mHandler.post(mError);
						} catch (IOException e) {} // TODO handle exceptions
					}
				};
				geocoderThread.start();
			}
		});
	}

	private static String addressToQueryString(Address address) {
		if (address == null)
			return "";
		String firstLine = address.getAddressLine(0);
		if (firstLine == null)
			return "";
		firstLine = firstLine.replaceFirst("(\\d+)-(\\d+)", "\\1");
		String result = firstLine;
		final String secondLine = address.getAddressLine(1);
		if (secondLine != null && address.getMaxAddressLineIndex() > 1)
			result += " " + secondLine;
		return result;
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		if (!ignoreTextChange && hasFocus()) {
			Log.d(TAG, "User changed text, setting state to CUSTOM");
			setState(State.CUSTOM);
		}
		super.onTextChanged(text, start, before, after);
	}
}
