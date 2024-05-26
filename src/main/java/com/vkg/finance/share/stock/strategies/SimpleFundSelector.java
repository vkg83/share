package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.client.FundDataProvider;
import com.vkg.finance.share.stock.model.Fund;
import com.vkg.finance.share.stock.model.FundType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleFundSelector extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFundSelector.class);

    private int minVolume;
    private Predicate<Fund> p = e->true;

    @Override
    public List<Fund> execute(List<Fund> allFunds) {
        Predicate<Fund> vol = e -> e.getVolume() >= minVolume;

        final List<Fund> funds = allFunds.stream().filter(p.and(vol)).collect(Collectors.toList());
        if (funds.size() < allFunds.size()) {
            LOGGER.info("Ignoring {} ETFs (Volume is less than {} minimum allowed)", allFunds.size() - funds.size(), minVolume);
        }
        return funds;
    }

    public SimpleFundSelector setMinVolume(int minVolume) {
        this.minVolume = minVolume;
        return this;
    }

    public SimpleFundSelector excludeAssets(String... assets) {
        for (String asset : assets) {
            Predicate<Fund> e = f->f.getAssets().toUpperCase().contains(asset);
            e = e.or(f->f.getSymbol().contains(asset));
            p = p.and(e.negate());
        }
        return this;
    }

    public SimpleFundSelector includeAssets(String... assets) {
        Predicate<Fund> e = x->false;
        for (String asset : assets) {
            e = e.or(f->f.getAssets().toUpperCase().contains(asset));
            e = e.or(f->f.getSymbol().contains(asset));
        }
        p = p.and(e);
        return this;
    }
}
