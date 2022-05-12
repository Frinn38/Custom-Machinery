package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class CMLogger {

    public static final Logger INSTANCE = CustomMachinery.LOGGER;

    public static void init() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        PatternLayout logPattern = PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss.SSS}][%level]: %msg%n%throwable")
                .build();

        Appender cmAppender = FileAppender.newBuilder()
                .withFileName("logs/custommachinery.log")
                .withAppend(false)
                .setName("Custom Machinery")
                .withImmediateFlush(true)
                .setIgnoreExceptions(false)
                .setConfiguration(config)
                .setLayout(logPattern)
                .build();

        cmAppender.start();

        config.addAppender(cmAppender);

        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, "Custom Machinery", "true", new AppenderRef[0], null, config, null);

        loggerConfig.addAppender(cmAppender, Level.ALL, null);
        loggerConfig.addAppender(config.getAppender("DebugFile"), Level.DEBUG, null);
        loggerConfig.addAppender(config.getAppender("File"), Level.INFO, null);
        loggerConfig.addAppender(config.getAppender("Console"), Level.DEBUG, null);
        loggerConfig.addAppender(config.getAppender("ServerGuiConsole"), Level.INFO, null);

        config.addLogger("Custom Machinery", loggerConfig);
        ctx.updateLoggers();
    }
}
