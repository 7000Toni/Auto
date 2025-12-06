import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class OriginalTickFileReader implements TickDataFileReader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" dd/MM/yyyy HH:mm:ss");
	private static Pattern datum = Pattern.compile("([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[Ee]([+-]?\\d+))?\\s\\d\\d/\\d\\d/\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d");
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			DatumChecker.check(datum, rfv.in);
			rfv.price = rfv.in.substring(0, rfv.in.indexOf(' '));
			rfv.dateTime = rfv.in.substring(rfv.in.indexOf(' '));
			rfv.ldt = LocalDateTime.parse(rfv.dateTime, dtf);
			rfv.val = Double.parseDouble(rfv.price);
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		readNextTick(rfv);
	}
	
	@Override
	public boolean validDatum(String datum) {
		boolean valid = true;
		try {
			DatumChecker.check(OriginalTickFileReader.datum, datum);
		} catch (Exception e) {
			valid = false;
		}	
		return valid;
	}
}
