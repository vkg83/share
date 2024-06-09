package com.vkg.finance.share.stock.repository;

import com.vkg.finance.share.stock.model.InvestmentProfile;
import com.vkg.finance.share.stock.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

@Repository
public class FileBasedInvestmentProfileDao implements InvestmentProfileDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedInvestmentProfileDao.class);
    public static final String PROFILE_EXT = ".profile";

    @Value("${data.storage.path}")
    private Path basePath;

    @Override
    public void save(InvestmentProfile profile) {
        Path profilePath = getProfilePath(profile.getProfileName());
        try {
            FileUtil.saveToFile(profilePath, profile);
        } catch (IOException e) {
            throw new RuntimeException("Not able to save profile", e);
        }
    }

    @Override
    public InvestmentProfile load(String profileName) {
        Path profilePath = getProfilePath(profileName);
        try {
            return FileUtil.loadFromFile(profilePath, InvestmentProfile.class);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Not able to load profile", e);
        }
    }

    private Path getProfilePath(String profileName) {
        return basePath.resolve(profileName + PROFILE_EXT);
    }
}
