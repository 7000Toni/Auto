import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OriginalTickFileReader implements TickDataFileReader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" dd/MM/yyyy HH:mm:ss");
	
	@Override
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException {
		rfv.add = false;
		rfv.in = rfv.br.readLine();
		if (rfv.in != null) {
			rfv.price = rfv.in.substring(0, rfv.in.indexOf(' '));
			rfv.dateTime = rfv.in.substring(rfv.in.indexOf(' '));
			rfv.ldt = LocalDateTime.parse(rfv.dateTime, dtf);
			rfv.val = Double.parseDouble(rfv.price);
			rfv.add = true;
		}
	}
	
	@Override
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException {
		readNextTick(rfv);
	}
}
