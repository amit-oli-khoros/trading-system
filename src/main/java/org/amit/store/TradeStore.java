package org.amit.store;

import org.amit.models.Trade;

import java.util.List;

public interface TradeStore {
    List<Trade> getAllTrades();
    Trade saveTrade(Trade trade);
}
