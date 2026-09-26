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
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.MachineComponentManager;
import fr.frinn.custommachinery.common.crafting.DummyProcessor;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.network.SRefreshCustomMachineTilePacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineAppearancePacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineGuiElementsPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachineStatusPacket;
import fr.frinn.custommachinery.common.network.syncable.StringSyncable;
import fr.frinn.custommachinery.common.upgrade.UpgradeManager;
import fr.frinn.custommachinery.common.util.MachineList;
import fr.frinn.custommachinery.common.util.sound.SoundManager;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomMachineTile extends MachineTile implements ISyncableStuff {

    public static final Identifier DUMMY = Identifier.fromNamespaceAndPath(CustomMachinery.MODID, "dummy");

    private Identifier id = DUMMY;
    private boolean paused = false;

    private IProcessor processor = new DummyProcessor(this);
    private MachineComponentManager componentManager = new DummyComponentManager(this);
    private final UpgradeManager upgradeManager = new UpgradeManager(this);

    @Nullable
    private SoundManager soundManager;

    private MachineStatus status = MachineStatus.IDLE;
    private Component errorMessage = Component.empty();

    //Set by recipes when processing
    @Nullable
    private MachineAppearance customAppearance = null;
    private List<IGuiElement> customGuiElements = Collections.emptyList();

    //Owner values
    @Nullable
    private Component ownerName;
    @Nullable
    private UUID ownerID;

    //Players currently interacting with this machine
    private final List<WeakReference<ServerPlayer>> players = new ArrayList<>();

    public CustomMachineTile(BlockPos pos, BlockState state) {
        super(CMRegistration.CUSTOM_MACHINE_TILE.get(), pos, state);
    }

    public void setId(Identifier id) {
        this.id = id;
        this.processor = getMachine().getProcessorTemplate().build(this);
        this.componentManager = new MachineComponentManager(getMachine().getComponentTemplates(), this);
        if(this.getLevel() != null) //In case of null level (loading machine BE from nbt) init will be done in setLevel which happen after setId
            this.componentManager.getComponents().values().forEach(IMachineComponent::init);
        this.upgradeManager.refresh();
    }

    /** MachineTile Implementation **/

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public CustomMachine getMachine() {
        CustomMachine machine = CustomMachinery.MACHINES.get(getId());
        return Objects.requireNonNullElse(machine, CustomMachine.DUMMY);
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
            if(this.getLevel() instanceof ServerLevel serverLevel) {
                BlockPos pos = this.getBlockPos();
                serverLevel.updateNeighborsAt(pos, this.getBlockState().getBlock());
                PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(pos), new SUpdateMachineStatusPacket(pos, this.status));
            }
        }
    }

    @Override
    public void refreshMachine(@Nullable Identifier id) {
        if(!(this.getLevel() instanceof ServerLevel serverLevel))
            return;

        //Reset the old processor before creating a new one, for clearing result slot in case of craft processor.
        this.processor.reset();

        TagValueOutput craftingManagerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
        this.processor.serialize(craftingManagerOutput);

        TagValueOutput componentManagerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
        this.componentManager.serialize(componentManagerOutput);

        //For invalidating caps on Forge
        this.invalidateCapabilities();

        if(id == null)
            id = getId();
        this.id = id;

        this.processor = getMachine().getProcessorTemplate().build(this);
        this.componentManager = new MachineComponentManager(getMachine().getComponentTemplates(), this);
        this.processor.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), craftingManagerOutput.buildResult()));
        this.componentManager.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), componentManagerOutput.buildResult()));
        this.componentManager.getComponents().values().forEach(IMachineComponent::init);
        this.upgradeManager.refresh();

        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(this.worldPosition), new SRefreshCustomMachineTilePacket(this.worldPosition, id));
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
        if(this.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = this.getBlockPos();
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(pos), new SUpdateMachineAppearancePacket(pos, this.customAppearance));
        }
    }

    @Override
    public List<IGuiElement> getGuiElements() {
        if(!this.customGuiElements.isEmpty())
            return this.customGuiElements;
        return this.getMachine().getGuiElements();
    }

    @Override
    public void setCustomGuiElements(List<IGuiElement> customGuiElements) {
        if(this.customGuiElements == customGuiElements || (!customGuiElements.isEmpty() && new HashSet<>(this.customGuiElements).containsAll(customGuiElements)))
            return;
        this.customGuiElements = customGuiElements;
        if(this.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = this.getBlockPos();
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(pos), new SUpdateMachineGuiElementsPacket(pos, this.customGuiElements));
            this.refreshMachineContainer();
        }
    }

    @Override
    public void refreshMachineContainer() {
        Iterator<WeakReference<ServerPlayer>> iterator = this.players.iterator();
        while(iterator.hasNext()) {
            ServerPlayer player = iterator.next().get();
            if(player == null || !(player.containerMenu instanceof CustomMachineContainer container) || container.getTile() != this) {
                iterator.remove();
                continue;
            }
            CustomMachineContainer.open(player, this);
        }
    }

    @Override
    public void setOwner(LivingEntity entity) {
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

        tile.componentManager.serverTick();

        if(tile.isPaused())
            return;

        try {
            tile.processor.tick();
        } catch (ComponentNotFoundException e) {
            CustomMachinery.LOGGER.error(e.getMessage());
            tile.setPaused(true);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CustomMachineTile tile) {

        tile.componentManager.clientTick();

        if(tile.soundManager == null)
            tile.soundManager = new SoundManager(pos);
        if(!tile.soundManager.isCurrentlyPlaying(tile.getAppearance().getAmbientSound())) {
            if(tile.getAppearance().getAmbientSound() == CMRegistration.AMBIENT_SOUND_PROPERTY.get().getDefaultValue())
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
        if(this.getId() != DUMMY) //In case of dummy machine (just placed) init will happen in setId after setLevel
            this.componentManager.getComponents().values().forEach(IMachineComponent::init);
        this.upgradeManager.refresh();
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
    public void setChanged() {
        if(this.level != null)
            this.level.blockEntityChanged(this.worldPosition);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("machineID", this.id.toString());
        this.processor.serialize(output.child("craftingManager"));
        this.componentManager.serialize(output.child("componentManager"));
        output.putString("status", this.status.toString());
        output.store("message", ComponentSerialization.CODEC, this.errorMessage);
        if(this.ownerID != null)
            output.putString("ownerID", this.ownerID.toString());
        output.storeNullable("ownerName", ComponentSerialization.CODEC, this.ownerName);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if(getMachine() == CustomMachine.DUMMY)
            input.getString("machineID").ifPresent(id -> this.setId(Identifier.parse(id)));
        input.child("craftingManager").ifPresent(this.processor::deserialize);
        input.child("componentManager").ifPresent(this.componentManager::deserialize);
        input.getString("status").ifPresent(status -> this.setStatus(MachineStatus.value(status)));
        this.errorMessage = input.read("message", ComponentSerialization.CODEC).orElse(Component.empty());
        this.ownerID = input.getString("ownerID").map(UUID::fromString).orElse(null);
        this.ownerName = input.read("ownerName", ComponentSerialization.CODEC).orElse(null);

        this.customAppearance = input.read("appearance", MachineAppearance.CODEC.codec()).map(MachineAppearance::new).orElse(null);
        this.customGuiElements = input.read("gui", IGuiElement.CODEC.listOf().codec()).orElse(Collections.emptyList());
    }

    //Needed for multiplayer sync
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        nbt.putString("machineID", getId().toString());
        nbt.putString("status", this.status.toString());
        nbt.putString("message", TextComponentUtils.toJSON(this.errorMessage));
        if(this.ownerID != null)
            nbt.putString("ownerID", this.ownerID.toString());
        if(this.ownerName != null)
            nbt.putString("ownerName", TextComponentUtils.toJSON(this.ownerName));
        if(this.customAppearance != null)
            MachineAppearance.CODEC.encodeStart(NbtOps.INSTANCE, this.customAppearance.properties()).result().ifPresent(appearance -> nbt.put("appearance", appearance));
        if(!this.customGuiElements.isEmpty())
            IGuiElement.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.customGuiElements).result().ifPresent(elements -> nbt.put("gui", elements));
        return nbt;
    }

    //Needed for multiplayer sync
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void refreshClientData() {
        requestModelDataUpdate();
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        this.loadAdditional(input);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(ClientHandler.APPEARANCE, getAppearance().copy())
                .with(ClientHandler.STATUS, getStatus())
                .build();
    }

    private boolean unloaded = false;

    public boolean isUnloaded() {
        return this.unloaded;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.unloaded = true;
    }

    /**CONTAINER STUFF**/

    @Override
    public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
        if(this.getLevel() == null)
            return;
        if(this.processor instanceof ISyncableStuff syncableProcessor)
            syncableProcessor.getStuffToSync(container);
        RegistryAccess registries = this.getLevel().registryAccess();
        this.componentManager.getStuffToSync(container);
        container.accept(StringSyncable.create(() -> this.status.toString(), status -> this.status = MachineStatus.value(status)));
        container.accept(StringSyncable.create(() -> TextComponentUtils.toJSON(this.errorMessage), errorMessage -> this.errorMessage = TextComponentUtils.fromJSON(errorMessage)));
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
