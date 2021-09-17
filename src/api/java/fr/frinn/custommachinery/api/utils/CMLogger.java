package fr.frinn.custommachinery.api.utils;

import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CMLogger {

    private BufferedWriter writer;

    public CMLogger() {
        reset();
    }

    public void info(String message, Object... args) {
        log("INFO", message, args);
    }

    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }

    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }

    public void log(String type, String message, Object... args) {
        if(!shouldLog())
            return;

        message = String.format("[%s][%s][%s]: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), EffectiveSide.get(), type, String.format(message, args));
        try {
            this.writer.append(message);
            this.writer.newLine();
            this.writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        try {
            if(this.writer != null)
                this.writer.close();
            this.writer = Files.newBufferedWriter(new File("logs/custommachinery.log").toPath(), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Can't create custommachinery.log file");
        }
    }

    public static boolean shouldLog() {
        return true;
    }

    public static boolean shouldLogMissingOptionals() {
        return false;
    }

    public static boolean shouldLogFirstEitherError() {
        return false;
    }
}
