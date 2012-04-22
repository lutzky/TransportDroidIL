package net.lutzky.transportdroidil;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRealtimeBusUpdater implements RealtimeBusUpdater {
	private final List<RealtimeBusUpdater.BusUpdateListener> listeners = new ArrayList<RealtimeBusUpdater.BusUpdateListener>();
	
	protected void notifyListeners() {
		for (BusUpdateListener listener : listeners) {
			listener.realtimeBusUpdate(this);
		}
	}
	
	@Override
	public void registerBusUpdateListener(BusUpdateListener listener) {
		listeners.add(listener);
	}
}
