package com.x256n.importer.msmguide.importer;

import com.x256n.importer.msmguide.ImportProcessor;
import com.x256n.importer.msmguide.common.Utils;
import com.x256n.importer.msmguide.db.DomainDao;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2
{
    public static void main(String[] args)
    {
        try
        {
//            DomainDao domainDao = new DomainDao();

            File catalog = new File("D://Catalog/");

            //--- Создание и заполнение основных таблиц
            ImportProcessor importProcessor = new ImportProcessor();
            importProcessor.process(catalog);

            //--- Заполнение промежуточных таблиц
            BindingTableImporter tableImporter = new BindingTableImporter();
            tableImporter.process(catalog);

            System.out.println("loading complete!");
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}


