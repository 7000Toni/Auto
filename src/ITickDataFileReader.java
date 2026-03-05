import java.io.IOException;

public interface ITickDataFileReader {
	public void readNextTick(DataSet.ReadFileVars rfv) throws IOException, Exception;
	public void readFirstTick(DataSet.ReadFileVars rfv) throws IOException, Exception;
	public boolean validDatum(String datum);
}
