package com.vkg.finance.share.stock.repository;


import com.vkg.finance.share.stock.model.FundInfo;

import java.util.Collection;
import java.util.List;

public interface FundDetailDao {
    List<FundInfo> loadAll();

    void saveAll(Collection<FundInfo> values);
}
