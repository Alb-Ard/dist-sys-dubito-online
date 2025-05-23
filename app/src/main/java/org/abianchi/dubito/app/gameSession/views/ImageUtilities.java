package org.abianchi.dubito.app.gameSession.views;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import javax.imageio.ImageIO;

public final class ImageUtilities {
    private ImageUtilities() {
    }

    public static Optional<Image> loadImageFromPath(final String path, final int width, final int height) {
        if (path == null) {
            return Optional.empty();
        }
        try {
            URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(path);
            if (resourceUrl != null) {
                    BufferedImage loadedImage = ImageIO.read(resourceUrl);
                    if (loadedImage == null) {
                        return Optional.empty();
                    }
                    Image correctSizeImage = loadedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return Optional.of(correctSizeImage);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public static Image rotateImage(final Image image, final Rotation rotation) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        // Create a new buffered image for rotation
        BufferedImage rotatedImage = new BufferedImage(
                height, width, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = rotatedImage.createGraphics();

        // Set up rotation transform
        AffineTransform transform = new AffineTransform();
        transform.translate(height / 2.0, width / 2.0);
        double angle = rotation == Rotation.LEFT ? Math.PI / 2 : -Math.PI / 2;
        transform.rotate(angle);
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Set the rotated image
        return rotatedImage;
    }
}
