import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class DukascopyNodeReader implements TickDataFileReader {
	private static Pattern datum = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d,([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[Ee]([+-]?\\d+))?,([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[Ee]([+-]?\\d+))?");
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			DatumChecker.check(datum, rfv.in);
			rfv.tokens = new StringTokenizer(rfv.in, ",");
			Instant ins = Instant.ofEpochMilli(Long.parseLong(rfv.tokens.nextToken()));
			rfv.tokens.nextToken();
			rfv.ldt = LocalDateTime.ofInstant(ins, ZoneId.of("Z"));
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
