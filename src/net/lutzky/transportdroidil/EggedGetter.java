package net.lutzky.transportdroidil;


public class EggedGetter extends BusGetter {
	public static final String url = "http://mslworld.egged.co.il/eggedtimetable/WebForms/wsUnicell.asmx/getAnswer";

	@Override
	String getQueryJson(String query) {
		return String.format("{\"str1\":\"%s\",\"strSession\":\"0\"}", query);
	}

	@Override
	String getUrl() {
		return url;
	}
}
