package com.x256n.importer.msmguide;

import com.x256n.core.msmguide.domain.*;
import com.x256n.core.msmguide.enums.Clazz;
import com.x256n.core.msmguide.enums.Element;
import com.x256n.importer.msmguide.common.Config;
import com.x256n.importer.msmguide.common.IConstants;
import com.x256n.importer.msmguide.common.Parsers;
import com.x256n.importer.msmguide.common.Utils;
import com.x256n.importer.msmguide.db.DomainDao;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Aleksey Permyakov (26.11.2015).
 */
public class ImportProcessor extends ImporterLibrary
{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ImportProcessor.class);

    /**
     * ID для DecorationsmonsterEntity
     */
    private static int DECORATION_MONSTER_ENTITY_ID = 1;

    public void process(File inputDirectory) throws Exception
    {
        final File[] listFiles = inputDirectory.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + inputDirectory.getAbsolutePath());
        }
        for (File currentFile : listFiles)
        {
            if (!currentFile.isDirectory())
            {
                continue;
            }
            final String name = currentFile.getName();
            final int das = Utils.getDigitsAtStart(name);
            switch (das)
            {
                case IConstants.DECORATIONS_01:
                    logger.info("=========================");
                    logger.info("Обрабатываем декорации...");
                    processDecorations(currentFile);
                    break;
                case IConstants.RARE_MONSTERS_02:
                    logger.info("===============================");
                    logger.info("Обрабатываем редких монстров...");
                    processRareMonsters(currentFile);
                    break;
                case IConstants.ISLANDS_03:
                    logger.info("=======================");
                    logger.info("Обрабатываем острова...");
                    processIslands(currentFile);
                    break;
                case IConstants.MONSTERS_04:
                    logger.info("========================");
                    logger.info("Обрабатываем монстров...");
                    processMonsters(currentFile);
                    break;
                case IConstants.BUILDINGS_06:
                    logger.info("========================");
                    logger.info("Обрабатываем строения...");
                    processBuildings(currentFile);
                    break;
            }
        }
        logger.info("Все папки обработаны!");
    }

    private void processBuildings(File currentFile) throws Exception
    {
        final File[] listFiles = currentFile.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + currentFile.getAbsolutePath());
        }
        for (File file : listFiles)
        {
            if (!file.isDirectory())
            {
                continue;
            }
            logger.info("");
            logger.info("Обрабатываю папку: {}", file.getName());
            processEachBuilding(file);
        }
    }

    private void processEachBuilding(File buildingDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(buildingDirectory);

        IconEntity iconEntity = null;
        LocalizedEntity localizedName = null;
        LocalizedEntity localizedDescription = null;

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.BUILDINGS_ICON_01:
                    iconEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconEntity.getGuid());
                    break;
                case IConstants.BUILDINGS_NAME_02:
                    localizedName = createOrLoadLocalization(config);
                    logger.info("Имя: {}", localizedName.getGuid());
                    break;
                case IConstants.BUILDINGS_DESCRIPTION_03:
                    localizedDescription = createOrLoadLocalization(config);
                    logger.info("Описание: {}", localizedDescription.getGuid());
                    break;
            }
        }
        assertEntity(iconEntity, "Не удалось создать/загрузить иконку: " + buildingDirectory.getAbsolutePath());
        assertEntity(localizedName, "Не удалось создать/загрузить имя: " + buildingDirectory.getAbsolutePath());
        assertEntity(localizedDescription, "Не удалось создать/загрузить описание: " + buildingDirectory.getAbsolutePath());

        Config config = new Config();
        config.setItemDirectory(buildingDirectory);
        createOrLoadBuilding(config, iconEntity, localizedName, localizedDescription);
    }

    private BuildingEntity createOrLoadBuilding(Config config, IconEntity iconEntity,
                                                LocalizedEntity localizedName,
                                                LocalizedEntity localizedDescription) throws Exception
    {
        DomainDao domainDao = new DomainDao();

        final String iconGuid = iconEntity.getGuid();

        final BuildingEntity entity;
        if (buildingStored(domainDao, config))
        {
            entity = updateBuilding(domainDao, config, iconGuid, localizedName, localizedDescription);
        }
        else
        {
            entity = createBuilding(domainDao, config, iconGuid, localizedName, localizedDescription);
        }
        return entity;
    }

    private BuildingEntity createBuilding(DomainDao domainDao,
                                          Config config,
                                          String iconGuid,
                                          LocalizedEntity nameGuid,
                                          LocalizedEntity descroptionGuid) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = UUID.randomUUID().toString();
        FileUtils.writeByteArrayToFile(guidFile, guid.getBytes("utf-8"));
        final BuildingEntity entity = new BuildingEntity();
        entity.setGuid(guid);
        entity.setIcon(iconGuid);
        entity.setName(nameGuid);
        entity.setDescription(descroptionGuid);
        domainDao.create(entity);
        return entity;
    }

    private BuildingEntity updateBuilding(DomainDao domainDao,
                                          Config config,
                                          String iconGuid,
                                          LocalizedEntity nameGuid,
                                          LocalizedEntity descroptionGuid) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = FileUtils.readFileToString(guidFile);
        final BuildingEntity entity = domainDao.findByGuid(BuildingEntity.class, guid);
        entity.setIcon(iconGuid);
        entity.setName(nameGuid);
        entity.setDescription(descroptionGuid);
        domainDao.update(entity);
        return entity;
    }

    private boolean buildingStored(DomainDao domainDao, Config config) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        if (guidFile.exists() && guidFile.length() > 0)
        {
            final String guid = FileUtils.readFileToString(guidFile);
            return domainDao.findByGuid(BuildingEntity.class, guid) != null;
        }
        return false;
    }


    private void processMonsters(File currentFile) throws Exception
    {
        final File[] listFiles = currentFile.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + currentFile.getAbsolutePath());
        }
        for (File file : listFiles)
        {
            if (!file.isDirectory())
            {
                continue;
            }
            logger.info("");
            logger.info("Обрабатываю папку: {}", file.getName());
            processEachMonster(file);
        }
    }

    private void processIslands(File currentFile) throws Exception
    {
        final File[] listFiles = currentFile.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + currentFile.getAbsolutePath());
        }
        for (File file : listFiles)
        {
            if (!file.isDirectory())
            {
                continue;
            }
            logger.info("");
            logger.info("Обрабатываю папку: {}", file.getName());
            processEachIsland(file);
        }
    }

    private void processEachIsland(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);

        IslandEntity entity = new IslandEntity();

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.ISLANDS_MAP_ICON_01:
                    final IconEntity iconMapEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconMapEntity.getGuid());
                    entity.setIconmap(iconMapEntity.getGuid());
                    break;
                case IConstants.ISLANDS_NAME_02:
                    final LocalizedEntity nameEntity = createOrLoadLocalization(config);
                    logger.info("Имя: {}", nameEntity.getGuid());
                    entity.setName(nameEntity);
                    break;
                case IConstants.ISLANDS_PRICE_03:
                    final LocalizedEntity priceEntity = createOrLoadLocalization(config);
                    logger.info("Стоимость: {}", priceEntity.getGuid());
                    entity.setPrice(priceEntity);
                    break;
                case IConstants.ISLANDS_ICON_04:
                    final IconEntity iconEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconEntity.getGuid());
                    entity.setIcon(iconEntity.getGuid());
                    break;
                case IConstants.ISLANDS_DESCRIPTION_05:
                    final LocalizedEntity descriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание: {}", descriptionEntity.getGuid());
                    entity.setDescription(descriptionEntity);
                    break;
                case IConstants.ISLANDS_MONSTERS_DESCRIPTION_06:
                    final LocalizedEntity monstersDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание монстров: {}", monstersDescriptionEntity.getGuid());
                    entity.setMonstersdescription(monstersDescriptionEntity);
                    break;
                case 7: //На острове выводятся!!!!!!!! (islandmonster)
                    break;
                case IConstants.ISLANDS_LANDSCAPE_DESCRIPTION_08:
                    final LocalizedEntity landscapeDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание ланшафта: {}", landscapeDescriptionEntity.getGuid());
                    entity.setLandscapedescription(landscapeDescriptionEntity);
                    break;
                case IConstants.ISLANDS_SPECIAL_DECORATIONS_09:
                    final LocalizedEntity specialDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Специальные декорации: {}", specialDescriptionEntity.getGuid());
                    entity.setSpecialdescription(specialDescriptionEntity);
                    break;
                case IConstants.ISLANDS_SPECIAL_MONSTERS_10:
                    //--- ДОЛЖНЫ ДОБАВИТЬ ПОЛЯ!!!
                    break;
                case IConstants.ISLANDS_IMPROVEMENT_DESCRIPTION_11:
                    final LocalizedEntity improvementDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание улучшения: {}", improvementDescriptionEntity.getGuid());
                    entity.setImprovementdescription(improvementDescriptionEntity);
                    break;
                case IConstants.ISLANDS_UPGRADES_TABLE_12:

                    break;
                case 15://Интересные факты о Золотом острове
                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
                    logger.info("Интересные факты: {}", interestingEntity.getGuid());
                    break;
            }
        }

        Config config = new Config();
        config.setItemDirectory(itemDirectory);
        final IslandEntity island = createOrLoadIsland(config, entity);

