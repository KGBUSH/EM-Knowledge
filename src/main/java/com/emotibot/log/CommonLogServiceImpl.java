/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 
 *
 */
public class CommonLogServiceImpl implements CommonLogService {

    private final static Map<String, CommonLogService> logServices = new HashMap<>();
    public static CommonLogService getInstance(String moduleName) {
        if (!logServices.containsKey(moduleName)) {
            logServices.put(moduleName, new CommonLogServiceImpl(moduleName));
        }
        return logServices.get(moduleName);
    }

    private SimpleDateFormat sdFormatWithSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdFormat        = new SimpleDateFormat("yyyy-MM-dd");
    private Logger           log;
    private LogFileHandler      fileHandler;
    private String           moduleName;

    /**
     * 
     */
    private CommonLogServiceImpl(String moduleName) {
        this.moduleName = moduleName;
        log = Logger.getLogger(moduleName);
        log.setLevel(Level.ALL);
        fileHandler = getFileHandler();
        log.addHandler(fileHandler);
    }

    /**
     * @param moduleName
     */
    @SuppressWarnings("deprecation")
    private LogFileHandler getFileHandler() {
        Date date = getDate();
        String fileName = generateFileName(date);
        LogFileHandler fileHandler;
        try {
            fileHandler = new LogFileHandler(fileName, date.getDate());
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFormatter());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return fileHandler;
    }

    /**
     * @param moduleName
     * @return
     */
    private String generateFileName(Date date) {
        File logFolder = new File("../log");
        if (!logFolder.exists()) {
            logFolder.mkdir();
        }
        return logFolder.getPath() + "/" + String.format("%s_%s.log", moduleName, sdFormat.format(date));
    }


    

    /**
     * @param text
     */
    @Override
    @SuppressWarnings("deprecation")
    public void log(String text) {
        Date date = getDate();
        if (date.getDate()  != fileHandler.date){
            fileHandler.close();
            log.removeHandler(fileHandler);
            fileHandler = getFileHandler();
            log.addHandler(fileHandler);
        }
        log.log(Level.INFO, text);
    }


    /**
     * explore this method for test.
     * @return
     */
    Date getDate() {
        return new Date();
    }

    class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            Date date = getDate();
            return String.format("[%s] %s\n", sdFormatWithSec.format(date), record.getMessage());
        }
    }

    class LogFileHandler extends FileHandler {
        //used to check if should change file.
        private int date;
        /**
         * @param pattern
         * @throws IOException
         * @throws SecurityException
         */
        public LogFileHandler(String pattern, int date) throws IOException, SecurityException {
            super(pattern, true);
            this.date = date;
        }

        /**
         * @return the date
         */
        public int getDate() {
            return date;
        }
    }

}
