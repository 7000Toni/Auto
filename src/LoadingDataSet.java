import java.io.File;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class LoadingDataSet {
	private String signature;
	private double y;
	private IntegerProperty progress = new SimpleIntegerProperty(0);
	private IntegerProperty addIndex = new SimpleIntegerProperty(0);
	
	public LoadingDataSet(double y, int addIndex, String signature) {
		this.y = y;
		this.addIndex.set(addIndex);
		this.signature = signature;
	}
	
	public DataSet load(File file, TickDataFileReader reader) {		
		DataSet data = new DataSet(file, reader, progress);
		if (data.failed()) {
			return null;
		} else {
			return data;
		}
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setAddIndex(int i) {
		addIndex.set(i);
	}
	
	public ReadOnlyIntegerProperty addIndex() {
		return IntegerProperty.readOnlyIntegerProperty(addIndex);
	}
	
	public double y() {
		return y;
	}
	
	public String signature() {
		return signature;
	}
	
	public ReadOnlyIntegerProperty progress() {
		return ReadOnlyIntegerProperty.readOnlyIntegerProperty(progress);
	}
}
