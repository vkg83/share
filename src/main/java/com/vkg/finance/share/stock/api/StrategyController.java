package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.model.SelectionStrategyRequest;
import com.vkg.finance.share.stock.service.StrategyManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("strategies")
public class StrategyController {
    @Autowired
    private StrategyManagementService strategyManagementService;

    @PostMapping("moving-average")
    public void applyMovingAverage(@RequestBody SelectionStrategyRequest selectionStrategyRequest) {
        strategyManagementService.applyMovingAverage(selectionStrategyRequest);
    }

    @PostMapping("buy/{strategyName}")
    public void applyBuyStrategy(String strategyName, @RequestBody SelectionStrategyRequest selectionStrategyRequest) {
        strategyManagementService.applyDarvos(selectionStrategyRequest);
    }

    @PostMapping("sell/{strategyName}")
    public void applySellStrategy(String strategyName, @RequestBody SelectionStrategyRequest selectionStrategyRequest) {
        strategyManagementService.applyDarvos(selectionStrategyRequest);
    }
}
