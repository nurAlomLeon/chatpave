/*
 * Located in: src/chatpave/util/ImageUtil.java
 */
package chatpave.util;

import javax.microedition.lcdui.Image;

/**
 * A utility class for image manipulation.
 */
public class ImageUtil {

    /**
     * Resizes an image to fit within a max width and height, maintaining aspect ratio.
     * @param source The original image to resize.
     * @param maxWidth The maximum width the new image can have.
     * @param maxHeight The maximum height the new image can have.
     * @return A new, resized Image object.
     */
    public static Image resizeImage(Image source, int maxWidth, int maxHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Determine the scaling factor to maintain aspect ratio
        float ratio = Math.min(
            (float) maxWidth / sourceWidth,
            (float) maxHeight / sourceHeight
        );

        // If the image is already smaller than the max dimensions, no need to resize
        if (ratio >= 1.0f) {
            return source;
        }

        int newWidth = (int) (sourceWidth * ratio);
        int newHeight = (int) (sourceHeight * ratio);

        // Create a buffer for the scaled image
        int[] buffer = new int[newWidth * newHeight];
        
        // Create a temporary array to hold one pixel at a time
        int[] pixel = new int[1];

        // Simple scaling algorithm (nearest neighbor)
        for (int y = 0; y < newHeight; y++) {
            int sourceY = (int) (y / ratio);
            for (int x = 0; x < newWidth; x++) {
                int sourceX = (int) (x / ratio);
                
                // Correct way to use getRGB:
                // 1. Call the method to populate the 'pixel' array.
                source.getRGB(pixel, 0, 1, sourceX, sourceY, 1, 1);
                // 2. Assign the value from the populated array to our main buffer.
                buffer[y * newWidth + x] = pixel[0];
            }
        }
        
        // Create a new image from the buffer
        return Image.createRGBImage(buffer, newWidth, newHeight, false);
    }
}
