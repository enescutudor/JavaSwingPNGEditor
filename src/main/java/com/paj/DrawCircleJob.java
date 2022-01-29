package com.paj;

import static java.lang.Math.sqrt;

public class DrawCircleJob extends Job {
    public static String Name = "Draw Circle";


    private int x, y, radius, R, G, B, A;

    public DrawCircleJob(int x, int y, int radius, int R, int G, int B, int A) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.R = R;
        this.G = G;
        this.B = B;
        this.A = A;
    }

    public int getR() {
        return R;
    }

    public int getG() {
        return G;
    }

    public int getB() {
        return B;
    }

    public int getA() {
        return A;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public int[][] executeProcessingJob(int[][] picture) throws Exception {
        int[][] result = new int[picture.length][picture[0].length];
        for (int i = 0; i < picture.length; i++) {
            for (int j = 0; j < picture[i].length; j++) {
                if (sqrt((i - x) * (i - x) + (j - y) * (j - y)) <= radius) {
                    int[] rgba = getRGBAFromPixel(picture[i][j]);
                    int newRed = (R + rgba[0]) * A / 255;
                    int newGreen = (G + rgba[1]) * A / 255;
                    int newBlue = (B + rgba[2]) * A / 255;

                    newRed = Math.min(newRed, 255);
                    newGreen = Math.min(newGreen, 255);
                    newBlue = Math.min(newBlue, 255);
                    result[i][j] = getColorIntValFromRGBA(new int[] {newRed, newGreen, newBlue, rgba[3]});
                } else {
                    result[i][j] = picture[i][j];
                }
            }
        }
        return result;
    }
}
