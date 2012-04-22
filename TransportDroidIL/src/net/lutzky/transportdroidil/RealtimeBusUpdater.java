package net.lutzky.transportdroidil;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

public interface RealtimeBusUpdater {
	Date getLastUpdateTime();
	String getRouteNumber();
	String getRouteTitle();
	List<Stop> getStops();
	List<Eta> getEtas();
	Float getBusPosition();
	Date getNextBus();
	void registerBusUpdateListener(BusUpdateListener listener);
	void update() throws ClientProtocolException, IOException;
	
	public class Stop {
		private final String title;
		private final double position;
		
		public Stop(String title, double d) {
			this.title = title;
			this.position = d;
		}

		public String getTitle() { return title; }
		public double getPosition() { return position; }
		
		@Override
		public String toString() {
			return "Stop(\"" + getTitle() + "\", " + getPosition() + ")";
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Stop) {
				Stop other = (Stop) o;
				return title.equals(other.title) && position == other.position;
			}
			else return false;
		}
		
		@Override
		public int hashCode() {
			return title.hashCode() + Double.valueOf(position).hashCode();
		}
	}
	
	public class Eta {
		private final float position;
		private final Date eta;
		
		public Eta(float position, Date eta) {
			this.position = position;
			this.eta = eta;
		}
		
		public float getPosition() {
			return position;
		}
		public Date getEta() {
			return eta;
		}
		
		@Override
		public String toString() {
			return "Eta(" + getPosition() + ", " + getEta() + ")";
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Eta) {
				Eta other = (Eta) o;
				return eta.equals(other.eta) && position == other.position;
			}
			else return false;
		}
		
		@Override
		public int hashCode() {
			return eta.hashCode() + Double.valueOf(position).hashCode();
		}
	}
	
	public interface BusUpdateListener {
		void realtimeBusUpdate(RealtimeBusUpdater source);
	}


}
