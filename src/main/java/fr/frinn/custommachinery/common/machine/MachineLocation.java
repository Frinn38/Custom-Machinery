package fr.frinn.custommachinery.common.machine;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Locale;

public record MachineLocation(ResourceLocation id, Loader loader, String packName, FileTime created, FileTime modified) {

    public static final NamedCodec<MachineLocation> CODEC = NamedCodec.record(machineLocationInstance ->
            machineLocationInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("id").forGetter(MachineLocation::id),
                    Loader.CODEC.fieldOf("loader").forGetter(MachineLocation::loader),
                    NamedCodec.STRING.fieldOf("packName").forGetter(MachineLocation::packName),
                    NamedCodec.LONG.xmap(FileTime::fromMillis, FileTime::toMillis, "Time").fieldOf("created").forGetter(MachineLocation::created),
                    NamedCodec.LONG.xmap(FileTime::fromMillis, FileTime::toMillis, "Time").fieldOf("modified").forGetter(MachineLocation::modified)
            ).apply(machineLocationInstance, MachineLocation::new), "Machine location"
    );

    public MachineLocation(ResourceLocation id, Loader loader, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        this.id = id;
        this.loader = loader;
        this.packName = packName;
        this.created = created == null ? FileTime.fromMillis(0) : created;
        this.modified = modified == null ? FileTime.fromMillis(0) : modified;
    }

    public static MachineLocation fromLoader(Loader loader, ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return switch (loader) {
            case DEFAULT -> fromDefault(id, packName);
            case DATAPACK -> fromDatapack(id, packName, created, modified);
            case DATAPACK_ZIP -> fromDatapackZip(id, packName, created, modified);
            case KUBEJS -> fromKubeJS(id, packName, created, modified);
            case KUBEJS_SCRIPT -> fromKubeJSScript(id, packName);
        };
    }

    public static MachineLocation fromDefault(ResourceLocation id, String packName) {
        return new MachineLocation(id, Loader.DEFAULT, packName, null, null);
    }

    public static MachineLocation fromDatapack(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        if (packName.startsWith("file"))
            packName = packName.substring(5);
        return new MachineLocation(id, Loader.DATAPACK, packName, created, modified);
    }

    public static MachineLocation fromDatapackZip(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return new MachineLocation(id, Loader.DATAPACK_ZIP, packName, created, modified);
    }

    public static MachineLocation fromKubeJS(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return new MachineLocation(id, Loader.KUBEJS, packName, created, modified);
    }

    public static MachineLocation fromKubeJSScript(ResourceLocation id, String packName) {
        return new MachineLocation(id, Loader.KUBEJS_SCRIPT, packName, null, null);
    }

    @Nullable
    public File getFile(MinecraftServer server) {
        return getFile(server, this.id, this.loader, this.packName);
    }

    @Nullable
    public static File getFile(MinecraftServer server, ResourceLocation id, Loader loader, String packName) {
        String pathFromData = "data" + File.separator + id.getNamespace() + File.separator + "machine" + File.separator + id.getPath() + ".json";
        String root = Path.of("").toFile().getAbsolutePath();
        //if (!FMLLoader.isProduction())
        //    root = root.substring(0, root.length() - 2);
        root = root + File.separator + "kubejs" + File.separator + "data" + File.separator + id.getNamespace() + File.separator + "machine";
        File kubeJS = new File(root, id.getPath() + ".json");
        return switch (loader) {
            case DATAPACK -> server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(packName + File.separator + pathFromData).normalize().toFile();
            case KUBEJS -> kubeJS;
            default -> null;
        };
    }

    public boolean canEdit() {
        return this.loader.canEdit;
    }

    public MutableComponent getText() {
        return this.loader.getTranslatedName().append(Component.literal(" : " + this.packName));
    }

    public enum Loader {
        DEFAULT(false, ChatFormatting.BLACK),
        DATAPACK(true, ChatFormatting.DARK_GREEN),
        DATAPACK_ZIP(false, ChatFormatting.DARK_RED),
        KUBEJS(true, ChatFormatting.DARK_PURPLE),
        KUBEJS_SCRIPT(false, ChatFormatting.DARK_RED);

        public static final NamedCodec<Loader> CODEC = NamedCodec.enumCodec(Loader.class);

        private final boolean canEdit;
        private final ChatFormatting color;

        Loader(boolean canEdit, ChatFormatting color) {
            this.canEdit = canEdit;
            this.color = color;
        }

        public MutableComponent getTranslatedName() {
            return Component.translatable("custommachinery.machine.loader." + this.name().toLowerCase(Locale.ROOT)).withStyle(this.color);
        }
    }
}
