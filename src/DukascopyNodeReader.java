import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.StringTokenizer;

public class DukascopyNodeReader implements TickDataFileReader {

	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			rfv.tokens = new StringTokenizer(rfv.in, ",");
			Instant ins = Instant.ofEpochMilli(Long.parseLong(rfv.tokens.nextToken()));
			rfv.tokens.nextToken();
			rfv.ldt = LocalDateTime.ofInstant(ins, ZoneId.of("Z"));
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
