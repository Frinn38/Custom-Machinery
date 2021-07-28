package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.ICapabilityComponent;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.client.render.CustomMachineBakedModel;
import fr.frinn.custommachinery.common.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SUpdateCustomTileLightPacket;
import fr.frinn.custommachinery.common.data.component.DummyComponentManager;
import fr.frinn.custommachinery.common.crafting.DummyCraftingManager;
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
import net.minecraft.util.text.StringTextComponent;
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

    @Override
    public CustomMachine getMachine() {
        CustomMachine machine = CustomMachinery.MACHINES.get(getId());
        if(machine != null)
            return machine;
        else
            return CustomMachine.DUMMY;
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

        this.world.notifyBlockUpdate(this.pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    public void tick() {
        if(this.world == null || this.componentManager == null || this.craftingManager == null)
            return;

        if(!this.world.isRemote()) {
            this.componentManager.tick();
            try {
                this.craftingManager.tick();
            } catch (ComponentNotFoundException e) {
                CustomMachinery.LOGGER.error(e.getMessage());
                setPaused(true);
            }

            if(this.needRefreshLightning())
                this.refreshLightning();

        } else {
            if(this.soundManager == null)
                this.soundManager = new SoundManager(this.pos);
            if(getMachine().getAppearance().getSound() != MachineAppearance.DEFAULT_SOUND && !getMachine().getAppearance().getSound().getName().equals(this.soundManager.getSoundID()))
                this.soundManager.setSound(getMachine().getAppearance().getSound());

            if (this.craftingManager.getStatus() == CraftingManager.STATUS.RUNNING && !this.soundManager.isPlaying())
                this.soundManager.play();
            else if(this.craftingManager.getStatus() != CraftingManager.STATUS.RUNNING && this.soundManager.isPlaying())
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

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 666, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(null, pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.putString("machineID", this.id.toString());
        nbt.put("craftingManager", this.craftingManager.serializeNBT());
        nbt.put("componentManager", this.componentManager.serializeNBT());
        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        if(nbt.contains("machineID", Constants.NBT.TAG_STRING))
            this.setId(new ResourceLocation(nbt.getString("machineID")));
        if(nbt.contains("craftingManager", Constants.NBT.TAG_COMPOUND))
            this.craftingManager.deserializeNBT(nbt.getCompound("craftingManager"));
        if(nbt.contains("componentManager", Constants.NBT.TAG_COMPOUND))
            this.componentManager.deserializeNBT(nbt.getCompound("componentManager"));
    }

    private boolean paused = false;
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return this.paused;
    }

    /**LIGHTNING STUFF**/

    private void refreshLightning() {
        if(world != null && !world.isRemote) {
            this.changeLightState();
            NetworkManager.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos.getX() / 16, pos.getZ() / 16)), new SUpdateCustomTileLightPacket(pos));
        }

    }

    private boolean emmitLight = false;
    public void changeLightState() {
        this.emmitLight = !this.emmitLight;
        if(world != null)world.getChunkProvider().getLightManager().checkBlock(pos);
    }

    private boolean needRefreshLightning() {
        return (getMachine().getAppearance().getLightMode() == MachineAppearance.LightMode.ALWAYS || getMachine().getAppearance().getLightMode().toString().equals(this.craftingManager.getStatus().toString())) != emmitLight;
    }

    public int getLightValue() {
        if(this.emmitLight)
            return getMachine().getAppearance().getLightLevel();
        else return 0;
    }

    /**CONTAINER STUFF**/

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getMachine().getName());
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
                .withInitial(CustomMachineBakedModel.APPEARANCE, getMachine().getAppearance().copy())
                .build();
    }
}
