package net.lutzky.transportdroidil.test;

import android.text.Spanned;
import net.lutzky.transportdroidil.BusGetter;
import net.lutzky.transportdroidil.BusGovIlGetter;
import net.lutzky.transportdroidil.EggedGetter;
import junit.framework.TestCase;

public class BusGetterTest extends TestCase {
	
	private void assertSaneResult(Spanned result) {
		assertTrue("Result was " + result, result.toString().indexOf("קו") > 0);
	}
	public void testEggedQuery() throws Throwable {
		BusGetter bg = new EggedGetter();
		bg.runQuery("חוף הכרמל לת\"א");
		assertSaneResult(bg.getFilteredResult());
		bg.runQuery("צומת ג'למה לחיפה");
		assertSaneResult(bg.getFilteredResult());
	}
	public void testBusGovIlQuery() throws Throwable {
		BusGetter bg = new BusGovIlGetter();
		bg.runQuery("חוף הכרמל לת\"א");
		assertSaneResult(bg.getFilteredResult());
		bg.runQuery("צומת ג'למה לחיפה");
		assertSaneResult(bg.getFilteredResult());
	}
}
