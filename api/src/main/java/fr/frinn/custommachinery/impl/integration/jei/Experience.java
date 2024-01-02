package fr.frinn.custommachinery.impl.integration.jei;

import net.minecraft.util.Mth;

public class Experience {
  private int xp;
  private final int capacity;
  private final double chance;
  private final boolean isPerTick;
  private final Form type;
  private int experienceLevel = 0;

  public Experience(int xp, int capacity, double chance, boolean isPerTick, Form type) {
    this.xp = xp;
    this.capacity = capacity;
    this.chance = chance;
    this.isPerTick = isPerTick;
    this.type = type;
  }

  public Experience(int xp, int capacity, Form type) {
    this(xp, capacity, 1D, false, type);
  }

  public Experience(int xp, Form type) {
    this(xp, xp, 1D, false, type);
  }

  public Experience(int xp, double chance, boolean isPerTick, Form type) {
    this(xp, xp, chance, isPerTick, type);
  }

  public Experience(int xp, int capacity, double chance, Form type) {
    this(xp, capacity, chance, false, type);
  }

  public Experience(int xp, double chance, Form type) {
    this(xp, xp, chance, false, type);
  }

  public Experience(int xp, int capacity, boolean isPerTick, Form type) {
    this(xp, capacity, 1D, isPerTick, type);
  }

  public Experience(int xp, boolean isPerTick, Form type) {
    this(xp, xp, 1D, isPerTick, type);
  }

  public int getXp() {
    return xp;
  }

  public int getLevels() {
    return experienceLevel;
  }

  public void setXp(int xp) {
    this.xp = xp;
    experienceLevel = getFromExperiencePoints(this.xp);
  }

  public void addXp(int xp) {
    this.xp += xp;
    experienceLevel = getFromExperiencePoints(this.xp);
  }

  public int getCapacity() {
    return capacity;
  }

  public double getChance() {
    return chance;
  }

  public boolean isPerTick() {
    return isPerTick;
  }

  public boolean isLevels() {
    return type == Form.LEVEL;
  }

  public boolean isPoints() {
    return type == Form.POINT;
  }

  public Form getForm() {
    return type;
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

  public enum Form {
    LEVEL, POINT;

    public boolean isLevel() {
      return this == LEVEL;
    }

    public boolean isPoint() {
      return this == POINT;
    }
  }
}
