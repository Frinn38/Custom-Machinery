package fr.frinn.custommachinery.api.machine;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.upgrade.IMachineUpgradeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The base class of the custom machine tile entity,
 * used to get some data about the tile like it's world or position or the ICustomMachine linked to this tile.
 */
public abstract class MachineTile extends BlockEntity {

    /**
     * Default constructor.
     */
    public MachineTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return The ICustomMachine currently linked to this MachineTile, or DUMMY.
     */
    public abstract ICustomMachine getMachine();

    /**
     * Calling this on the server side will recreate the machine craftingManager and componentManager.
     * This will be called for any loaded MachineTile after /reload.
     * If machineId param is not null, the MachineTile custom machine will change to the corresponding machine.
     * @param machineId The id of the new machine linked to the tile, or null if the tile should keep its current machine.
     */
    public abstract void refreshMachine(@Nullable ResourceLocation machineId);

    /**
     * Pause or resume the MachineTile process.
     * This will not stop the tile from ticking, but managers can check if the tile is paused and stop,
     * or choose to ignore that if their work is not dependant of the tile status.
     * @param paused true to pause, false to resume.
     */
    public abstract void setPaused(boolean paused);

    /**
     * @return true if the MachineTile is paused, false if not.
     */
    public abstract boolean isPaused();

    /**
     * Can be used in {@link IMachineComponent#onRemoved()} to check if the machine is removed because of a chunk unload.
     * @return True if the chunk containing this {@link MachineTile} is being unloaded, false otherwise.
     */
    public abstract boolean isUnloaded();

    /**
     * @return The machine current status, available on both sides as it's synced automatically.
     */
    public abstract MachineStatus getStatus();

    public abstract Component getMessage();

    public abstract void setStatus(MachineStatus status, Component message);

    public void setStatus(MachineStatus status) {
        this.setStatus(status, Component.empty());
    }

    /**
     * Stop the current recipe processing and reset the crafting manager to it's idle state.
     */
    public abstract void resetProcess();

    /**
     * Must be called when the appearance of the machine change.
     */
    public abstract void refreshClientData();

    public abstract IMachineComponentManager getComponentManager();

    public abstract IMachineUpgradeManager getUpgradeManager();

    public abstract IProcessor getProcessor();

    /**
     * Get the current machine appearance.
     * The returned {@link IMachineAppearance} will be either a custom appearance set by the recipe currently processing
     * or the default appearance specified in the machine json.
     * This is synced from the server to client.
     * @return The current {@link IMachineAppearance} for this {@link MachineTile}
     */
    public abstract IMachineAppearance getAppearance();

    /**
     * This allows to set a custom {@link IMachineAppearance} to this machine tile only (other tiles from the same machine won't be changed).
     * It is used by recipes for setting a custom running appearance per recipe.
     * This method should be called on the server side only, as it will be synced to all clients automatically.
     * Pass null to make the machine use its default appearance, as specified in the machine json.
     * @param appearance A custom machine appearance to display.
     */
    public abstract void setCustomAppearance(@Nullable IMachineAppearance appearance);

    /**
     * Get the current list of {@link IGuiElement} this tile will use in a gui.
     * The returned list will be either a custom list set by the recipe currently processing
     * or the default list specified in the machine json.
     * This is synced from the server to client.
     * @return The current {@link IMachineAppearance} for this {@link MachineTile}
     */
    public abstract List<IGuiElement> getGuiElements();

    /**
     * This allows to set a custom list of {@link IGuiElement} to this machine tile only (other tiles from the same machine won't be changed).
     * It is used by recipes for setting a custom list of elements per recipe.
     * This method should be called on the server side only, as it will be synced to all clients automatically.
     * Pass null to make the machine use its default gui elements list, as specified in the machine json.
     * @param guiElements A custom list of {@link IGuiElement} to display.
     */
    public abstract void setCustomGuiElements(@Nullable List<IGuiElement> guiElements);

    /**
     * Set an entity as the owner of the machine.
     * This is done automatically when the machine block is placed by a player of fake-player.
     */
    public abstract void setOwner(LivingEntity entity);

    /**
     * Get the UUID of the {@link net.minecraft.world.entity.LivingEntity} that placed the machine,
     * or null if the machine was not placed by an entity.
     */
    @Nullable
    public abstract UUID getOwnerId();

    /**
     * Get the name of the owner of the machine, or null if the machine doesn't have an owner.
     */
    @Nullable
    public abstract Component getOwnerName();

    /**
     * Check if the machine is owned by the specified entity.
     * @param entity The entity to check.
     * @return True if the machine is owned by this entity, false otherwise.
     */
    public boolean isOwner(LivingEntity entity) {
        return entity.getUUID().equals(this.getOwnerId());
    }

    /**
     * Try to get the machine owner as a {@link LivingEntity}.
     * This works only if the owner is present on the server.
     * @return The entity that own the machine.
     */
    @Nullable
    public LivingEntity getOwner() {
        if(this.getOwnerId() == null || this.getLevel() == null || this.getLevel().getServer() == null)
            return null;

        //Try to get the owner as a player
        ServerPlayer player = this.getLevel().getServer().getPlayerList().getPlayer(this.getOwnerId());
        if(player != null)
            return player;

        //Try to get the owner as a player
        for(ServerLevel level : this.getLevel().getServer().getAllLevels()) {
            Entity entity = level.getEntity(this.getOwnerId());
            if(entity instanceof LivingEntity living)
                return living;
        }

        return null;
    }
}
