import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signature {
	private static Pattern full = Pattern.compile("[0-9]+\s[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+");			
	private static Pattern partial = Pattern.compile("[A-Za-z0-9]+\s[0-9]*\\.[0-9]+\s[0-9]+");		
	
	public static boolean validFull(String signature) {
		Matcher m = full.matcher(signature);	
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean validPartial(String signature) {
		Matcher m = partial.matcher(signature);	
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}
}
