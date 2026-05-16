public class MarketReplayTradeHistory implements TradeHistory {	
	private boolean buy;
	private int entryIndex;
	private int exitIndex;
	
	public MarketReplayTradeHistory(boolean buy, int entryIndex, int exitIndex) {
		this.buy = buy;
		this.entryIndex = entryIndex;
		this.exitIndex = exitIndex;
	}
	
	public boolean buy() {
		return buy;
	}
	
	public int entryIndex() { 
		return entryIndex;
	}
	
	public int exitIndex() { 
		return exitIndex;
	}
}
