package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.service.FundManagementServiceImpl;
import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.service.InvestmentSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("nse")
public class NSEController {
    @Autowired
    private FundManagementServiceImpl fundManagementService;
    @Autowired
    private InvestmentSimulator investmentSimulator;

    @GetMapping("etf")
    public List<FundInfo> loadEtfInfo() {
        return fundManagementService.loadEtfInfo();
    }
    @GetMapping("jwel")
    public List<FundInfo> loadJwelleryInfo() {
        return fundManagementService.loadJwelInfo();
    }
    @GetMapping("darvos")
    public List<FundInfo> applyDarvos() {
        return fundManagementService.applyDarvos();
    }
    @GetMapping("simulate-etf")
    public void simulateShop() {
        investmentSimulator.simulateEtfShop();
    }
    @GetMapping("simulate-lifo")
    public void simulateLifo() {
        investmentSimulator.simulateLifoShop();
    }
}
