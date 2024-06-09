package com.vkg.finance.share.stock.repository;

import com.vkg.finance.share.stock.model.FundInfo;
import com.vkg.finance.share.stock.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
public class FileBasedFundDetailDao implements FundDetailDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedFundDetailDao.class);
    public static final String ALL_FUND_INFO_FILE_NAME = "all_fund_info.txt";

    @Value("${data.storage.path}")
    private Path basePath;

    @Override
    public List<FundInfo> loadAll() {
        List<FundInfo> infoList;

        try {
            var list = FileUtil.loadFromFile(getFundInfoPath(), FundInfo[].class);
            infoList = Arrays.asList(list);
        } catch (FileNotFoundException e) {
            infoList = new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Not able to load fund info", e);
        }

        return infoList;
    }

    @Override
    public void saveAll(Collection<FundInfo> values) {
        Path fundInfoPath = getFundInfoPath();
        try {
            FileUtil.saveToFile(fundInfoPath, values);
        } catch (IOException e) {
            LOGGER.error("Not able to save fundInfo list", e);
        }
    }

    private Path getFundInfoPath() {
        return basePath.resolve(ALL_FUND_INFO_FILE_NAME);
    }
}
