package net.lutzky.transportdroidil.test;

import net.lutzky.transportdroidil.BusGetter;
import net.lutzky.transportdroidil.BusGovIlGetter;
import net.lutzky.transportdroidil.EggedGetter;
import junit.framework.TestCase;

public class BusGetterTest extends TestCase {
	
	private void assertSaneResult(String result) {
		assertTrue("Result was " + result, result.indexOf("קו") > 0);
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
