package com.vkg.finance.share.stock.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.vkg.finance.share.stock.model.FundHistory;

import java.io.IOException;

public class HistoryFromCurrent extends StdDeserializer<FundHistory> {

    protected HistoryFromCurrent() {
        super(FundHistory.class);
    }

    @Override
    public FundHistory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final TreeNode node = p.getCodec().readTree(p);
        var x = node.get("data");
        return null;
    }
}
