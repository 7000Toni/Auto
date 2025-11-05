import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class OptimizedMarketTickFileReader implements TickDataFileReader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyMMddHHmmssSSSSSS");	
	private static Pattern datum = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d;([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[Ee]([+-]?\\d+))?");
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			DatumChecker.check(datum, rfv.in);
			rfv.tokens = new StringTokenizer(rfv.in, ";");
			rfv.ldt = LocalDateTime.parse(rfv.tokens.nextToken(), dtf);
			rfv.val = Double.parseDouble(rfv.tokens.nextToken());
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		while (!rfv.add && rfv.in != null) {
			readNextTick(rfv);						
		}
	}
}
