package net.lutzky.transportdroidil;

public class EggedGetter extends BusGetter {
	public static final String url = "http://mslworld.egged.co.il/eggedtimetable/WebForms/wsUnicell.asmx/getAnswer";

	@Override
	String getQueryJson(String query) {
		return String.format("{\"str1\":\"%s\",\"strSession\":\"0\"}", query);
	}

	@Override
	protected String getAppropriateReferer() {
		return "http://mslworld.egged.co.il/eggedtimetable/WebForms/wfrmMain.aspx?state=3&company=1&language=he&freelang=+&width=1024";
	}

	@Override
	String getUrl() {
		return url;
	}
}
