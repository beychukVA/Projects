package com.x256n.importer.msmguide.importer;

import com.x256n.core.msmguide.domain.IslandEntity;
import com.x256n.core.msmguide.domain.MonsterEntity;
import com.x256n.importer.msmguide.common.Config;
import com.x256n.importer.msmguide.db.DomainDao;

/**
 * @author Aleksey Permyakov (09.12.2015).
 */
public class MonsterImporter extends AbstractImporter {
    private final IFillEntity<MonsterEntity> fillEntity;

    public MonsterImporter(DomainDao domainDao, Config config, IFillEntity<MonsterEntity> fillEntity) {
        super(domainDao, config);
        this.fillEntity = fillEntity;
    }

    public boolean isStored(IslandEntity islandEntity, MonsterEntity monsterEntity) {
//        domainDao.findQuery("from " + MonsterEntity.class.getSimpleName() + " where")
        return false;
    }

    public void update(IslandEntity islandEntity, MonsterEntity monsterEntity) {

    }

    public void create(IslandEntity islandEntity, MonsterEntity monsterEntity) {

    }

}
