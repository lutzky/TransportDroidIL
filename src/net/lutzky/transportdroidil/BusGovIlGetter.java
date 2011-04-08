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

	@Override
	protected String getAppropriateReferer() {
		return "http://bus.gov.il/WebForms/wfrmMain.aspx?width=1024&company=1&language=he&state=";
	}

}
