package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.Fund;

import java.util.List;

public interface SelectionStrategy {
    List<Fund> select(List<Fund> funds);
}
