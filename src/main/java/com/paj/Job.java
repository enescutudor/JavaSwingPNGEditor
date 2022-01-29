package com.paj;

import javax.swing.*;
import java.awt.*;

public abstract class Job {
    public abstract int[][] executeProcessingJob(int[][] picture) throws Exception;

    int[] getRGBAFromPixel(int pixelColorValue) {
        Color pixelColor = new Color(pixelColorValue);
        return new int[] { pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue(), pixelColor.getAlpha() };
    }

    int getColorIntValFromRGBA(int[] colorData) {
        Color color = new Color(colorData[0], colorData[1], colorData[2], colorData[3]);
        return color.getRGB();

    }
}
