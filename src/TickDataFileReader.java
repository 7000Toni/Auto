import java.io.IOException;

public interface TickDataFileReader {
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException;
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException;
}
