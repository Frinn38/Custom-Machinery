package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.common.util.ExperienceUtils;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ExperienceMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff {

  private final int capacity;
  private final int capacityLevels;
  private final boolean retrieveFromSlots;
  private final List<String> slotIds;
  private int xp = 0;
  private int xpLevels = 0;

  public ExperienceMachineComponent(IMachineComponentManager manager, int capacity, boolean retrieveFromSlots, List<String> slotIds) {
    super(manager, ComponentIOMode.BOTH);
    this.capacity = capacity;
    this.capacityLevels = ExperienceUtils.getLevelFromXp(capacity);
    this.retrieveFromSlots = retrieveFromSlots;
    this.slotIds = slotIds;
  }

  // For GUI element rendering
  public int getXp() {
    return this.xp;
  }

  public int getLevels() {
    return this.xpLevels;
  }

  public int getCapacity() {
    return this.capacity;
  }

  public int getCapacityLevels() {
    return this.capacityLevels;
  }

  public void setXp(int xp) {
    this.xp = Mth.clamp(xp, 0, this.capacity);
    this.xpLevels = ExperienceUtils.getLevelFromXp(xp);
    getManager().markDirty();
  }

  public int receiveXp(int maxReceive, boolean simulate) {
    int xpReceived = Math.min(this.getCapacity() - this.getXp(), maxReceive);
    if (!simulate && xpReceived > 0) {
      this.setXp(this.getXp() + xpReceived);
    }
    return xpReceived;
  }

  public int receiveLevel(int levels, boolean simulate) {
    int toReceive = 0;
    for (int i = this.xpLevels; i < this.xpLevels + levels; i++) {
      toReceive += ExperienceUtils.getXpNeededForNextLevel(i);
    }
    int prevLevels = this.xpLevels;
    int received = receiveXp(toReceive, simulate);
    return (received == toReceive) ? levels : prevLevels + ExperienceUtils.getLevelFromXp(received);
  }

  public int extractXp(int maxExtract, boolean simulate) {
    int xpExtracted = Math.min(this.getXp(), maxExtract);
    if (!simulate && xpExtracted > 0) {
      this.setXp(this.getXp() - xpExtracted);
    }
    return xpExtracted;
  }

  public int extractLevel(int levels, boolean simulate) {
    int toExtract = 0;
    for (int i = this.xpLevels; i > this.xpLevels - levels; i--) {
      toExtract += ExperienceUtils.getXpNeededForNextLevel(i);
    }
    int prevLevels = this.xpLevels;
    int extracted = extractXp(toExtract, simulate);
    return (extracted == toExtract) ? levels : prevLevels - ExperienceUtils.getLevelFromXp(extracted);
  }

  @Override
  public MachineComponentType<?> getType() {
    return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
  }

  @Override
  public void serialize(CompoundTag nbt) {
    nbt.putInt("xp", this.xp);
    nbt.putInt("levels", this.xpLevels);
  }

  @Override
  public void deserialize(CompoundTag nbt) {
    if (nbt.contains("xp", Tag.TAG_INT))
      this.xp = nbt.getInt("xp");
    if (nbt.contains("levels", Tag.TAG_INT))
      this.xpLevels = Math.min(nbt.getInt("levels"), this.capacity);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(IntegerSyncable.create(() -> this.xp, xp -> this.xp = xp));
    container.accept(IntegerSyncable.create(() -> this.xpLevels, xpLevels -> this.xpLevels = xpLevels));
  }

  public boolean canRetrieveFromSlots() {
    return retrieveFromSlots;
  }

  public List<String> slotsFromCanRetrieve() {
    return slotIds;
  }

  //levelDiff positive = give to player, negative = take from player
  public void addLevelToPlayer(int levelDiff, Player player) {
    int requestedLevel = player.experienceLevel + levelDiff;
    requestedLevel = Math.max(requestedLevel, 0);
    int playerXP = ExperienceUtils.getPlayerTotalXp(player);
    int requestedXP = ExperienceUtils.getXpFromLevel(requestedLevel) - playerXP;
    int awardXP = levelDiff > 0 ? Math.min(this.xp, requestedXP) : -Math.min(Math.abs(requestedXP), this.capacity - this.xp);
    awardXP(awardXP, player);
  }

  public void addAllLevelToPlayer(boolean give, Player player) {
    int awardXP;
    if (give) {
      awardXP = this.xp;
    } else {
      awardXP = -Math.min(ExperienceUtils.getPlayerTotalXp(player), this.capacity - this.xp);
    }
    awardXP(awardXP, player);
  }

  public void awardXP(int xp, Player player) {
      this.setXp(this.xp - xp);
      player.giveExperiencePoints(xp);
  }

  public static class Template implements IMachineComponentTemplate<ExperienceMachineComponent> {
    public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
      templateInstance.group(
        NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
        NamedCodec.BOOL.optionalFieldOf("retrieve", false).forGetter(template -> template.retrieveFromSlots),
        NamedCodec.STRING.listOf().optionalFieldOf("slots", Collections.emptyList()).aliases("slot").forGetter(template -> template.slotIds)
      ).apply(templateInstance, Template::new),
      "Experience machine component"
    );

    private final int capacity;
    private final boolean retrieveFromSlots;
    private final List<String> slotIds;

    public Template(int capacity, boolean retrieveFromSlots, List<String> slotIds) {
      this.capacity = capacity;
      this.retrieveFromSlots = retrieveFromSlots;
      this.slotIds = slotIds;
    }

    @Override
    public MachineComponentType<ExperienceMachineComponent> getType() {
      return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
    }

    @Override
    public String getId() {
      return "";
    }

    @Override
    public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
      return ingredient instanceof Float;
    }

    @Override
    public ExperienceMachineComponent build(IMachineComponentManager manager) {
      return new ExperienceMachineComponent(manager, this.capacity, this.retrieveFromSlots, this.slotIds);
    }
  }
}
