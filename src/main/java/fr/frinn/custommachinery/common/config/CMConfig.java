package fr.frinn.custommachinery.common.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

public class CMConfig {

    public static final CMConfig INSTANCE = new CMConfig();

    private final ForgeConfigSpec spec;

    public final BooleanValue enableLogging;
    public final BooleanValue logMissingOptional;
    public final BooleanValue logFirstEitherError;
    public final ConfigValue<List<? extends String>> allowedLogs;

    public CMConfig()  {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Logging");
        this.enableLogging = builder.comment("Set to false to disable all logging messages, the custommachinery.log file will still be created but never written.")
                .define("enableLogging", true);
        this.logMissingOptional = builder.comment("If true, all missing optional properties and their default values will be logged when parsing custom machines jsons.")
                .define("logMissingOptional", false);
        this.logFirstEitherError = builder.comment("When parsing custom machines json files, some properties can be read with 2 serializers.", "Set this to true to log when the first serializer throw an error, even if the second succeed.")
                .define("logFirstEitherError", false);
        this.allowedLogs = builder.comment("Customize which types of logs you want to be written in the custommachinery.log file.", "Default values: [\"INFO\", \"WARN\", \"ERROR\"]")
                .defineList("allowedLogs", Lists.newArrayList("INFO", "WARN", "ERROR"), o -> o instanceof String);
        builder.pop();
        this.spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }
}
