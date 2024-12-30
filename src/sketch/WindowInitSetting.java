package sketch;

public class WindowInitSetting {
    public int width;
    public int height;

    public WindowInitSetting(
        int _width,
        int _height
    ) {
        width = _width;
        height = _height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}