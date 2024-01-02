package fr.frinn.custommachinery.impl.integration.jei;

import net.minecraft.util.Mth;

public class Experience {
  private float xp;
  private final float capacity, maxIn, maxOut;
  private final double chance;
  private final boolean isPerTick;
  private final Form type;
  private int experienceLevel = 0;

  public Experience(float xp, float capacity, float maxIn, float maxOut, double chance, boolean isPerTick, Form type) {
    this.xp = xp;
    this.capacity = capacity;
    this.maxIn = maxIn;
    this.maxOut = maxOut;
    this.chance = chance;
    this.isPerTick = isPerTick;
    this.type = type;
  }

  public Experience(float xp, float capacity, float maxIn, float maxOut, Form type) {
    this(xp, capacity, maxIn, maxOut, 1D, false, type);
  }

  public Experience(float xp, float capacity, Form type) {
    this(xp, capacity, capacity, capacity, 1D, false, type);
  }

  public Experience(float xp, Form type) {
    this(xp, xp, xp, xp, 1D, false, type);
  }


  public Experience(float xp, float capacity, double chance, boolean isPerTick, Form type) {
    this(xp, capacity, capacity, capacity, chance, isPerTick, type);
  }

  public Experience(float xp, double chance, boolean isPerTick, Form type) {
    this(xp, xp, xp, xp, chance, isPerTick, type);
  }

  public Experience(float xp, float capacity, float maxIn, float maxOut, double chance, Form type) {
    this(xp, capacity, maxIn, maxOut, chance, false, type);
  }

  public Experience(float xp, float capacity, double chance, Form type) {
    this(xp, capacity, capacity, capacity, chance, false, type);
  }

  public Experience(float xp, double chance, Form type) {
    this(xp, xp, xp, xp, chance, false, type);
  }

  public Experience(float xp, float capacity, float maxIn, float maxOut, boolean isPerTick, Form type) {
    this(xp, capacity, maxIn, maxOut, 1D, isPerTick, type);
  }

  public Experience(float xp, float capacity, boolean isPerTick, Form type) {
    this(xp, capacity, capacity, capacity, 1D, isPerTick, type);
  }

  public Experience(float xp, boolean isPerTick, Form type) {
    this(xp, xp, xp, xp, 1D, isPerTick, type);
  }

  public float getXp() {
    return xp;
  }

  public int getLevels() {
    return experienceLevel;
  }

  public void setXp(float xp) {
    this.xp = xp;
    experienceLevel = getFromExperiencePoints(0);
  }

  public void addXp(float xp) {
    this.xp += xp;
    experienceLevel = getFromExperiencePoints(0);
  }

  public float getCapacity() {
    return capacity;
  }

  public float getMaxInput() {
    return maxIn;
  }

  public float getMaxOutput() {
    return maxOut;
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

  private int getFromExperiencePoints(int xpPoints) {
    float experienceProgress = (float)xpPoints / (float) this.getXpNeededForNextLevel(experienceLevel);
    xp = Mth.clamp(xp + xpPoints, 0, Integer.MAX_VALUE);

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