//        updateAdditionalInformation(entrySet, island);
    }


    protected IslandEntity createOrLoadIsland(Config config, IslandEntity islandEntity) throws Exception
    {
        DomainDao domainDao = new DomainDao();

        final IslandEntity entity;
        if (islandStored(domainDao, config))
        {
            entity = updateIsland(domainDao, config, islandEntity);
        }
        else
        {
            entity = createIsland(domainDao, config, islandEntity);
        }
        return entity;
    }


    protected IslandEntity createIsland(DomainDao domainDao, Config config, IslandEntity islandEntity) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = UUID.randomUUID().toString();
        FileUtils.writeByteArrayToFile(guidFile, guid.getBytes("utf-8"));
        islandEntity.setGuid(guid);
        domainDao.create(islandEntity);
        return islandEntity;
    }

    protected IslandEntity updateIsland(DomainDao domainDao, Config config, IslandEntity islandEntity) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = FileUtils.readFileToString(guidFile);
        final IslandEntity entity = domainDao.findByGuid(IslandEntity.class, guid);

        entity.setIconmap(islandEntity.getIconmap());
        entity.setName(islandEntity.getName());
        entity.setPrice(islandEntity.getPrice());
        entity.setIcon(islandEntity.getIcon());
        entity.setDescription(islandEntity.getDescription());
        entity.setMonstersdescription(islandEntity.getMonstersdescription());
        entity.setLandscapedescription(islandEntity.getLandscapedescription());
        entity.setSpecialdescription(islandEntity.getSpecialdescription());
        entity.setImprovementdescription(islandEntity.getImprovementdescription());

        domainDao.update(entity);
        return entity;
    }

    protected boolean islandStored(DomainDao domainDao, Config config) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        if (guidFile.exists() && guidFile.length() > 0)
        {
            final String guid = FileUtils.readFileToString(guidFile);
            return domainDao.findByGuid(IslandEntity.class, guid) != null;
        }
        return false;
    }


    private void processRareMonsters(File currentFile) throws Exception
    {
        final File[] listFiles = currentFile.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + currentFile.getAbsolutePath());
        }
        for (File file : listFiles)
        {
            if (!file.isDirectory())
            {
                continue;
            }
            logger.info("");
            logger.info("Обрабатываю папку: {}", file.getName());
            processEachRareMonsters(file);
        }
    }
    private void processEachRareMonsters(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);

        DomainDao domainDao = new DomainDao();
        MonsterEntity monsterEntity = new MonsterEntity();
        //--- Записываю Имя Редкого монстра
        LocalizedEntity nameEntity = new LocalizedEntity();
        nameEntity.setGuid(UUID.randomUUID().toString());
        nameEntity.setLocale((byte)1);
        nameEntity.setText(Utils.makeNameFromDirName(itemDirectory));
        domainDao.create(nameEntity);
        logger.info("Имя: {}", nameEntity.getGuid());
        monsterEntity.setName(nameEntity);

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case 1:
                    final IconEntity iconEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconEntity.getGuid());
                    monsterEntity.setIcon(iconEntity.getGuid());
                    break;
                case 2:
                    final LocalizedEntity descriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание: {}", descriptionEntity.getGuid());
                    monsterEntity.setDescription(descriptionEntity);
                    break;
                case 3:
                    final LocalizedEntity createDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание выведения: {}", createDescriptionEntity.getGuid());
                    monsterEntity.setCreatedescription(createDescriptionEntity);
                    break;
                case 4:
                    final String clazz = getString(config);
                    logger.info("Класс: {}", clazz);
                    final Clazz parseClazz = Parsers.parseClazz(clazz);
                    if (parseClazz != null)
                    {
                        monsterEntity.setClazz(parseClazz.toValue());
                    }
                    break;
                case 5:
                    final int place = findInteger(config);
                    logger.info("Занимает места: {}", place);
                    monsterEntity.setPlace(Byte.parseByte(Integer.toString(place), 10));
                    break;
                case 7: //Время созревания (monstercreatetime) - time
                    break;
                case 8: //Обитает на островах (monsterlive) + обновление (monstercreatetime) - добавляем остров
                    break;
                case 9: //бонус за перемещение на Золотой Остров (monstermove)
                    break;
                case 10: //Выведение (monstercreate)
                    break;
                case 11: //Элементы (monsterelement)
                    break;
                case 12: //Как выглядит яйцо (icon)
                    IconEntity eggEntity = createOrLoadIcon(config);
                    monsterEntity.setEgg(eggEntity.getGuid());
                    logger.info("Как выглядит яйцо: {}", eggEntity.toString());
                    break;
                case 13: //Как перенести на остров (monstermove) + update
                    final LocalizedEntity moveEntity = createOrLoadLocalization(config);
                    logger.info("Как переместить: {}", moveEntity.getGuid());
                    monsterEntity.setMove(moveEntity);
                    break;
                case 14: //Влечения (monstercreate) - targetmonster
                    break;
                case 15: //Редкий (monster) - rare
                    break;
                case 16: //Интересные факты (monster) - interesting
                    String interesting = Utils.readFileToString(value);
                    LocalizedEntity interestingLocalized = createOrLoadLocalization(config);
                    interestingLocalized.setLocale((byte)1);
                    interestingLocalized.setText(interesting);
                    logger.info("Интересные факты: {}", interestingLocalized.getText());
                    monsterEntity.setInteresting(interestingLocalized);
                    break;
                case 17: //Прибыль золота по уровням (monster) profitlevel
