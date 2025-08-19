import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class Chart {
	private final int HSB_WIDTH = 100;
	private final int HSB_HEIGHT = 10;
	private final int CHT_MARGIN = 5;
	private final int CHT_DATA_MARGIN = 10;
	private final int PRICE_MARGIN = 100;
	
	private final int PRICE_PTS = 20;
	private final int PRICE_DASH_SIZE = 5;
	private final int PRICE_DASH_MARGIN = 5;
	
	//CrossHair
	private int crossHairX = 0;
	private int crossHairY = 0;
	private boolean crossHairVisible = false;
	
	
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
	private int numDataPoints = 1495;//299
	private int numDecimalPts;
	private int startIndex;
	private int endIndex;
	
	private int size = -1;
	
	private class DataPair {
		private double price;
		private LocalDateTime dateTime;		
		
		public DataPair(double price, LocalDateTime dateTime) {
			this.price = price;
			this.dateTime = dateTime;
		}
	}
	
	public Chart(String filePath, int size, int width, int height, double tickSize, int numDecimalPts) {
		this.width = width;
		this.height = height;
		this.tickSize = tickSize;
		if (numDecimalPts < 0) {
			numDecimalPts = 0;
		}
		this.numDecimalPts = numDecimalPts;
		if (size > 0) {
			this.size = size;
		}
		readData(filePath);
	}
	
	public Chart(String filePath, int width, int height, double tickSize, int numDecimalPts) {
		this.width = width;
		this.height = height;
		this.tickSize = tickSize;
		if (numDecimalPts < 0) {
			numDecimalPts = 0;
		}
		this.numDecimalPts = numDecimalPts;
		readData(filePath);
	}
	
	public Canvas getCanvas() {
		return this.canvas;
	}
	
	public GraphicsContext getGraphicsContext() {
		return this.gc;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public HorizontalScrollBar getHSB() {
		return hsb;
	}
	
	public void drawCrosshair() {
		double fontSize = gc.getFont().getSize();
		
		gc.strokeLine(CHT_MARGIN, crossHairY, width - PRICE_MARGIN, crossHairY);
		gc.strokeLine(crossHairX, CHT_MARGIN, crossHairX, height - HSB_HEIGHT - CHT_MARGIN);
		
		double price = ((((chartHeight - (CHT_DATA_MARGIN*2)) - (crossHairY - CHT_MARGIN - CHT_DATA_MARGIN))/ (double)(chartHeight - (CHT_DATA_MARGIN*2))) * range) + lowest;
		double roundedPrice = Math.round(price * Math.pow(10, numDecimalPts)) / Math.pow(10, numDecimalPts);
		
		gc.fillRect(chartWidth + CHT_MARGIN, crossHairY - fontSize/2, 100, fontSize);
		gc.setStroke(Color.WHITE);
		gc.strokeText(((Double)(roundedPrice)).toString(), chartWidth + CHT_MARGIN + PRICE_DASH_MARGIN, crossHairY + fontSize/3, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
		gc.setStroke(Color.BLACK);
		
		int index = startIndex + (int)(((crossHairX-CHT_MARGIN)/(double)chartWidth)*(endIndex-startIndex));
		double dateBarHalfWidth = fontSize * 5;
		double dateBarX;
		if (crossHairX < CHT_MARGIN + dateBarHalfWidth) {
			dateBarX = CHT_MARGIN;
		} else if (crossHairX > chartWidth + CHT_MARGIN - dateBarHalfWidth) {
			dateBarX = chartWidth + CHT_MARGIN - dateBarHalfWidth*2;
		} else {
			dateBarX = crossHairX - dateBarHalfWidth;
		}
		
		gc.fillRect(dateBarX, chartHeight - CHT_MARGIN - 1, dateBarHalfWidth*2, fontSize);
		gc.setStroke(Color.WHITE);
		gc.strokeText(data.get(index).dateTime.toString(), dateBarX + fontSize / 3, chartHeight + CHT_MARGIN - 1, dateBarHalfWidth*2);
		gc.setStroke(Color.BLACK);
	}
	
	public void onMouseMoved(MouseEvent e) {			
		if (onChart((int)e.getX(), (int)e.getY())) {	
			crossHairX = (int)e.getX();
			crossHairY = (int)e.getY();
			crossHairVisible = true;
		} else {
			crossHairVisible = false;
		}
		drawChart();
	}		
	
	public void onMousePressed(MouseEvent e) {	
	}
	
	public void onMouseReleased(MouseEvent e) {	
	}
	
	public void onMouseDragged(MouseEvent e) {	
		onMouseMoved(e);
	}
	
	public void onScroll(ScrollEvent e) {
		//TODO set boundaries
		if (e.getDeltaY() > 0) {
			numDataPoints -= 100;
		} else {
			numDataPoints += 100;
		}
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
			hsb = new HorizontalScrollBar(this, HSB_HEIGHT, HSB_WIDTH, data.size(), numDataPoints);
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
	
	
	
	private void calculateRange(int beginIndex, int endIndex) {
		lowest = data.get(beginIndex).price;
		highest = data.get(beginIndex).price;				
		
		for (int i = beginIndex; i < endIndex; i++) {			
			double val = data.get(i).price;
			if (val > highest) {
				highest = val;
			} else if (val < lowest) {
				lowest = val;
			}
		}
		
		range = highest - lowest;
	}
	
	public void drawChart() {		
		double xDiff = (width-PRICE_MARGIN-CHT_MARGIN) / (double)numDataPoints;
		startIndex = (int)((hsb.position() / (width - HSB_WIDTH)) * (data.size() - numDataPoints - 1));
		endIndex = startIndex + numDataPoints;
		gc.clearRect(0, 0, width, height);
		gc.strokeRect(CHT_MARGIN, CHT_MARGIN, chartWidth, chartHeight);
		hsb.drawHSB();
		calculateRange(startIndex, endIndex);
		double tickSizeOnChart = (chartHeight - CHT_DATA_MARGIN * 2) / (range / tickSize);
		double conversionVar = tickSize / tickSizeOnChart;		
		double startY = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN - (((data.get(startIndex).price - lowest) / range) * (chartHeight - CHT_DATA_MARGIN * 2));		
		double prevY = startY - ((data.get(startIndex + 1).price - data.get(startIndex).price) / conversionVar);
		gc.strokeLine(CHT_MARGIN, startY, xDiff+CHT_MARGIN, prevY);		
		for (int i = 1; i < numDataPoints; i++) {
			gc.strokeLine((i*xDiff)+CHT_MARGIN, prevY, ((i+1)*xDiff)+CHT_MARGIN, prevY - ((data.get(startIndex + i + 1).price - data.get(startIndex + i).price) / conversionVar));
			prevY = prevY - ((data.get(startIndex + i + 1).price - data.get(startIndex + i).price) / conversionVar);
		}	

		int spacing =  (chartHeight - CHT_DATA_MARGIN*2) / (PRICE_PTS-1);
		int index = chartHeight - CHT_DATA_MARGIN + CHT_MARGIN;
		int priceDashPos = chartWidth + CHT_MARGIN;
		int pricePos = priceDashPos + PRICE_DASH_SIZE + PRICE_DASH_MARGIN;
		int pricePosYMargin = (int)(gc.getFont().getSize()/3);
		double diff = Math.round(range*(Math.pow(10, numDecimalPts)) / (PRICE_PTS - 1))/Math.pow(10, numDecimalPts); 
		for (int i = 0; i < PRICE_PTS; i++) {
			gc.strokeLine(priceDashPos, index, priceDashPos + PRICE_DASH_SIZE, index);
			gc.strokeText(((Double)(lowest + (diff*i))).toString(), pricePos, index + pricePosYMargin, PRICE_MARGIN - PRICE_DASH_SIZE - PRICE_DASH_MARGIN);
			index -= spacing;
		}
		if (crossHairVisible) {
			drawCrosshair();
		}
	}

	public void setNumDataPoints(int numDataPoints) {
		this.numDataPoints = numDataPoints;
		hsb.updateHSBMove(numDataPoints, numDataPoints);
	}
}
