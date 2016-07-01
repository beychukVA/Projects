package com.x256n.importer.msmguide;

import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author liosha (01.09.2015).
 */
public class Main {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String VALIDATE_SHORT = "v";
    public static final String VALIDATE = "validate";
    public static final String IMPORT_SHORT = "i";
    public static final String IMPORT = "import";
    public static final String HELP_SHORT = "h";
    public static final String HELP = "help";

    private static final Option optionValidate = new Option(VALIDATE_SHORT, VALIDATE, true, "Validate input ZIP file");
    private static final Option optionImport = new Option(IMPORT_SHORT, IMPORT, true, "Import input ZIP file");
    private static final Option optionHelp = new Option(HELP_SHORT, HELP, true, "Help");
    private static final Options posixOptions = new Options();

    static {
        optionValidate.setArgs(1); // one argument
        optionValidate.setOptionalArg(false);
        optionValidate.setArgName("ZIP file name");
        posixOptions.addOption(optionValidate);

        optionImport.setArgs(1); // one argument
        optionImport.setOptionalArg(false);
        optionImport.setArgName("ZIP file name");
        posixOptions.addOption(optionImport);

        optionHelp.setArgs(0); // no arguments
        optionHelp.setOptionalArg(true);
        optionHelp.setArgName("Help");
        posixOptions.addOption(optionHelp);
    }

    public static void main(String[] params) throws Exception {
        CommandLineParser cmdLinePosixParser = new PosixParser();
        CommandLine commandLine = cmdLinePosixParser.parse(posixOptions, params);
        try {
            BasicConfigurator.configure();

            if (commandLine.hasOption(VALIDATE_SHORT)) {
                String[] arguments = commandLine.getOptionValues(VALIDATE_SHORT);
                final String zipPath = arguments[0];
//                logger.debug("Путь к папке: " + zipPath);
//                File inputFile = new File(zipPath);
//                if (!inputFile.exists()) {
//                    throw new Exception("Ошибка! Папка не найдена!");
//                }
//
//                if (!inputFile.isDirectory()) {
//                    throw new Exception("Ошибка! Это не папка!");
//                }
//                final File[] listFiles = inputFile.listFiles();
//                if (listFiles != null && listFiles.length < 3) {
//                    inputFile = listFiles[0];
//                }
//                new ImportProcessor().process(inputFile);

//                final ZipValidator validator = Factory.createValidator();
//                try (InputStream inputStream = new FileInputStream(inputFile)) {
//                    validator.bindFile(inputStream);
//                    validator.validate();
//                    logger.debug("Все в порядке, файл корректный - OK!");
//                }
            } else if (commandLine.hasOption(IMPORT_SHORT)) {
                String[] arguments = commandLine.getOptionValues(IMPORT_SHORT);
                final String inputDirectory = arguments[0];
                logger.debug("Путь к папке: " + inputDirectory);
                File inputFile = new File(inputDirectory);
                if (!inputFile.exists()) {
                    throw new Exception("Ошибка! Папка не найдена!");
                }

                if (!inputFile.isDirectory()) {
                    throw new Exception("Ошибка! Это не папка!");
                }
                final File[] listFiles = inputFile.listFiles();
                if (listFiles != null && listFiles.length < 3) {
                    inputFile = listFiles[0];
                }

                final ImportProcessor importProcessor = new ImportProcessor();
                importProcessor.process(inputFile);

            } else if (commandLine.hasOption(HELP_SHORT)) {
                printHelp(
                        posixOptions, // опции по которым составляем help
                        80, // ширина строки вывода
                        "Options", // строка предшествующая выводу
                        "-- HELP --", // строка следующая за выводом
                        3, // число пробелов перед выводом опции
                        5, // число пробелов перед выводом опцисания опции
                        true, // выводить ли в строке usage список команд
                        System.out // куда производить вывод
                );
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    public static void printHelp(
            final Options options,
            final int printedRowWidth,
            final String header,
            final String footer,
            final int spacesBeforeOption,
            final int spacesBeforeOptionDescription,
            final boolean displayUsage,
            final OutputStream out) {
        final String commandLineSyntax = "java -jar msm-guide-importer-0.1.0.jar";//подсказка по запуску самой программы
        final PrintWriter writer = new PrintWriter(out);// куда печатаем help
        final HelpFormatter helpFormatter = new HelpFormatter();// создаем объект для вывода help`а
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);//формирование справки
        writer.flush(); // вывод
    }
}
