public class TradeHistory implements ITradeHistory {	
	private boolean buy;
	private int entryIndex;
	private int exitIndex;
	
	public TradeHistory(boolean buy, int entryIndex, int exitIndex) {
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
