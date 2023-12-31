package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.*;
import fr.frinn.custommachinery.api.network.ISyncable;
import fr.frinn.custommachinery.api.network.ISyncableStuff;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.syncable.FloatSyncable;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ExperienceMachineComponent extends AbstractMachineComponent implements ITickableComponent, ISerializableComponent, ISyncableStuff {
  private float xp;
  private final float capacity;
  private final float maxInput;
  private final float maxOutput;
  private final String id;
  private final boolean retrieveFromSlots;
  private final List<String> slotIds;

  public ExperienceMachineComponent(IMachineComponentManager manager, float capacity, float maxInput, float maxOutput, String id, boolean retrieveFromSlots, List<String> slotIds) {
    super(manager, ComponentIOMode.BOTH);
    this.xp = 0;
    this.capacity = capacity;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.id = id;
    this.retrieveFromSlots = retrieveFromSlots;
    this.slotIds = slotIds;
  }

  public String getId(){
    return this.id;
  }

  public float getMaxInput() {
    return this.maxInput;
  }

  public float getMaxOutput() {
    return this.maxOutput;
  }

  // For GUI element rendering
  public float getXp() {
    return this.xp;
  }

  public float getCapacity() {
    return this.capacity;
  }

  public void setXp(float xp) {
    this.xp = xp;
    getManager().markDirty();
  }

  public float receiveXp(float maxReceive, boolean simulate) {
    if (this.getMaxInput() <= 0) return 0;
    float xpReceived = Math.min(this.getCapacity() - this.getXp(), Math.min(this.getMaxInput(), maxReceive));
    if (!simulate && xpReceived > 0) {
      this.setXp(this.getXp() + xpReceived);
      this.getManager().markDirty();
    }

    return xpReceived;
  }

  public float extractXp(float maxExtract, boolean simulate) {
    if (this.getMaxOutput() <= 0) return 0;
    float xpExtracted = Math.min(this.getXp(), Math.min(this.getMaxOutput(), maxExtract));
    if (!simulate && xpExtracted > 0) {
      this.setXp(this.getXp() - xpExtracted);
      this.getManager().markDirty();
    }

    return xpExtracted;
  }

  @Override
  public MachineComponentType<?> getType() {
    return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
  }

  @Override
  public void serialize(CompoundTag nbt) {
    nbt.putFloat("xp", this.xp);
  }

  @Override
  public void deserialize(CompoundTag nbt) {
    if (nbt.contains("xp", Tag.TAG_FLOAT))
      this.xp = Math.min(nbt.getFloat("xp"), this.capacity);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(FloatSyncable.create(() -> this.xp, xp -> this.xp = xp));
  }

  /** Recipe Stuff **/

  public float receiveRecipeXp(float maxReceive, boolean simulate) {
    float xpReceived = Math.min(this.capacity - this.xp, maxReceive);
    if(!simulate) {
      receiveXp(xpReceived, false);
    }
    return xpReceived;
  }

  public float extractRecipeXp(float maxExtract, boolean simulate) {
    float xpExtracted = Math.min(this.xp, maxExtract);
    if (!simulate) {
      extractXp(xpExtracted, false);
    }
    return xpExtracted;
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
        NamedCodec.STRING.fieldOf("id").forGetter(template -> template.id),
        NamedCodec.floatRange(1, Float.MAX_VALUE).fieldOf("capacity").forGetter(template -> template.capacity),
        NamedCodec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("maxInput").forGetter(template -> Optional.of(template.maxInput)),
        NamedCodec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("maxOutput").forGetter(template -> Optional.of(template.maxOutput)),
        NamedCodec.BOOL.optionalFieldOf("retrieveFromSlots").forGetter(template -> Optional.of(template.retrieveFromSlots)),
        NamedCodec.STRING.listOf().optionalFieldOf("retrieveSlotsId").forGetter(template -> Optional.of(template.slotIds))
      ).apply(templateInstance, (id, capacity, maxInput, maxOutput, retrieveFromSlots, slotIds) ->
          new ExperienceMachineComponent.Template(capacity, maxInput.orElse(capacity), maxOutput.orElse(capacity), id, retrieveFromSlots.orElse(true), slotIds.orElse(Lists.newArrayList()))
      ),
      "Experience machine component"
    );

    private final float capacity;
    private final float maxInput;
    private final float maxOutput;
    private final String id;
    private final boolean retrieveFromSlots;
    private final List<String> slotIds;

    public Template(float capacity, float maxInput, float maxOutput, String id, boolean retrieveFromSlots, List<String> slotIds) {
      this.capacity = capacity;
      this.maxInput = maxInput;
      this.maxOutput = maxOutput;
      this.id = id;
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
      return new ExperienceMachineComponent(manager, capacity, maxInput, maxOutput, id, retrieveFromSlots, slotIds);
    }
  }
}
