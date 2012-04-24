package net.lutzky.transportdroidil;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

public class MockRealtimeBusUpdater implements RealtimeBusUpdater {
	Date lastUpdate;
	private UpdateListener listener;

	@Override
	public Date getLastUpdateTime() {
		return lastUpdate;
	}

	@Override
	public boolean isServiceActive() {
		return false;
	}

	@Override
	public String getRouteNumber() {
		return null;
	}

	@Override
	public String getRouteTitle() {
		return null;
	}

	@Override
	public List<Stop> getStops() {
		return Collections.emptyList();
	}

	@Override
	public List<Eta> getEtas() {
		return Collections.emptyList();
	}

	@Override
	public List<Bus> getBuses() {
		return Collections.emptyList();
	}

	@Override
	public Date getNextBus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update() throws ClientProtocolException, IOException {
		lastUpdate = new Date();
		if (listener != null)
			listener.updated();
	}
	
	public void setListener(UpdateListener l) {
		listener = l;
	}

	public interface UpdateListener {
		void updated();
	}
}
