package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.extractor.ExtractionCategory;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.service.FundManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("market-data")
public class MarketDataController {
    @Autowired
    private FundManagementService fundManagementService;

    @PostMapping(value = "funds-details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateMarketCapitalization(@RequestParam MultipartFile file, @RequestParam ExtractionCategory extraction) {
        fundManagementService.updateFundDetails(extraction.extract(file));
    }

    @GetMapping(value = "funds-details")
    public List<FundInfo> getFundDetails() {
        return fundManagementService.getFundDetails();
    }

    @GetMapping(value = "etfs")
    public List<FundInfo> getFunds() {
        return fundManagementService.getAllEtfs();
    }

    @DeleteMapping
    public void removeCache() {
        fundManagementService.clearCache();
    }


}
