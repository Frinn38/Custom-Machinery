package fr.frinn.custommachinery.common.machine;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.Locale;

public class MachineLocation {

    public static final NamedCodec<MachineLocation> CODEC = NamedCodec.record(machineLocationInstance ->
            machineLocationInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("id").forGetter(MachineLocation::getId),
                    Loader.CODEC.fieldOf("loader").forGetter(MachineLocation::getLoader),
                    NamedCodec.STRING.fieldOf("packName").forGetter(MachineLocation::getPackName)
            ).apply(machineLocationInstance, MachineLocation::new), "Machine location"
    );

    private final ResourceLocation id;
    private final Loader loader;
    private final String packName;

    private MachineLocation(ResourceLocation id, Loader loader, String packName) {
        this.id = id;
        this.loader = loader;
        this.packName = packName;
    }

    public static MachineLocation fromLoader(Loader loader, ResourceLocation id, String packName) {
        return switch (loader) {
            case DEFAULT -> fromDefault(id);
            case DATAPACK -> fromDatapack(id, packName);
            case CRAFTTWEAKER -> fromCraftTweaker(id);
            case KUBEJS -> fromKubeJS(id);
        };
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

        public static final NamedCodec<Loader> CODEC = NamedCodec.enumCodec(Loader.class);

        public TranslatableComponent getTranslatedName() {
            return new TranslatableComponent("custommachinery.machine.loader." + this.name().toLowerCase(Locale.ENGLISH));
        }

        @SuppressWarnings("ConstantConditions")
        public int getColor() {
            return switch (this) {
                case DEFAULT -> ChatFormatting.BLACK.getColor();
                case DATAPACK -> ChatFormatting.DARK_GREEN.getColor();
                case KUBEJS -> ChatFormatting.DARK_PURPLE.getColor();
                case CRAFTTWEAKER -> ChatFormatting.DARK_AQUA.getColor();
            };
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
