package fr.frinn.custommachinery.impl.integration.jei;

public interface IExperienceStorage {

  float getXp();
  void setXp(float xp);
  void addXp(float xp);

  float getCapacity();
  float getMaxInput();
  float getMaxOutput();
}
