package com.x256n.importer.msmguide.importer;

import com.x256n.importer.msmguide.common.Config;
import com.x256n.importer.msmguide.db.DomainDao;

/**
 * @author Aleksey Permyakov (09.12.2015).
 */
public abstract class AbstractImporter {
    protected final Config config;
    protected final DomainDao domainDao;

    public AbstractImporter(DomainDao domainDao, Config config) {
        this.domainDao = domainDao;
        this.config = config;
    }
}
