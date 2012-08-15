package net.lutzky.transportdroidil.test;

import net.lutzky.transportdroidil.MockRealtimeBusUpdater;
import net.lutzky.transportdroidil.MockRealtimeBusUpdater.UpdateListener;
import net.lutzky.transportdroidil.RealtimeBusActivity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

public class RealtimeBusActivityTest extends ActivityInstrumentationTestCase2<RealtimeBusActivity> implements UpdateListener {

	private boolean gotUpdate;
	private RealtimeBusActivity activity;

	public RealtimeBusActivityTest() {
		super(RealtimeBusActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("provider", "MockCompany");
		intent.putExtra("routeId", "1");
		setActivityIntent(intent);
		gotUpdate = false;
		activity = getActivity();
		((MockRealtimeBusUpdater) activity.getModel()).setListener(this);
	}
	
	public void testPauseResume() throws InterruptedException {
		getInstrumentation().callActivityOnResume(activity);
		waitForUpdate();
		waitForUpdate();
		
		getInstrumentation().callActivityOnPause(activity);
		gotUpdate = false;
		Thread.sleep(6000); // Update time is 5sec
		if (gotUpdate) {
			gotUpdate = false;
			Thread.sleep(6000);
		}
		assertFalse(gotUpdate); // assert no update when paused
		
		getInstrumentation().callActivityOnResume(activity);
		waitForUpdate();
		waitForUpdate();
	}

	@Override
	public synchronized void updated() {
		synchronized (this) {
			gotUpdate  = true;
			notify();
		}
	}

	private synchronized void waitForUpdate() {
		while (!gotUpdate) {
			try {
				wait(6000);
				if (!gotUpdate)
					throw new AssertionError("Missing update.");
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
}
