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
	
	static class ApproxTradeHistory implements TradeHistory {
		private String original;
		private boolean buy;
		private LocalDateTime open;
		private LocalDateTime close;
		private int entryIndex = -1;
		private int exitIndex = -1;
		
		public ApproxTradeHistory(String original, boolean buy, LocalDateTime open, LocalDateTime close) {
			this.original = original;
			this.buy = buy;
			this.open = open;
			this.close = close;
		}
		
		public ApproxTradeHistory(String original, boolean buy, LocalDateTime open, LocalDateTime close, int entryIndex, int exitIndex) {
			this.original = original;
			this.buy = buy;
			this.open = open;
			this.close = close;
			this.entryIndex = entryIndex;
			this.exitIndex = exitIndex;
		}
		
		public String original() { 
			return original;
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
	
	public static ArrayList<ApproxTradeHistory> loadApproxHistory(File history) {
		ArrayList<ApproxTradeHistory> approxHst = new ArrayList<ApproxTradeHistory>();
		try (FileInputStream fis = new FileInputStream(history);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			br.readLine();
			String in = br.readLine();
			while (in != null) {
				StringTokenizer tokens = new StringTokenizer(in, ",");				
				String original = in;
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
				approxHst.add(new ApproxTradeHistory(original, buy, open, close));
				in = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return approxHst;
	}
	
	public static void generateIndices(ArrayList<ApproxTradeHistory> history, ArrayList<DataSet.DataPair> data) {
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
						break;
					}
				}
				if (h.close().isAfter(data.get(data.size() - 1).dateTime())) {
					if (h.exitIndex() == -1) {
						h.setExitIndex(data.size() - 1);
					}
					break;
				}
			}
		}
	}
}

