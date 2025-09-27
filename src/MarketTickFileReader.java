import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

public class MarketTickFileReader implements TickDataFileReader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyMMddHHmmssSSSSSS");	
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			rfv.tokens = new StringTokenizer(rfv.in, ";");
			rfv.ldt = LocalDateTime.parse(rfv.tokens.nextToken(), dtf);
			rfv.tokens.nextToken();
			if (!rfv.tokens.nextToken().equals("2")) {
				return;
			}
			rfv.val = Double.parseDouble(rfv.tokens.nextToken());
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException {
		while (!rfv.add && rfv.in != null) {
			readNextTick(rfv);						
		}
	}
}
