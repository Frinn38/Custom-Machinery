package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.ISerializableComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.function.Consumer;

public class ExperienceMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff {
  private int xp;
  private final int capacity;
  private final boolean retrieveFromSlots;
  private final List<String> slotIds;
  private final int capacityLevels;
  private int xpLevels;

  public ExperienceMachineComponent(IMachineComponentManager manager, int capacity, boolean retrieveFromSlots, List<String> slotIds) {
    super(manager, ComponentIOMode.BOTH);
    this.xp = 0;
    this.capacity = capacity;
    this.retrieveFromSlots = retrieveFromSlots;
    this.slotIds = slotIds;
    this.capacityLevels = getLevels(capacity);
    this.xpLevels = getLevels(xp);
  }

  private int getLevels(int xp) {
    int levels = 0;
    xp = Mth.clamp(xp, 0, Integer.MAX_VALUE);
    float experienceProgress = xp / (float) this.getXpNeededForNextLevel(levels);

    while(experienceProgress < 0.0F) {
      float f = experienceProgress * (float) getXpNeededForNextLevel(levels);
      if (levels > 0) {
        levels -= 1;
        experienceProgress = 1.0F + f / (float) getXpNeededForNextLevel(levels);
      } else {
        levels -= 1;
        experienceProgress = 0.0F;
      }
    }

    while(experienceProgress >= 1.0F) {
      experienceProgress = (experienceProgress - 1.0F) * (float) getXpNeededForNextLevel(levels);
      levels += 1;
      experienceProgress /= (float) getXpNeededForNextLevel(levels);
    }

    return levels;
  }

  private int getXpNeededForNextLevel(int experienceLevel) {
    if (experienceLevel >= 30) {
      return 112 + (experienceLevel - 30) * 9;
    } else {
      return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
    }
  }

  // For GUI element rendering
  public int getXp() {
    return this.xp;
  }

  public int getCapacity() {
    return this.capacity;
  }

  public int getLevels() {
    return this.xpLevels;
  }

  public int getCapacityLevels() {
    return this.capacityLevels;
  }

  public void setXp(int xp) {
    this.xp = xp;
    this.xpLevels = getLevels(xp);
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
    for (int i = xpLevels; i < xpLevels + levels; i++) {
      toReceive += getXpNeededForNextLevel(i);
    }
    int prevLevels = xpLevels;
    int received = receiveXp(toReceive, simulate);
    return (received == toReceive) ? levels : prevLevels + getLevels(received);
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
    for (int i = xpLevels; i > xpLevels - levels; i--) {
      toExtract += getXpNeededForNextLevel(i);
    }
    int prevLevels = xpLevels;
    int extracted = extractXp(toExtract, simulate);
    return (extracted == toExtract) ? levels : prevLevels - getLevels(extracted);
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
      this.xp = Math.min(nbt.getInt("xp"), this.capacity);
    if (nbt.contains("levels", Tag.TAG_INT))
      this.xpLevels = Math.min(nbt.getInt("levels"), this.capacityLevels);
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

  public void receiveLevelFromPlayer(int levels, ServerPlayer player) {
    int pointsToExtract = 0;
    for (int i = xpLevels; i < (xpLevels + levels); i++) {
      pointsToExtract += getXpNeededForNextLevel(i);
    }
    if (pointsToExtract > player.totalExperience)
      pointsToExtract = player.totalExperience;
    if (this.receiveXp(pointsToExtract, true) == pointsToExtract) {
      this.receiveXp(pointsToExtract, false);
      player.giveExperiencePoints(-pointsToExtract);
    }
  }

  public void receiveLevelFromPlayer(ServerPlayer player) {
    player.giveExperiencePoints(-(capacity - xp));
    receiveXp(capacity - xp, false);
  }

  public void giveLevelToPlayer(int levels, ServerPlayer player) {
    int pointsToExtract = 0;
    for (int i = xpLevels; i > (xpLevels - levels); i--) {
      pointsToExtract += getXpNeededForNextLevel(i);
    }
    if (pointsToExtract > xp)
      pointsToExtract = xp;
    if (this.extractXp(pointsToExtract, true) == pointsToExtract) {
      this.extractXp(pointsToExtract, false);
      player.giveExperiencePoints(pointsToExtract);
    }
  }

  public void giveLevelToPlayer(ServerPlayer player) {
    player.giveExperiencePoints(xp);
    extractXp(xp, false);
  }

  public static class Template implements IMachineComponentTemplate<ExperienceMachineComponent> {
    public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
      templateInstance.group(
        NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
        NamedCodec.BOOL.optionalFieldOf("retrieveFromSlots", true).forGetter(template -> template.retrieveFromSlots),
        NamedCodec.STRING.listOf().optionalFieldOf("retrieveSlotsId", Lists.newArrayList()).forGetter(template -> template.slotIds)
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
      return new ExperienceMachineComponent(manager, capacity, retrieveFromSlots, slotIds);
    }
  }
}
