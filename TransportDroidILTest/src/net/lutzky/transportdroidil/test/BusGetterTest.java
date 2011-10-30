package net.lutzky.transportdroidil.test;

import android.text.Spanned;
import android.text.style.ClickableSpan;
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
	
	public void testInteraction(BusGetter bg) throws Throwable {
		bg.runQuery("תל אביב לירושלים");
		Spanned result = bg.getFilteredResult();
		ClickableSpan[] spans = result.getSpans(0, result.length(), ClickableSpan.class);
		assertEquals(2, spans.length);
		bg.runQuery("", 1);
		result = bg.getFilteredResult();
		assertSaneResult(result);
	}
	
	public void testMotInteraction() throws Throwable {
		testInteraction(new BusGovIlGetter());
	}
	
	public void testEggedInteraction() throws Throwable {
		testInteraction(new EggedGetter());
	}
}
