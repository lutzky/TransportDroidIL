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
	List<Bus> getBuses();
	Date getNextBus();
	void update() throws ClientProtocolException, IOException;
	
	public abstract class Entity {
		private final double position;

		public Entity(double position) {
			this.position = position;
		}

		public double getPosition() {
			return position;
		}
		
		abstract void visit(EntityVisitor visitor);
	}
	
	public class Stop extends Entity {
		private final String title;
		
		public Stop(String title, double position) {
			super(position);
			this.title = title;
		}

		public String getTitle() { return title; }
		
		@Override
		public String toString() {
			return "Stop(\"" + getTitle() + "\", " + getPosition() + ")";
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Stop) {
				Stop other = (Stop) o;
				return title.equals(other.title) && super.equals(other);
			}
			else return false;
		}
		
		@Override
		public int hashCode() {
			return title.hashCode() + super.hashCode();
		}

		@Override
		void visit(EntityVisitor visitor) {
			visitor.visitStop(this);
		}
	}
	
	public class Eta extends Entity {
		private final boolean direction;
		private final Date eta;
		
		public Eta(boolean direction, double position, Date eta) {
			super(position);
			this.direction = direction;
			this.eta = eta;
		}
		
		public Date getEta() {
			return eta;
		}
		
		@Override
		public String toString() {
			return "Eta(" + getPosition() + ", " + getEta() + ")";
		}
		
		public boolean getDirection() {
			return direction;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Eta) {
				Eta other = (Eta) o;
				return eta.equals(other.eta) && super.equals(o);
			}
			else return false;
		}
		
		@Override
		public int hashCode() {
			return eta.hashCode() + super.hashCode() + Boolean.valueOf(direction).hashCode();
		}

		@Override
		void visit(EntityVisitor visitor) {
			visitor.visitEta(this);
		}
	}

	public class Bus extends Entity {
		private final boolean direction;
		
		public Bus(boolean direction, double position) {
			super(position);
			this.direction = direction;
		}

		public boolean getDirection() {
			return direction;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Bus) {
				Bus other = (Bus) o;
				return direction == other.direction && super.equals(o);
			}
			else return false;
		}
		
		@Override
		public int hashCode() {
			return Boolean.valueOf(direction).hashCode() + super.hashCode();
		}

		@Override
		void visit(EntityVisitor visitor) {
			visitor.visitBus(this);
		}		
	}
	
	public interface EntityVisitor {
		void visitStop(Stop stop);
		void visitEta(Eta eta);
		void visitBus(Bus bus);
	}
}
