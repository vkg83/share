package com.vkg.finance.share.stock.extractor;

import org.springframework.web.multipart.MultipartFile;

public interface DataExtractor<T> {
    T extract(MultipartFile file);
}
