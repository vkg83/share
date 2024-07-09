package com.vkg.finance.share.stock.repository.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vkg.finance.share.stock.model.FundInfo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AllFund {

    private List<FundInfo> data;

    public List<FundInfo> getData() {
        return data;
    }

    public void setData(List<FundInfo> data) {
        this.data = data;
    }

}
