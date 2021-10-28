package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ICapabilityComponent;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.client.render.CustomMachineBakedModel;
import fr.frinn.custommachinery.common.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.DummyCraftingManager;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.DummyComponentManager;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.util.SoundManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class CustomMachineTile extends MachineTile implements ITickableTileEntity, INamedContainerProvider, ISyncableStuff {

    public static final ResourceLocation DUMMY = new ResourceLocation(CustomMachinery.MODID, "dummy");

    private ResourceLocation id = DUMMY;
    private boolean paused = false;

    public CraftingManager craftingManager = new DummyCraftingManager(this);
    public MachineComponentManager componentManager = new DummyComponentManager(this);
    public SoundManager soundManager;

    public CustomMachineTile() {
        super(Registration.CUSTOM_MACHINE_TILE.get());
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
        if(this.world == null || this.world.isRemote())
            return;
        CompoundNBT craftingManagerNBT = this.craftingManager.serializeNBT();
        CompoundNBT componentManagerNBT = this.componentManager.serializeNBT();
        if(id == null)
            id = getId();
        this.setId(id);
        this.craftingManager.deserializeNBT(craftingManagerNBT);
        this.componentManager.deserializeNBT(componentManagerNBT);

        NetworkManager.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.world.getChunkAt(this.pos)), new SRefreshCustomMachineTilePacket(this.pos, id));
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
        if(this.world == null || this.world.isRemote())
            return;
        this.craftingManager.reset();
    }

    @Override
    public MachineComponentManager getComponentManager() {
        return this.componentManager;
    }

    /** TileEntity Stuff **/

    @Override
    public void tick() {
        if(this.world == null || this.componentManager == null || this.craftingManager == null)
            return;

        if(!this.world.isRemote()) {
            this.world.getProfiler().startSection("Component tick");
            this.componentManager.serverTick();
            this.world.getProfiler().endStartSection("Crafting Manager tick");
            try {
                this.craftingManager.tick();
            } catch (ComponentNotFoundException e) {
                CustomMachinery.LOGGER.error(e.getMessage());
                setPaused(true);
            }
            this.world.getProfiler().endSection();
        } else {
            this.componentManager.clientTick();

            if(this.soundManager == null)
                this.soundManager = new SoundManager(this.pos);
            if(getMachine().getAppearance(getStatus()).getSound() != MachineAppearance.DEFAULT_SOUND && !getMachine().getAppearance(getStatus()).getSound().getName().equals(this.soundManager.getSoundID()))
                this.soundManager.setSound(getMachine().getAppearance(getStatus()).getSound());

            if (this.craftingManager.getStatus() == MachineStatus.RUNNING && !this.soundManager.isPlaying())
                this.soundManager.play();
            else if(this.craftingManager.getStatus() != MachineStatus.RUNNING && this.soundManager.isPlaying())
                this.soundManager.stop();
        }
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        if(this.componentManager != null)
            this.componentManager.getCapabilityComponents().forEach(ICapabilityComponent::invalidateCapability);
    }

    @Override
    public void remove() {
        if(this.world != null && this.world.isRemote() && this.soundManager != null)
            this.soundManager.stop();
        super.remove();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(this.componentManager == null)
            return LazyOptional.empty();
        for (ICapabilityComponent component : this.componentManager.getCapabilityComponents()) {
            LazyOptional<T> capability = component.getCapability(cap, side);
            if(capability != LazyOptional.empty())
                return capability;
        }
        return LazyOptional.empty();
    }

    @ParametersAreNonnullByDefault
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putString("machineID", this.id.toString());
        nbt.put("craftingManager", this.craftingManager.serializeNBT());
        nbt.put("componentManager", this.componentManager.serializeNBT());
        return nbt;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        if(nbt.contains("machineID", Constants.NBT.TAG_STRING) && getMachine() == CustomMachine.DUMMY)
            this.setId(new ResourceLocation(nbt.getString("machineID")));

        if(nbt.contains("craftingManager", Constants.NBT.TAG_COMPOUND))
            this.craftingManager.deserializeNBT(nbt.getCompound("craftingManager"));

        if(nbt.contains("componentManager", Constants.NBT.TAG_COMPOUND))
            this.componentManager.deserializeNBT(nbt.getCompound("componentManager"));
    }

    //Needed for multiplayer sync
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.putString("machineID", getId().toString());
        return nbt;
    }

    //Needed for multiplayer sync
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 666, getUpdateTag());
    }

    //Needed for multiplayer sync
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(Registration.CUSTOM_MACHINE_BLOCK.get().getDefaultState(), pkt.getNbtCompound());
    }

    /**CONTAINER STUFF**/

    @Override
    public ITextComponent getDisplayName() {
        return getMachine().getName();
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new CustomMachineContainer(id, inv, this);
    }

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        this.craftingManager.getStuffToSync(container);
        this.componentManager.getStuffToSync(container);
    }

    /**CLIENT STUFF**/

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(CustomMachineBakedModel.APPEARANCE, getMachine().getAppearance(getStatus()).copy())
                .build();
    }
}
