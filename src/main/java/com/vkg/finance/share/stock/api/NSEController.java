package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.NSEService;
import com.vkg.finance.share.stock.model.Fund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("nse")
public class NSEController {
    @Autowired
    private NSEService nseService;

    @GetMapping("etf")
    public List<Fund> loadEtfInfo() {
        return nseService.loadEtfInfo();
    }
    @GetMapping("jwel")
    public List<Fund> loadJwelleryInfo() {
        return nseService.loadJwelInfo();
    }
    @GetMapping("darvos")
    public List<Fund> applyDarvos() {
        return nseService.applyDarvos();
    }
}
