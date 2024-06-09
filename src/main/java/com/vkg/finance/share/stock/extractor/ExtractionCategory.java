package com.vkg.finance.share.stock.extractor;

import org.springframework.web.multipart.MultipartFile;

public enum ExtractionCategory {
    NAME_MARKET_CAP(MarketCapExtractor.class);

    private final Class<? extends DataExtractor<?>> extractorClass;

    ExtractionCategory(Class<? extends DataExtractor<?>> extractorClass) {
        this.extractorClass = extractorClass;
    }

    public Class<? extends DataExtractor<?>> getExtractorClass() {
        return extractorClass;
    }

    @SuppressWarnings("unchecked")
    public <T> T extract(MultipartFile file) {
        Class<? extends DataExtractor<?>> c = getExtractorClass();
        final DataExtractor<T> bean = (DataExtractor<T>)new MarketCapExtractor();
        return bean.extract(file);
    }

}
