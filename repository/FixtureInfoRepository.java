package com.coin.app.repository;

import java.util.List;

import com.coin.app.model.FixtureInfo;
import com.coin.app.model.enums.FixtureInfoType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureInfoRepository extends JpaRepository<FixtureInfo, Long>
{
    FixtureInfo findByName(String name);

    FixtureInfo findByTypeAndName(FixtureInfoType type, String name);
}
