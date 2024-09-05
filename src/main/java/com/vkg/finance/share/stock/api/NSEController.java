package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.service.FundManagementServiceImpl;
import com.vkg.finance.share.stock.simulators.EtfShopSimulator;
import com.vkg.finance.share.stock.simulators.LifoShopSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("nse")
public class NSEController {
    @Autowired
    private FundManagementServiceImpl fundManagementService;
    @Autowired
    private LifoShopSimulator lifoShopSimulator;
    @Autowired
    private EtfShopSimulator etfShopSimulator;

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
    public void simulateEtf() {
        etfShopSimulator.simulate();
    }
    @GetMapping("simulate-lifo")
    public void simulateLifo() {
        lifoShopSimulator.simulate();
    }
}
