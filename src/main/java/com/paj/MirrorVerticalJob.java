package com.paj;

public class MirrorVerticalJob extends Job {
    public static String Name = "Mirror Vertically";

    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {
        int [][] mirroredPicture = new int[picture.length][picture[0].length];
        for (int i = 0; i < picture.length; i++) {
            for (int j = 0; j < picture[i].length; j++) {
                mirroredPicture[i][j] = picture[picture.length - 1 - i][j];
            }
        }
        return mirroredPicture;
    }
}
