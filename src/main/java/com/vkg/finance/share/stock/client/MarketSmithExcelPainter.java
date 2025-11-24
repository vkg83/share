package com.vkg.finance.share.stock.client;

import com.opencsv.CSVReaderBuilder;
import com.vkg.finance.share.stock.util.NumberUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MarketSmithExcelPainter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketSmithExcelPainter.class);
    public static final Path TEMPLATE = Path.of("C:\\Users\\ADMIN\\Documents\\Weekly Analysis Template.xlsx");
    public static final Path DOWNLOAD_DIR = Path.of("C:\\Users\\ADMIN\\Downloads");
    public static final String GROUP_FILE = "industryGroupList.csv";
    public static final String INSIDE_PIVOT_FILE = "Filter_India_Stocks.csv";
    public static final String REACHING_PIVOT_FILE = "Filter_India_Stocks (1).csv";
    public static final String FUNDAMENTALS_RANK = "Fundamentals Rank";
    public static final String FUNDAMENTALS = "Fundamentals";
    private final Path filePath;
    private CellStyle colorStyle;
    private final Map<String, Group> groupMap;
    private final Set<String> pivots;

    public MarketSmithExcelPainter(Path filePath) {
        this.filePath = filePath;
        groupMap = new HashMap<>();
        loadGroups(GROUP_FILE, line -> {
            var g = new Group(line);
            groupMap.put(g.getId(), g);
        });
        pivots = new HashSet<>();
        loadGroups(INSIDE_PIVOT_FILE, line -> pivots.add(line[1]));
        loadGroups(REACHING_PIVOT_FILE, line -> pivots.add(line[1]));
    }

    private void loadGroups(String file, Consumer<String[]> consumer) {
        try (var is = getFileReader(file);
             var wb = new CSVReaderBuilder(is).withSkipLines(1).build()) {
            String[] line;
            while((line = wb.readNext()) != null) {
                consumer.accept(line);
            }
        } catch (Exception ex) {
            LOGGER.error("Not able to load from {}", file, ex);
        }
    }

    private static FileReader getFileReader(String fileName) throws FileNotFoundException {
        return new FileReader(DOWNLOAD_DIR.resolve(fileName).toFile());
    }

    public void writeFile(List<StockInfo> list) {
        try (var is = new FileInputStream(TEMPLATE.toFile());
             var wb = WorkbookFactory.create(is);
             var os = new FileOutputStream(filePath.toFile())) {
            colorStyle = wb.createCellStyle();
            colorStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            colorStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            var sheet = wb.getSheet(FUNDAMENTALS);
            fillSheet(sheet, list);
            var rSheet = wb.getSheet(FUNDAMENTALS_RANK);
            fillRankSheet(rSheet, list);
            wb.write(os);
            LOGGER.info("Created excel: {}", filePath);
        } catch (Exception ex) {
            LOGGER.error("Not able to create excel", ex);
            throw new RuntimeException(ex);
        }
    }

    private void fillSheet(Sheet sheet, List<StockInfo> stocks) {
        for (int i = 0; i < stocks.size(); i++) {
            Row row = sheet.createRow(3 + i);
            fillRow(row, stocks.get(i));
        }
    }

    private void fillRankSheet(Sheet sheet, List<StockInfo> stocks) {
        for (int i = 0; i < stocks.size(); i++) {
            Row row = sheet.createRow(3 + i);
            StockInfo info = stocks.get(i);
            try {
                fillRankRow(row, info);
            } catch (Exception ex) {
                LOGGER.info("Failed to fill: {}", info.getSymbol());
                throw ex;
            }
        }
    }

    private void fillRankRow(Row row, StockInfo info) {
        int colNum = 0;
        String symbol = info.getSymbol();
        var cell = fillCell(row, colNum, symbol);
        var ch = row.getSheet().getWorkbook().getCreationHelper();
        var link = ch.createHyperlink(Hyperlink.LINK_URL);
        link.setAddress("https://marketsmithindia.com/mstool/eval/"+symbol.toLowerCase()+"/evaluation.jsp#/");
        cell.setHyperlink(link);
        if(info.getRedFlags() > 0) {
            addStyle(cell);
        }
        var ranker = new StockRanker(info);
        int rowNum = row.getRowNum() + 1;

        fillFormula(row, ++colNum, "COUNTIF(C" + rowNum + ":N" + rowNum + ",1)");
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isQtrEpsGrowthGE(40)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isQtrEpsGrowthAcc(3)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isYrEpsGrowthGE(25)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isStabilityLE(25)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isYrEpsPositive(3)));
        fillCell(row, ++colNum, StockRanker.rank(ranker::isYrEpsAllTimeHigh));
        fillCell(row, ++colNum, StockRanker.rank(ranker::isQGTY));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isQtrSalesGrowthGE(25)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isQtrSalesGrowthAcc(3)));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isRoeGE(17)));
        fillCell(row, ++colNum, StockRanker.rank(ranker::isNetMarginAllTimeHigh));
        fillCell(row, ++colNum, StockRanker.rank(() -> ranker.isNetMarginExpanding(3)));
        fillCell(row, ++colNum, StockRanker.rankAll(() -> ranker.isNetMarginExpanding(5), () -> ranker.isNetMarginExpanding(10)));
        var yoyExpanding = StockRanker.rankAll(
                () -> ranker.isNetMarginYoYExpanding(3),
                () -> ranker.isNetMarginYoYExpanding(5),
                () -> ranker.isNetMarginYoYExpanding(10));
        fillCell(row, ++colNum, yoyExpanding);
        fillFormula(row, ++colNum, "COUNTIF(C" + rowNum + ":D" + rowNum + ",1) + COUNTIF(J" + rowNum + ":K" + rowNum + ", 1)");
        fillCell(row, ++colNum, info.getPriceStrength());
        fillCell(row, ++colNum, info.getGroupRank());
        Group group = groupMap.get(info.getGroupId());
        if(group != null) {
            fillCell(row, ++colNum, group.getLastWeekRank());
            fillCell(row, ++colNum, group.getLast3MonthRank());
            fillCell(row, ++colNum, group.getLast6MonthRank());
        } else {
            LOGGER.warn("No Industry group found with name: {} for {}", info.getGroupId(), symbol);
            colNum += 3;
        }
        fillFormula(row, ++colNum, "AVERAGE(S"+ rowNum +":V"+rowNum+") > S"+rowNum+"* 1.2");
        fillCell(row, ++colNum, info.getEpsStrength());
        fillCell(row, ++colNum, info.getBuyerDemand());
        fillCell(row, ++colNum, info.getMasterScore());
        fillCell(row, ++colNum, info.getAverageWeeklyVolume());
        fillCell(row, ++colNum, info.getWeeklyVolume());
        fillCell(row, ++colNum, info.isInsideBar()? "Y" : "");
        fillCell(row, ++colNum, pivots.contains(symbol)? "Y" : "");
    }

    private void addStyle(Cell cell) {
        cell.setCellStyle(colorStyle);
    }

    private void fillFormula(Row row, int i, String value) {
        Cell cell = row.createCell(i);
        cell.setCellFormula(value);
    }


    private void fillRow(Row row, StockInfo info) {
        int colNum = 0;
        fillCell(row, colNum, info.getSymbol());
        int rowNum = row.getRowNum() + 1;
        fillFormula(row, ++colNum, "'Fundamentals Rank'!B" + rowNum);
        var quarterlyEpsGrowth = info.getQuarterlyEpsGrowth();
        fillCell(row, ++colNum, quarterlyEpsGrowth.getFirst());
        fillCell(row, ++colNum, quarterlyEpsGrowth.subList(0, 3));
        fillCell(row, ++colNum, info.getEpsGrowth());
        fillCell(row, ++colNum, info.getEpsStability());
        var yearlyEps = info.getYearlyEps();
        fillCell(row, ++colNum, yearlyEps.subList(0, 3));
        fillCell(row, ++colNum, yearlyEps);
        fillCell(row, ++colNum, Arrays.asList(quarterlyEpsGrowth.getFirst(), info.getEpsGrowth()));
        var qtrSales = info.getQuarterlySalesGrowth();
        fillCell(row, ++colNum, qtrSales.getFirst());
        fillCell(row, ++colNum, qtrSales.subList(0, 3));
        fillCell(row, ++colNum, info.getReturnOnEquity());
        var netMargin = info.getNetMargin();
        fillCell(row, ++colNum, netMargin);
        fillCell(row, ++colNum, netMargin.getFirst());
        fillFormula(row, ++colNum, "'Fundamentals Rank'!Q" + rowNum);

    }

    private void fillCell(Row row, int i, List<BigDecimal> values) {
        var strValue = values.stream().map(v -> v == null ? "*" : v.toString())
                .collect(Collectors.joining(","));
        fillCell(row, i, strValue);
    }

    private void fillCell(Row row, int i, Number value) {
        if (value != null) {
            Cell cell = row.createCell(i);
            cell.setCellValue(value.doubleValue());
        }
    }

    private Cell fillCell(Row row, int i, String value) {
        Cell cell = null;
        if (value != null) {
            cell = row.createCell(i);
            cell.setCellValue(value);
        }
        return cell;
    }

    public List<StockInfo> readFile() {
        List<StockInfo> stockInfos = new ArrayList<>();
        try(var is = new FileInputStream(filePath.toFile());
            var wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheet(FUNDAMENTALS_RANK);
            var fmt = new DataFormatter();
            CreationHelper creationHelper = wb.getCreationHelper();
            var eval = creationHelper.createFormulaEvaluator();
            for(var row: sheet) {
                if(row.getRowNum() < 3 || row.getZeroHeight()) {
                    continue;
                }
                var info = new StockInfo(fmt.formatCellValue(row.getCell(0), eval));
                var avgVolStr = fmt.formatCellValue(row.getCell(26));
                info.setAverageWeeklyVolume(NumberUtil.getBigDecimal(avgVolStr));
                var volStr = fmt.formatCellValue(row.getCell(27));
                info.setWeeklyVolume(NumberUtil.getBigDecimal(volStr));
                var insideBar = fmt.formatCellValue(row.getCell(28));
                info.setInsideBar("Y".equals(insideBar));
                var nearPivot = fmt.formatCellValue(row.getCell(29));
                info.setNearPivot("Y".equals(nearPivot));
                stockInfos.add(info);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return stockInfos;
    }
}