//                    int profitlevel = findInteger(config);
//                    logger.debug("Прибыль золота по уровням: {}", profitlevel);
//                    monsterEntity.setProfitlevel(String.valueOf(profitlevel));
//                    ??????????????????????????????????
                    break;
                case 18: //Заработок золота в минуту (monster) profittime
                    break;
                case 19: //Заработок осколков в час
                    break;
                case 20: //Прибыль осколков по уровням
                    break;
                case 21: //Продается в Маркете за Изумруды (monster) - priceemerald
                    int priceemerald = findInteger(config);
                    logger.info("Продается в Маркете за Изумруды: {}", priceemerald);
                    monsterEntity.setPriceemerald(priceemerald);
                    break;
            }
        }

        Config config = new Config();
        config.setItemDirectory(itemDirectory);
        final MonsterEntity monster = createOrLoadMonster(config, monsterEntity);

//        updateAdditionalInformation(entrySet, monster);
    }

    private void processDecorations(File currentFile) throws Exception
    {
        final File[] listFiles = currentFile.listFiles();
        if (listFiles == null)
        {
            throw new Exception("Папка пустая! " + currentFile.getAbsolutePath());
        }
        for (File file : listFiles)
        {
            if (!file.isDirectory())
            {
                continue;
            }
            logger.info("");
            logger.info("Обрабатываю папку: {}", file.getName());
            processEachDecoration(file);
        }
    }

    private void processEachDecoration(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);

        DecorationsEntity decorationsEntity = new DecorationsEntity();

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.DECORATIONS_ICON_01:
                    final IconEntity iconEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconEntity.getGuid());
                    decorationsEntity.setIcon(iconEntity.getGuid());
                    break;
                case IConstants.DECORATIONS_NAME_02:
                    final LocalizedEntity localizedEntity = createOrLoadLocalization(config);
                    logger.info("Название: {}", localizedEntity.getGuid());
                    decorationsEntity.setName(localizedEntity);
                    break;
                case IConstants.DECORATIONS_LEVEL_03:
                    final int level = findInteger(config);
                    logger.info("Уровень: {}", level);
                    decorationsEntity.setLevel(Byte.parseByte(Integer.toString(level), 10));
                    break;
                case IConstants.DECORATIONS_PRICE_GOLD_04:
                    final int pricegold = findInteger(config);
                    logger.info("Стоимость в золоте: {}", pricegold);
                    decorationsEntity.setPricegold(pricegold);
                    break;
                case IConstants.DECORATIONS_PRICE_EMERALD_05:
                    final int priceemerald = findInteger(config);
                    logger.info("Стоимость в изумрудах: {}", priceemerald);
                    decorationsEntity.setPriceemerald(priceemerald);
                    break;
                case IConstants.DECORATIONS_DESCRIPTION_06:
                    final LocalizedEntity descriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание: {}", descriptionEntity.getGuid());
                    decorationsEntity.setDescription(descriptionEntity);
                    break;
                case IConstants.DECORATIONS_DESCRIPTION_GAME_07:
                    final LocalizedEntity descriptionGameEntity = createOrLoadLocalization(config);
                    logger.info("Игровое описание: {}", descriptionGameEntity.getGuid());
                    decorationsEntity.setDescriptiongame(descriptionGameEntity);
                    break;
                case IConstants.DECORATIONS_MONSTERS_LIST_08:
                    break;
            }
        }
        Config config = new Config();
        config.setItemDirectory(itemDirectory);
        createOrLoadDecoration(config, decorationsEntity);
    }

    private void processEachMonster(File itemDirectory) throws Exception
    {
//        // Save monster name as 00
//        final String monsterNameKey = "00-Имя монстра.txt";
//        FileUtils.writeStringToFile(new File(itemDirectory.getAbsolutePath() + File.separator + monsterNameKey), itemDirectory.getName().substring(3));

        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);

        MonsterEntity entity = new MonsterEntity();
        DomainDao domainDao = new DomainDao();

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.MONSTERS_NAME_00:
                    final LocalizedEntity nameEntity = createOrLoadLocalization(config);
                    logger.info("Имя: {}", nameEntity.getGuid());
                    entity.setName(nameEntity);
                    break;
                case IConstants.MONSTERS_ICON_01:
                    final IconEntity iconEntity = createOrLoadIcon(config);
                    logger.info("Иконка: {}", iconEntity.getGuid());
                    entity.setIcon(iconEntity.getGuid());
                    break;
                case IConstants.MONSTERS_DESCRIPTION_02:
                    final LocalizedEntity descriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание: {}", descriptionEntity.getGuid());
                    entity.setDescription(descriptionEntity);
                    break;
                case IConstants.MONSTERS_CREATE_DESCRIPTION_03:
                    final LocalizedEntity createDescriptionEntity = createOrLoadLocalization(config);
                    logger.info("Описание выведения: {}", createDescriptionEntity.getGuid());
                    entity.setCreatedescription(createDescriptionEntity);
                    break;
                case IConstants.MONSTERS_CLASS_04:
                    final String clazz = getString(config);
                    logger.info("Класс: {}", clazz);
                    final Clazz parseClazz = Parsers.parseClazz(clazz);
                    if (parseClazz != null)
                    {
                        entity.setClazz(parseClazz.toValue());
                    }
                    break;
                case IConstants.MONSTERS_PLACE_05:
                    final int place = findInteger(config);
                    logger.info("Занимает места: {}", place);
                    entity.setPlace(Byte.parseByte(Integer.toString(place), 10));
                    break;
                case IConstants.MONSTERS_PRICE_GOLD_06:
                    final int priceGold = findInteger(config);
                    logger.info("Стоимость золота: {}", priceGold);
                    entity.setPricegold(priceGold);
                    break;
                case IConstants.MONSTERS_TIME_CREATION_07: //monstercreatetime
