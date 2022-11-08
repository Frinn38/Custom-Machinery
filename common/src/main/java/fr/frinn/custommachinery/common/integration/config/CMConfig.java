package fr.frinn.custommachinery.common.integration.config;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = CustomMachinery.MODID)
public class CMConfig implements ConfigData {

    //LOGS
    @Category("Logs")
    @Comment("If true, all missing optional properties\nand their default values will be logged\nwhen parsing custom machines jsons.")
    public boolean logMissingOptional = false;

    @Category("Logs")
    @Comment("When parsing custom machines json files,\nsome properties can be read with 2 serializers.\nSet this to true to log when the first serializer throw an error,\neven if the second succeed.")
    public boolean logFirstEitherError = false;

    //RENDER
    @Category("Rendering")
    @Comment("The time in milliseconds the block requirement\nworking box will be rendered around the machines\nwhen clicking on the icon in the jei recipe.")
    public int boxRenderTime = 10000;

    @Category("Rendering")
    @Comment("The time in milliseconds the structure requirement\nstructure will render in world when clicking\non the icon in the jei recipe.")
    public int structureRenderTime = 10000;

    @Category("Rendering")
    @Comment("The time in milliseconds each blocks will be shown\nwhen using a block tag in a structure.")
    public int blockTagCycleTime = 1000;

    @Category("Rendering")
    @Comment("The time in milliseconds the ghost item will be shown\nin a slot when a tag or more than 1 item is specified.")
    public int itemSlotCycleTime = 1000;

    @Category("Rendering")
    @Comment("If true the player will need to enable advanced item tooltips (F3 + H)\nto see the recipe name when hovering the progress arrow on a recipe in jei")
    public boolean needAdvancedInfoForRecipeID = true;

    //MISC
    @Category("Misc")
    @Comment("A list of folder names where CM will load models json.\nThese folders must be under the \"assets/namespace/models\" folder.")
    public List<String> modelFolders = Lists.newArrayList("machine", "machines");

    public static CMConfig get() {
        return AutoConfig.getConfigHolder(CMConfig.class).getConfig();
    }


}
