package com.vkg.finance.share.stock.extractor;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.model.FundType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MarketCapExtractor implements DataExtractor<List<FundInfo>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketCapExtractor.class);
    @Override
    public List<FundInfo> extract(MultipartFile file) {
        List<FundInfo> fundInfoList = List.of();
        try(InputStream is = file.getInputStream()) {
            Workbook wb = new XSSFWorkbook(is);
            fundInfoList = readAllFundInfo(wb.getSheetAt(0));
        } catch(IOException e) {
            LOGGER.error("failed to extract {}", file.getName(), e);
        }
        return fundInfoList;
    }

    private static List<FundInfo> readAllFundInfo(Sheet sheet) {
        List<FundInfo> fundInfoList = new ArrayList<>();
        for (Row row : sheet) {
            if(row == null || row.getCell(0) == null
                    || row.getCell(0).getCellType() != Cell.CELL_TYPE_NUMERIC) continue;
            fundInfoList.add(readFundInfo(row));
        }
        return fundInfoList;
    }

    private static FundInfo readFundInfo(Row row) {
        final FundInfo fundInfo = new FundInfo();
        fundInfo.setSymbol(getString(row.getCell(1)));
        fundInfo.setName(getString(row.getCell(2)));
        fundInfo.setType(FundType.STOCK);
        fundInfo.setMarketCap(getDouble(row.getCell(3)));
        return fundInfo;
    }

    private static String getString(Cell cell) {
        return cell.getStringCellValue();
    }

    private static Double getDouble(Cell cell) {
        return cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? cell.getNumericCellValue() : null;
    }
}