//                    entity = createOrLoadMonster(config, entity);
//                    MonstercreatetimeEntity monstercreatetimeEntity = new MonstercreatetimeEntity();
//                    monstercreatetimeEntity.setId(DECORATION_MONSTER_ENTITY_ID);
//                    monstercreatetimeEntity.setTime(convertTimeInLong(config));
//                    monstercreatetimeEntity.setMonster(entity);
//                    domainDao.create(monstercreatetimeEntity);
                    break;
                case IConstants.MONSTERS_EGG_12:
                    IconEntity icon = domainDao.findIcon(value);
                    if(icon == null)
                    {
                        icon = createOrLoadIcon(config);
                    }
                    logger.info("Яйцо: {}", icon.getGuid());
                    entity.setEgg(icon.getGuid());
                    break;
                case IConstants.MONSTERS_MOVE_13:
                    final LocalizedEntity moveEntity = createOrLoadLocalization(config);
                    logger.info("Как переместить: {}", moveEntity.getGuid());
                    entity.setMove(moveEntity);
                    break;
                case 14://Влечения - monsterappetencemonster
                    break;
                case IConstants.MONSTERS_RARE_15:
                    String line = Utils.readFileToString(value);
                    if(line != null && !line.isEmpty())
                    {
                        String[] arrRare = line.trim().split("\\n");
                        for (int i = 0; i < arrRare.length; i++)
                        {
                            String guide = domainDao.findMonsterByName(arrRare[i].trim());
                            if(guide != null)
                            {
                                entity.setRare(guide);
                            }
                        }
                    }
                    break;
                case IConstants.MONSTERS_INTERESTING_16:
                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
                    logger.info("Интересы: {}", interestingEntity.getGuid());
                    entity.setInteresting(interestingEntity);
                    break;
                case IConstants.MONSTERS_PROFIT_BY_LEVEL_17:
