package projectPartA;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

    static long startTime = System.currentTimeMillis(); // Record the start time for runtime measurement
    public static short[][] grayscaleImage;
    public static int imgWidth;
    public static int imgHeight;
    private static BufferedImage sourceImg;
    private static BufferedImage templateImg;
    private static boolean[][] visitedPixels;

    public static void main(String[] args) throws IOException {
        // Define paths to the source and template images
        String sourceImagePath = "TenCardG.jpg";
        String templateImagePath = "Template.jpg";

        // Read and convert the images to grayscale
        sourceImg = ImageIO.read(new File(sourceImagePath));
        short[][] sourceGray = convertToGrayscale(sourceImagePath);
        templateImg = ImageIO.read(new File(templateImagePath));
        short[][] templateGray = convertToGrayscale(templateImagePath);

        // Find all matching regions of the template in the source image
        Rectangle[] matches = findAllMatches(templateGray, sourceGray);

        // Draw rectangles around all matched regions
        for (Rectangle rect : matches) {
            drawRectangle(sourceImg, rect);
        }

        // Save the result image with rectangles drawn on it
        String resultImagePath = "Output.jpg";
        ImageIO.write(sourceImg, "jpg", new File(resultImagePath));
        System.out.println("Check " + resultImagePath + " to view the image");

        // Display the result image in a window
        displayImage(resultImagePath, "Source Image with Rectangles");

        // Output the number of matches found and their locations
        System.out.println("Total Number of Matches Found: " + matches.length);
        for (Rectangle rect : matches) {
            System.out.println("Match was found at: (" + rect.x + ", " + rect.y + ")");
        }

        // Measure and output the total runtime
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runtime time: " + totalTime + " milliseconds");
    }

    // Convert an image to grayscale
    public static short[][] convertToGrayscale(String fileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));
        imgWidth = image.getWidth();
        imgHeight = image.getHeight();

        byte[] pixelData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        grayscaleImage = new short[imgHeight][imgWidth];
        visitedPixels = new boolean[imgHeight][imgWidth];

        int pixelIndex, red, green, blue;
        for (int row = 0; row < imgHeight; row++) {
            for (int col = 0; col < imgWidth; col++) {
                pixelIndex = 3 * (row * imgWidth + col);
                red = pixelData[pixelIndex] & 0xff;
                green = pixelData[pixelIndex + 1] & 0xff;
                blue = pixelData[pixelIndex + 2] & 0xff;
                grayscaleImage[row][col] = (short) Math.round(0.299 * red + 0.587 * green + 0.114 * blue);
            }
        }
        return grayscaleImage;
    }

    // Check if a region of the image has been visited
    public static boolean isVisited(int row, int col, int templateHeight, int templateWidth) {
        for (int i = Math.max(0, row); i < Math.min(row + templateHeight, imgHeight); i++) {
            for (int j = Math.max(0, col); j < Math.min(col + templateWidth, imgWidth); j++) {
                if (visitedPixels[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    // Mark a region of the image as visited
    public static void markVisited(int row, int col, int templateHeight, int templateWidth) {
        for (int i = Math.max(0, row); i < Math.min(row + templateHeight, imgHeight); i++) {
            for (int j = Math.max(0, col); j < Math.min(col + templateWidth, imgWidth); j++) {
                visitedPixels[i][j] = true;
            }
        }
    }

    // Find all matches of the template in the source image
    public static Rectangle[] findAllMatches(short[][] templateImage, short[][] sourceImage) {
        int sourceHeight = sourceImage.length;
        int sourceWidth = sourceImage[0].length;
        int templateHeight = templateImage.length;
        int templateWidth = templateImage[0].length;
        double minAbsDiff = Double.MAX_VALUE;
        int templateSize = templateHeight * templateWidth;
        List<Rectangle> matchesList = new ArrayList<Rectangle>();

        double[][] absDiffMatrix = new double[sourceHeight - templateHeight + 1][sourceWidth - templateWidth + 1];

        for (int i = 0; i <= sourceHeight - templateHeight; i++) {
            for (int j = 0; j <= sourceWidth - templateWidth; j++) {
                double absDiff = 0;

                if (isVisited(i, j, templateHeight, templateWidth)) {
                    continue;
                }

                for (int k = 0; k < templateHeight; k++) {
                    for (int l = 0; l < templateWidth; l++) {
                        absDiff += Math.abs(sourceImage[i + k][j + l] - templateImage[k][l]);
                    }
                }
                absDiff /= templateSize;
                absDiffMatrix[i][j] = absDiff;

                if (absDiff < minAbsDiff) {
                    minAbsDiff = absDiff;
                }
            }
        }

        double threshold = 10 * minAbsDiff;

        for (int i = 0; i <= sourceHeight - templateHeight; i++) {
            for (int j = 0; j <= sourceWidth - templateWidth; j++) {
                if (absDiffMatrix[i][j] <= threshold) {
                    matchesList.add(new Rectangle(j, i, templateWidth, templateHeight));
                    markVisited(i, j, templateHeight, templateWidth);
                }
            }
        }

        return matchesList.toArray(new Rectangle[0]);
    }

    // Draw a rectangle around the matched region
    public static void drawRectangle(BufferedImage image, Rectangle rect) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED); // Set the rectangle color to red
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height); // Draw the rectangle
        graphics.dispose();
    }

    // Display the image in a window
    public static void displayImage(String fileName, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon(fileName);
        JLabel label = new JLabel(icon);
        frame.add(label);

        frame.pack();
        frame.setVisible(true);
    }
}
