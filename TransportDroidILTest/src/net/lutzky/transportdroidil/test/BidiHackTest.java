package net.lutzky.transportdroidil.test;

import junit.framework.TestCase;
import net.lutzky.transportdroidil.BidiHack;

public class BidiHackTest extends TestCase {
	BidiHack bh;
	
	protected void setUp() {
		bh = new BidiHack();
	}
	
	public void testHebrewSanity() {
		assertEquals("שלום עולם", bh.reorder("שלום עולם"));
	}
	
	public void testNumberAndText() {
		assertEquals("שלום 123 עולם", bh.reorder("שלום 321 עולם"));
	}
	public void testDate() {
		assertEquals("09/10/2011", bh.reorder("1102/01/90"));
	}
	
	public void testHour() {
		assertEquals("06:35", bh.reorder("53:60"));
	}
	
	public void testMessage() {
		String line1Orig = "מחיפה,גימנסיה כרמל למרכז הכרמל בתאריך 1102/01/90";
		String line2Orig = "* קו 32 אגד, 50:60 52:60; 54:60; ";
		String line1Fixed = "מחיפה,גימנסיה כרמל למרכז הכרמל בתאריך 09/10/2011";
		String line2Fixed = "* קו 23 אגד, 06:05 06:25; 06:45; ";
		assertEquals(line1Fixed, bh.reorder(line1Orig));
		assertEquals(line2Fixed, bh.reorder(line2Orig));
		assertEquals(line1Fixed + "\n" + line2Fixed, bh.reorder(line1Orig + "\n" + line2Orig));
	}

}
