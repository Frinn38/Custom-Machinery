package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.data.component.ICapabilityMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SUpdateCustomTilePacket;
import fr.frinn.custommachinery.common.util.SoundManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class CustomMachineTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    public static final ResourceLocation DUMMY = new ResourceLocation(CustomMachinery.MODID, "dummy");

    private ResourceLocation id = DUMMY;

    public CraftingManager craftingManager;
    public MachineComponentManager componentManager;
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

    public CustomMachine getMachine() {
        CustomMachine machine = CustomMachinery.MACHINES.get(getId());
        if(machine != null)
            return machine;
        else
            return CustomMachine.DUMMY;
    }

    @Override
    public void tick() {
        if(this.world == null)
            return;

        if(!this.world.isRemote()) {
            this.craftingManager.tick();

            if(this.needSyncing) {
                this.needSyncing = false;
                this.sync();
            }
        } else {
            if(this.soundManager == null)
                this.soundManager = new SoundManager(this.pos);
            if(getMachine().getAppearance().getSound() != MachineAppearance.DEFAULT_SOUND && !getMachine().getAppearance().getSound().getName().equals(this.soundManager.getSound()))
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
        this.componentManager.getCapabilityComponents().forEach(ICapabilityMachineComponent::invalidateCapability);
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        for (ICapabilityMachineComponent component : this.componentManager.getCapabilityComponents()) {
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
        CompoundNBT craftingManagerNBT = new CompoundNBT();
        craftingManagerNBT.putString("status", this.craftingManager.getStatus().toString());
        if(this.craftingManager.getStatus() == CraftingManager.STATUS.ERRORED)
            craftingManagerNBT.putString("message", this.craftingManager.getErrorMessage().getString());
        craftingManagerNBT.putInt("recipeProgressTime", this.craftingManager.recipeProgressTime);
        nbt.put("craftingManager", craftingManagerNBT);

        this.componentManager.getComponents().forEach(component -> component.serialize(nbt));
        return nbt;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        if(nbt.contains("machineID", Constants.NBT.TAG_STRING) && getMachine() == CustomMachine.DUMMY)
            this.setId(new ResourceLocation(nbt.getString("machineID")));
        if(nbt.contains("craftingManager", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT craftingManagerNBT = nbt.getCompound("craftingManager");
            if(craftingManagerNBT.contains("status", Constants.NBT.TAG_STRING)) {
                CraftingManager.STATUS status = CraftingManager.STATUS.value(craftingManagerNBT.getString("status"));
                switch (status) {
                    case IDLE:
                        this.craftingManager.setIdle();
                        break;
                    case RUNNING:
                        this.craftingManager.setRunning();
                        break;
                    case ERRORED:
                        this.craftingManager.setErrored(ITextComponent.getTextComponentOrEmpty(craftingManagerNBT.getString("message")));
                        break;
                }
            }
            if(craftingManagerNBT.contains("recipeProgressTime", Constants.NBT.TAG_INT))
                this.craftingManager.recipeProgressTime = craftingManagerNBT.getInt("recipeProgressTime");
        }
        this.componentManager.getComponents().forEach(component -> component.deserialize(nbt));
    }

    /**SYNCING STUFF**/

    private void sync() {
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        if(world != null) {
            this.trackingPlayers.forEach(player -> NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateCustomTilePacket(this.getPos(), this.getUpdateTag())));
        }
        markDirty();
    }

    private final List<ServerPlayerEntity> trackingPlayers = new ArrayList<>();
    public void removeTrackingPlayer(ServerPlayerEntity player) {
        this.trackingPlayers.remove(player);
    }

    private boolean needSyncing = false;
    public void markForSyncing() {
        this.needSyncing = true;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
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
        if(player instanceof ServerPlayerEntity) {
            this.trackingPlayers.add((ServerPlayerEntity)player);
            NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SUpdateCustomTilePacket(this.getPos(), this.getUpdateTag()));
        }
        return new CustomMachineContainer(id, inv, this);
    }
}
