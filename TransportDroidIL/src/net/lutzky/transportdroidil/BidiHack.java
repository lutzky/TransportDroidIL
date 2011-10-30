package net.lutzky.transportdroidil;

/**
 * A quick hack to get around Bidi limitations in some Android versions:
 * reverses number runs.
 * 
 * @author Ohad Lutzky
 * @email ohad@lutzky.net
 */
public class BidiHack {
	StringBuilder forward = new StringBuilder();
	StringBuilder reverse = new StringBuilder();
	protected final String charsToFlip = "01234567890:/";
	
	protected void flushReverse() {
		if (reverse.length() == 0) {
			return;
		}
		
		forward.append(reverse.reverse());
		clear(reverse);
	}
	
	protected static void clear(StringBuilder sb) {
		sb.setLength(0);
	}
	
	protected boolean needsFlipping(char c) {
		return (charsToFlip.indexOf(c) != -1);
	}
	
	public String reorder(String s) {
		clear(forward);
		clear(reverse);
		
		for (char c : s.toCharArray()) {
			if (needsFlipping(c)) {
				reverse.append(c);
			}
			else {
				flushReverse();
				forward.append(c);
			}
		}
		flushReverse();
		
		return forward.toString();
	}
}
