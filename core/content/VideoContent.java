package core.content;

import core.exporter.Exporter;

/**
 * VideoContent - Represente une video
 */
public class VideoContent implements Content {

    private String videoPath;
    private int duration; // duree en secondes

    public VideoContent(String videoPath, int duration) {
        this.videoPath = videoPath;
        this.duration = duration;
    }

    public VideoContent(String videoPath) {
        this(videoPath, 0);
    }

    @Override
    public String display() {
        String result = "[VIDEO] " + videoPath;
        if (duration > 0) {
            result += " (" + formatDuration(duration) + ")";
        }
        return result;
    }

    @Override
    public String accept(Exporter exporter) {
        return exporter.exportVideo(this);
    }

    /**
     * Formate la duree en format mm:ss
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    // Getters et Setters
    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "VideoContent{videoPath='" + videoPath + "', duration=" + duration + "s}";
    }
}
