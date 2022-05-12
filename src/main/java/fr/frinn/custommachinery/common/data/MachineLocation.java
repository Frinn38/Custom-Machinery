package fr.frinn.custommachinery.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.Locale;

public class MachineLocation {

    public static final Codec<MachineLocation> CODEC = RecordCodecBuilder.create(machineLocationInstance ->
            machineLocationInstance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(MachineLocation::getId),
                    Codecs.LOADER_CODEC.fieldOf("loader").forGetter(MachineLocation::getLoader),
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

    public static MachineLocation fromLoader(Loader loader, ResourceLocation id, String packName) {
        switch (loader) {
            case DEFAULT:
                return fromDefault(id);
            case DATAPACK:
                return fromDatapack(id, packName);
            case CRAFTTWEAKER:
                return fromCraftTweaker(id);
            case KUBEJS:
                return fromKubeJS(id);
        }
        throw new IllegalStateException("Invalid Custom Machine Loader: " + loader.name());
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

        public TranslatableComponent getTranslatedName() {
            return new TranslatableComponent("custommachinery.machine.loader." + this.name().toLowerCase(Locale.ENGLISH));
        }

        @SuppressWarnings("ConstantConditions")
        public int getColor() {
            switch (this) {
                case DEFAULT:
                    return ChatFormatting.BLACK.getColor();
                case DATAPACK:
                    return ChatFormatting.DARK_GREEN.getColor();
                case KUBEJS:
                    return ChatFormatting.DARK_PURPLE.getColor();
                case CRAFTTWEAKER:
                    return ChatFormatting.DARK_AQUA.getColor();
            }
            return 0;
        }

        public static Loader value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
