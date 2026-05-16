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
	
	static class ApproxTradeHistory {
		String original;
		boolean buy;
		LocalDateTime open;
		LocalDateTime close;
		int openIndex = -1;
		int closeIndex = -1;
	}
	
	public static ArrayList<ApproxTradeHistory> loadApproxHistory(File history) {
		ArrayList<ApproxTradeHistory> approxHst = new ArrayList<ApproxTradeHistory>();
		try (FileInputStream fis = new FileInputStream(history);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
			br.readLine();
			String in = br.readLine();
			while (in != null) {
				StringTokenizer tokens = new StringTokenizer(in, ",");
				ApproxTradeHistory hst = new ApproxTradeHistory();
				hst.original = in;
				hst.open = LocalDateTime.parse(tokens.nextToken(), dtf).minusHours(3);
				tokens.nextToken();
				tokens.nextToken();
				if (tokens.nextToken().equals("buy")) {
					hst.buy = true;
				} else {
					hst.buy = false;
				}
				if (tokens.countTokens() > 7) {
					tokens.nextToken();
					if (tokens.countTokens() > 7) {
						tokens.nextToken();
					}
				}
				tokens.nextToken();
				tokens.nextToken();
				hst.close = LocalDateTime.parse(tokens.nextToken(), dtf).minusHours(3);
				approxHst.add(hst);
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
				if (h.open.isBefore(data.get(0).dateTime()) || h.open.isAfter(data.get(data.size() - 1).dateTime())) {
					break;
				}
				if (h.open.isEqual(data.get(i).dateTime()) || h.open.isAfter(data.get(i).dateTime()) && h.open.isBefore(data.get(i + 1).dateTime())) {
					if (h.openIndex == -1) {
						h.openIndex = i;			
					}
				}
				if (h.close.isEqual(data.get(i).dateTime()) || h.close.isAfter(data.get(i).dateTime()) && h.close.isBefore(data.get(i + 1).dateTime())) {
					if (h.closeIndex == -1) {
						h.closeIndex = i;	
						break;
					}
				}
				if (h.close.isAfter(data.get(data.size() - 1).dateTime())) {
					if (h.closeIndex == -1) {
						h.closeIndex = data.size() - 1;
					}
					break;
				}
			}
		}
	}
}

