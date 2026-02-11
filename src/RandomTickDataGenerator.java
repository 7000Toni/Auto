import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;

public class RandomTickDataGenerator implements TickDataFileReader {
	private static int size;
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		Random ran = new Random();
		rfv.add = false;
		if (size > -1) {
			rfv.in = "read";
		} else {
			rfv.in = null;
		}
		if (rfv.in != null) {
			size--;
			rfv.ldt = Instant.ofEpochMilli(System.currentTimeMillis() - (size - 1000000) * 1000).atZone(ZoneId.systemDefault()).toLocalDateTime();
			if (ran.nextBoolean()) {
				rfv.val = rfv.prevPrice + 0.25;
			} else {
				rfv.val = rfv.prevPrice - 0.25;
			}
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException, Exception {
		size = 1000000;
		while (!rfv.add) {
			readNextTick(rfv);						
		}
		size--;
	}
	
	@Override
	public boolean validDatum(String datum) {
		return true;
	}
}
