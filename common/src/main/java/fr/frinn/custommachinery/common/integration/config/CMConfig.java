package fr.frinn.custommachinery.common.integration.config;

import fr.frinn.custommachinery.CustomMachinery;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CustomMachinery.MODID)
public class CMConfig implements ConfigData {

    //LOGS
    @Category("Logs")
    @Comment("If true, all missing optional properties and their default values will be logged when parsing custom machines jsons.")
    public boolean logMissingOptional = false;

    @Category("Logs")
    @Comment("When parsing custom machines json files, some properties can be read with 2 serializers. Set this to true to log when the first serializer throw an error, even if the second succeed.")
    public boolean logFirstEitherError = false;

    //RENDER
    @Category("Rendering")
    @Comment("The time in milliseconds the block requirement working box will be rendered around the machines when clicking on the green + in the jei recipe.")
    public int boxRenderTime = 10000;

    @Category("Rendering")
    @Comment("The time in milliseconds the structure requirement structure will render in world when clicking on the green + in the jei recipe.")
    public int structureRenderTime = 10000;

    @Category("Rendering")
    @Comment("The time in milliseconds each blocks will be shown when using a block tag in a structure.")
    public int blockTagCycleTime = 1000;

    @Category("Rendering")
    @Comment("The time in milliseconds the ghost item will be shown in a slot when a tag or more than 1 item is specified.")
    public int itemSlotCycleTime = 1000;

    @Category("Rendering")
    @Comment("If true the player will need to enable advanced item tooltips (F3 + H) to see the recipe name when hovering the progress arrow on a recipe in jei")
    public boolean needAdvancedInfoForRecipeID = true;

    public static CMConfig get() {
        return AutoConfig.getConfigHolder(CMConfig.class).getConfig();
    }


}
