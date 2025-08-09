import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class Chart {
	private final int HSB_WIDTH = 100;
	private final int HSB_HEIGHT = 10;
	private final int CHT_MARGIN = 5;
	private final int CHT_DATA_MARGIN = 10;
	private final int PRICE_MARGIN = 100;
	
	private Canvas canvas;
	private int width;
	private int height;
	private int chartWidth;
	private int chartHeight;
	private GraphicsContext gc;
	private ArrayList<DataPair> data = new ArrayList<DataPair>();
	private double tickSize;
	private double range;	
	private double lowest;
	private double highest;
	private HorizontalScrollBar hsb;
	private int numDataPoints = 1495;
	
	private int size = -1;
	
	private class DataPair {
		private double price;
		private LocalDateTime dateTime;		
		
		public DataPair(double price, LocalDateTime dateTime) {
			this.price = price;
			this.dateTime = dateTime;
		}
		
		public double price() {
			return price;
		}
		
		public LocalDateTime dateTime() {
			return dateTime;
		}
	}
	
	public Chart(String filePath, int size, int width, int height, double tickSize) {
		this.width = width;
		this.height = height;
		this.tickSize = tickSize;
		if (size > 0) {
			this.size = size;
		}
		readData(filePath);
	}
	
	public Chart(String filePath, int width, int height, double tickSize) {
		this.width = width;
		this.height = height;
		this.tickSize = tickSize;
		readData(filePath);
	}
	
	public Canvas getCanvas() {
		return this.canvas;
	}
	
	public GraphicsContext getGraphicsContext() {
		return this.gc;
	}
	
	private void readData(String filePath) {
		try (FileInputStream fis = new FileInputStream(filePath);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(" dd/MM/yyyy HH:mm:ss");
			String in;		
			String dateTime;
			String price;
			in = br.readLine();
			price = in.substring(0, in.indexOf(' '));
			dateTime = in.substring(in.indexOf(' '));
			LocalDateTime ldt = LocalDateTime.parse(dateTime, dtf);
			double val = Double.parseDouble(price);
			data.add(new DataPair(val, ldt));								
			int progress = 0;
			
			while (true) {
				progress++;
				in = br.readLine();
				if (size != -1) {
					int percent = (int)((double)progress/size*100);
					if (percent > 100) {
						System.out.println("99%");
					} else {
						System.out.println(percent + "%");
					}
				}
				if (in == null) {
					break;
				}
				if (progress == 10000) {
					break;
				}
				price = in.substring(0, in.indexOf(' '));
				dateTime = in.substring(in.indexOf(' '));
				ldt = LocalDateTime.parse(dateTime, dtf);
				val = Double.parseDouble(price);
				
				data.add(new DataPair(val, ldt));	
			}
			
			chartWidth = width - PRICE_MARGIN - CHT_MARGIN;
			chartHeight = height - HSB_HEIGHT - CHT_MARGIN*2;
			canvas = new Canvas(width, height);
			canvas.setOnMouseMoved(e -> {
				drawChart();
				if (onChart((int)e.getX(), (int)e.getY())) {				
					drawCrosshair(e.getX(), e.getY());
				}
			});
			hsb = new HorizontalScrollBar(this, HSB_HEIGHT, HSB_WIDTH);
			gc = canvas.getGraphicsContext2D();							
			drawChart();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
	
	public boolean onChart(int x, int y) {
		if (y <= height - HSB_HEIGHT - CHT_MARGIN*2 && y >= CHT_MARGIN) {
			if (x <= width - PRICE_MARGIN - CHT_MARGIN && x >= CHT_MARGIN) {
				return true;
			}
		}
		
		return false;
	}
	
	public void drawCrosshair(double x, double y) {
		gc.strokeLine(CHT_MARGIN, y, width - PRICE_MARGIN, y);
		gc.strokeLine(x, CHT_MARGIN, x, height - HSB_HEIGHT - CHT_MARGIN);
	}
	
	private void calculateRange(int beginIndex, int endIndex) {
		lowest = data.get(beginIndex).price();
		highest = data.get(beginIndex).price();				
		
		for (int i = beginIndex; i < endIndex; i++) {
			double val = data.get(i).price();
			if (val > highest) {
				highest = val;
			} else if (val < lowest) {
				lowest = val;
			}
		}
		
		range = highest - lowest;
	}
	
	public void drawChart() {
		int startIndex = (int)(((double)hsb.position() / (width - HSB_WIDTH)) * (data.size() - numDataPoints - 1));
		int endIndex = startIndex + numDataPoints;
		gc.clearRect(0, 0, width, height);
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		hsb.drawHSB();
		calculateRange(startIndex, endIndex);
		double tickSizeOnChart = (chartHeight - CHT_DATA_MARGIN * 2) / (range / tickSize);
		double conversionVar = tickSize / tickSizeOnChart;		
		double startY = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN - (((data.get(startIndex).price() - lowest) / range) * (chartHeight - CHT_DATA_MARGIN * 2));		
		double prevY = startY - ((data.get(startIndex + 1).price() - data.get(startIndex).price()) / conversionVar);
		gc.strokeLine(CHT_MARGIN, startY, 1+CHT_MARGIN, prevY);		
		for (int i = 1; i < numDataPoints; i++) {
			gc.strokeLine(i+CHT_MARGIN, prevY, i+1+CHT_MARGIN, prevY - ((data.get(startIndex + i + 1).price() - data.get(startIndex + i).price()) / conversionVar));
			prevY = prevY - ((data.get(startIndex + i + 1).price() - data.get(startIndex + i).price()) / conversionVar);
		}	
	}
}
