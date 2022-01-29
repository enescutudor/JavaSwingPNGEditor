package com.paj;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SaveJob extends Job {
    public static String Name = "Save";

    public String getPath() {
        return path;
    }

    private String path;

    public SaveJob(String path) {
        this.path = path;
    }


    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {

        int height = picture.length;
        int width = picture[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.setRGB(j, i, picture[i][j]);
            }
        }

        File output = new File(path);
        ImageIO.write(result, "png", output);
        return picture;

    }
}
