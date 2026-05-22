package com.github._7000toni.auto.marketreplay.trade.history;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.github._7000toni.auto.chart.Chart;
import com.github._7000toni.auto.dataset.DataSet;

import javafx.beans.property.IntegerProperty;

public class TradeHistoryLoader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");	
	
	public static class ApproxTradeHistory implements ITradeHistory {
		private boolean buy;
		private LocalDateTime open;
		private LocalDateTime close;
		private int entryIndex = -1;
		private int exitIndex = -1;
		
		public ApproxTradeHistory(boolean buy, LocalDateTime open, LocalDateTime close) {
			this.buy = buy;
			this.open = open;
			this.close = close;
		}
		
		public boolean buy() { 
			return buy;
		}
		
		public LocalDateTime open() { 
			return open;
		}
		
		public LocalDateTime close() { 
			return close;
		}
		
		public int entryIndex() { 
			return entryIndex;
		}
		
		public int exitIndex() { 
			return exitIndex;
		}
		
		public void setEntryIndex(int entryIndex) {
			this.entryIndex = entryIndex;
		}
		
		public void setExitIndex(int exitIndex) {
			this.exitIndex = exitIndex;
		}
	}
	
	public static ArrayList<TradeHistory> loadApproxHistory(File history, ArrayList<DataSet.DataPair> data) {
		return loadApproxHistory(history, data, null, null);
	}
	
	public static ArrayList<TradeHistory> loadApproxHistory(File history, ArrayList<DataSet.DataPair> data, IntegerProperty progress, Chart chart) {
		ArrayList<ApproxTradeHistory> approxHst = new ArrayList<ApproxTradeHistory>();
		try (FileInputStream fis = new FileInputStream(history);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			if (!br.readLine().equals("Time,Position,Symbol,Type,Volume,Price,S / L,T / P,Time,Price,Commission,Swap,Profit,,")) {
				return loadMRHistory(history);
			}			
			String in = br.readLine();
			while (in != null) {
				StringTokenizer tokens = new StringTokenizer(in, ",");	
				LocalDateTime open = LocalDateTime.parse(tokens.nextToken(), dtf).minusHours(3);
				tokens.nextToken();
				tokens.nextToken();
				boolean buy;
				if (tokens.nextToken().equals("buy")) {
					buy = true;
				} else {
					buy = false;
				}
				if (tokens.countTokens() > 7) {
					tokens.nextToken();
					if (tokens.countTokens() > 7) {
						tokens.nextToken();
					}
				}
				tokens.nextToken();
				tokens.nextToken();
				LocalDateTime close = LocalDateTime.parse(tokens.nextToken(), dtf).minusHours(3);
				approxHst.add(new ApproxTradeHistory(buy, open, close));
				in = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return generateIndices(approxHst, data, progress, chart);
	}
	
	public static ArrayList<TradeHistory> loadMRHistory(File history) {
		ArrayList<TradeHistory> approxHst = new ArrayList<TradeHistory>();
		try (FileInputStream fis = new FileInputStream(history);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String in = br.readLine();
			while (in != null) {
				StringTokenizer tokens = new StringTokenizer(in, ",");				
				boolean buy = Boolean.parseBoolean(tokens.nextToken());
				int entryIndex = Integer.parseInt(tokens.nextToken());
				int exitIndex = Integer.parseInt(tokens.nextToken());
				approxHst.add(new TradeHistory(buy, entryIndex, exitIndex));
				in = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return approxHst;
	}
	
	private static ArrayList<TradeHistory> generateIndices(ArrayList<ApproxTradeHistory> history, ArrayList<DataSet.DataPair> data, IntegerProperty progress, Chart chart) {
		ArrayList<TradeHistory> hst = new ArrayList<TradeHistory>();
		long j = 0;
		progress.set(0);		
		for (ApproxTradeHistory h : history) {
			h.setEntryIndex(indexSearch(h.open(), data, 0, data.size() - 1));
			h.setExitIndex(indexSearch(h.close(), data, 0, data.size() - 1));
			if (h.entryIndex() != -1) {
				hst.add(new TradeHistory(h.buy(), h.entryIndex(), h.exitIndex()));
				if (h.exitIndex() ==  -1) {
					h.setExitIndex(data.size() - 1);
				}
			}	
			j++;
			progress.set((int)((j / (double)history.size()) * 100));
		}	
		return hst;
	}
	
	private static int indexSearch(LocalDateTime date, ArrayList<DataSet.DataPair> data, int start, int end) {
		if (Math.abs(start - end) < 2) {
			if (start == 0) {
				if (date.isEqual(data.get(0).dateTime())) {
					return start;
				}
			} else if (end == data.size() - 1) {
				if (date.isEqual(data.get(data.size() - 1).dateTime())) {
					return end;
				}
			} else if (dateNear(date, data.get(end - 1).dateTime(), data.get(end).dateTime(), data.get(end + 1).dateTime())){
				return end;
			}
			return -1;
		} else {
			int i = start + (end - start) / 2;
			if (dateNear(date, data.get(i-1).dateTime(),  data.get(i).dateTime(), data.get(i+1).dateTime())) {
				return i;
			}
			if (date.isBefore(data.get(i).dateTime())) {
				return indexSearch(date, data, start, i);
			} else {
				return indexSearch(date, data, i, end);
			}
		}
	}
	
	private static boolean dateNear(LocalDateTime dateTest, LocalDateTime dateBefore, LocalDateTime dateMiddle, LocalDateTime dateAfter) {
		if (dateTest.isEqual(dateMiddle) || 
				dateTest.isAfter(dateBefore) && dateTest.isBefore(dateMiddle) ||
				dateTest.isAfter(dateMiddle) && dateTest.isBefore(dateAfter)) {
			return true;
		}
		return false;
	}
}

