package com.paj;

public class MirrorHorizontalJob extends Job {
    public static String Name = "Mirror Horizontally";

    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {
        int [][] mirroredPicture = new int[picture.length][picture[0].length];
        for (int i = 0; i < picture.length; i++) {
            for (int j = 0; j < picture[i].length; j++) {
                mirroredPicture[i][j] = picture[i][picture[i].length - 1 - j];
            }
        }
        return mirroredPicture;
    }
}
