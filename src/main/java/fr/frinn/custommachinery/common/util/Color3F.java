package fr.frinn.custommachinery.common.util;

public class Color3F {

    private float red;
    private float green;
    private float blue;

    private Color3F(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Color3F of(float red, float green, float blue) {
        return new Color3F(red, green, blue);
    }

    public static Color3F of(int red, int green, int blue) {
        return new Color3F(red / 255F, green / 255F, blue / 255F);
    }

    public static Color3F of(int colorARGB) {
        return new Color3F(((colorARGB >> 16) & 0xFF) / 255F, ((colorARGB >> 8) & 0xFF) / 255F, ((colorARGB >> 0) & 0xFF) / 255F);
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }
}
