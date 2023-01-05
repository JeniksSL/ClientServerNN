package com.neuronNet.dataTransfer;


import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * This class contains the data extracted from the image {@link javafx.scene.image.Image}.

 * @author  Yauheni Slabko
 * @since   1.0
 */
public class ImageData  {

    /**
     * An array ARGB data from the image.
     * Coordinates of point (x, y) transform to (x*height+y) in array.
     * The color pixels stored in grayscale where red=green=blue.
     * The alpha always is 1.0.
     */
    protected final byte[] imageArgb;
    /**
     * An Image width in pixels.
     */
    protected final int width;
    /**
     * An Image height in pixels.
     */
    protected final int height;
    /**
     * Constants, witch is dimensions of the image that is passed to the neural network for recognition.
     */
    public static final int DATA_WIDTH=10;
    public static final int DATA_HEIGHT = 10;



    /**
     * Allocates a new {@code ImageData} from data of image.
     *
     * @param  imageArgb
     *         Source array
     *
     * @param  width
     *         The initial width
     *
     * @param  height
     *         The initial height
     * @throws IllegalArgumentException
     *         If {@code imageArgb} is null or {@code width} non-positive,
     *         or {@code height} non-positive, or {@code imageArgb} length
     *         not corresponds image dimensions.
     */
    public ImageData(byte[] imageArgb, int width, int height) {
        if (imageArgb!=null&&width>0&&height>0&&imageArgb.length==width*height) {
            this.imageArgb = Arrays.copyOf(imageArgb, imageArgb.length);
            this.width = width;
            this.height = height;
        } else {
            //Non valid data.
            throw new IllegalArgumentException("Non valid data.");
        }
    }
    /**
     * Allocates a new {@code ImageData} from another {@code ImageData} or extended class.
     *
     * @param  imageData
     *         source {@code ImageData}
     * @throws IllegalArgumentException
     *         If {@code imageData} is null.
     */
    public ImageData(ImageData imageData) {
        if (imageData!=null) {
            this.height=imageData.height;
            this.width=imageData.width;
            this.imageArgb= Arrays.copyOf(imageData.imageArgb, imageData.imageArgb.length);
        } else {
            //Non valid data.
            throw new IllegalArgumentException("Non valid data.");
        }
    }

    /**
     * Extracts data from the image {@link javafx.scene.image.Image}
     * and returns a new {@code ImageData} object based on the data.
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
     * Returns image {@link javafx.scene.image.Image} that represented in {@code ImageData} object.
     *
     * @return image {@link javafx.scene.image.Image}.
     */
    public Image getImage(){
        WritableImage writableImage=new WritableImage(this.width, this.height);
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                byte grayColor=this.imageArgb[i*this.height+j];
                int aRGB = ((0xFF000000)  | ((grayColor << 16) & 0x00FF0000) | ((grayColor << 8) & 0x0000FF00) | (grayColor) & 0x0000FF) ;
                writableImage.getPixelWriter().setArgb(i,j,aRGB);
            }
        }
        return writableImage;
    }

    /**
     * Returns {@code ImageData} represents fit scaled image of initial {@code ImageData}.
     * A search is performed for extreme points that are not white,
     * and the image is cropped on all sides to the coordinates of the found points.
     *
     * @return new fit scaled {@code ImageData}. If initial {@code ImageData} represents
     * fully withe image, return it not scaled;

     */
    private ImageData getFitScaleData() {
        int xStart=this.width, yStart=this.height, xEnd=0, yEnd=0;
        byte aRGB;
        byte whiteColor=(byte)0xFF; //White color in grayscale is 0xFF.
        //Search for extreme non-white points.
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                aRGB=this.imageArgb[i*height+j];
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
                        int index=(i+xStart)*height+j+yStart;
                        if(index>=width*height) {
                            tempData[i*newHeight+j]=(byte) 0xFF;
                        }
                        else {
                            tempData[i*newHeight+j]=this.imageArgb[index];
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
            tempData=this.imageArgb;
            newWidth=this.width;
            newHeight=this.height;
        }

        return new ImageData(tempData, newWidth, newHeight);
    }

    /**
     * Returns {@code ImageData} represents image of initial {@code ImageData} rescaled to
     * standard dimensions: {@code DATA_WIDTH} and {@code DATA_HEIGHT}.
     * Rescaling is carried out by dividing the initial image into sections, the number
     * of which is equal to the number of pixels of the standard image ({@code DATA_WIDTH}*{@code DATA_HEIGHT}).
     * The color of each pixel of new image in grayscale is calculated as the
     * arithmetic mean of the sum of colors of all the original pixels in the area.
     * The alpha always is 1.0.
     * @return new rescaled {@code ImageData}.
     */
    public ImageData getStandardData(){
        ImageData fitScaleImage= getFitScaleData();
        byte[] imageData=new byte[DATA_WIDTH*DATA_HEIGHT];
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
                imageData[i*DATA_HEIGHT+j]=(byte) (grayColor & 0xFF);

            }

        }
        return new ImageData(imageData,DATA_WIDTH,DATA_HEIGHT);
    }

    /**
     * Encodes this {@code ImageData} into a sequence of bytes using the given
     * {@link java.nio.ByteBuffer}, storing the result into a
     * new byte array.
     *
     * @return The resultant byte array.
     */
    public byte[] serialize(){
        int size=Integer.BYTES*2+Byte.BYTES*imageArgb.length;
        ByteBuffer byteBuffer=ByteBuffer.allocate(size);
        byteBuffer.putInt(width);
        byteBuffer.putInt(height);
        byteBuffer.put(imageArgb);
        return byteBuffer.array();
    }

    /**
     * Constructs a new {@code ImageData} from {@code byte} array source.
     *
     *
     * @param   sourceArray     the byte array.
     * @return  a new {@code ImageData}.
     * @throws  InvalidObjectException
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



    /**
     * Returns a string with short description about object.
     *
     * @return string with information about this {@code width}, {@code height}, {@code imageArgb} length.
     */
    @Override
    public String toString() {
        return "ImageData, width: "+this.width+", height:"+this.height+", imageArgb length: " + imageArgb.length;
    }

}
