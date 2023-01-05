package com.clientservernn.dataTransfer;

import javafx.scene.image.Image;

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;




/**
 * This abstract class contains only static methods for manipulation
 * {@link ImageData} class objects.

 * @author  Yauheni Slabko
 * @since   1.0
 */


public abstract class ImageDataUtil extends ImageData {

    /**
     * Constants, dimensions of the image that is passed to the neural network for recognition.
     */
    public static final int DATA_WIDTH=10;
    public static final int DATA_HEIGHT = 10;


    private ImageDataUtil(ImageData imageData) {
        super(imageData);
    }

    /**
     * Extracts data from the image {@link javafx.scene.image.Image}
     * and returns a new {@link  ImageData} object based on the data.
     *
     * @param   image
     *          Source image (javafx.scene.image.Image).
     * @return  a {@code ImageData} that contains data of {@param image}.
     * @throws  IllegalArgumentException
     *          If {@code image} is null.
     */
    public static ImageData ofImage(Image image) {
        if (image==null) {
            //Non valid data.
            throw new IllegalArgumentException("Non valid data.");
        }
        int width=(int) (image.getWidth());
        int height=(int) (image.getHeight());
        byte []imageArgb=new byte[width*height];
        int red, green, blue;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int argb = image.getPixelReader().getArgb(i, j);
                //Next part can be simplified because used grayscale where red=green=blue.
                //alpha = (argb >> 24) & 0xFF;
                red = (argb >> 16) & 0xFF;
                green = (argb >> 8) & 0xFF;
                blue = (argb) & 0xFF;
                int average=(red+green+blue)/3;
                byte greyColor=(byte) (0xFF&average);
                imageArgb[i*height+j]=greyColor;
            }
        }
        return new ImageData(imageArgb,width,height);
    }


    /**
     * Returns {@link  ImageData} represents fit scaled image of initial {@code ImageData}.
     * A search is performed for extreme points that are not white,
     * and the image is cropped on all sides to the coordinates of the found points.
     *
     * @return new fit scaled {@code ImageData}. If initial {@code ImageData} represents
     * fully withe image, return it not scaled;
     */
    private static ImageData getFitScaleData(ImageData imageData) {
        int xStart=imageData.width, yStart=imageData.height, xEnd=0, yEnd=0;
        byte aRGB;
        byte whiteColor=(byte)0xFF; //White color in grayscale is 0xFF.
        //Search for extreme non-white points.
        for (int i = 0; i < imageData.width; i++) {
            for (int j = 0; j < imageData.height; j++) {
                aRGB=imageData.imageArgb[i*imageData.height+j];
                if (aRGB>whiteColor) {
                    xStart= Math.min(xStart, i);
                    yStart= Math.min(yStart, j);
                    xEnd= Math.max(xEnd, i);
                    yEnd= Math.max(yEnd, j);
                }
            }
        }
        int foundWidth=xEnd-xStart;
        int foundHeight=yEnd-yStart;
        //The new dimensions must be a multiple of the standard to avoid loss of a part of the image.
        //Next operation can lead to fact that the coordinates of new image may go beyond the original.
        int newWidth=(1+foundWidth/DATA_WIDTH)*(DATA_WIDTH);
        int newHeight=(1+foundHeight/DATA_HEIGHT)*(DATA_HEIGHT);
        byte [] tempData;
        if (newWidth>0&&newHeight>0) {
            tempData = new byte[newWidth*newHeight];
            for (int i = 0; i < newWidth; i++) {
                for (int j = 0; j < newHeight; j++) {

                    try{//TODO
                        int index=(i+xStart)*imageData.height+j+yStart;
                        if(index>=imageData.width*imageData.height) {
                            tempData[i*newHeight+j]=(byte) 0xFF;
                        }
                        else {
                            tempData[i*newHeight+j]=imageData.imageArgb[index];
                        }

                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        tempData[i*newHeight+j]=(byte) 0xFF;
                    }
                }
            }
        }
        else {
            //Return not scaled data;
            tempData=imageData.imageArgb;
            newWidth=imageData.width;
            newHeight=imageData.height;
        }
        return new ImageData(tempData, newWidth, newHeight);
    }

    /**
     * Returns {@link  ImageData} represents image of initial {@code ImageData} rescaled to
     * standard dimensions: {@code DATA_WIDTH} and {@code DATA_HEIGHT}.
     * Rescaling is carried out by dividing the initial image into sections, the number
     * of which is equal to the number of pixels of the standard image ({@code DATA_WIDTH}*{@code DATA_HEIGHT}).
     * The color of each pixel of new image in grayscale is calculated as the
     * arithmetic mean of the sum of colors of all the original pixels in the area.
     * The alpha always is 1.0.
     * @return new rescaled {@code StandardImage}.
     */
    public static ImageData getStandardData(ImageData imageData){
        if (imageData.isStandard()){
            return imageData;
        }
        ImageData fitScaleImage= getFitScaleData(imageData);
        byte[] imageARGB=new byte[DATA_WIDTH*DATA_HEIGHT];
        int scaleWidth=fitScaleImage.width/ DATA_WIDTH;
        int scaleHeight=fitScaleImage.height/ DATA_HEIGHT;
        int grayColor;
        for (int i = 0; i < DATA_WIDTH; i++) {
            for (int j = 0; j < DATA_HEIGHT; j++) {
                grayColor =0;
                for (int k = 0; k < scaleWidth; k++) {
                    for (int l = 0; l < scaleHeight; l++) {
                        byte argb = fitScaleImage.imageArgb[(k + scaleWidth * i) * fitScaleImage.height + l + scaleHeight * j];
                        grayColor += (argb) & 0xFF;
                    }
                }
                grayColor /= (scaleWidth * scaleHeight);
                imageARGB[i*DATA_HEIGHT+j]=(byte) (grayColor & 0xFF);

            }
        }
        return new ImageData(imageARGB, DATA_WIDTH, DATA_HEIGHT);
    }


    /**
     * Encodes this {@link  ImageData} into a sequence of bytes using the given
     * {@link ByteBuffer}, storing the result into a
     * new byte array.
     *
     * @return The resultant byte array.
     */
    public static byte[] serialize(ImageData imageData){
        int size=Integer.BYTES*2+Byte.BYTES*imageData.imageArgb.length;
        ByteBuffer byteBuffer=ByteBuffer.allocate(size);
        byteBuffer.putInt(imageData.width);
        byteBuffer.putInt(imageData.height);
        byteBuffer.put(imageData.imageArgb);
        return byteBuffer.array();
    }

    /**
     * Constructs a new {@link  ImageData} from {@code byte} array source.
     *
     *
     * @param   sourceArray     the byte array.
     * @return  a new {@code ImageData}.
     * @throws InvalidObjectException
     *          If {@code ImageData} object failed validation tests.
     * @throws  IllegalArgumentException
     *          If {@code sourceArray} is {@code null} or have not enough size.
     */
    public static ImageData deserialize(byte[] sourceArray) throws InvalidObjectException {
        if (sourceArray==null||sourceArray.length<=Integer.BYTES*2) {
            //Non valid data.
            throw new IllegalArgumentException("Non valid data.");
        }
        ByteBuffer byteBuffer=ByteBuffer.wrap(sourceArray);
        int width=byteBuffer.getInt();
        int height=byteBuffer.getInt();
        byte[] imageArgb=new byte[width*height];
        byteBuffer.get(imageArgb);
        if (imageArgb.length+Integer.BYTES*2!= sourceArray.length) {
            //Non valid data.
            throw new InvalidObjectException("Non valid data.");
        }
        return new ImageData(imageArgb,width,height);
    }


}
