package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.MachineComponentManager;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.DummyCraftingManager;
import fr.frinn.custommachinery.common.crafting.UpgradeManager;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class CustomMachineTile extends MachineTile implements ISyncableStuff {

    public static final ResourceLocation DUMMY = new ResourceLocation(CustomMachinery.MODID, "dummy");

    private ResourceLocation id = DUMMY;
    private boolean paused = false;

    public CraftingManager craftingManager = new DummyCraftingManager(this);
    public MachineComponentManager componentManager = new DummyComponentManager(this);
    public UpgradeManager upgradeManager = new UpgradeManager(this);
    public SoundManager soundManager;

    public CustomMachineTile(BlockPos pos, BlockState state) {
        super(Registration.CUSTOM_MACHINE_TILE.get(), pos, state);
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
        this.craftingManager = new CraftingManager(this);
        this.componentManager = new MachineComponentManager(getMachine().getComponentTemplates(), this);
    }

    /** MachineTile Implementation **/

    @Override
    public CustomMachine getMachine() {
        CustomMachine machine = CustomMachinery.MACHINES.get(getId());
        if(machine != null)
            return machine;
        else
            return CustomMachine.DUMMY;
    }

    @Override
    public MachineStatus getStatus() {
        return this.craftingManager.getStatus();
    }

    @Override
    public void refreshMachine(@Nullable ResourceLocation id) {
        if(this.level == null || this.level.isClientSide())
            return;
        CompoundTag craftingManagerNBT = this.craftingManager.serializeNBT();
        CompoundTag componentManagerNBT = this.componentManager.serializeNBT();
        if(id == null)
            id = getId();
        this.setId(id);
        this.craftingManager.deserializeNBT(craftingManagerNBT);
        this.componentManager.deserializeNBT(componentManagerNBT);

        new SRefreshCustomMachineTilePacket(this.worldPosition, id).sendToChunkListeners(this.level.getChunkAt(this.worldPosition));
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void resetProcess() {
        if(this.level == null || this.level.isClientSide())
            return;
        this.craftingManager.reset();
    }

    @Override
    public MachineComponentManager getComponentManager() {
        return this.componentManager;
    }

    @Override
    public UpgradeManager getUpgradeManager() {
        return this.upgradeManager;
    }

    @Override
    public MachineAppearance getAppearance() {
        return getMachine().getAppearance(getStatus());
    }

    /** TileEntity Stuff **/

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomMachineTile tile) {
        if(tile.componentManager == null || tile.craftingManager == null)
            return;

        level.getProfiler().push("Component tick");
        tile.componentManager.serverTick();
        level.getProfiler().popPush("Crafting Manager tick");
        try {
            tile.craftingManager.tick();
        } catch (ComponentNotFoundException e) {
            CustomMachinery.LOGGER.error(e.getMessage());
            tile.setPaused(true);
        }
        level.getProfiler().pop();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CustomMachineTile tile) {
        if(tile.componentManager == null || tile.craftingManager == null)
            return;

        tile.componentManager.clientTick();

        if(tile.soundManager == null)
            tile.soundManager = new SoundManager(pos);
        if(tile.getAppearance().getAmbientSound() != Registration.AMBIENT_SOUND_PROPERTY.get().getDefaultValue() && !tile.getAppearance().getAmbientSound().getLocation().equals(tile.soundManager.getSoundID()))
            tile.soundManager.setSound(tile.getMachine().getAppearance(tile.getStatus()).getAmbientSound());

        if (!tile.soundManager.isPlaying())
            tile.soundManager.play();
    }

    @Override
    public void setRemoved() {
        if(this.level != null && this.level.isClientSide() && this.soundManager != null)
            this.soundManager.stop();

        if(this.level != null && !this.level.isClientSide())
            this.componentManager.getComponents().values().forEach(IMachineComponent::onRemoved);
        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("machineID", this.id.toString());
        nbt.put("craftingManager", this.craftingManager.serializeNBT());
        nbt.put("componentManager", this.componentManager.serializeNBT());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if(nbt.contains("machineID", Tag.TAG_STRING) && getMachine() == CustomMachine.DUMMY)
            this.setId(new ResourceLocation(nbt.getString("machineID")));

        if(nbt.contains("craftingManager", Tag.TAG_COMPOUND))
            this.craftingManager.deserializeNBT(nbt.getCompound("craftingManager"));

        if(nbt.contains("componentManager", Tag.TAG_COMPOUND))
            this.componentManager.deserializeNBT(nbt.getCompound("componentManager"));
    }

    //Needed for multiplayer sync
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.putString("machineID", getId().toString());
        nbt.put("craftingManager", this.craftingManager.serializeNBT());
        return nbt;
    }

    //Needed for multiplayer sync
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**CONTAINER STUFF**/

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.craftingManager.getStuffToSync(container);
        this.componentManager.getStuffToSync(container);
    }

    /**CLIENT STUFF**/

    /*
    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(CustomMachineBakedModel.APPEARANCE, getMachine().getAppearance(getStatus()).copy())
                .withInitial(CustomMachineBakedModel.STATUS, getStatus())
                .build();
    }*/
}
