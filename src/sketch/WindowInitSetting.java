package sketch;

public class WindowInitSetting {
    public int width;
    public int height;
    public String title = "Sketch";

    public WindowInitSetting(
        int _width, int _height
    ) {
        width = _width;
        height = _height;
    }

    public WindowInitSetting(
        int _width, int _height,
        String _title
    ) {
        width = _width;
        height = _height;
        title = _title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public String getTitle() {
        return title;
    }
}