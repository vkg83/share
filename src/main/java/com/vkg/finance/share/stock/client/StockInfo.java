package com.vkg.finance.share.stock.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class StockInfo {
    private final String symbol;
    private Integer epsStrength;
    private Integer priceStrength;
    private String buyerDemand;
    private Integer groupRank;
    private List<BigDecimal> yearlyEps;
    private List<BigDecimal> quarterlyEpsGrowth;
    private List<BigDecimal> quarterlySalesGrowth;
    private List<BigDecimal> netMargin;
    private BigDecimal epsGrowth;
    private BigDecimal epsStability;
    private BigDecimal returnOnEquity;
    private BigDecimal masterScore;
    private int redFlags;
    private String groupId;
    private BigDecimal weeklyVolume;
    private BigDecimal averageWeeklyVolume;
    private boolean insideBar;

    public StockInfo(String symbol) {
        this.symbol = symbol;
    }

    public void setEpsStrength(Integer epsStrength) {
        this.epsStrength = epsStrength;
    }

    public void setPriceStrength(Integer priceStrength) {
        this.priceStrength = priceStrength;
    }

    public void setBuyerDemand(String buyerDemand) {
        this.buyerDemand = buyerDemand;
    }

    public void setGroupRank(Integer groupRank) {
        this.groupRank = groupRank;
    }

    public void setYearlyEps(List<BigDecimal> yearlyEps) {
        this.yearlyEps = yearlyEps;
    }

    public void setQuarterlyEpsGrowth(List<BigDecimal> quarterlyEpsGrowth) {
        this.quarterlyEpsGrowth = quarterlyEpsGrowth;
    }

    public void setQuarterlySalesGrowth(List<BigDecimal> quarterlySalesGrowth) {
        this.quarterlySalesGrowth = quarterlySalesGrowth;
    }

    public void setNetMargin(List<BigDecimal> netMargin) {
        this.netMargin = netMargin;
    }

    public void setEpsGrowth(BigDecimal bigDecimal) {
        this.epsGrowth = bigDecimal;
    }

    public void setEpsStability(BigDecimal epsStability) {
        this.epsStability = epsStability;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<BigDecimal> getQuarterlyEpsGrowth() {
        return quarterlyEpsGrowth;
    }

    public BigDecimal getEpsGrowth() {
        return epsGrowth;
    }

    public BigDecimal getEpsStability() {
        return epsStability;
    }

    public List<BigDecimal> getYearlyEps() {
        return yearlyEps;
    }

    public List<BigDecimal> getQuarterlySalesGrowth() {
        return quarterlySalesGrowth;
    }

    public void setReturnOnEquity(BigDecimal returnOnEquity) {
        this.returnOnEquity = returnOnEquity;
    }

    public BigDecimal getReturnOnEquity() {
        return returnOnEquity;
    }

    public List<BigDecimal> getNetMargin() {
        return netMargin;
    }

    public Integer getPriceStrength() {
        return priceStrength;
    }

    public Integer getGroupRank() {
        return groupRank;
    }

    public Integer getEpsStrength() {
        return epsStrength;
    }

    public String getBuyerDemand() {
        return buyerDemand;
    }

    public void setMasterScore(BigDecimal masterScore) {
        this.masterScore = masterScore;
    }

    public BigDecimal getMasterScore() {
        return masterScore;
    }

    public void setRedFlags(int redFlags) {
        this.redFlags = redFlags;
    }

    public int getRedFlags() {
        return redFlags;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public String getGroupId() {
        return groupId;
    }

    public BigDecimal getAverageWeeklyVolume() {
        return averageWeeklyVolume;
    }

    public BigDecimal getWeeklyVolume() {
        return weeklyVolume;
    }

    public void setWeeklyVolume(BigDecimal weeklyVolume) {
        this.weeklyVolume = weeklyVolume;
    }

    public void setInsideBar(boolean insideBar) {
        this.insideBar = insideBar;
    }

    public boolean isInsideBar() {
        return insideBar;
    }

    public void setAverageWeeklyVolume(BigDecimal averageWeeklyVolume) {
        this.averageWeeklyVolume = averageWeeklyVolume;
    }
}
