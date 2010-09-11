package net.lutzky.transportdroidil;

public class BusGovIlGetter extends BusGetter {
	public static final String url = "http://bus.gov.il/WebForms/wsUnicell.asmx/getAnswerMot";

	@Override
	String getQueryJson(String query) {
		return String.format("{\"sParams\":\"%s\",\"strSession\":\"0\"}", query);
	}

	@Override
	String getUrl() {
		return url;
	}

}