//                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
//                    entity.setMove(interestingEntity.getGuid());
                    break;
                case IConstants.MONSTERS_PROFIT_BY_TIME_18:
//                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
//                    entity.setMove(interestingEntity.getGuid());
                    break;
                case IConstants.MONSTERS_PROFIT_SPLINTER_TIME_19:
//                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
//                    entity.setMove(interestingEntity.getGuid());
                    break;
                case IConstants.MONSTERS_PROFIT_SPLINTER_LEVEL_20:
//                    final LocalizedEntity interestingEntity = createOrLoadLocalization(config);
//                    entity.setMove(interestingEntity.getGuid());
                    break;
                case IConstants.MONSTERS_PRICE_EMERALD_21:
                    final int priceEmerald = findInteger(config);
                    logger.info("Стоимость изумрудов: {}", priceEmerald);
                    entity.setPriceemerald(priceEmerald);
                    break;
            }
        }

        Config config = new Config();
        config.setItemDirectory(itemDirectory);
        final MonsterEntity monster = createOrLoadMonster(config, entity);

//        updateAdditionalInformation(entrySet, monster);
    }

    private void updateAdditionalInformation(Set<Map.Entry<Integer, File>> entrySet, IslandEntity island) throws Exception
    {
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.ISLANDS_MONSTERS_07:
                    createIslandMonstersIfNotExists(config, island);
                    break;
            }
        }
    }

    private void createIslandMonstersIfNotExists(Config config, IslandEntity island) throws Exception
    {
        final File file = config.getItemFile();
        final String timeCreationString = Utils.readFileToString(file);
        final String[] splitLines = timeCreationString.split("[\\r\\n]");
        DomainDao domainDao = new DomainDao();
        // Delete exists
        island.getMonsters().clear();
        domainDao.update(island);
        domainDao.deleteAllIslandmonsterForIsland(island.getGuid());
        // Create new
        for (String splitLine : splitLines)
        {
            if (splitLine != null)
            {
                splitLine = splitLine.trim().toUpperCase();
                if (!splitLine.isEmpty())
                {
                    final MonsterEntity monster = domainDao.monsterByName(splitLine.trim().toUpperCase());
                    if (monster != null)
                    {
                        IslandmonsterEntity entity = new IslandmonsterEntity();
                        entity.setIsland(island);
                        entity.setMonster(monster);
                        domainDao.create(entity);
                        island.getMonsters().add(entity);
                    }
                }
            }
        }
        domainDao.update(island);
    }

    private void updateAdditionalInformation(Set<Map.Entry<Integer, File>> entrySet, MonsterEntity monster) throws Exception
    {
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.MONSTERS_ELEMENTS_11:
                    createElementsIfNotExists(config, monster);
                    break;
                case IConstants.MONSTERS_TIME_CREATION_07:
                    createTimeCreationIfNotExists(config, monster);
                    break;
                case IConstants.MONSTERS_CREATION_10:
                    createMonsterDeductionsIfNotExists(config, monster);
                    break;
                case IConstants.MONSTERS_ISLANDS_08:
                    createMonsterLocationsIfNotExists(config, monster);
                    break;
                case IConstants.MONSTERS_LIKES_14:
                    createMonsterLikesIfNotExists(config, monster);
                    break;
            }
        }
    }

    private void createMonsterLikesIfNotExists(Config config, MonsterEntity monster) throws Exception
    {
        final File file = config.getItemFile();
        final String timeCreationString = Utils.readFileToString(file);
        final String[] splitLines = timeCreationString.split("[\\r\\n]");
        DomainDao domainDao = new DomainDao();
        // Delete exists
        domainDao.deleteAllAppetenceDecorationsForMonster(monster.getGuid());
        domainDao.deleteAllAppetenceMonstersForMonster(monster.getGuid());
        // Create new
        for (String splitLine : splitLines)
        {
            if (splitLine != null)
            {
                splitLine = splitLine.trim().toUpperCase();
                if (!splitLine.isEmpty())
                {
                    final DecorationsEntity decorationEntity = domainDao.decorationByName(splitLine);
                    final MonsterEntity monsterEntity = domainDao.monsterByName(splitLine);
                    if (decorationEntity != null)
                    {
                        MonsterappetencedecorationsEntity entity = new MonsterappetencedecorationsEntity();
                        entity.setDecoration(decorationEntity);
                        entity.setMonster(monster);
                        domainDao.create(entity);
                    }
                    if (monsterEntity != null)
                    {
                        MonsterappetencemonsterEntity entity = new MonsterappetencemonsterEntity();
                        entity.setTarget(monsterEntity);
                        entity.setMonster(monster);
                        domainDao.create(entity);
                    }
                }
            }
        }
    }

    private void createMonsterLocationsIfNotExists(Config config, MonsterEntity monster) throws Exception
    {
        final File file = config.getItemFile();
        final String timeCreationString = Utils.readFileToString(file);
        final String[] splitLines = timeCreationString.split("[\\.,\\s]");
        DomainDao domainDao = new DomainDao();
        // Delete exists
        domainDao.deleteAllLocationsForMonster(monster.getGuid());
        // Create new
        for (String splitLine : splitLines)
        {
            if (splitLine != null && splitLine.trim().length() > 5)
            {
                final IslandEntity island = domainDao.islandByName(splitLine.substring(2, splitLine.length() - 4).toUpperCase());
                MonsterliveEntity entity = new MonsterliveEntity();
                entity.setIsland(island);
                entity.setMonster(monster);
                if (island != null)
                {
                    domainDao.create(entity);
                }
            }
        }
    }

    private void createMonsterDeductionsIfNotExists(Config config, MonsterEntity monster) throws Exception
    {
        final File file = config.getItemFile();
        final String timeCreationString = Utils.readFileToString(file);
        final String[] splitLines = timeCreationString.split("[\\r\\n]");
        DomainDao domainDao = new DomainDao();
        // Delete exists
        domainDao.deleteAllDeductionsForMonster(monster.getGuid());
        // Create new
        for (String splitLine : splitLines)
        {
            if (splitLine != null)
            {
                final String[] splitTokens = splitLine.split("\\+");
                if (splitTokens.length < 2)
                {
                    continue;
                }
                MonsterEntity src = domainDao.monsterByName(splitTokens[0]);
                MonsterEntity trg = domainDao.monsterByName(splitTokens[1]);

                MonstercreateEntity entity = new MonstercreateEntity();
                entity.setMonster(monster);
                entity.setSourcemonster(src);
                entity.setTargetmonster(trg);
                if (src != null && trg != null)
                {
                    domainDao.create(entity);
                }
            }
        }
    }

    private void createTimeCreationIfNotExists(Config config, MonsterEntity monster) throws Exception
    {
        final File file = config.getItemFile();
        final String timeCreationString = FileUtils.readFileToString(file);
        final String[] split = timeCreationString.split("(\\s[и]\\s|[,;])");
        DomainDao domainDao = new DomainDao();
        String logTimes = "";
        // Delete exists
        monster.getCreatetime().clear();
        domainDao.update(monster);
        domainDao.deleteAllCreateTimesForMonster(monster.getGuid());
        // Create new
        for (String timeString : split)
        {
            long timeCreation = Parsers.parseTimeCreation(timeString);
            String placeCreation = Parsers.parsePlaceCreation(timeString);
            IslandEntity islandEntity = placeCreation == null ? null : domainDao.islandByName(placeCreation);

            MonstercreatetimeEntity entity = new MonstercreatetimeEntity();
            entity.setIsland(islandEntity);
            entity.setMonster(monster);
            entity.setTime(timeCreation);
            domainDao.create(entity);
            monster.getCreatetime().add(entity);

            logTimes += placeCreation + "[" + timeCreation + "], ";
        }
        domainDao.update(monster);

        logger.debug("Время созревания: {}", logTimes);
    }

    private void createElementsIfNotExists(Config config, MonsterEntity monster) throws Exception
    {
        final File file = config.getItemFile();
        final String elementsString = FileUtils.readFileToString(file);
        final String[] elementsSplit = elementsString.split("[\r\n]");
        DomainDao domainDao = new DomainDao();
        for (String elementString : elementsSplit)
        {
            final Element element = Parsers.parseElement(elementString);
            if (element != null)
            {
                final MonsterelementEntity me = domainDao.monsterElementByMonsterAndElement(monster.getGuid(), element.toValue());
                if (me == null)
                {
                    MonsterelementEntity entity = new MonsterelementEntity();
                    entity.setMonster(monster);
                    entity.setElement(element.toValue());
                    domainDao.create(entity);
                    monster.getElements().add(entity);
                }
            }
        }
        domainDao.update(monster);

        logger.debug("Элементы: {}", elementsString.replaceAll("[\n\r]", " ; "));
    }

    private MonsterEntity createOrLoadMonster(Config config, MonsterEntity createdEntity) throws Exception
    {
        DomainDao domainDao = new DomainDao();

        final MonsterEntity entity;
        if (monsterStored(domainDao, config))
        {
            entity = updateMonster(domainDao, config, createdEntity);
        }
        else
        {
            entity = createMonster(domainDao, config, createdEntity);
        }
        return entity;
    }

    private MonsterEntity createMonster(DomainDao domainDao, Config config, MonsterEntity createdEntity) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = UUID.randomUUID().toString();
        FileUtils.writeByteArrayToFile(guidFile, guid.getBytes("utf-8"));
        createdEntity.setGuid(guid);
        domainDao.create(createdEntity);
        return createdEntity;
    }

    private MonsterEntity updateMonster(DomainDao domainDao, Config config, MonsterEntity createdEntity) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = FileUtils.readFileToString(guidFile);
        final MonsterEntity entity = domainDao.findByGuid(MonsterEntity.class, guid);
        entity.setName(createdEntity.getName());
        entity.setIcon(createdEntity.getIcon());
        entity.setDescription(createdEntity.getDescription());
        entity.setCreatedescription(createdEntity.getCreatedescription());
        entity.setClazz(createdEntity.getClazz());
        entity.setPlace(createdEntity.getPlace());
        entity.setPriceemerald(createdEntity.getPriceemerald());
        entity.setPricegold(createdEntity.getPricegold());
        entity.setEgg(createdEntity.getEgg());
        entity.setMove(createdEntity.getMove());
        entity.setRare(createdEntity.getRare());
        entity.setInteresting(createdEntity.getInteresting());
        entity.setProfitlevel(createdEntity.getProfitlevel());
        entity.setProfittime(createdEntity.getProfittime());

        domainDao.update(entity);
        return entity;
    }

    private boolean monsterStored(DomainDao domainDao, Config config) throws Exception
    {
        final File guidFile = config.getDirectoryGuidFile();
        if (guidFile.exists() && guidFile.length() > 0)
        {
            final String guid = FileUtils.readFileToString(guidFile);
            return domainDao.findByGuid(MonsterEntity.class, guid) != null;
        }
        return false;
    }

}
