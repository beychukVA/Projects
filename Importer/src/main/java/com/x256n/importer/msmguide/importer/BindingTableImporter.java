package com.x256n.importer.msmguide.importer;

import com.x256n.core.msmguide.domain.*;
import com.x256n.core.msmguide.enums.Element;
import com.x256n.importer.msmguide.ImporterLibrary;
import com.x256n.importer.msmguide.common.Config;
import com.x256n.importer.msmguide.common.IConstants;
import com.x256n.importer.msmguide.common.Parsers;
import com.x256n.importer.msmguide.common.Utils;
import com.x256n.importer.msmguide.db.DomainDao;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BindingTableImporter extends ImporterLibrary
{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(BindingTableImporter.class);
    private static int DECORATIONS_ID = 1;
    private static int RARE_MONSTER_MONSTER_CREATE_TIME_ID = 1;
    private static int RARE_MONSTER_MONSTER_MOVE_TIME_ID = 1;
    private static int RARE_MONSTER_MONSTER_CREATE_ID = 1;
    private static int RARE_MONSTER_APPETENCE_ID = 1;
    private static int ISLAND_MONSTER_ID = 1;
    private static int ISLAND_IMPROVEMENT_ID = 1;
    private static int LANDSCAPE_ID = 1;
    DomainDao domainDao;

    public void process(File inputDirectory) throws Exception
    {
        domainDao = new DomainDao();
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
                    logger.debug("=========================");
                    logger.debug("Обрабатываем декорации...");
                    processDecorations(currentFile);
                    break;
                case IConstants.RARE_MONSTERS_02:
                    logger.debug("===============================");
                    logger.debug("Обрабатываем редких монстров...");
                    processRareMonsters(currentFile);
                    break;
                case IConstants.ISLANDS_03:
                    logger.debug("=======================");
                    logger.debug("Обрабатываем острова...");
                    processIslands(currentFile);
                    break;
                case IConstants.MONSTERS_04:
                    logger.debug("========================");
                    logger.debug("Обрабатываем монстров...");
                    processMonsters(currentFile);
                    break;
                case IConstants.ISLAND_LANDSCAPE_05:
                    logger.debug("========================");
                    logger.debug("Обрабатываем ландшафт острова...");
                    processLandscape(currentFile);
                    break;
            }
        }
        logger.debug("Все папки обработаны!");
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
            logger.debug("");
            logger.debug("Обрабатываю папку: {}", file.getName());
            processEachDecoration(file);
        }
    }
    private void processEachDecoration(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);
        //--- Текущее имя монстра
        DomainDao domainDao = new DomainDao();
