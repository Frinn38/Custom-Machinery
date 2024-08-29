package fr.frinn.custommachinery.impl.integration.jei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public record Experience(int xp, int capacity, double chance, boolean isPerTick, Form type) {

  public static final Codec<Experience> CODEC = RecordCodecBuilder.create(experienceInstance ->
          experienceInstance.group(
                  Codec.INT.fieldOf("xp").forGetter(Experience::xp),
                  Codec.INT.fieldOf("capacity").forGetter(Experience::capacity),
                  Codec.DOUBLE.fieldOf("chance").forGetter(Experience::chance),
                  Codec.BOOL.fieldOf("perTick").forGetter(Experience::isPerTick),
                  StringRepresentable.fromEnum(Form::values).fieldOf("form").forGetter(Experience::type)
          ).apply(experienceInstance, Experience::new)
  );

  public Experience(int xp, double chance, boolean isPerTick, Form type) {
    this(xp, xp, chance, isPerTick, type);
  }

  public int getLevels() {
      int experienceLevel = 0;
      return experienceLevel;
  }

  public boolean isLevels() {
    return type == Form.LEVEL;
  }

  public boolean isPoints() {
    return type == Form.POINT;
  }

  private int getXpNeededForNextLevel(int experienceLevel) {
    if (experienceLevel >= 30) {
      return 112 + (experienceLevel - 30) * 9;
    } else {
      return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
    }
  }

  private int getFromExperiencePoints(int xp) {
    int experienceLevel = 0;
    xp = Mth.clamp(xp, 0, Integer.MAX_VALUE);
    float experienceProgress = (float) xp / (float) this.getXpNeededForNextLevel(experienceLevel);

    while(experienceProgress < 0.0F) {
      float f = experienceProgress * (float) this.getXpNeededForNextLevel(experienceLevel);
      if (experienceLevel > 0) {
        experienceLevel -= 1;
        experienceProgress = 1.0F + f / (float) this.getXpNeededForNextLevel(experienceLevel);
      } else {
        experienceLevel -= 1;
        experienceProgress = 0.0F;
      }
    }

    while(experienceProgress >= 1.0F) {
      experienceProgress = (experienceProgress - 1.0F) * (float) this.getXpNeededForNextLevel(experienceLevel);
      experienceLevel += 1;
      experienceProgress /= (float) this.getXpNeededForNextLevel(experienceLevel);
    }
    return experienceLevel;
  }

  public enum Form implements StringRepresentable {
    LEVEL, POINT;

    public boolean isLevel() {
      return this == LEVEL;
    }

    public boolean isPoint() {
      return this == POINT;
    }

    @Override
    public String getSerializedName() {
      return this.name().toLowerCase(Locale.ROOT);
    }
  }
}
