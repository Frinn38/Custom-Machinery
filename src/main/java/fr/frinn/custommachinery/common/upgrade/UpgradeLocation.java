package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public record UpgradeLocation(ResourceLocation id, MachineLocation.Loader loader, String packName, FileTime created, FileTime modified) {

    public static final NamedCodec<UpgradeLocation> CODEC = NamedCodec.record(upgradeLocationInstance ->
            upgradeLocationInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.fieldOf("id").forGetter(UpgradeLocation::id),
                    MachineLocation.Loader.CODEC.fieldOf("loader").forGetter(UpgradeLocation::loader),
                    NamedCodec.STRING.fieldOf("packName").forGetter(UpgradeLocation::packName),
                    NamedCodec.LONG.xmap(FileTime::fromMillis, FileTime::toMillis, "Time").fieldOf("created").forGetter(UpgradeLocation::created),
                    NamedCodec.LONG.xmap(FileTime::fromMillis, FileTime::toMillis, "Time").fieldOf("modified").forGetter(UpgradeLocation::modified)
            ).apply(upgradeLocationInstance, UpgradeLocation::new), "Upgrade location"
    );

    public UpgradeLocation(ResourceLocation id, MachineLocation.Loader loader, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        this.id = id;
        this.loader = loader;
        this.packName = packName;
        this.created = created == null ? FileTime.fromMillis(0) : created;
        this.modified = modified == null ? FileTime.fromMillis(0) : modified;
    }

    public static UpgradeLocation fromLoader(MachineLocation.Loader loader, ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return switch (loader) {
            case DEFAULT -> fromDefault(id, packName);
            case DATAPACK -> fromDatapack(id, packName, created, modified);
            case DATAPACK_ZIP -> fromDatapackZip(id, packName, created, modified);
            case KUBEJS -> fromKubeJS(id, packName, created, modified);
            case KUBEJS_SCRIPT -> fromKubeJSScript(id, packName);
        };
    }

    public static UpgradeLocation fromDefault(ResourceLocation id, String packName) {
        return new UpgradeLocation(id, MachineLocation.Loader.DEFAULT, packName, null, null);
    }

    public static UpgradeLocation fromDatapack(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        if (packName.startsWith("file"))
            packName = packName.substring(5);
        return new UpgradeLocation(id, MachineLocation.Loader.DATAPACK, packName, created, modified);
    }

    public static UpgradeLocation fromDatapackZip(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return new UpgradeLocation(id, MachineLocation.Loader.DATAPACK_ZIP, packName, created, modified);
    }

    public static UpgradeLocation fromKubeJS(ResourceLocation id, String packName, @Nullable FileTime created, @Nullable FileTime modified) {
        return new UpgradeLocation(id, MachineLocation.Loader.KUBEJS, packName, created, modified);
    }

    public static UpgradeLocation fromKubeJSScript(ResourceLocation id, String packName) {
        return new UpgradeLocation(id, MachineLocation.Loader.KUBEJS_SCRIPT, packName, null, null);
    }

    @Nullable
    public File getFile(MinecraftServer server) {
        return getFile(server, this.id, this.loader, this.packName);
    }

    @Nullable
    public static File getFile(MinecraftServer server, ResourceLocation id, MachineLocation.Loader loader, String packName) {
        String pathFromData = "data" + File.separator + id.getNamespace() + File.separator + "upgrade" + File.separator + id.getPath() + ".json";
        String root = Path.of("").toFile().getAbsolutePath();
        root = root + File.separator + "kubejs" + File.separator + "data" + File.separator + id.getNamespace() + File.separator + "upgrade";
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
}
