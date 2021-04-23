package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;
import java.util.Locale;

public class MachineLocation {

    public static final Codec<MachineLocation> CODEC = RecordCodecBuilder.create(machineLocationInstance ->
            machineLocationInstance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(MachineLocation::getId),
                    Loader.CODEC.fieldOf("loader").forGetter(MachineLocation::getLoader),
                    Codec.STRING.fieldOf("packName").forGetter(MachineLocation::getPackName)
            ).apply(machineLocationInstance, MachineLocation::new)
    );

    private ResourceLocation id;
    private Loader loader;
    private String packName;

    private MachineLocation(ResourceLocation id, Loader loader, String packName) {
        this.id = id;
        this.loader = loader;
        this.packName = packName;
    }

    public static MachineLocation fromDefault(ResourceLocation id) {
        return new MachineLocation(id, Loader.DEFAULT, "");
    }

    public static MachineLocation fromDatapack(ResourceLocation id, String packName) {
        return new MachineLocation(id, Loader.DATAPACK, packName);
    }

    public static MachineLocation fromCraftTweaker(ResourceLocation id) {
        return new MachineLocation(id, Loader.CRAFTTWEAKER, "");
    }

    public static MachineLocation fromKubeJS(ResourceLocation id) {
        return new MachineLocation(id, Loader.KUBEJS, "");
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Loader getLoader() {
        return this.loader;
    }

    public String getPackName() {
        return this.packName;
    }

    public String getPath() {
        return this.packName + File.separator + "data" + File.separator + this.id.getNamespace() + File.separator + "machines" + File.separator + this.id.getPath() + ".json";
    }

    public enum Loader {
        DEFAULT,
        DATAPACK,
        CRAFTTWEAKER,
        KUBEJS;

        public static final Codec<Loader> CODEC = Codec.STRING.xmap(Loader::valueOf, Loader::toString).stable();

        public TranslationTextComponent getTranslatedName() {
            return new TranslationTextComponent("custommachinery.machine.loader." + this.name().toLowerCase(Locale.ENGLISH));
        }

        @SuppressWarnings("ConstantConditions")
        public int getColor() {
            switch (this) {
                case DEFAULT:
                    return TextFormatting.BLACK.getColor();
                case DATAPACK:
                    return TextFormatting.DARK_GREEN.getColor();
                case KUBEJS:
                    return TextFormatting.DARK_PURPLE.getColor();
                case CRAFTTWEAKER:
                    return TextFormatting.DARK_AQUA.getColor();
            }
            return 0;
        }
    }
}
