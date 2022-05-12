package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.CustomMachinery;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.FileWriter;
import java.io.IOException;

public class CMLogger {

    public static final Logger INSTANCE = CustomMachinery.LOGGER;

    private static boolean shouldReset = false;

    public static void init() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        PatternLayout logPattern = PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss.SSS}][%level]: %msg%n%throwable")
                .build();

        TriggeringPolicy policy = new TriggeringPolicy() {
            @Override
            public void initialize(RollingFileManager manager) {

            }

            @Override
            public boolean isTriggeringEvent(LogEvent logEvent) {
                if(shouldReset) {
                    shouldReset = false;
                    return true;
                }
                return false;
            }
        };

        RolloverStrategy strategy = manager -> new RolloverDescriptionImpl(manager.getFileName(), true, new AbstractAction() {
                    @Override
                    public boolean execute() throws IOException {
                        new FileWriter(manager.getFileName(), false).close();
                        return false;
                    }
                }, null);

        RollingFileAppender cmAppender = RollingFileAppender.newBuilder()
                .withFileName("logs/custommachinery.log")
                .withAppend(false)
                .withFilePattern("logs/custommachinery-%i.log.gz")
                .withPolicy(policy)
                .withStrategy(strategy)
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

    public static void reset() {
        shouldReset = true;
    }
}
