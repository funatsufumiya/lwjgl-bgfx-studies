package sketch.core;

public class PMath {
    public final static float PI = 3.14159265358979323846f;
    public static float sin(float angle) {
        return (float) Math.sin(angle);
    }
    public static float cos(float angle) {
        return (float) Math.cos(angle);
    }
    public static float randf() {
        return (float) Math.random();
    }
    public static float randf(float max) {
        return randf() * max;
    }
    public static float randf(float min, float max) {
        return min + (max - min) * randf();
    }
}