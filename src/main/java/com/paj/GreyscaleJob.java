package com.paj;

public class GreyscaleJob extends Job {
    public static String Name = "Greyscale";
    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {
        int[][] result = new int[picture.length][picture[0].length];
        for (int i = 0; i < picture.length; i++) {
            for (int j = 0; j < picture[0].length; j++) {
                int[] rgba = getRGBAFromPixel(picture[i][j]);
                rgba[0] = rgba[1] = rgba[2] = (rgba[0] + rgba[1] + rgba[2]) / 3;
                result[i][j] = getColorIntValFromRGBA(rgba);
            }
        }
        return result;
    }
}
