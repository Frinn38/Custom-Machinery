package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.FloatSyncable;
import fr.frinn.custommachinery.common.network.syncable.IntegerSyncable;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ExperienceMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff {
  private float xp;
  private final float capacity;
  private final boolean retrieveFromSlots;
  private final List<String> slotIds;
  private final int capacityLevels;
  private int xpLevels;

  public ExperienceMachineComponent(IMachineComponentManager manager, float capacity, boolean retrieveFromSlots, List<String> slotIds) {
    super(manager, ComponentIOMode.BOTH);
    this.xp = 0;
    this.capacity = capacity;
    this.retrieveFromSlots = retrieveFromSlots;
    this.slotIds = slotIds;
    this.capacityLevels = getLevels(capacity);
    this.xpLevels = getLevels(xp);
  }
  private int getLevels(float xp) {
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
  public float getXp() {
    return this.xp;
  }

  public float getCapacity() {
    return this.capacity;
  }

  public int getLevels() {
    return this.xpLevels;
  }

  public int getCapacityLevels() {
    return this.capacityLevels;
  }

  public void setXp(float xp) {
    this.xp = xp;
    this.xpLevels = getLevels(xp);
    getManager().markDirty();
  }

  public float receiveXp(float maxReceive, boolean simulate) {
    float xpReceived = Math.min(this.getCapacity() - this.getXp(), maxReceive);
    if (!simulate && xpReceived > 0) {
      this.setXp(this.getXp() + xpReceived);
    }
    return xpReceived;
  }

  public void receiveLevel(float levels, boolean simulate) {
    float toReceive = 0;
    for (int i = 0; i < levels; i++) {
      toReceive += getXpNeededForNextLevel(i);
    }
    receiveXp(toReceive, simulate);
  }

  public float extractXp(float maxExtract, boolean simulate) {
    float xpExtracted = Math.min(this.getXp(), maxExtract);
    if (!simulate && xpExtracted > 0) {
      this.setXp(this.getXp() - xpExtracted);
    }
    return xpExtracted;
  }

  public void extractLevel(float levels, boolean simulate) {
    float toExtract = 0;
    for (int i = 0; i < levels; i++) {
      toExtract += getXpNeededForNextLevel(i);
    }
    extractXp(toExtract, simulate);
  }

  @Override
  public MachineComponentType<?> getType() {
    return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
  }

  @Override
  public void serialize(CompoundTag nbt) {
    nbt.putFloat("xp", this.xp);
    nbt.putInt("levels", this.xpLevels);
  }

  @Override
  public void deserialize(CompoundTag nbt) {
    if (nbt.contains("xp", Tag.TAG_FLOAT))
      this.xp = Math.min(nbt.getFloat("xp"), this.capacity);
    if (nbt.contains("levels", Tag.TAG_INT))
      this.xpLevels = Math.min(nbt.getInt("levels"), this.capacityLevels);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(FloatSyncable.create(() -> this.xp, xp -> this.xp = xp));
    container.accept(IntegerSyncable.create(() -> this.xpLevels, xpLevels -> this.xpLevels = xpLevels));
  }

  /** Recipe Stuff **/

  public float receiveRecipeXp(float maxReceive, boolean simulate) {
    float xpReceived = Math.min(this.capacity - this.xp, maxReceive);
    if(!simulate) {
      receiveXp(xpReceived, false);
    }
    return xpReceived;
  }

  public float receiveRecipeLevel(float amount, boolean simulate) {
    float levelsReceived = Math.min(this.capacityLevels - this.xpLevels, amount);
    if (!simulate) {
      receiveLevel(levelsReceived, false);
    }
    return levelsReceived;
  }

  public float extractRecipeXp(float maxExtract, boolean simulate) {
    float xpExtracted = Math.min(this.xp, maxExtract);
    if (!simulate) {
      extractXp(xpExtracted, false);
    }
    return xpExtracted;
  }

  public float extractRecipeLevel(float amount, boolean simulate) {
    float levelsExtracted = Math.min(capacityLevels, amount);
    if (!simulate) {
      extractLevel(levelsExtracted, false);
    }
    return levelsExtracted;
  }

  public boolean canRetrieveFromSlots() {
    return retrieveFromSlots;
  }

  public List<String> slotsFromCanRetrieve() {
    return slotIds;
  }

  public static class Template implements IMachineComponentTemplate<ExperienceMachineComponent> {
    public static final NamedCodec<Template> CODEC = NamedCodec.record(templateInstance ->
      templateInstance.group(
        NamedCodec.floatRange(1, Float.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
        NamedCodec.BOOL.optionalFieldOf("retrieveFromSlots").forGetter(template -> Optional.of(template.retrieveFromSlots)),
        NamedCodec.STRING.listOf().optionalFieldOf("retrieveSlotsId").forGetter(template -> Optional.of(template.slotIds))
      ).apply(templateInstance, (capacity, retrieveFromSlots, slotIds) ->
          new ExperienceMachineComponent.Template(capacity, retrieveFromSlots.orElse(true), slotIds.orElse(Lists.newArrayList()))
      ),
      "Experience machine component"
    );

    private final float capacity;
    private final boolean retrieveFromSlots;
    private final List<String> slotIds;

    public Template(float capacity, boolean retrieveFromSlots, List<String> slotIds) {
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
