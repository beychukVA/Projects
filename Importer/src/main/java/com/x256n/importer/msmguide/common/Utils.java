package com.x256n.importer.msmguide.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author liosha (12.09.2015).
 */
public class Utils {
    public static void assertNotNullOrEmpty(String value, String name) throws Exception {
        if (value == null || value.isEmpty()) {
            throw new Exception("Значение '" + name + "' не должно быть пустым!");
        }
    }

    public static void assertNotNullOrEmptyMatch(String value, String regex, String name) throws Exception {
        if (value == null || value.isEmpty()) {
            throw new Exception("Значение '" + name + "' не должно быть пустым!");
        }
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(value);
        if (!matcher.matches()) {
            throw new Exception(value + " - ('" + name + "'): неверное значение!");
        }
    }

    public static String asString(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        try (final InputStream inputStream = zipFile.getInputStream(zipEntry)) {
            return IOUtils.toString(inputStream).replaceAll("\n\\s*\n", "\r\n").replaceAll("\r\n\\s*\r\n", "\r\n");
        }
    }

    public static int getDigitsAtStart(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char ch : string.toCharArray()) {
            if (CharUtils.isAsciiNumeric(ch)) {
                stringBuilder.append(ch);
            }
        }
        final String result = stringBuilder.toString();
        return result.isEmpty() || string.endsWith(".guid") ? -1 : Integer.parseInt(result);
    }

    public static boolean isAsciiAlphaRU(char ch) {
        return (ch >= 'А' && ch <= 'Я') || (ch >= 'а' && ch <= 'я') || (ch == ' ');
    }
    public static boolean isAsciiAlphaEN(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch == ' ');
    }

    public static String makeNameFromDirName(File itemDirectory)
    {
        int cnt = 0;
        String str = null;
        StringBuilder builder = new StringBuilder();
        for (char ch:itemDirectory.getName().toCharArray())
        {
            if((str = new String(new char[]{ch})).equals("-"))
            {
                cnt++;
                if(cnt == 2)
                {
                    builder.append(ch);
                }
            }
            if(isAsciiAlphaRU(ch) || isAsciiAlphaEN(ch))
            {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
    public static String makeNameFromString(String itemDirectory)
    {
        StringBuilder builder = new StringBuilder();
        for (char ch:itemDirectory.toCharArray())
        {
            if(isAsciiAlphaRU(ch))
            {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public static String readFileToString(File file) throws IOException {
        FileInputStream in = null;

        String var3;
        try {
            in = FileUtils.openInputStream(file);
            var3 = toString(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        if (var3.length() > 0 && var3.charAt(0) == '\uFEFF') {
            var3 = var3.substring(1);
        }
        return var3;
    }

    public static String toString(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        copy((InputStream) input, (Writer) sw);
        return sw.toString();
    }

    public static int copy(InputStream input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static long copyLarge(InputStream input, Writer output) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        byte[] buffer = new byte[4096];
        long count = 0L;

        int n1;
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            detector.handleData(buffer, 0, n1);
            arrayOutputStream.write(buffer, 0, n1);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding == null || "MACCYRILLIC".equalsIgnoreCase(encoding) || "KOI8-R".equalsIgnoreCase(encoding)) {
            output.write(arrayOutputStream.toString("windows-1251"));
        } else {
            output.write(arrayOutputStream.toString(encoding));
        }
        arrayOutputStream.close();
        return count;
    }

    /**
     * Метод для перебора текстовых файлов в папках и изменение кодировки
     * на указанную (ТРЕБУЕТ ДОРАБОТКИ)
     */
    public static void decodeFile(String catalog, String needEncoding) throws Exception
    {
        String currentCatalog = null;
        if(catalog == null) throw new NullPointerException("Укажите путь к каталогу!");
        if(needEncoding == null) throw new NullPointerException("Укажите желаемую кодировку!");
        File fileCatalog = new File(catalog);
        if(fileCatalog.exists() && fileCatalog.isDirectory())
        {
            System.out.println("Начало декодирования!");
            System.out.println("Каталог: " + fileCatalog.getName());
            File[] allFilesInCatalog = fileCatalog.listFiles();
            for (int i = 0; i < allFilesInCatalog.length; i++)
            {
                if(allFilesInCatalog[i].isFile() && allFilesInCatalog[i].getName().endsWith("txt"))
                {
                    System.out.println("Декодируем файл: " + allFilesInCatalog[i].getName());
                    FileInputStream FIS = new FileInputStream(allFilesInCatalog[i]);
                    byte[] buffer = new byte[FIS.available()];
                    while (true)
                    {
                        int cnt = FIS.read(buffer, 0, buffer.length);
                        if(cnt == -1 || cnt == 0) break;
                    }
                    String newData = new String(buffer, needEncoding);
                    System.out.println("newData = " + newData);
                    FileOutputStream FOS = new FileOutputStream(allFilesInCatalog[i]);
                    FOS.write(newData.getBytes());
                    FOS.close();
                    FIS.close();
                    System.out.println("Успешно!");
                }
            }
            System.out.println("Декодирование завершено!");

        } else throw new IllegalArgumentException("Указанный каталог не существует или не является каталогом!");
    }

    /**
     * Метод читает построчно файл и возвращает коллекцию данных
     */
    public static ArrayList<String> readFileByLine(File file)
    {
        ArrayList<String> data = new ArrayList<>();
        FileInputStream FIS = null;
        InputStreamReader ISR = null;
        LineNumberReader LNR = null;

        try
        {
            FIS = new FileInputStream(file);
            ISR = new InputStreamReader(FIS);
            LNR = new LineNumberReader(ISR);

            String line;
            while((line = LNR.readLine()) != null)
            {
                data.add(line);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                FIS.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return data;
    }
}
