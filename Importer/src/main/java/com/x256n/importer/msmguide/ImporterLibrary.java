package com.x256n.importer.msmguide;

import com.x256n.core.msmguide.domain.DecorationsEntity;
import com.x256n.core.msmguide.domain.IconEntity;
import com.x256n.core.msmguide.domain.LocalizedEntity;
import com.x256n.core.msmguide.enums.Locale;
import com.x256n.importer.msmguide.common.Config;
import com.x256n.importer.msmguide.common.Utils;
import com.x256n.importer.msmguide.db.DomainDao;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Aleksey Permyakov (03.12.2015).
 */
public abstract class ImporterLibrary {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ImporterLibrary.class);

    protected Map<Integer, File> listEntityFiles(File buildingDirectory) throws Exception {
        final File[] listFiles = buildingDirectory.listFiles();
        if (listFiles == null) {
            throw new Exception("Папка пустая! " + buildingDirectory.getAbsolutePath());
        }
        Map<Integer, File> result = new HashMap<>();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                continue;
            }
            final String fileName = file.getName();
            final int das = Utils.getDigitsAtStart(fileName);
            if (das == -1) {
                continue;
            }
            result.put(das, file);
        }
        return result;
    }

    protected static String urlToBaseName(String iconUrl) {
        final int start = iconUrl.lastIndexOf("/") + 1, end = iconUrl.contains("?") ? iconUrl.indexOf("?") : iconUrl.length();
        final int preEnd = iconUrl.substring(0, start).lastIndexOf("/"), preStart = iconUrl.substring(0, preEnd).lastIndexOf("/") + 1;
        return iconUrl.substring(preStart, preEnd) + "_" + iconUrl.substring(start, end);
    }


    protected String urlToGuid(String iconUrl) {
        final String baseName = urlToBaseName(iconUrl);
        return baseName.substring(0, baseName.lastIndexOf(".")).replace("'", "");
    }
    public static String urlToGuidMy(String iconUrl) {
        final String baseName = urlToBaseName(iconUrl);
        return baseName.substring(0, baseName.lastIndexOf(".")).replace("'", "");
    }


    protected LocalizedEntity createLocalization(DomainDao domainDao, Config config) throws Exception {
        final File guidFile = config.getItemGuidFile();
        final String localizedGuid = UUID.randomUUID().toString();
        FileUtils.writeByteArrayToFile(guidFile, localizedGuid.getBytes("cp1251"));
        final LocalizedEntity localizedEntity = new LocalizedEntity();
        localizedEntity.setGuid(localizedGuid);
        localizedEntity.setLocale(Locale.RU.getValue());
        localizedEntity.setText(Utils.readFileToString(config.getItemFile()));
        domainDao.create(localizedEntity);
        return localizedEntity;
    }

    protected LocalizedEntity updateLocalization(DomainDao domainDao, Config config) throws Exception {
        final File guidFile = config.getItemGuidFile();
        final String localizationGuid = Utils.readFileToString(guidFile);
        final LocalizedEntity localizedEntity = domainDao.findByGuid(LocalizedEntity.class, localizationGuid);
        localizedEntity.setLocale(Locale.RU.getValue());
        localizedEntity.setText(Utils.readFileToString(config.getItemFile()));
        domainDao.update(localizedEntity);
        return localizedEntity;
    }

    protected boolean localizationStored(DomainDao domainDao, Config config) throws Exception {
        final File guidFile = config.getItemGuidFile();
        if (guidFile.exists() && guidFile.length() > 0) {
            final String localizationGuid = Utils.readFileToString(guidFile);
            return domainDao.findByGuid(LocalizedEntity.class, localizationGuid) != null;
        }
        return false;
    }


    protected IconEntity createIcon(DomainDao domainDao, String iconGuid, File iconFile) throws Exception {
        IconEntity iconEntity = new IconEntity();
        iconEntity.setGuid(iconGuid);
        iconEntity.setData(FileUtils.readFileToByteArray(iconFile));
        domainDao.create(iconEntity);
        return iconEntity;
    }

    protected IconEntity updateIcon(DomainDao domainDao, String iconGuid, File iconFile) throws Exception {
        final IconEntity iconEntity = domainDao.findByGuid(IconEntity.class, iconGuid);
        iconEntity.setData(FileUtils.readFileToByteArray(iconFile));
        domainDao.update(iconEntity);
        return iconEntity;
    }

    protected boolean iconStored(DomainDao domainDao, String iconGuid, File iconFile) throws Exception {
        return iconFile.exists() && iconFile.length() > 0 && domainDao.findByGuid(IconEntity.class, iconGuid) != null;
    }


    protected void storeIcon(File iconFile, String iconUrl) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(iconFile)) {
            IOUtils.copy(new BufferedInputStream(new URL(iconUrl).openStream()), outputStream);
        }
    }

    protected void assertEntity(Object iconEntity, String message) throws Exception {
        if (iconEntity == null) {
            throw new Exception(message);
        }
    }

    protected IconEntity createOrLoadIcon(Config config) throws Exception {
        DomainDao domainDao = new DomainDao();

        final String iconUrl = Utils.readFileToString(config.getItemFile());
        if (iconUrl == null || iconUrl.isEmpty()) {
            logger.warn("Нет иконки {}", config.getItemFile().getName());
            return null;
        }
//        final String iconName = urlToBaseName(iconUrl);
        final String iconGuid = urlToGuid(iconUrl);

        final File iconFile = config.getIconFile(iconGuid);

        final IconEntity iconEntity;
        if (iconStored(domainDao, iconGuid, iconFile)) {
            iconEntity = updateIcon(domainDao, iconGuid, iconFile);
        } else {
            storeIcon(iconFile, iconUrl);
            iconEntity = createIcon(domainDao, iconGuid, iconFile);
        }
        return iconEntity;
    }

    protected LocalizedEntity createOrLoadLocalization(Config config) throws Exception {
        DomainDao domainDao = new DomainDao();

        final LocalizedEntity localizedEntity;
        if (localizationStored(domainDao, config)) {
            localizedEntity = updateLocalization(domainDao, config);
        } else {
            localizedEntity = createLocalization(domainDao, config);
        }
        return localizedEntity;
    }

    protected DecorationsEntity createDecoration(DomainDao domainDao, Config config, DecorationsEntity decorationsEntity) throws Exception {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = UUID.randomUUID().toString();
        FileUtils.writeByteArrayToFile(guidFile, guid.getBytes("cp1251"));
        decorationsEntity.setGuid(guid);
        domainDao.create(decorationsEntity);
        return decorationsEntity;
    }

    protected DecorationsEntity updateDecoration(DomainDao domainDao, Config config, DecorationsEntity decorationsEntity) throws Exception {
        final File guidFile = config.getDirectoryGuidFile();
        final String guid = Utils.readFileToString(guidFile);
        final DecorationsEntity entity = domainDao.findByGuid(DecorationsEntity.class, guid);

        entity.setIcon(decorationsEntity.getIcon());
        entity.setName(decorationsEntity.getName());
        entity.setLevel(decorationsEntity.getLevel());
        entity.setPricegold(decorationsEntity.getPricegold());
        entity.setPriceemerald(decorationsEntity.getPriceemerald());
        entity.setDescription(decorationsEntity.getDescription());
        entity.setDescriptiongame(decorationsEntity.getDescriptiongame());

        domainDao.update(entity);
        return entity;
    }

    protected boolean decorationStored(DomainDao domainDao, Config config) throws Exception {
        final File guidFile = config.getDirectoryGuidFile();
        if (guidFile.exists() && guidFile.length() > 0) {
            final String guid = Utils.readFileToString(guidFile);
            return domainDao.findByGuid(DecorationsEntity.class, guid) != null;
        }
        return false;
    }

    protected int getInteger(Config config) throws IOException {
        final File guidFile = config.getItemFile();
        return Integer.parseInt(Utils.readFileToString(guidFile), 10);
    }

    /**
     * Метод считывает строку из файла, где указано время
     * в формате: "число - "пробел" - <признак времени>"
     * признак времени:
     * [час, часов, часа]
     * [минут, минуты, минута]
     * [секунда, секунды, секунд]
     * формирует время и переводит его в (long) миллисекунды
     */
    public long convertTimeInLong(Config config) throws IOException, IllegalArgumentException
    {
        //время в long
        long timeInLong = 0;
        //часы
        String hour1 = "час";
        String hour2 = "часов";
        String hour3 = "часа";
        String currentHour = "";
        int currentHourInt = 0;
        //минуты
        String minute1 = "минут";
        String minute2 = "минуты";
        String minute3 = "минута";
        String currentMinute = "";
        int currentMinuteInt = 0;
        //секунды
        String second1 = "секунд";
        String second2 = "секунды";
        String second3 = "секунда";
        String currentSecond = "";
        int currentSecondInt = 0;

        String[] result = new String[3];

        //считываю содержымое файла в строку
        final File guidFile = config.getItemFile();
        String fileContent = Utils.readFileToString(guidFile);
        System.out.println("fileContent: " + fileContent);

        //проверяю что файл не пустой
        if(fileContent != null)
        {
            if(fileContent.contains(hour3))
            {
                currentHour = fileContent.substring(0, fileContent.indexOf(hour3) + hour3.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }
            else
            if(fileContent.contains(hour2))
            {
                currentHour = fileContent.substring(0, fileContent.indexOf(hour2) + hour2.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }
            else
            if(fileContent.contains(hour1))
            {
                currentHour = fileContent.substring(0, fileContent.indexOf(hour1) + hour1.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }

            if(fileContent.contains(minute3))
            {
                currentMinute = fileContent.substring(currentHour.length(), fileContent.indexOf(minute3) + minute3.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }
            else
            if(fileContent.contains(minute2))
            {
                currentMinute = fileContent.substring(currentHour.length(), fileContent.indexOf(minute2) + minute2.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }
            else
            if(fileContent.contains(minute1))
            {
                currentMinute = fileContent.substring(currentHour.length(), fileContent.indexOf(minute1) + minute1.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }

            if(fileContent.contains(second3))
            {
                currentSecond = fileContent.substring(currentHour.length() + currentMinute.length(), fileContent.indexOf(second3) + second3.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            if(fileContent.contains(second2))
            {
                currentSecond = fileContent.substring(currentHour.length() + currentMinute.length(), fileContent.indexOf(second2) + second2.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            if(fileContent.contains(second1))
            {
                currentSecond = fileContent.substring(currentHour.length() + currentMinute.length(), fileContent.indexOf(second1) + second1.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            //Проверяюсь корректность введенных данных
            String TMP = currentHour + currentMinute + currentSecond;
            int currentTimeSumm = findInteger(TMP);
            System.out.println("currentTimeSumm = " + currentTimeSumm);
            int contentTimeSumm = findInteger(fileContent);
            System.out.println("contentTimeSumm = " + contentTimeSumm);

            if(contentTimeSumm > currentTimeSumm)
            {
                throw new IllegalArgumentException("Неверный формат времени!");
            }
            else
            {
                System.out.println("Формат времени введен верно!");
                long hourInMillis = (((currentHourInt * 60) * 60) * 1000);
                long minuteInMillis = ((currentMinuteInt * 60) * 1000);
                long secondInMillis = (currentSecondInt * 1000);
                timeInLong = hourInMillis + minuteInMillis + secondInMillis;
                System.out.println("timeInLong = " + timeInLong);
            }
        }
        else throw new NullPointerException("Файл: \"Время созревания\" пустой!");

        return timeInLong;
    }

    /**
     * Метод считывает строку из файла, где указано время
     * в формате: "число - "пробел" - <признак времени>"
     * признак времени:
     * [час, часов, часа]
     * [минут, минуты, минута]
     * [секунда, секунды, секунд]
     * формирует время и переводит его в (long) миллисекунды
     */
    public long convertTimeToLong(String time) throws IOException, IllegalArgumentException
    {
        //время в long
        long timeInLong = 0;
        //часы
        String hour1 = "час";
        String hour2 = "часов";
        String hour3 = "часа";
        String currentHour = "";
        int currentHourInt = 0;
        //минуты
        String minute1 = "минут";
        String minute2 = "минуты";
        String minute3 = "минута";
        String currentMinute = "";
        int currentMinuteInt = 0;
        //секунды
        String second1 = "секунд";
        String second2 = "секунды";
        String second3 = "секунда";
        String currentSecond = "";
        int currentSecondInt = 0;

        String[] result = new String[3];

        //проверяю что файл не пустой
        if(time != null)
        {
            if(time.contains(hour3))
            {
                currentHour = time.substring(0, time.indexOf(hour3) + hour3.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }
            else
            if(time.contains(hour2))
            {
                currentHour = time.substring(0, time.indexOf(hour2) + hour2.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }
            else
            if(time.contains(hour1))
            {
                currentHour = time.substring(0, time.indexOf(hour1) + hour1.length());
                System.out.println("currentHour = " + currentHour);
                currentHourInt = this.findInteger(currentHour);
                System.out.println("currentHourInt = " + currentHourInt);
            }

            if(time.contains(minute3))
            {
                currentMinute = time.substring(currentHour.length(), time.indexOf(minute3) + minute3.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }
            else
            if(time.contains(minute2))
            {
                currentMinute = time.substring(currentHour.length(), time.indexOf(minute2) + minute2.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }
            else
            if(time.contains(minute1))
            {
                currentMinute = time.substring(currentHour.length(), time.indexOf(minute1) + minute1.length());
                System.out.println("currentMinute = " + currentMinute);
                currentMinuteInt = this.findInteger(currentMinute);
                System.out.println("currentMinuteInt = " + currentMinuteInt);
            }

            if(time.contains(second3))
            {
                currentSecond = time.substring(currentHour.length() + currentMinute.length(), time.indexOf(second3) + second3.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            if(time.contains(second2))
            {
                currentSecond = time.substring(currentHour.length() + currentMinute.length(), time.indexOf(second2) + second2.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            if(time.contains(second1))
            {
                currentSecond = time.substring(currentHour.length() + currentMinute.length(), time.indexOf(second1) + second1.length());
                System.out.println("currentSecond = " + currentSecond);
                currentSecondInt = this.findInteger(currentSecond);
                System.out.println("currentSecondInt = " + currentSecondInt);
            }
            //Проверяюсь корректность введенных данных
            String TMP = currentHour + currentMinute + currentSecond;
            int currentTimeSumm = findInteger(TMP);
            System.out.println("currentTimeSumm = " + currentTimeSumm);
            int contentTimeSumm = findInteger(time);
            System.out.println("contentTimeSumm = " + contentTimeSumm);

            if(contentTimeSumm > currentTimeSumm)
            {
                throw new IllegalArgumentException("Неверный формат времени!");
            }
            else
            {
                System.out.println("Формат времени введен верно!");
                long hourInMillis = (((currentHourInt * 60) * 60) * 1000);
                long minuteInMillis = ((currentMinuteInt * 60) * 1000);
                long secondInMillis = (currentSecondInt * 1000);
                timeInLong = hourInMillis + minuteInMillis + secondInMillis;
                System.out.println("timeInLong = " + timeInLong);
            }
        }
        else throw new NullPointerException("Файл: \"Время созревания\" пустой!");

        return timeInLong;
    }


    protected int findInteger(Config config) throws IOException {
        final File guidFile = config.getItemFile();
        final String fileContent = Utils.readFileToString(guidFile);
        return findInteger(fileContent);
    }

    protected int findInteger(String fileContent) {
        String result = "";
        for (char ch : fileContent.toCharArray()) {
            final String valueOf = String.valueOf(ch);
            if (StringUtils.isNumeric(valueOf)){
                result += valueOf;
            }
        }
        return Integer.parseInt(result.isEmpty() ? "0" : result, 10);
    }

    protected DecorationsEntity createOrLoadDecoration(Config config, DecorationsEntity decorationsEntity) throws Exception {
        DomainDao domainDao = new DomainDao();

        final DecorationsEntity entity;
        if (decorationStored(domainDao, config)) {
            entity = updateDecoration(domainDao, config, decorationsEntity);
        } else {
            entity = createDecoration(domainDao, config, decorationsEntity);
        }
        return entity;
    }

    protected String getString(Config config) throws IOException {
        final File guidFile = config.getItemFile();
        return Utils.readFileToString(guidFile);
    }
}
