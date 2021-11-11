package fr.frinn.custommachinery.common.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

import java.util.List;

public class CMConfig {

    public static final CMConfig INSTANCE = new CMConfig();

    private final ForgeConfigSpec spec;

    //LOGS
    public final BooleanValue enableLogging;
    public final BooleanValue logMissingOptional;
    public final BooleanValue logFirstEitherError;
    public final ConfigValue<List<? extends String>> allowedLogs;

    //RENDER
    public final IntValue boxRenderTime;
    public final IntValue structureRenderTime;
    public final IntValue blockTagCycleTime;
    public final IntValue itemSlotCycleTime;
    public final BooleanValue needAdvancedInfoForRecipeID;

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

        builder.push("Rendering");
        this.boxRenderTime = builder.comment("The time in milliseconds the block requirement working box will be rendered around the machines when clicking on the green + in the jei recipe.")
                .defineInRange("boxRenderTime", 10000, 0, Integer.MAX_VALUE);
        this.structureRenderTime = builder.comment("The time in milliseconds the structure requirement structure will render in world when clicking on the green + in the jei recipe.")
                .defineInRange("structureRenderTime", 10000, 0, Integer.MAX_VALUE);
        this.blockTagCycleTime = builder.comment("The time in milliseconds each blocks will be shown when using a block tag in a structure.")
                .defineInRange("blockTagCycleTime", 1000, 0, Integer.MAX_VALUE);
        this.itemSlotCycleTime = builder.comment("The time in milliseconds the ghost item will be shown in a slot when a tag or more than 1 item is specified.")
                .defineInRange("itemSlotCycleTime", 1000, 0, Integer.MAX_VALUE);
        this.needAdvancedInfoForRecipeID = builder.comment("If true the player will need to enable advanced item tooltips (F3 + H) to see the recipe name when hovering the progress arrow on a recipe in jei")
                .define("needAdvancedInfoForRecipeID", true);
        builder.pop();

        this.spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }
}
