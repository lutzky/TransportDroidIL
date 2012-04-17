package net.lutzky.transportdroidil.test;

import net.lutzky.transportdroidil.AutolocationTextView;
import net.lutzky.transportdroidil.AutolocationTextView.State;
import net.lutzky.transportdroidil.R;
import net.lutzky.transportdroidil.TransportDroidIL;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

public class TransportDroidILTest extends ActivityInstrumentationTestCase2<TransportDroidIL> {

	public TransportDroidILTest() {
		super("net.lutzky.transportdroidil", TransportDroidIL.class);
	}
	
	@UiThreadTest
	public void testOrientation() throws InterruptedException {
		TransportDroidIL activity = getActivity();
		AutolocationTextView fromField = (AutolocationTextView) activity.findViewById(R.id.query_from);
		
		// Set custom state by typing something
		fromField.requestFocus();
		fromField.setText("Test");

		assertEquals("Test", fromField.getText().toString());
		assertEquals(State.CUSTOM, fromField.getState());
		
		// Change orientation simulation
		Bundle icicle = new Bundle();
		getInstrumentation().callActivityOnSaveInstanceState(activity, icicle);
		activity.finish();
		getInstrumentation().callActivityOnCreate(activity, null);
		getInstrumentation().callActivityOnRestoreInstanceState(activity, icicle);
		fromField = (AutolocationTextView) activity.findViewById(R.id.query_from);
		
		assertEquals("Test", fromField.getText().toString());
		assertEquals(State.CUSTOM, fromField.getState());
	}

}
