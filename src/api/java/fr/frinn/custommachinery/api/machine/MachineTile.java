package fr.frinn.custommachinery.api.machine;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

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
     * @return The machine current status, available on both sides as it's synced automatically.
     */
    public abstract MachineStatus getStatus();

    /**
     * Stop the current recipe processing and reset the crafting manager to it's idle state.
     */
    public abstract void resetProcess();

    public abstract IMachineComponentManager getComponentManager();

    public abstract IMachineAppearance getAppearance();
}
