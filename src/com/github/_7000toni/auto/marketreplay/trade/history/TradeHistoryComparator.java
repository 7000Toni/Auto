package com.github._7000toni.auto.marketreplay.trade.history;

import java.util.Comparator;

public class TradeHistoryComparator implements Comparator<ITradeHistory> {
	
	@Override
	public int compare(ITradeHistory th1, ITradeHistory th2) {
		return th1.entryIndex() - th2.entryIndex();
	}
}
