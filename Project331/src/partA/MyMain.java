package PartA;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.image.DataBufferByte;

public class MyMain {

public static short[][] grayImage;
public static int width;
public static int height;
private static BufferedImage image;

public static void main(String[] args){
String fileNameInp="TenCardG.jpg";
String fileNameOut="Template.jpg";




File inp=new File(fileNameInp);
try {
image = ImageIO.read(inp);
} catch (IOException e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
int width_o = image.getWidth();
int height_o = image.getHeight();
int tempSize = width_o*height_o;
System.out.println(fileNameInp + " width: " + width_o + " and height:" + height_o + "\nThe temp size is "+ tempSize);


File inp1=new File(fileNameOut);
try {
image = ImageIO.read(inp1);
} catch (IOException e) {
e.printStackTrace();
}
width = image.getWidth();
height = image.getHeight();
int tempSize1 = width*height;
System.out.println(fileNameOut + " width: " + width + " and height:" + height + "\nThe temp size is "+ tempSize1);


readColourImage(fileNameOut);
readColourImage(fileNameInp);
}
public static short[][] readColourImage(String fileName) {

try
{
// RGB pixel values
byte[] pixels;

File inp=new File(fileName);
image = ImageIO.read(inp);
width = image.getWidth();
height = image.getHeight();


pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
System.out.println("Dimension of the " + fileName +" image: WxH= " + width + " x "+height+" "+ "| num of pixels: "+ pixels.length);



//rgb2gray in a 2D array grayImage
int pr;// red
int pg;// green
int pb;// blue

grayImage =new short [height][width];
int coord;
for (int i=0; i for(int j=0;j {
coord= 3*(i*width+j);
pr= ((short) pixels[coord] & 0xff); // red
pg= (((short) pixels[coord+1] & 0xff));// green
pb= (((short) pixels[coord+2] & 0xff));// blue

grayImage[i][j]=(short)Math.round(0.299 *pr + 0.587 * pg + 0.114 * pb);

}
}
catch (IOException e) {
e.printStackTrace();
}
return grayImage;


}


}
