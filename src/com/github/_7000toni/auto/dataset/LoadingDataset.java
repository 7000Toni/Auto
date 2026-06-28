package com.github._7000toni.auto.dataset;
import java.io.File;

import com.github._7000toni.auto.dataset.reader.ITickDataFileReader;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class LoadingDataset {
	private String signature;
	private double y;
	private IntegerProperty progress = new SimpleIntegerProperty(0);
	private IntegerProperty addIndex = new SimpleIntegerProperty(0);
	
	public LoadingDataset(double y, int addIndex, String signature) {
		this.y = y;
		this.addIndex.set(addIndex);
		this.signature = signature;
	}
	
	public Dataset load(File file, ITickDataFileReader reader) {		
		Dataset data = new Dataset(file, reader, progress);
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
