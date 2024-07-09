package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vkg.finance.share.stock.model.FundHistory;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AllFundHistory {

    private List<FundHistory> data;

    public List<FundHistory> getData() {
        return data;
    }

    public void setData(List<FundHistory> data) {
        this.data = data;
    }

}
