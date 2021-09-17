package fr.frinn.custommachinery.api.utils;

public interface ICMLogger {

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    boolean enableLogging();

    boolean logMissingOptional();

    boolean logFirstEitherError();

    boolean shouldLog(String type);
}
