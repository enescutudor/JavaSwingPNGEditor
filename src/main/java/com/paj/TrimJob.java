package com.paj;
public class TrimJob extends Job{

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int width, height;
    public static String Name = "Trim";

    public TrimJob(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int[][] executeProcessingJob(int[][] picture) {
        int presentHeight = picture.length;
        int presentWidth = picture[0].length;
        int[][] result = new int[presentHeight - 2 * height][presentWidth - 2 * width];
        for (int i = height; i < presentHeight - height; i++) {
            for (int j = width; j < presentWidth - width; j++) {
                result[i - height][j - width] = picture[i][j];
            }
        }
        return result;
    }
}
