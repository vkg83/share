package com.vkg.finance.share.stock.strategies;

import com.vkg.finance.share.stock.model.FundHistory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.repository.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleFundSelector extends AbstractSelectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFundSelector.class);

    private int minVolume;
    private Predicate<FundInfo> p = e->true;
    private final MarketDataProvider dataProvider;

    public SimpleFundSelector(MarketDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public List<FundInfo> execute(List<FundInfo> allFundInfos, LocalDate date) {
        FundHistory LOWVOL = new FundHistory();
        LOWVOL.setVolume(0);
        final List<FundInfo> fundInfos = allFundInfos.stream()
                .filter(p)
                .filter(i -> dataProvider.getHistory(i.getSymbol(), date).orElse(LOWVOL).getVolume() >= minVolume)
                .collect(Collectors.toList());
        if (fundInfos.size() < allFundInfos.size()) {
            LOGGER.info("Ignoring {} ETFs (Volume is less than {} minimum allowed)", allFundInfos.size() - fundInfos.size(), minVolume);
        }
        return fundInfos;
    }

    public SimpleFundSelector setMinVolume(int minVolume) {
        this.minVolume = minVolume;
        return this;
    }

    public SimpleFundSelector excludeAssets(String... assets) {
        for (String asset : assets) {
            Predicate<FundInfo> e = f->f.getName().toUpperCase().contains(asset);
            e = e.or(f->f.getSymbol().contains(asset));
            p = p.and(e.negate());
        }
        return this;
    }

    public SimpleFundSelector includeAssets(String... assets) {
        Predicate<FundInfo> e = x->false;
        for (String asset : assets) {
            e = e.or(f->f.getName().toUpperCase().contains(asset));
            e = e.or(f->f.getSymbol().contains(asset));
        }
        p = p.and(e);
        return this;
    }
}
