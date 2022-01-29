package com.paj;

import java.awt.*;

public class NegativeJob extends Job {
    public static String Name = "Negative";

    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {
        int[][] negativePicture = new int[picture.length][picture[0].length];
        for (int i = 0; i < picture.length; i++) {
            for (int j = 0; j < picture[i].length; j++) {
                int[] rgba = getRGBAFromPixel(picture[i][j]);
                rgba[0] = 255 - rgba[0];
                rgba[1] = 255 - rgba[1];
                rgba[2] = 255 - rgba[2];
                negativePicture[i][j] = getColorIntValFromRGBA(rgba);
            }
        }
        return negativePicture;
    }
}
