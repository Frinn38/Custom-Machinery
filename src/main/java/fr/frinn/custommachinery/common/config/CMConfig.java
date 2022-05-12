package fr.frinn.custommachinery.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class CMConfig {

    public static final CMConfig INSTANCE = new CMConfig();

    private final ForgeConfigSpec spec;

    //LOGS
    public final BooleanValue logMissingOptional;
    public final BooleanValue logFirstEitherError;

    //RENDER
    public final IntValue boxRenderTime;
    public final IntValue structureRenderTime;
    public final IntValue blockTagCycleTime;
    public final IntValue itemSlotCycleTime;
    public final BooleanValue needAdvancedInfoForRecipeID;

    public CMConfig()  {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Logging");
        this.logMissingOptional = builder.comment("If true, all missing optional properties and their default values will be logged when parsing custom machines jsons.")
                .define("logMissingOptional", false);
        this.logFirstEitherError = builder.comment("When parsing custom machines json files, some properties can be read with 2 serializers.", "Set this to true to log when the first serializer throw an error, even if the second succeed.")
                .define("logFirstEitherError", false);
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
