package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.MachineComponentManager;
import fr.frinn.custommachinery.common.crafting.DummyProcessor;
import fr.frinn.custommachinery.common.crafting.UpgradeManager;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineAppearancePacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineGuiElementsPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineStatusPacket;
import fr.frinn.custommachinery.common.network.syncable.StringSyncable;
import fr.frinn.custommachinery.common.util.MachineList;
import fr.frinn.custommachinery.common.util.SoundManager;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class CustomMachineTile extends MachineTile implements ISyncableStuff {

    public static final ResourceLocation DUMMY = new ResourceLocation(CustomMachinery.MODID, "dummy");

    private ResourceLocation id = DUMMY;
    private boolean paused = false;

    private IProcessor processor = new DummyProcessor(this);
    private MachineComponentManager componentManager = new DummyComponentManager(this);
    private final UpgradeManager upgradeManager = new UpgradeManager(this);
    private SoundManager soundManager;

    private MachineStatus status = MachineStatus.IDLE;
    private Component errorMessage = Component.empty();

    //Set by recipes when processing
    @Nullable
    private MachineAppearance customAppearance = null;
    @Nullable
    private List<IGuiElement> customGuiElements = null;

    //Owner values
    @Nullable
    private Component ownerName;
    @Nullable
    private UUID ownerID;

    //Players currently interacting with this machine
    private List<WeakReference<ServerPlayer>> players = new ArrayList<>();

    public CustomMachineTile(BlockPos pos, BlockState state) {
        super(Registration.CUSTOM_MACHINE_TILE.get(), pos, state);
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
        this.processor = getMachine().getProcessorTemplate().build(this);
        this.componentManager = new MachineComponentManager(getMachine().getComponentTemplates(), this);
        this.componentManager.getComponents().values().forEach(IMachineComponent::init);
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
        if(this.isPaused())
            return MachineStatus.PAUSED;
        return this.status;
    }

    @Override
    public Component getMessage() {
        return this.errorMessage;
    }

    @Override
    public void setStatus(MachineStatus status, Component message) {
        if(this.status != status) {
            this.componentManager.getComponents().values().forEach(component -> component.onStatusChanged(this.status, status, message));
            this.status = status;
            this.errorMessage = message;
            this.setChanged();
            if(this.getLevel() != null && !this.getLevel().isClientSide()) {
                BlockPos pos = this.getBlockPos();
                new SUpdateMachineStatusPacket(pos, this.status).sendToChunkListeners(this.getLevel().getChunkAt(pos));
            }
        }
    }

    @Override
    public void refreshMachine(@Nullable ResourceLocation id) {
        if(this.level == null || this.level.isClientSide())
            return;
        CompoundTag craftingManagerNBT = this.processor.serialize();
        CompoundTag componentManagerNBT = this.componentManager.serializeNBT();
        if(id == null)
            id = getId();
        this.id = id;
        this.processor = getMachine().getProcessorTemplate().build(this);
        this.componentManager = new MachineComponentManager(getMachine().getComponentTemplates(), this);
        this.processor.deserialize(craftingManagerNBT);
        this.componentManager.deserializeNBT(componentManagerNBT);
        this.componentManager.getComponents().values().forEach(IMachineComponent::init);

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
        this.processor.reset();
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
    public IProcessor getProcessor() {
        return this.processor;
    }

    @Override
    public MachineAppearance getAppearance() {
        if(this.customAppearance != null)
            return this.customAppearance;
        return this.getMachine().getAppearance(getStatus());
    }

    @Override
    public void setCustomAppearance(@Nullable IMachineAppearance customAppearance) {
        if(this.customAppearance == customAppearance)
            return;

        this.customAppearance = (MachineAppearance) customAppearance;
        if(this.getLevel() != null && !this.getLevel().isClientSide()) {
            BlockPos pos = this.getBlockPos();
            new SUpdateMachineAppearancePacket(pos, this.customAppearance).sendToChunkListeners(this.getLevel().getChunkAt(pos));
        }
    }

    @Override
    public List<IGuiElement> getGuiElements() {
        if(this.customGuiElements != null && !this.customGuiElements.isEmpty())
            return this.customGuiElements;
        return this.getMachine().getGuiElements();
    }

    @Override
    public void setCustomGuiElements(@Nullable List<IGuiElement> customGuiElements) {
        if(this.customGuiElements == customGuiElements || (this.customGuiElements != null && customGuiElements != null && !customGuiElements.isEmpty() && new HashSet<>(this.customGuiElements).containsAll(customGuiElements)))
            return;
        this.customGuiElements = customGuiElements;
        if(this.getLevel() != null && !this.getLevel().isClientSide()) {
            BlockPos pos = this.getBlockPos();
            new SUpdateMachineGuiElementsPacket(pos, this.customGuiElements).sendToChunkListeners(this.getLevel().getChunkAt(pos));
            Iterator<WeakReference<ServerPlayer>> iterator = this.players.iterator();
            while(iterator.hasNext()) {
                ServerPlayer player = iterator.next().get();
                if(player == null || !(player.containerMenu instanceof CustomMachineContainer container) || container.getTile() != this) {
                    iterator.remove();
                    continue;
                }
                container.init();
            }
        }
    }

    @Override
    public void setOwner(LivingEntity entity) {
        if(entity == null)
            return;

        this.ownerName = entity.getName();
        this.ownerID = entity.getUUID();
    }

    @Nullable
    @Override
    public UUID getOwnerId() {
        return this.ownerID;
    }

    @Nullable
    @Override
    public Component getOwnerName() {
        return this.ownerName;
    }

    /** TileEntity Stuff **/

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomMachineTile tile) {
        if(tile.componentManager == null || tile.processor == null)
            return;

        level.getProfiler().push("Component tick");
        tile.componentManager.serverTick();
        level.getProfiler().pop();

        if(tile.isPaused())
            return;

        level.getProfiler().push("Crafting Manager tick");
        try {
            tile.processor.tick();
        } catch (ComponentNotFoundException e) {
            CustomMachinery.LOGGER.error(e.getMessage());
            tile.setPaused(true);
        }
        level.getProfiler().pop();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CustomMachineTile tile) {
        if(tile.componentManager == null || tile.processor == null)
            return;

        tile.componentManager.clientTick();

        if(tile.soundManager == null)
            tile.soundManager = new SoundManager(pos);
        if(!tile.getAppearance().getAmbientSound().getLocation().equals(tile.soundManager.getSoundID())) {
            if(tile.getAppearance().getAmbientSound() == Registration.AMBIENT_SOUND_PROPERTY.get().getDefaultValue())
                tile.soundManager.setSound(null);
            else
                tile.soundManager.setSound(tile.getAppearance().getAmbientSound());
        }

        if (!tile.soundManager.isPlaying())
            tile.soundManager.play();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        MachineList.addMachine(this);
        this.componentManager.getComponents().values().forEach(IMachineComponent::init);
    }

    @Override
    public void setRemoved() {
        if(this.level != null && this.level.isClientSide() && this.soundManager != null)
            this.soundManager.stop();

        if(this.level != null && !this.level.isClientSide())
            this.componentManager.getComponents().values().forEach(IMachineComponent::onRemoved);
        super.setRemoved();
    }

    private boolean unloaded = false;
    /**
     * Called when the chunk is unloaded.
     */
    public void unload() {
        this.unloaded = true;
    }

    public boolean isUnloaded() {
        return this.unloaded;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("machineID", this.id.toString());
        nbt.put("craftingManager", this.processor.serialize());
        nbt.put("componentManager", this.componentManager.serializeNBT());
        nbt.putString("status", this.status.toString());
        nbt.putString("message", TextComponentUtils.toJsonString(this.errorMessage));
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if(nbt.contains("machineID", Tag.TAG_STRING) && getMachine() == CustomMachine.DUMMY)
            this.setId(new ResourceLocation(nbt.getString("machineID")));

        if(nbt.contains("craftingManager", Tag.TAG_COMPOUND))
            this.processor.deserialize(nbt.getCompound("craftingManager"));

        if(nbt.contains("componentManager", Tag.TAG_COMPOUND))
            this.componentManager.deserializeNBT(nbt.getCompound("componentManager"));

        if(nbt.contains("status", Tag.TAG_STRING))
            this.setStatus(MachineStatus.value(nbt.getString("status")));

        if(nbt.contains("message", Tag.TAG_STRING))
            this.errorMessage = TextComponentUtils.fromJsonString(nbt.getString("message"));

        if(nbt.contains("appearance", Tag.TAG_COMPOUND))
            this.customAppearance = MachineAppearance.CODEC.read(NbtOps.INSTANCE, nbt.get("appearance")).result().map(MachineAppearance::new).orElse(null);

        if(nbt.contains("gui", Tag.TAG_LIST))
            this.customGuiElements = IGuiElement.CODEC.listOf().read(NbtOps.INSTANCE, nbt.getList("gui", Tag.TAG_COMPOUND)).result().orElse(Collections.emptyList());
    }

    //Needed for multiplayer sync
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.putString("machineID", getId().toString());
        nbt.putString("status", this.status.toString());
        nbt.putString("message", TextComponentUtils.toJsonString(this.errorMessage));
        if(this.customAppearance != null)
            MachineAppearance.CODEC.encodeStart(NbtOps.INSTANCE, this.customAppearance.getProperties()).result().ifPresent(appearance -> nbt.put("appearance", appearance));
        if(this.customGuiElements != null && !this.customGuiElements.isEmpty())
            IGuiElement.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.customGuiElements).result().ifPresent(elements -> nbt.put("gui", elements));
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
        if(this.processor instanceof ISyncableStuff syncableProcessor)
            syncableProcessor.getStuffToSync(container);
        this.componentManager.getStuffToSync(container);
        container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = MachineStatus.value(status)));
        container.accept(StringSyncable.create(() -> Component.Serializer.toJson(this.errorMessage), errorMessage -> this.errorMessage = Component.Serializer.fromJson(errorMessage)));
    }

    public void startInteracting(ServerPlayer player) {
        if(this.players.stream().noneMatch(ref -> ref.get() == player))
            this.players.add(new WeakReference<>(player));
    }

    public void stopInteracting(ServerPlayer player) {
        Iterator<WeakReference<ServerPlayer>> iterator = this.players.iterator();
        while(iterator.hasNext()) {
            ServerPlayer ref = iterator.next().get();
            if(ref == null || ref == player || !(ref.containerMenu instanceof CustomMachineContainer))
                iterator.remove();
        }
    }
}
