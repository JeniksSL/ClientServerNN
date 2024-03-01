package com.clientservernn.dataTransfer;


import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.Arrays;

import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_HEIGHT;
import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_WIDTH;


/**
 * This class contains the data extracted from the image {@link Image}.

 * @author  Yauheni Slabko
 * @since   1.0
 */
public class ImageData {

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
     * Returns image {@link Image} that represented in {@code ImageData} object.
     *
     * @return image {@link Image}.
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

    //TODO

    public boolean isStandard(){
        return this.width==DATA_WIDTH&&this.height==DATA_HEIGHT;
    }


    //TODO
    public byte[] getImageArgb(){
        return Arrays.copyOf(imageArgb,imageArgb.length);
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
