package net.lutzky.transportdroidil;

import java.io.IOException;

import org.json.JSONException;

public class AlternateMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello, world");
		
		BusGetter bg = new EggedGetter();
		try {
			bg.runQuery("עדי למפרץ");
		} catch (IOException e) {
			System.out.println("Meh, IO exception :(");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Okay, here's what we got:");
		try {
			System.out.println(bg.getFilteredResult());
		} catch (JSONException e) {
			System.err.println("Damnit, invalid server response.");
			e.printStackTrace();
		}
	}

}
