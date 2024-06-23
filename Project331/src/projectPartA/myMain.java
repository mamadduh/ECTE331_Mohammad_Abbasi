package projectPartA;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class myMain {

    // Start time to measure the execution duration
    static long startTimestamp = System.currentTimeMillis();

    // Global variables to hold image data and dimensions
    public static short[][] grayscaleImage;
    public static int imgWidth;
    public static int imgHeight;
    private static BufferedImage sourceImage;
    private static BufferedImage templateImage;
    private static double minDifference = 100000000.0; // Shared minimum value across threads

    public static void main(String[] args) throws IOException {
        // File names for the source and template images
        String sourceFilename = "TenCardG.jpg";
        String templateFilename = "Template.jpg";

        // Load the images from files
        File sourceFile = new File(sourceFilename);
        File templateFile = new File(templateFilename);
        sourceImage = ImageIO.read(sourceFile);
        templateImage = ImageIO.read(templateFile);

        // Convert images to grayscale
        short[][] sourceMatrix = convertToGrayscaleMatrix(sourceFilename);
        short[][] templateMatrix = convertToGrayscaleMatrix(templateFilename);

        // Number of threads to use for template matching
        int threadCount = 8;

        // Perform multithreaded template matching
        performTemplateMatchingMultithreaded(sourceMatrix, templateMatrix, threadCount);

        // Save the result image with rectangles drawn on matches
        String resultFilename = "multithreaded_result.jpg";
        saveImageWithRectangles(resultFilename);

        System.out.println("Image saved as: " + resultFilename);

        // Verify file existence
        File resultFile = new File(resultFilename);
        if (resultFile.exists()) {
            System.out.println(resultFilename + " has been created.");
        } else {
            System.out.println("Error creating " + resultFilename);
        }

        // Calculate and print the total execution time
        long endTimestamp = System.currentTimeMillis();
        long executionTime = endTimestamp - startTimestamp;
        System.out.println("Total runtime time: " + executionTime + " milliseconds");
    }

    // Method to read an image and convert it to a grayscale matrix
    public static short[][] convertToGrayscaleMatrix(String filename) throws IOException {
        BufferedImage image = ImageIO.read(new File(filename));
        imgWidth = image.getWidth();
        imgHeight = image.getHeight();

        byte[] pixelData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        grayscaleImage = new short[imgHeight][imgWidth];

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

    // Method to perform template matching using multiple threads
    public static void performTemplateMatchingMultithreaded(final short[][] source, final short[][] template, int threadCount) {
        int sourceWidth = source.length;
        final int sourceHeight = source[0].length;
        final int templateWidth = template.length;
        final int templateHeight = template[0].length;
        final int templateSize = templateWidth * templateHeight;
        final double[][] differenceMatrix = new double[sourceWidth - templateWidth + 1][sourceHeight - templateHeight + 1];

        int rowsPerThread = (sourceWidth - templateWidth + 1) / threadCount;
        List<int[]> matchCoordinates = new ArrayList<int[]>();

        Thread[] threads = new Thread[threadCount];

        // Create and start threads for template matching
        for (int i = 0; i < threadCount; i++) {
            final int startRow = i * rowsPerThread;
            final int endRow = (i == threadCount - 1) ? (sourceWidth - templateWidth + 1) : (startRow + rowsPerThread);

            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for (int row = startRow; row < endRow; row++) {
                        for (int col = 0; col <= sourceHeight - templateHeight; col++) {
                            double absoluteDifference = 0.0;
                            for (int templateRow = 0; templateRow < templateWidth; templateRow++) {
                                for (int templateCol = 0; templateCol < templateHeight; templateCol++) {
                                    absoluteDifference += Math.abs(source[row + templateRow][col + templateCol] - template[templateRow][templateCol]);
                                }
                            }
                            absoluteDifference /= templateSize;
                            differenceMatrix[row][col] = absoluteDifference;

                            // Update minimum in a thread-safe manner
                            updateMinDifference(absoluteDifference);
                        }
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        try {
            for (int t = 0; t < threadCount; t++) {
                threads[t].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Find and print coordinates of matches based on a threshold
        double matchThreshold = 10 * minDifference;
        for (int row = 0; row <= sourceWidth - templateWidth; row++) {
            for (int col = 0; col <= sourceHeight - templateHeight; col++) {
                if (differenceMatrix[row][col] <= matchThreshold) {
                    matchCoordinates.add(new int[]{row, col});
                    System.out.println("Match was found at row: " + row + " column: " + col);
                }
            }
        }

        // Draw rectangles around matches on the source image
        for (int[] coord : matchCoordinates) {
            drawRectangle(sourceImage, coord[1], coord[0], templateHeight, templateWidth);
        }
    }

    // Synchronized method to update the minimum value found
    public static synchronized void updateMinDifference(double difference) {
        if (difference < minDifference) {
            minDifference = difference;
        }
    }

    // Method to draw a rectangle on the image
    public static void drawRectangle(BufferedImage image, int x, int y, int rectHeight, int rectWidth) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.drawRect(x, y, rectWidth, rectHeight);
        graphics.dispose();
    }

    // Method to save the image with rectangles
    public static void saveImageWithRectangles(String filename) throws IOException {
        ImageIO.write(sourceImage, "jpg", new File(filename));
    }
}
