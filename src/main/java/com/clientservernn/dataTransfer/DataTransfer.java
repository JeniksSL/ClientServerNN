package com.clientservernn.dataTransfer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;


/**
 * The class is used to transfer data between the client and the server
 * using a byte data transfer stream and its own way of converting data to bytes.
 * The data in the class is contained in the form of an {@link ImageData} {@code imageData}, a command
 * in the form of {@link Command} {@code command}, an array of {@link String} {@code message}
 * and positions for CRUD operations int {@code position}.
 * Also, each object of the class can be assigned its own transfer code {@code transferCode}.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public class DataTransfer {


    /**
     * A constant holding current version of {@code DataTransfer}
     */
    public static final double versionID= 1.01;

    /**
     * A sum of sizes in bytes of {@code DataTransfer} primitive
     * data fields with constant size, except itself, including {@code command}.
     * Field {@code command} is determined as {@code int}.
     */
    private static final int dataLength=Double.BYTES+Integer.BYTES*6;

    /**
     * The type of transferred command, determined as  {@code Command} enum constant.
     */
    public final Command command;

    /**
     * The transferred {@code ImageData}. Can be a {@code null} if no image data required to transfer.
     */
    private final ImageData imageData;

    /**
     * The array of transferred messages. Can be a {@code null} if no message required to transfer.
     */
    private final String[] message;

    /**
     * The serial number of current transfer operation.
     */
    private int transferCode;

    /**
     * The position of item for CRUD operations.
     * Is negative if no CRUD operations is required.
     */
    private final int position;


    /**
     * Creates a new {@code DataTransfer} with image {@code ImageData}, message {@code String},
     * command {@code Command} and position for CRUD operations.
     *
     * @param  imageData
     *         source image data {@code ImageData}. Can be a {@code null}.
     * @param  message
     *         source message as {@code String}. Can be a {@code null}.
     * @param  command
     *         source command as {@code Command} constant.
     *         If a {@code command} is {@code null}, set by {@code Command.DEFAULT}
     * @param  position
     *         initial position {@code int}
     */

    public DataTransfer(ImageData imageData, Command command, int position, String... message){
        if (imageData!=null) {
            this.imageData=new ImageData(imageData);
        } else {
            this.imageData=null;
        }
        this.command = Objects.requireNonNullElse(command, Command.EXCEPTION);
        if (message!=null) {
            this.message=Arrays.copyOf(message, message.length);
        } else {
            this.message=null;
        }
        this.position=position;
    }

    /**
     * Creates a new {@code DataTransfer} with image {@code ImageData}, message {@code String},
     * command {@code Command}. Position set by -1, it is means that no CRUD operations is required;
     *
     * @param  imageData
     *         source image data {@code ImageData}. Can be a {@code null}.
     * @param  message
     *         source message as {@code String}. Can be a {@code null}.
     * @param  command
     *         source command as {@code Command} constant.
     *         If a {@code command} is {@code null}, set by {@code Command.DEFAULT}
     */


    public DataTransfer(ImageData imageData, Command command, String... message){
        this(imageData,command,-1,message);
    }

    /**
     * Set the {@code transferCode} for this {@code DataTransfer}.
     * @param transferCode
     *        new transfer code {@code int};
     */
    public void setTransferCode(int transferCode) {
        this.transferCode = transferCode;
    }


    /**
     * Returns the copy of this {@code DataTransfer} as a new {@code DataTransfer}.
     * @return  the new {@code DataTransfer} with same field values as initial {@code DataTransfer}.
     */
    public DataTransfer getCopy(){
        DataTransfer copy=new DataTransfer(this.imageData, this.command, this.position, this.message);
        copy.transferCode=this.transferCode;
        return copy;
    }


    /**
     * Returns the {@code transferCode} of this {@code DataTransfer}
     * as a {@code int}.
     * @return  the {@code transferCode} of this object
     */
    public int getTransferCode() {
        return transferCode;
    }

    /**
     * Returns the {@code position} of this {@code DataTransfer}
     * as a {@code int}.
     * @return  the {@code position} of this object
     */
    public int getPosition() {return position;}

    /**
     * Returns the {@code command} of this {@code DataTransfer}
     * as a {@code Command} constant.
     * @return  the {@code command} of this object
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns the copy of {@code imageData} of this {@code DataTransfer}
     * as a new {@code ImageData} object or {@code null} if {@code imageData} is null.
     * @return  the copy of {@code imageData} of this object or {@code null}
     */
    public ImageData getImageData() {
        ImageData imageData=null;
        if (this.imageData!=null) {
            imageData=new ImageData(this.imageData);
        }
        return imageData;
    }

    /**
     * Returns the {@code message} of this {@code DataTransfer}
     * as a {@code String[]} or {@code null} if {@code message} is null.
     * @return  the {@code message} of this object
     */
    public String[] getMessage() {
        return message;
    }

    /**
     * Returns the value specified {@code index} of {@code message} array
     * this {@code DataTransfer} as a {@code String} or {@code null}
     * if {@code message} is null or no such index.
     * @param index the index of message array.
     * @return  the {@code message} of this object
     * @throws IndexOutOfBoundsException if the {@code index} is out of bounds
     */

    public String getMessage(int index)  {
        if (message==null) {return null;}
        Objects.checkIndex(index,message.length);
        return message[index];
    }


    /**
     * Convert this object {@code DataTransfer} to {@code byte[]}
     * like serialization, and returns resulting {@code byte[]}. This {@code byte[]} array
     * required to use is in {@link #fromByteArray(byte[] sourceArray)} method
     * for creating new object {@code DataTransfer} with initial properties.
     * <pre>
     * byteBuffer[0-7]= versionID
     * byteBuffer[8-11]= overallSize;
     * byteBuffer[12-15]= imageDataSize;
     * byteBuffer[16-19]=messageSize;
     * dataArray[20-23]=transferCode;
     * dataArray[24-27]=position;
     * dataArray[28-31]= command;
     * dataLength=32;
     * dataArray[32->and 32+imageDataSize] - imageData;
     * dataArray[32+imageDataSize->and rest] - message;
     * </pre>
     * {@code String[] message} of {@code DataTransfer} serialize by ObjectOutputStream.
     * @return  The resultant byte array
     * @throws  IOException if an I/O error occurs while writing stream header
     * by {@code ObjectOutputStream}
     *
     * */

    public byte[] toByteArray() throws IOException {

        int overallSize, imageByteSize=0, messageByteSize=0;
        byte[] messageByteArray=null;
        byte[] imageByteArray=null;

        if(this.message!=null) {
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(message);
            messageByteArray=baos.toByteArray();
            messageByteSize = messageByteArray.length;
        }
        if(this.imageData!=null){
            imageByteArray=ImageDataUtil.serialize(imageData);
            imageByteSize=imageByteArray.length;
        }
        overallSize=dataLength+imageByteSize+messageByteSize;


        ByteBuffer byteBuffer= ByteBuffer.allocate(overallSize);
        byteBuffer.putDouble(versionID);
        byteBuffer.putInt(overallSize);
        byteBuffer.putInt(imageByteSize);
        byteBuffer.putInt(messageByteSize);
        byteBuffer.putInt(transferCode);
        byteBuffer.putInt(position);
        byteBuffer.putInt( command.ordinal());
        if (imageByteSize>0) {
            byteBuffer.put(imageByteArray);
        }
        if (messageByteSize>0) {
            byteBuffer.put(messageByteArray);
        }
        return byteBuffer.array();
    }


    /**
     * Constructs a new {@code DataTransfer} from {@code byte} array source.
     * Source array required to be created via {@link #toByteArray()} method.
     * Properties of a new {@code DataTransfer} and source are identical.
     *
     * @param   sourceArray
     *          the byte array.
     * @return  a new {@code DataTransfer}.
     * @throws  IOException
     *          If {@code ImageData} object failed validation tests, or {@code message}
     *          read object fail.
     * @throws  IllegalArgumentException
     *          If {@code sourceArray} is {@code null} or have not enough size.
     */

    public static DataTransfer fromByteArray(byte[] sourceArray) throws IOException {
        byte[] imageByteArray;
        String[] message=null;
        DataTransfer dataTransfer;
        ImageData imageData=null;

        if (sourceArray==null||sourceArray.length<dataLength){
            throw new IllegalArgumentException("Non valid data");
        }
        ByteBuffer byteBuffer=ByteBuffer.wrap(sourceArray);

        double version=byteBuffer.getDouble();
        int overallSize=byteBuffer.getInt();
        int imageByteSize=byteBuffer.getInt();
        int messageByteSize=byteBuffer.getInt();
        int transferCode=byteBuffer.getInt();
        int position=byteBuffer.getInt();
        int command=byteBuffer.getInt();

        if (version!=versionID||overallSize!=sourceArray.length||command>Command.values().length) {
            String exception="Version: "+version+", size: " + overallSize+"command: "+command;
            throw  new IOException("Decode error: "+exception);
        }


        if (imageByteSize>0) {
            imageByteArray=new byte[imageByteSize];
            byteBuffer.get(imageByteArray);
            imageData=ImageDataUtil.deserialize(imageByteArray);

        }
        if (messageByteSize>0) {
            byte[] messageByteArray=new byte[messageByteSize];
            byteBuffer.get(messageByteArray);
            ByteArrayInputStream bais=new ByteArrayInputStream(messageByteArray);
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                message= (String[]) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }

        }

        dataTransfer=new DataTransfer(imageData,Command.class.getEnumConstants()[command], position,message);
        dataTransfer.setTransferCode(transferCode);
        return dataTransfer;
    }


    @Override
    public String toString() {

        String str="Image: "+(this.imageData!=null?imageData.toString():"no image");
        str+=", message length : "+(this.message!=null?message.length:"no message");
        str+=", command: "+ this.command.name();
        str+=", position: "+this.position;
        str+=", code: "+transferCode;
        str+=", version: "+versionID;
        return str;
    }

    /**
     * Returns the string representation of the {@code DataTransfer} argument.
     *
     * @param   dataTransfer   a {@code DataTransfer}.
     * @return  if the argument is {@code null}, then a string equal to
     *          {@code "null"}; otherwise, the value of
     *          {@code dataTransfer.toString()} is returned.
     * @see     Object#toString()
     */
    public static String getDescription(DataTransfer dataTransfer) {
        return dataTransfer==null?"null":dataTransfer.toString();
    }


}
