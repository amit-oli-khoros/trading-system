package org.amit.store;

import org.amit.common.Logger;
import org.amit.models.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryTradeStore implements TradeStore {
    private final List<Trade> trades = Collections.synchronizedList(new ArrayList<>());

    @Override
    public List<Trade> getAllTrades() {
        return new ArrayList<>(trades);
    }

    @Override
    public Trade saveTrade(Trade trade) {
        Logger.log("Save trade "+ trade);
        trades.add(trade);
        return trade;
    }
}
