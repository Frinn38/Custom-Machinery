package fr.frinn.custommachinery.common.config;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.common.util.LoggingLevel;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CMConfig {

    public static final CMConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        Pair<CMConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(CMConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    //LOGS
    public final ConfigValue<Boolean> logMissingOptional;
    public final ConfigValue<Boolean> logFirstEitherError;
    public final ConfigValue<LoggingLevel> debugLevel;
    public final ConfigValue<Boolean> logLegacyFolderFiles;

    //RENDERING
    public final ConfigValue<Integer> boxRenderTime;
    public final ConfigValue<Integer> structureRenderTime;
    public final ConfigValue<Integer> blockTagCycleTime;
    public final ConfigValue<Integer> itemSlotCycleTime;

    //MISC
    public final ConfigValue<List<String>> modelFolders;

    public CMConfig(ModConfigSpec.Builder builder) {
        //LOGS
        builder.push("logs");
        this.logMissingOptional = builder
                .comment("If true, all missing optional properties and their default values will be logged when parsing custom machines jsons.")
                .define("log_missing_optional", false);
        this.logFirstEitherError = builder
                .comment("When parsing custom machines json files, some properties can be read with 2 serializers. Set this to true to log when the first serializer throw an error, even if the second succeed.")
                .define("log_first_either_error", false);
        this.debugLevel = builder
                .comment("Configure what logs will be printed in the custommachinery.log file. Only logs with level higher or equal than selected will be printed. FATAL > ERROR > WARN > INFO > DEBUG > ALL")
                .defineEnum("debug_level", LoggingLevel.INFO);
        this.logLegacyFolderFiles = builder
                .comment("Displays a warning if there are files in legacy data folders, such as 'machines' instead of 'machine'")
                .define("log_legacy_folder_files", true);
        builder.pop();

        //RENDER
        builder.push("rendering");
        this.boxRenderTime = builder
                .comment("The time in milliseconds the block requirement working box will be rendered around the machines when clicking on the icon in the jei recipe.")
                .defineInRange("box_render_time", 10000, 1, Integer.MAX_VALUE);
        this.structureRenderTime = builder
                .comment("The time in milliseconds the structure requirement structure will render in world when clicking on the icon in the jei recipe.")
                .defineInRange("structure_render_time", 10000, 1, Integer.MAX_VALUE);
        this.blockTagCycleTime = builder
                .comment("The time in milliseconds each blocks will be shown when using a block tag in a structure.")
                .defineInRange("block_tag_cycle_time", 1000, 1, Integer.MAX_VALUE);
        this.itemSlotCycleTime = builder
                .comment("The time in milliseconds the ghost item will be shown in a slot when a tag or more than 1 item is specified.")
                .defineInRange("item_slot_cycle_time", 1000, 1, Integer.MAX_VALUE);
        builder.pop();

        //MISC
        builder.push("misc");
        this.modelFolders = builder
                .comment("A list of folder names where CM will load models json. These folders must be under the 'assets/namespace/models' folder.")
                .define("model_folders", Lists.newArrayList("machine", "machines"));
    }
}
