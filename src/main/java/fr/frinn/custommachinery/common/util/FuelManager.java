package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.init.CustomMachineTile;

public class FuelManager {

    private CustomMachineTile tile;
    private int fuel;
    private int maxFuel;

    public FuelManager(CustomMachineTile tile) {
        this.tile = tile;
    }

    public int getFuel() {
        return this.fuel;
    }

    public int getMaxFuel() {
        return this.maxFuel;
    }

    public void addFuel(int fuel) {
        this.fuel += fuel;
        this.maxFuel = fuel;
    }

    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public boolean consume() {
        if(this.fuel > 0) {
            this.fuel--;
            return true;
        }
        return false;

    }
}