//        DecorationsEntity currentDecoration = domainDao.decorationByName(Utils.makeNameFromDirName(itemDirectory));
        String currentDecoration = domainDao.findDecorationByName(Utils.makeNameFromDirName(itemDirectory).trim());
        logger.info("ТЕКУЩЕЕ УКРАШЕНИЕ: {}", currentDecoration);
        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case IConstants.DECORATIONS_MONSTERS_LIST_08:
                    //--- Считываю монстров из файла
                    String values = Utils.readFileToString(value);
                    String[] monsters = values.split("\n");
                    //--- Перебираю монстров и делаю запись для каждого
                    if(monsters.length > 0 && monsters[0] != null && !monsters[0].equals(""))
                    {
                        for (int i = 0; i < monsters.length; i++)
                        {
                            DecorationsmonsterEntity decorationsmonster = new DecorationsmonsterEntity();
                            logger.info("ЗАПИСЬ: " + decorationsmonster);
                            logger.info("monsters.get(i) = " + monsters[i]);
                            String monsterGuid = domainDao.findMonsterByName(monsters[i].trim());
                            //--- Нахожу монстра
                            logger.info("Монстер: {}", monsterGuid);
                            //--- Запись в таблице
                            decorationsmonster.setId(DECORATIONS_ID);
                            decorationsmonster.setDecorations(currentDecoration);
                            decorationsmonster.setMonster(monsterGuid);
                            domainDao.create(decorationsmonster);
                            DECORATIONS_ID++;
                        }
                    }
                    break;
            }
        }
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
            logger.debug("");
            logger.debug("Обрабатываю папку: {}", file.getName());
            processEachRareMonsters(file);
        }
    }
    private void processEachRareMonsters(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);
        //--- Текущий монстр
        DomainDao domainDao = new DomainDao();
        //guid
        String monsterGuid = domainDao.findMonsterByName(Utils.makeNameFromDirName(itemDirectory).trim());
        //monster
        MonsterEntity monsterEntity = domainDao.findByGuid(MonsterEntity.class, monsterGuid);
        logger.info("ТЕКУЩИЙ РЕДКИЙ МОНСТР: " + monsterEntity);
        //--- Время созревания монстра
        long timeCreate = 0;

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case 7: //Время созревания (monstercreatetime) - time
                    //--- Считываю время из файла
                    timeCreate = convertTimeInLong(config);
                    logger.info("ВРЕМЯ СОЗРЕВАНИЯ: " + timeCreate);
                    break;
                case 8: //Обитает на островах (monsterlive) + обновление (monstercreatetime) - добавляем остров
                    //Считываю острова
                    String line = Utils.readFileToString(value);
                    if(line != null && !line.isEmpty() && line.length() > 0)
                    {
                        line = line.trim().replace(".", "");
                        String[] islands = line.trim().split(",");
                        for (int i = 0; i < islands.length; i++)
                        {
                            //--- Нахожу guid острова
                            String islandGuid = null;
                            if(islands[i].contains("остров") || islands[i].contains("Остров"))
                            {
                                islandGuid = domainDao.findIslandByName(islands[i].trim());
                            }
                            else
                            {
                                islandGuid = domainDao.findIslandByName(islands[i].trim() + " остров");
                            }
                            IslandEntity islandEntity = domainDao.findByGuid(IslandEntity.class, islandGuid);
                            //--- Создаю запись в таблице monstercreatetime
                            MonstercreatetimeEntity monstercreatetimeEntity = new MonstercreatetimeEntity();
                            monstercreatetimeEntity.setId(RARE_MONSTER_MONSTER_CREATE_TIME_ID);
                            monstercreatetimeEntity.setIsland(islandEntity);
                            monstercreatetimeEntity.setMonster(monsterEntity);
                            monstercreatetimeEntity.setTime(timeCreate);
                            domainDao.create(monstercreatetimeEntity);
                            //--- Создаю запись в таблице monsterlive
                            MonsterliveEntity monsterliveEntity = new MonsterliveEntity();
                            monsterliveEntity.setId(RARE_MONSTER_MONSTER_CREATE_TIME_ID);
                            monsterliveEntity.setIsland(islandEntity);
                            monsterliveEntity.setMonster(monsterEntity);
                            domainDao.create(monsterliveEntity);
                            RARE_MONSTER_MONSTER_CREATE_TIME_ID++;
                        }
                    }
                    break;
                case 9: //бонус за перемещение на Золотой Остров (monstermove)
                    //--- Нахожу guid Золотого острова
                    String goldGuid = domainDao.findIslandByName("Золотой остров");
                    //monster
                    MonsterEntity monster = domainDao.findByGuid(MonsterEntity.class, monsterGuid);
                    //--- Описание перемещения
                    LocalizedEntity move = monster.getMove();
                    //--- Делаю запись в таблице monstermove
                    MonstermoveEntity monstermoveEntity = new MonstermoveEntity();
                    monstermoveEntity.setId(RARE_MONSTER_MONSTER_MOVE_TIME_ID);
                    monstermoveEntity.setIsland(goldGuid);
                    monstermoveEntity.setMonster(monster.getGuid());
                    monstermoveEntity.setDescription(move.getGuid());
                    monstermoveEntity.setBonus(findInteger(config));
                    domainDao.create(monstermoveEntity);
                    RARE_MONSTER_MONSTER_MOVE_TIME_ID++;
                    break;
                case 10: //Выведение (monstercreate)
                    //--- Считываю файл
                    logger.info("ТЕКУЩИЙ МОНСТР = " + monsterEntity);
                    String line2 = Utils.readFileToString(value);
                    if(line2 != null && !line2.isEmpty() && line2.length() > 0)
                    {
                        line2 = line2.replace("\n", " ");
                        String[] arrMonsters = line2.trim().split(" ");
                        for (int i = 0; i < arrMonsters.length; i++)
                        {
                            //--- Розбиваю пару
                            String[] monsters = arrMonsters[i].split("\\+");
                            //--- Нахожу sourcemonster
                            //guid
                            String sourceGuid = domainDao.findMonsterByName(monsters[0].trim());
                            logger.info("SOURCEMONSTER = " + sourceGuid);
                            //monster
                            MonsterEntity sourceMonster = domainDao.findByGuid(MonsterEntity.class, sourceGuid);
                            //--- Нахожу targetmonster
                            //guid
                            String targetGuid = domainDao.findMonsterByName(monsters[1].trim());
                            logger.info("TARGETMONSTER = " + targetGuid);
                            //monster
                            MonsterEntity targetMonster = domainDao.findByGuid(MonsterEntity.class, targetGuid);
                            //--- Делаю запись в таблице monstercreate
                            MonstercreateEntity monstercreateEntity = new MonstercreateEntity();
                            monstercreateEntity.setId(RARE_MONSTER_MONSTER_CREATE_ID);
                            monstercreateEntity.setMonster(monsterEntity);
                            monstercreateEntity.setSourcemonster(sourceMonster);
                            monstercreateEntity.setTargetmonster(targetMonster);
                            domainDao.create(monstercreateEntity);
                            RARE_MONSTER_MONSTER_CREATE_ID++;

                        }
                    }
                    break;
                case 11: //Элементы (monsterelement)
                    createElementsIfNotExists(config, monsterEntity);
                    break;
                case 14: //Влечения таблицы monsterappetencemonster, monsterappetencedecorations
                    //--- считываю файл и формирую массив обьектов
                    String line1 = Utils.readFileToString(value);
                    if(line1 != null && !line1.isEmpty() && line1.length() > 0)
                    {
                        String[] arrObject = line1.trim().split("\\n");
                        //--- Перебираю объекты и определяю монстер это или украшение
                        //и заполняю соответствующие таблицы
                        for (int i = 0; i < arrObject.length; i++)
                        {
                            //Нахожу guid по имени объекта
                            String monsterGuide = domainDao.findMonsterByName(arrObject[i].trim());

                            //--- Ищу в монстрах
                            MonsterEntity monster1 = domainDao.findByGuid(MonsterEntity.class, monsterGuide);
                            if(monster1 != null)
                            {
                                //Делаю запись в таблице monsterappetencemonster
                                MonsterappetencemonsterEntity appetenceMonster = new MonsterappetencemonsterEntity();
                                appetenceMonster.setId(RARE_MONSTER_APPETENCE_ID);
                                appetenceMonster.setMonster(monsterEntity);
                                appetenceMonster.setTarget(monster1);
                                domainDao.create(appetenceMonster);
                            }
                            else //Если не монстер, ищу в украшениях
                            {
                                String decorationGuid = domainDao.findDecorationByName(arrObject[i].trim());
                                //Ищу в укоашения
                                DecorationsEntity decorations = domainDao.findByGuid(DecorationsEntity.class, decorationGuid);
                                //Делаю запись в таблице monsterappetencedecorations
                                MonsterappetencedecorationsEntity appetenceDecoration = new MonsterappetencedecorationsEntity();
                                appetenceDecoration.setId(RARE_MONSTER_APPETENCE_ID);
                                appetenceDecoration.setMonster(monsterEntity);
                                appetenceDecoration.setDecoration(decorations);
                                domainDao.create(appetenceDecoration);
                            }
                            RARE_MONSTER_APPETENCE_ID++;
                        }
                    }
                    break;
                case 15: //Редкий (monster) - rare
                    break;
                case 17: //Прибыль золота по уровням (monster) profitlevel - таблица - monsterprofitgoldlevel
                    ArrayList<String> lines = Utils.readFileByLine(value);
                    if(lines.size() > 0)
                    {
                        int level = 0;
                        int maxprofit = 0;
                        int food = 0;
                        for (int i = 1; i < lines.size(); i++)
                        {
                            String[] splits = lines.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            maxprofit = findInteger(splits[1]);
                            if(i < 15)
                            {
                                String[] foods = splits[2].trim().split("=");
                                food = findInteger(foods[1]);
                            }
                            else
                            {
                                food = 0;
                            }
                            //--- Делаю запись в таблице monsterprofitgoldlevel
                            MonsterprofitgoldlevelEntity monsterprofitgoldlevel = new MonsterprofitgoldlevelEntity();
                            monsterprofitgoldlevel.setGuid(UUID.randomUUID().toString());
                            monsterprofitgoldlevel.setMonster(monsterEntity);
                            monsterprofitgoldlevel.setLevel(level);
                            monsterprofitgoldlevel.setMeals(food);
                            monsterprofitgoldlevel.setProfit(maxprofit);
                            domainDao.create(monsterprofitgoldlevel);
                        }
                    }
                    break;
                case 18: //Заработок золота в минуту (monster) profittime - таблица - monsterprofitgoldtime
                    ArrayList<String> lines1 = Utils.readFileByLine(value);
                    if(lines1.size() > 0)
                    {
                        int level = 0;
                        int zero = 0;
                        int twentyfive = 0;
                        int fifty = 0;
                        int seventyfive = 0;
                        int hundred = 0;
                        for (int i = 1; i < lines1.size(); i++)
                        {
                            String[] splits = lines1.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            zero = findInteger(splits[1]);
                            twentyfive = findInteger(splits[2]);
                            fifty = findInteger(splits[3]);
                            seventyfive = findInteger(splits[4]);
                            hundred = findInteger(splits[5]);
                            //--- Делаю запись в таблице monsterprofitgoldtime
                            MonsterprofitgoldtimeEntity monsterprofitgoldtime = new MonsterprofitgoldtimeEntity();
                            monsterprofitgoldtime.setGuid(UUID.randomUUID().toString());
                            monsterprofitgoldtime.setMonster(monsterEntity);
                            monsterprofitgoldtime.setLevel(level);
                            monsterprofitgoldtime.setZeropercent(zero);
                            monsterprofitgoldtime.setTwentyfivepercent(twentyfive);
                            monsterprofitgoldtime.setFiftypercent(fifty);
                            monsterprofitgoldtime.setSeventyfivepercent(seventyfive);
                            monsterprofitgoldtime.setHandredrpercent(hundred);
                            domainDao.create(monsterprofitgoldtime);
                        }
                    }
                    break;
                case 19: //Заработок осколков в час - таблица - monsterprofitsplintertime
                    ArrayList<String> lines2 = Utils.readFileByLine(value);
                    if(lines2.size() > 0)
                    {
                        int level = 0;
                        int zero = 0;
                        int twentyfive = 0;
                        int fifty = 0;
                        int seventyfive = 0;
                        int hundred = 0;
                        for (int i = 1; i < lines2.size(); i++)
                        {
                            String[] splits = lines2.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            zero = findInteger(splits[1]);
                            twentyfive = findInteger(splits[2]);
                            fifty = findInteger(splits[3]);
                            seventyfive = findInteger(splits[4]);
                            hundred = findInteger(splits[5]);
                            //--- Делаю запись в таблице monsterprofitgoldtime
                            MonsterprofitsplintertimeEntity monsterprofitsplintertime = new MonsterprofitsplintertimeEntity();
                            monsterprofitsplintertime.setGuid(UUID.randomUUID().toString());
                            monsterprofitsplintertime.setMonster(monsterEntity);
                            monsterprofitsplintertime.setLevel(level);
                            monsterprofitsplintertime.setZeropercent(zero);
                            monsterprofitsplintertime.setTwentyfivepercent(twentyfive);
                            monsterprofitsplintertime.setFiftypercent(fifty);
                            monsterprofitsplintertime.setSeventyfivepercent(seventyfive);
                            monsterprofitsplintertime.setHandredrpercent(hundred);
                            domainDao.create(monsterprofitsplintertime);
                        }
                    }
                    break;
                case 20: //Прибыль осколков по уровням - таблица - monsterprofitsplinterlevel
                    ArrayList<String> lines3 = Utils.readFileByLine(value);
                    if(lines3.size() > 0)
                    {
                        int level = 0;
                        int splinter = 0;
                        int meals = 0;
                        for (int i = 1; i < lines3.size(); i++)
                        {
                            String[] splits = lines3.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            splinter = findInteger(splits[1]);
                            if(i < 15)
                            {
                                String[] foods = splits[2].trim().split("=");
                                meals = findInteger(foods[1]);
                            }
                            else
                            {
                                meals = 0;
                            }
                            //--- Делаю запись в таблице monsterprofitgoldlevel
                            MonsterprofitsplinterlevelEntity monsterprofitsplinterlevel = new MonsterprofitsplinterlevelEntity();
                            monsterprofitsplinterlevel.setGuid(UUID.randomUUID().toString());
                            monsterprofitsplinterlevel.setMonster(monsterEntity);
                            monsterprofitsplinterlevel.setLevel(level);
                            monsterprofitsplinterlevel.setMeals(meals);
                            monsterprofitsplinterlevel.setSplinter(splinter);
                            domainDao.create(monsterprofitsplinterlevel);
                        }
                    }
                    break;
            }
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
            logger.debug("");
            logger.debug("Обрабатываю папку: {}", file.getName());
            processEachIsland(file);
        }
    }

    private void processEachIsland(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);

        //--- Текущий остров
        DomainDao domainDao = new DomainDao();
        //guid
        String islandGuid = domainDao.findIslandByName(Utils.makeNameFromDirName(itemDirectory).trim());
        //island
        IslandEntity islandEntity = domainDao.findByGuid(IslandEntity.class, islandGuid);
        logger.info("ТЕКУЩИЙ ОСТРОВ: " + islandEntity);

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case 7: //На острове выводятся!!!!!!!! (islandmonster)
                    //--- Считываю файл
                    String line = Utils.readFileToString(value);
                    if(line != null && !line.isEmpty() && line.length() > 0)
                    {
                        String[] monsters = line.trim().split("\\n");
                        for (int i = 0; i < monsters.length; i++)
                        {
                            String monsterGuid = domainDao.findMonsterByName(monsters[i].trim());
                            MonsterEntity monster = domainDao.findByGuid(MonsterEntity.class, monsterGuid);
                            if(monster != null)
                            {
                                IslandmonsterEntity islandmonster = new IslandmonsterEntity();
                                islandmonster.setId(ISLAND_MONSTER_ID);
                                islandmonster.setIsland(islandEntity);
                                islandmonster.setMonster(monster);
                                islandmonster.setMinlevel((byte)1);
                                domainDao.create(islandmonster);
                                ISLAND_MONSTER_ID++;
                            }
                        }
                    }
                    break;
                case IConstants.ISLANDS_SPECIAL_MONSTERS_10:
                    //?????????????????????????????????? (10-Специальные монстры)
                    break;
                case IConstants.ISLANDS_UPGRADES_TABLE_12:
                    ArrayList<String> lines = Utils.readFileByLine(value);
                    if(lines.size() > 0)
                    {
                        int pricegold = 0;
                        int priceemerald = 0;
                        long time = 0;
                        int prize = 0;
                        int bed = 0;
                        for (int i = 1; i < lines.size(); i++)
                        {
                            String[] line5 = lines.get(i).trim().split("\\t");
                            //--- название дворца
                            LocalizedEntity castleName = new LocalizedEntity();
                            castleName.setGuid(UUID.randomUUID().toString());
                            castleName.setLocale((byte)1);
                            castleName.setText(line5[0].trim());
                            domainDao.create(castleName);
                            //Стоимость (золото/изумруды)
                            if(i < 5){pricegold = findInteger(line5[1].trim()); priceemerald = 0;}
                            else{priceemerald = findInteger(line5[1].trim()); pricegold = 0;}
                            //время
                            time = convertTimeToLong(line5[2].trim());
                            //приз
                            prize = findInteger(line5[3].trim());
                            //кроватей
                            bed = findInteger(line5[4].trim());
                            //--- Делаю запись в таблице
                            IslandimprovementEntity islandimprovement = new IslandimprovementEntity();
                            islandimprovement.setId(ISLAND_IMPROVEMENT_ID);
                            islandimprovement.setIsland(islandGuid);
                            islandimprovement.setName(castleName.getGuid());
                            islandimprovement.setBed((byte)bed);
                            islandimprovement.setPriceemerald(priceemerald);
                            islandimprovement.setPricegold(pricegold);
                            islandimprovement.setTime(time);
                            islandimprovement.setPrize(prize);
                            domainDao.create(islandimprovement);
                            ISLAND_IMPROVEMENT_ID++;
                        }
                    }
                    break;
            }
        }
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
            logger.debug("");
            logger.debug("Обрабатываю папку: {}", file.getName());
            processEachMonster(file);
        }
    }

    private void processEachMonster(File itemDirectory) throws Exception
    {
        final Map<Integer, File> entityFiles = listEntityFiles(itemDirectory);
        //--- Текущий монстр
        DomainDao domainDao = new DomainDao();
        //guid
        String monsterGuid = domainDao.findMonsterByName(Utils.makeNameFromDirName(itemDirectory).trim());
        //monster
        MonsterEntity monsterEntity = domainDao.findByGuid(MonsterEntity.class, monsterGuid);
        logger.info("ТЕКУЩИЙ РЕДКИЙ МОНСТР: " + monsterEntity);
        //--- Время созревания монстра
        long timeCreate = 0;

        final Set<Map.Entry<Integer, File>> entrySet = entityFiles.entrySet();
        for (Map.Entry<Integer, File> fileEntry : entrySet)
        {
            final Integer key = fileEntry.getKey();
            final File value = fileEntry.getValue();
            final Config config = new Config(value);
            switch (key)
            {
                case 7: //Время созревания (monstercreatetime) - time ===> ОШИБКА!
                    //--- Считываю время из файла
                    timeCreate = convertTimeInLong(config);
                    logger.info("ВРЕМЯ СОЗРЕВАНИЯ: " + timeCreate);
                    break;
                case 8: //Обитает на островах (monsterlive) + обновление (monstercreatetime) - добавляем остров ===> ОШИБКА!
                    //Считываю острова
                    //====================== ИЗМЕНИТЬ ЛОГИКУ !!! ===========================================================
                    String line = Utils.readFileToString(value);
                    if(line != null && !line.isEmpty() && line.length() > 0)
                    {
                        line = line.trim().replace(".", "");
                        String[] islands = line.trim().split(",");
                        for (int i = 0; i < islands.length; i++)
                        {
                            //--- Нахожу guid острова
                            String islandGuid = null;
                            if(islands[i].contains("остров") || islands[i].contains("Остров"))
                            {
                                islandGuid = domainDao.findIslandByName(islands[i].trim());
                            }
                            else
                            {
                                islandGuid = domainDao.findIslandByName(islands[i].trim() + " остров");
                            }
                            IslandEntity islandEntity = domainDao.findByGuid(IslandEntity.class, islandGuid);
                            //--- Создаю запись в таблице monstercreatetime
                            MonstercreatetimeEntity monstercreatetimeEntity = new MonstercreatetimeEntity();
                            monstercreatetimeEntity.setId(RARE_MONSTER_MONSTER_CREATE_TIME_ID);
                            monstercreatetimeEntity.setIsland(islandEntity);
                            monstercreatetimeEntity.setMonster(monsterEntity);
                            monstercreatetimeEntity.setTime(timeCreate);
                            domainDao.create(monstercreatetimeEntity);
                            //--- Создаю запись в таблице monsterlive
                            MonsterliveEntity monsterliveEntity = new MonsterliveEntity();
                            monsterliveEntity.setId(RARE_MONSTER_MONSTER_CREATE_TIME_ID);
                            monsterliveEntity.setIsland(islandEntity);
                            monsterliveEntity.setMonster(monsterEntity);
                            domainDao.create(monsterliveEntity);
                            RARE_MONSTER_MONSTER_CREATE_TIME_ID++;
                        }
                    }
                    break;
                case 9: //бонус за перемещение на Золотой Остров (monstermove)===> РАБОТАЕТ!
                    //--- Нахожу guid Золотого острова
                    String goldGuid = domainDao.findIslandByName("Золотой остров");
                    //monster
                    MonsterEntity monster = domainDao.findByGuid(MonsterEntity.class, monsterGuid);
                    //--- Описание перемещения
                    LocalizedEntity move = monster.getMove();
                    //--- Делаю запись в таблице monstermove
                    MonstermoveEntity monstermoveEntity = new MonstermoveEntity();
                    monstermoveEntity.setId(RARE_MONSTER_MONSTER_MOVE_TIME_ID);
                    monstermoveEntity.setIsland(goldGuid);
                    monstermoveEntity.setMonster(monster.getGuid());
                    monstermoveEntity.setDescription(move.getGuid());
                    monstermoveEntity.setBonus(findInteger(config));
                    domainDao.create(monstermoveEntity);
                    RARE_MONSTER_MONSTER_MOVE_TIME_ID++;
                    break;
                case 10: //Выведение (monstercreate) - РАБОТАЕТ!
                    //--- Считываю файл
                    logger.info("ТЕКУЩИЙ МОНСТР = " + monsterEntity);
                    String line2 = Utils.readFileToString(value);
                    if(line2 != null && !line2.isEmpty() && line2.length() > 0)
                    {
                        line2 = line2.replace("\n", " ");
                        String[] arrMonsters = line2.trim().split(" ");
                        for (int i = 0; i < arrMonsters.length; i++)
                        {
                            //--- Розбиваю пару
                            String[] monsters = arrMonsters[i].split("\\+");
                            //--- Нахожу sourcemonster
                            //guid
                            String sourceGuid = domainDao.findMonsterByName(monsters[0].trim());
                            logger.info("SOURCEMONSTER = " + sourceGuid);
                            //monster
                            MonsterEntity sourceMonster = domainDao.findByGuid(MonsterEntity.class, sourceGuid);
                            //--- Нахожу targetmonster
                            //guid
                            String targetGuid = domainDao.findMonsterByName(monsters[1].trim());
                            logger.info("TARGETMONSTER = " + targetGuid);
                            //monster
                            MonsterEntity targetMonster = domainDao.findByGuid(MonsterEntity.class, targetGuid);
                            //--- Делаю запись в таблице monstercreate
                            MonstercreateEntity monstercreateEntity = new MonstercreateEntity();
                            monstercreateEntity.setId(RARE_MONSTER_MONSTER_CREATE_ID);
                            monstercreateEntity.setMonster(monsterEntity);
                            monstercreateEntity.setSourcemonster(sourceMonster);
                            monstercreateEntity.setTargetmonster(targetMonster);
                            domainDao.create(monstercreateEntity);
                            RARE_MONSTER_MONSTER_CREATE_ID++;
                        }
                    }
                    break;
                case 11: //Элементы (monsterelement) - РАБОТАЕТ!
                    createElementsIfNotExists(config, monsterEntity);
                    break;
                case 14: //Влечения таблицы monsterappetencemonster, monsterappetencedecorations - РАБОТАЕТ!
                    //--- считываю файл и формирую массив обьектов
                    String line1 = Utils.readFileToString(value);
                    if(line1 != null && !line1.isEmpty() && line1.length() > 0)
                    {
                        String[] arrObject = line1.trim().split("\\n");
                        //--- Перебираю объекты и определяю монстер это или украшение
                        //и заполняю соответствующие таблицы
                        for (int i = 0; i < arrObject.length; i++)
                        {
                            //Нахожу guid по имени объекта
                            String monsterGuide = domainDao.findMonsterByName(arrObject[i].trim());

                            //--- Ищу в монстрах
                            MonsterEntity monster1 = domainDao.findByGuid(MonsterEntity.class, monsterGuide);
                            if(monster1 != null)
                            {
                                //Делаю запись в таблице monsterappetencemonster
                                MonsterappetencemonsterEntity appetenceMonster = new MonsterappetencemonsterEntity();
                                appetenceMonster.setId(RARE_MONSTER_APPETENCE_ID);
                                appetenceMonster.setMonster(monsterEntity);
                                appetenceMonster.setTarget(monster1);
                                domainDao.create(appetenceMonster);
                            }
                            else //Если не монстер, ищу в украшениях
                            {
                                String decorationGuid = domainDao.findDecorationByName(arrObject[i].trim());
                                //Ищу в укоашения
                                DecorationsEntity decorations = domainDao.findByGuid(DecorationsEntity.class, decorationGuid);
                                //Делаю запись в таблице monsterappetencedecorations
                                MonsterappetencedecorationsEntity appetenceDecoration = new MonsterappetencedecorationsEntity();
                                appetenceDecoration.setId(RARE_MONSTER_APPETENCE_ID);
                                appetenceDecoration.setMonster(monsterEntity);
                                appetenceDecoration.setDecoration(decorations);
                                domainDao.create(appetenceDecoration);
                            }
                            RARE_MONSTER_APPETENCE_ID++;
                        }
                    }
                    break;
                case 15: //Редкий (monster) - rare
                    break;
                case 17: //Прибыль золота по уровням (monster) profitlevel - таблица - monsterprofitgoldlevel ===> РАБОТАЕТ!
                    ArrayList<String> lines = Utils.readFileByLine(value);
                    if(lines.size() > 0)
                    {
                        int level = 0;
                        int maxprofit = 0;
                        int food = 0;
                        for (int i = 1; i < lines.size(); i++)
                        {
                            String[] splits = lines.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            maxprofit = findInteger(splits[1]);
                            if(i < 15)
                            {
                                String[] foods = splits[2].trim().split("=");
                                food = findInteger(foods[1]);
                            }
                            else
                            {
                                food = 0;
                            }
                            //--- Делаю запись в таблице monsterprofitgoldlevel
                            MonsterprofitgoldlevelEntity monsterprofitgoldlevel = new MonsterprofitgoldlevelEntity();
                            monsterprofitgoldlevel.setGuid(UUID.randomUUID().toString());
                            monsterprofitgoldlevel.setMonster(monsterEntity);
                            monsterprofitgoldlevel.setLevel(level);
                            monsterprofitgoldlevel.setMeals(food);
                            monsterprofitgoldlevel.setProfit(maxprofit);
                            domainDao.create(monsterprofitgoldlevel);
                        }
                    }
                    break;
                case 18: //Заработок золота в минуту (monster) profittime - таблица - monsterprofitgoldtime ===> РАБОТАЕТ!
                    ArrayList<String> lines1 = Utils.readFileByLine(value);
                    if(lines1.size() > 0)
                    {
                        int level = 0;
                        int zero = 0;
                        int twentyfive = 0;
                        int fifty = 0;
                        int seventyfive = 0;
                        int hundred = 0;
                        for (int i = 1; i < lines1.size(); i++)
                        {
                            String[] splits = lines1.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            zero = findInteger(splits[1]);
                            twentyfive = findInteger(splits[2]);
                            fifty = findInteger(splits[3]);
                            seventyfive = findInteger(splits[4]);
                            hundred = findInteger(splits[5]);
                            //--- Делаю запись в таблице monsterprofitgoldtime
                            MonsterprofitgoldtimeEntity monsterprofitgoldtime = new MonsterprofitgoldtimeEntity();
                            monsterprofitgoldtime.setGuid(UUID.randomUUID().toString());
                            monsterprofitgoldtime.setMonster(monsterEntity);
                            monsterprofitgoldtime.setLevel(level);
                            monsterprofitgoldtime.setZeropercent(zero);
                            monsterprofitgoldtime.setTwentyfivepercent(twentyfive);
                            monsterprofitgoldtime.setFiftypercent(fifty);
                            monsterprofitgoldtime.setSeventyfivepercent(seventyfive);
                            monsterprofitgoldtime.setHandredrpercent(hundred);
                            domainDao.create(monsterprofitgoldtime);
                        }
                    }
                    break;
                case 19: //Заработок осколков в час - таблица - monsterprofitsplintertime ===> РАБОТАЕТ!
                    ArrayList<String> lines2 = Utils.readFileByLine(value);
                    if(lines2.size() > 0)
                    {
                        int level = 0;
                        int zero = 0;
                        int twentyfive = 0;
                        int fifty = 0;
                        int seventyfive = 0;
                        int hundred = 0;
                        for (int i = 1; i < lines2.size(); i++)
                        {
                            String[] splits = lines2.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            zero = findInteger(splits[1]);
                            twentyfive = findInteger(splits[2]);
                            fifty = findInteger(splits[3]);
                            seventyfive = findInteger(splits[4]);
                            hundred = findInteger(splits[5]);
                            //--- Делаю запись в таблице monsterprofitgoldtime
                            MonsterprofitsplintertimeEntity monsterprofitsplintertime = new MonsterprofitsplintertimeEntity();
                            monsterprofitsplintertime.setGuid(UUID.randomUUID().toString());
                            monsterprofitsplintertime.setMonster(monsterEntity);
                            monsterprofitsplintertime.setLevel(level);
                            monsterprofitsplintertime.setZeropercent(zero);
                            monsterprofitsplintertime.setTwentyfivepercent(twentyfive);
                            monsterprofitsplintertime.setFiftypercent(fifty);
                            monsterprofitsplintertime.setSeventyfivepercent(seventyfive);
                            monsterprofitsplintertime.setHandredrpercent(hundred);
                            domainDao.create(monsterprofitsplintertime);
                        }
                    }
                    break;
                case 20: //Прибыль осколков по уровням - таблица - monsterprofitsplinterlevel
                    ArrayList<String> lines3 = Utils.readFileByLine(value);
                    if(lines3.size() > 0)
                    {
                        int level = 0;
                        int splinter = 0;
                        int meals = 0;
                        for (int i = 1; i < lines3.size(); i++)
                        {
                            String[] splits = lines3.get(i).trim().split("\t");
                            level = findInteger(splits[0]);
                            splinter = findInteger(splits[1]);
                            if(i < 15)
                            {
                                String[] foods = splits[2].trim().split("=");
                                meals = findInteger(foods[1]);
                            }
                            else
                            {
                                meals = 0;
                            }
                            //--- Делаю запись в таблице monsterprofitgoldlevel
                            MonsterprofitsplinterlevelEntity monsterprofitsplinterlevel = new MonsterprofitsplinterlevelEntity();
                            monsterprofitsplinterlevel.setGuid(UUID.randomUUID().toString());
                            monsterprofitsplinterlevel.setMonster(monsterEntity);
                            monsterprofitsplinterlevel.setLevel(level);
                            monsterprofitsplinterlevel.setMeals(meals);
                            monsterprofitsplinterlevel.setSplinter(splinter);
                            domainDao.create(monsterprofitsplinterlevel);
                        }
                    }
                    break;
            }
        }
    }

    public void processLandscape(File currentFile) throws Exception
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
            logger.debug("");
            logger.debug("Обрабатываю папку: {}", file.getName());
            processEachLandscape(file);
        }
    }

    public void processEachLandscape(File landscapeDirectory) throws Exception
    {
        //--- Нахожу текущий остров в БД
        String islandGuid = domainDao.findIslandByName(Utils.makeNameFromDirName(landscapeDirectory));
        logger.info("ТЕКУЩИЙ ОСТРОВ: " + "[Название = " + Utils.makeNameFromDirName(landscapeDirectory) + " guid = " + islandGuid + "]");
        File[] fileList = landscapeDirectory.listFiles();
        for (int i = 0; i < fileList.length; i++)
        {
            if(fileList[i].isDirectory()) continue;
            Config config = new Config(fileList[i]);
            //--- Считываю содержимое файла
            final String iconsUrls = Utils.readFileToString(fileList[i]);
            if (iconsUrls == null || iconsUrls.isEmpty())
            {
                logger.warn("Остров: " + Utils.makeNameFromDirName(landscapeDirectory) + " Нет иконок в файле {}", fileList[i].getName());
            }
            else //Если файл не пустой
            {
                String[] icons = iconsUrls.trim().split("\\n");
                //--- Перебираю иконки
                for (int j = 0; j < icons.length; j++)
                {
                    //--- Формирую guid иконки из ее имени
                    final String iconGuid = urlToGuid(icons[j].trim());
                    logger.info("iconGuid = " + iconGuid);
                    //--- Создаю файл иконки
                    final File iconFile = config.getIconFile(iconGuid);
                    logger.info("iconFile = " + iconFile);
                    //--- Создаю запись в таблице иконок
                    IconEntity iconEntity;
                    if (iconStored(domainDao, iconGuid, iconFile))
                    {
                        iconEntity = updateIcon(domainDao, iconGuid, iconFile);
                    }
                    else
                    {
                        storeIcon(iconFile, icons[j]);
                        iconEntity = createIcon(domainDao, iconGuid, iconFile);
                    }
                    //--- Делаю запись в таблице islanddecorations
                    IslanddecorationsEntity islanddecorations = new IslanddecorationsEntity();
                    islanddecorations.setId(LANDSCAPE_ID);
                    islanddecorations.setIcon(iconEntity.getGuid());
                    islanddecorations.setIsland(islandGuid);
                    domainDao.create(islanddecorations);
                    LANDSCAPE_ID++;
                }
            }
        }
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
}
