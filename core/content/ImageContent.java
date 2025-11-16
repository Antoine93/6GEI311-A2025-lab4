package core.content;

import core.exporter.Exporter;

/**
 * ImageContent - Represente une capture d'ecran ou une image
 */
public class ImageContent implements Content {

    private String imagePath;
    private String caption;

    public ImageContent(String imagePath, String caption) {
        this.imagePath = imagePath;
        this.caption = caption;
    }

    public ImageContent(String imagePath) {
        this(imagePath, "");
    }

    @Override
    public String display() {
        String result = "[IMAGE] " + imagePath;
        if (!caption.isEmpty()) {
            result += " - " + caption;
        }
        return result;
    }

    @Override
    public String accept(Exporter exporter) {
        return exporter.exportImage(this);
    }

    // Getters et Setters
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return "ImageContent{imagePath='" + imagePath + "', caption='" + caption + "'}";
    }
}
