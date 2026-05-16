import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class TradeHistoryLoader {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");	
	
	private static class ApproxTradeHistory implements ITradeHistory {
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
		return generateIndices(approxHst, data);
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
	
	private static ArrayList<TradeHistory> generateIndices(ArrayList<ApproxTradeHistory> history, ArrayList<DataSet.DataPair> data) {
		ArrayList<TradeHistory> hst = new ArrayList<TradeHistory>();
		for (ApproxTradeHistory h : history) {
			for (int i = 0; i < data.size() - 1; i++) {
				if (h.open().isBefore(data.get(0).dateTime()) || h.open().isAfter(data.get(data.size() - 1).dateTime())) {
					break;
				}
				if (h.open().isEqual(data.get(i).dateTime()) || h.open().isAfter(data.get(i).dateTime()) && h.open().isBefore(data.get(i + 1).dateTime())) {
					if (h.entryIndex() == -1) {
						h.setEntryIndex(i);			
					}
				}
				if (h.close().isEqual(data.get(i).dateTime()) || h.close().isAfter(data.get(i).dateTime()) && h.close().isBefore(data.get(i + 1).dateTime())) {
					if (h.exitIndex() == -1) {
						h.setExitIndex(i);	
						hst.add(new TradeHistory(h.buy(), h.entryIndex(), h.exitIndex()));
						break;
					}
				}
				if (h.close().isAfter(data.get(data.size() - 1).dateTime())) {
					if (h.exitIndex() == -1) {
						h.setExitIndex(data.size() - 1);
						hst.add(new TradeHistory(h.buy(), h.entryIndex(), h.exitIndex()));
					}
					break;
				}
			}
		}		
		return hst;
	}
}

