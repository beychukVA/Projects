package com.x256n.importer.msmguide.common;

import com.x256n.core.msmguide.enums.Clazz;
import com.x256n.core.msmguide.enums.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aleksey Permyakov (16.01.2016).
 */
public class Parsers {
    private static final Map<Clazz, String> CLAZZ_REGEXS = new HashMap<Clazz, String>(){{
        put(Clazz.ETERIALS, ".+ериаль.+");
        put(Clazz.LEGENDAR, ".+егенда.+");
        put(Clazz.NATURE, ".+рирод.+");
        put(Clazz.SEASONS, ".+езон.+");
        put(Clazz.SUPERNATURAL, ".+х.?ествест.+");
    }};
    private static final Map<Element, String> ELEMENT_REGEXS = new HashMap<Element, String>(){{
        put(Element.HOLOD, ".+olo[^/]+");
        put(Element.IYAD, ".+iya[^/]+");
        put(Element.VODA, ".+vod[^/]+");
        put(Element.VOZDUH, ".+ozd[^/]+");
        put(Element.ZEMLYA, ".+eml[^/]+");
        put(Element.RAST, ".+ast[^/]+");
        put(Element.KOROBAS, ".+oro[^/]+");
        put(Element.KRISTALL, ".+ist[^/]+");
        put(Element.MEHANIZM, ".+han[^/]+");
        put(Element.LEGENDA, ".+gen[^/]+");
        put(Element.TEN, ".+ten[^/]+");
        put(Element.PLAZMA, ".+laz[^/]+");
    }};
    private static final Map<String, Integer> TIME_REGEXS = new HashMap<String, Integer>(){{
        put("(\\d+).*сек.*", 1000); // seconds
        put("(\\d+).*мин.*", 1000 * 60); // minutes
        put("(\\d+).*час.*", 1000 * 60 * 60); // hours
        put("(\\d+).*ден.*", 1000 * 60 * 60 * 24); // days
    }};
    private static final String PLACE_REGEXS = "(стит|олод|здуш|дяно|млян|лото|ериа|хабу|емен)";

    public static Clazz parseClazz(String clazzString) {
        for (Clazz clazz : CLAZZ_REGEXS.keySet()) {
            final String regEx = CLAZZ_REGEXS.get(clazz);
            Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(clazzString);
            if (matcher.matches()) {
                return clazz;
            }
        }
        return null;
    }

    public static Element parseElement(String elementString) {
        for (Element element : ELEMENT_REGEXS.keySet()) {
            final String regEx = ELEMENT_REGEXS.get(element);
            Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(elementString.trim());
            if (matcher.matches()) {
                return element;
            }
        }
        return null;
    }

    public static long parseTimeCreation(String timeString) {
        long timeLong = 0;
        if (timeString != null) {
            for (String reg : TIME_REGEXS.keySet()) {
                final Integer time = TIME_REGEXS.get(reg);
                Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
                Matcher matcher = pattern.matcher(timeString);
                if (matcher.find()) {
                    timeLong +=  Long.parseLong(matcher.group(1)) * time;
                }
            }
        }
        return timeLong;
    }

    public static String parsePlaceCreation(String placeString) {
        if (placeString != null) {
            Pattern pattern = Pattern.compile(PLACE_REGEXS, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(placeString);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
