package fr.frinn.custommachinery.api.utils;

import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CMLogger {

    private FileChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);

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
        message = String.format("[%s][%s][%s]: %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), EffectiveSide.get(), type, String.format(message, args));
        this.buffer.put(message.getBytes(StandardCharsets.UTF_8));
        this.buffer.flip();
        try {
            this.channel.write(this.buffer);
            this.channel.force(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.buffer.clear();
    }

    public void reset() {
        try {
            this.channel = new FileOutputStream("logs/custommachinery.log").getChannel();
        } catch (FileNotFoundException e) {
            System.out.println("Can't create custommachinery.log file");
        }
    }

    public static boolean shouldLogMissingOptionals() {
        return false;
    }

    public static boolean shouldLogFirstEitherError() {
        return false;
    }
}
