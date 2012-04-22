package net.lutzky.transportdroidil.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import net.lutzky.transportdroidil.OmniExpBusUpdater;
import net.lutzky.transportdroidil.RealtimeBusUpdater;
import net.lutzky.transportdroidil.RealtimeBusUpdater.BusUpdateListener;
import net.lutzky.transportdroidil.RealtimeBusUpdater.Stop;
import junit.framework.TestCase;

public class OmniExpRealtimeTest extends TestCase implements BusUpdateListener {
	private boolean gotUpdate;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		gotUpdate = false;
	}
	
	public void testRoute15() throws ClientProtocolException, IOException {
		RealtimeBusUpdater r = new OmniExpBusUpdater("01501097");
		r.registerBusUpdateListener(this);
		r.update();
		waitForUpdate();
		
		assertEquals("015", r.getRouteNumber());
		assertEquals("מסוף יקנעם - מסוף יקנעם-הורדה", r.getRouteTitle());
		
		List<Stop> expectedStops = Arrays.asList(new Stop[] {
			new Stop("מסוף יקנעם", 0.0), 
			new Stop("כיכר הגיבורים", 10.166017), 
			new Stop("מרכז מסחרי", 15.549702), 
			new Stop("אלונים", 18.713612), 
			new Stop("בי'ס ארזים", 25.212046), 
			new Stop("יער אודם\\רבין", 34.174202), 
			new Stop("חרמון/יער אודם", 40.599415), 
			new Stop("ירדן/בניאס", 56.27035), 
			new Stop("ירדן\\דליות", 58.845554), 
			new Stop("צאלים/הדקל", 76.01492), 
			new Stop("בי'ס אורט יגאל אלון", 89.057625), 
			new Stop("מסוף יקנעם-הורדה", 100.0)
		});
		List<Stop> actualStops = r.getStops();
		assertEquals(expectedStops.size(), actualStops.size());
		for (int i = 0; i < expectedStops.size(); ++i) {
			Stop expectedStop = expectedStops.get(i);
			Stop actualStop = actualStops.get(i);
			assertEquals(expectedStop.getTitle(), actualStop.getTitle());
			assertTrue(Math.abs(expectedStop.getPosition() - actualStop.getPosition()) < 0.001);
		}
		
		Float pos = r.getBusPosition();
		if (pos != null)
			assertTrue("Invalid bus position", pos >= 0 && pos <= 100);
		else
			assertTrue("Invalid next bus time", r.getNextBus() != null);
	}

	private void waitForUpdate() {
		while (!gotUpdate) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					// try again
				}
			}
		}
	}

	@Override
	public synchronized void realtimeBusUpdate(RealtimeBusUpdater source) {
		gotUpdate = true;
		notify();
	}
}
