package fr.frinn.custommachinery.common.data.component;

import net.minecraft.nbt.CompoundNBT;

import java.util.Locale;

public interface IMachineComponent {

    MachineComponentType getType();

    void serialize(CompoundNBT nbt);

    void deserialize(CompoundNBT nbt);

    enum Mode {

        INPUT(true, false),
        OUTPUT(false, true),
        BOTH(true, true),
        NONE(false, false);

        private boolean isInput;
        private boolean isOutput;

        Mode(boolean isInput, boolean isOutput) {
            this.isInput = isInput;
            this.isOutput = isOutput;
        }

        public boolean isInput() {
            return this.isInput;
        }

        public boolean isOutput() {
            return this.isOutput;
        }

        public static Mode value(String value) {
            return Mode.valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
