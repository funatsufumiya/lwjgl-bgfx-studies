package sketch;

import java.io.IOException;

public abstract class Sketch {
    protected long startTimeMillis = System.currentTimeMillis();

    public void _setStartTimeMillis(long _startTimeMillis) {
        startTimeMillis = _startTimeMillis;
    }
    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }
    public float elapsedTimeSeconds() {
        return elapsedTimeMillis() / 1000.0f;
    }

    public abstract void setup() throws IOException;
    public abstract void draw();
    public void exit() {}
}