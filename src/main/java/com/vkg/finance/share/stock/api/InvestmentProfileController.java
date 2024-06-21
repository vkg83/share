package com.vkg.finance.share.stock.api;

import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.model.Investment;
import com.vkg.finance.share.stock.service.InvestmentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profiles")
public class InvestmentProfileController {
    @Autowired
    private InvestmentProfileService profileService;

    @PostMapping("{profileName}")
    public InvestmentProfile createProfile(@PathVariable String profileName) {
        return profileService.createProfile(profileName, 0);
    }

    @GetMapping("{profileName}")
    public InvestmentProfile getProfile(@PathVariable String profileName) {
        return profileService.getProfile(profileName);
    }

    @PostMapping("{profileName}/invest")
    public void addFund(@PathVariable String profileName, @RequestBody Investment investment) {
        profileService.purchase(profileName,investment.getStockSymbol(), investment.getDate(), investment.getQuantity(), investment.getPrice());
    }
}
