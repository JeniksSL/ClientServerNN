
package com.clientservernn.server.utilities;

import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.server.guiFX.ExceptionHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javafx.util.Pair;

/**
 * The class {@code RawCharData} contains methods for performing operations
 * with data of character images. For one character can be instantiated only one
 * object {@code RawCharData} and it can not be used outside class.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public final class RawCharData {

    /**
     * The {@link ArrayList} that contains all images data as byte array.
     * In fact, it corresponds to an array {@code imageArgb} from standard {@link ImageData}
     * received from getStandardData() method {@link com.clientservernn.dataTransfer.ImageDataUtil}
     * class.
     *
     */
    private final ArrayList<byte[]> dataList;

    /**
     * The static {@link ArrayList} that contains all instances of {@code RawCharData} class.
     *
     */
    private static final ArrayList<RawCharData> listOfClassInstances = new ArrayList<>();

    /**
     * The character (letter) that corresponds {@code RawCharData}.
     *
     */
    private final String character;
    /**
     * The charset that corresponds {@code RawCharData}.
     *
     */
    private final String charset;

    /**
     * The time value of the base timer to delete the class instance.
     *
     */
    private static final long timerCount = 100000L;
    /**
     * The timer to delete the class instance from {@code listOfClassInstances}.
     *
     */
    private Timer freeTimer;

    /**
     * Allocates a new {@code RawCharData} corresponds requested {@code charset} and {@code character}.
     * Automatically downloads saved data to {@code dataList} from storage
     * uses {@link FileManager} method {@code loadObject()}.
     * If data can not be downloaded {@code dataList} declared as new {@link ArrayList}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     *
     */
    private RawCharData(String charset, String character) {
        this.character = character;
        this.charset = charset;
        this.dataList =  FileManager.loadObject(charset, character, new ArrayList<byte[]>()).orElse(new ArrayList<>());
        //Check for null arrays in datalist.
        int i = 0;
        while(i < this.dataList.size()) {
            if (this.dataList.get(i) == null) {
                this.dataList.remove(i);
            } else {
                ++i;
            }
        }
        this.freeTimer = new Timer();
    }

    /**
     * <p>Returns the instance of {@code RawCharData} corresponds
     * requested {@code charset} and {@code character} from {@code listOfClassInstances}.
     * If no such instance in {@code listOfClassInstances}, allocate a new {@code RawCharData}
     * and add it to {@code listOfClassInstances}.<p/>
     * <p>Sets a timer for the existence of an object, after the timer ends instance of {@code RawCharData}
     * is removed from the {@code listOfClassInstances}. Since in other classes is not stored a reference
     * to this instance, in fact, the instance is subject to removal from memory.
     * If the timer already existed, resets it.<p/>
     * To prevent concurrent access to the {@code listOfClassInstances}, the method is synchronized by it.
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     *
     * @return the instance of {@code RawCharData} corresponds
     * requested {@code charset} and {@code character}.
     *  @throws NullPointerException if any param is null.
     */
    private static RawCharData getInstance(String charset, String character) throws NullPointerException {
        Objects.requireNonNull(charset);
        Objects.requireNonNull(character);
        RawCharData data=null;
        synchronized (listOfClassInstances) {
            if (!listOfClassInstances.isEmpty()) {
                for (RawCharData rowCharData : listOfClassInstances) {
                    if (rowCharData.equalsTo(charset, character)) {
                        data = rowCharData;
                        break;
                    }
                }
            }
            if (data==null) {
                data= new RawCharData(charset, character);
                listOfClassInstances.add(data);
            }
            long variableTimer=timerCount/(listOfClassInstances.size()+1);
            data.setTimer(variableTimer);
        }
        return data;
    }

    /**
     * Sets a timer in milliseconds {@code millis} for the existence of an object,
     * after the timer ends, instance of {@code RawCharData} is removed from the
     * {@code listOfClassInstances}.
     * To prevent concurrent access to the {@code listOfClassInstances}, the remove operation is synchronized by it.
     * If the timer already existed, cancels and resets it.
     * Before deletion, the {@code RawCharData} instance is saved by the {@code save()} method.
     * If any save exception occurs, the class instance is not removed from {@code listOfClassInstances} and
     * caught exception adds to {@link ExceptionHandler}.
     * If {@code millis} <0 set millis as 0 and adds to {@link ExceptionHandler} new {@link IllegalArgumentException}
     * "Incorrect timer set".
     * @param  millis
     *         time in milliseconds for timer set.
     */
    private void setTimer(long millis){
        final RawCharData thisObj = this;
        if (millis < 0L) {
            millis=0;
            ExceptionHandler.setException(thisObj.toString(), new IllegalArgumentException("Incorrect timer set"));
        }
        final long timerSet=millis;
        this.freeTimer.cancel();
        this.freeTimer = new Timer();
        this.freeTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    thisObj.save();
                } catch (IOException exception) {
                    ExceptionHandler.setException(thisObj.toString(), exception);
                    return;
                }
                synchronized (listOfClassInstances){
                    listOfClassInstances.remove(thisObj);
                    System.out.println(thisObj+", timer set: "+timerSet+"remove");
                }
            }
        }, millis);
    }

    /**
     * Checks if saved in storage {@code datalist} and current {@code datalist} of {@code RawCharData} instance
     * is same by method {@code compareDataList()}, and if it is not same, saves {@code datalist} of
     * {@code RawCharData} instance in storage.
     * Uses {@link FileManager} methods {@code loadObject()} and {@code saveAsObject()}.
     * For clarity, also saves {@code datalist} as CSV file use {@link FileManager}.saveAsCSV() method.
     * @throws IOException if {@link FileManager} save operations throws it.
     */
    private void save() throws IOException {
        ArrayList<byte[]> savedList = FileManager.loadObject(this.charset, this.character, new ArrayList<byte[]>()).orElse(new ArrayList<>());
        if (!this.dataList.equals(savedList)) {
            FileManager.saveAsObject(this.charset, this.character, this.dataList);
            FileManager.saveAsCSV(this.charset, this.character, this.dataList);
        }
    }

    /**
     * Compares this {@code RawCharData} {@code charset} and {@code character} to another
     * {@code charset} and {@code character} using {@code equals()}
     * {@link String} method.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     *
     * @return  {@code true} if the arguments is not {@code null} and its
     *          represents an equivalent {@code Strings}; {@code
     *          false} otherwise
     *
     */
    private boolean equalsTo(String charset, String character) {
        return this.character.equals(character) && this.charset.equals(charset);
    }

    /**
     * Creates tne new {@link List} with {@link Pair} of {@link String} key represents
     * characters of {@code charset} and value {@link double[]} represents refactored
     * by {@code getDoublesArray()} method data of character image saved as {@code imageArgb}
     * of {@link ImageData}.
     * {@code List} contains all available characters and its data {@code Pairs}.
     *
     * @param  charset
     *         requested charset.
     *
     * @return  {@code List} that contains {@code Pairs} of all available characters and its refactored
     * image data for given {code charset}.
     *
     */
    public static List<Pair<String, double[]>> getTrainData(String charset) {
        Objects.requireNonNull(charset);
        ArrayList<Pair<String, double[]>> trainDataList = new ArrayList<>();
        for (String character : FileManager.getCharList(charset)) {
            RawCharData rawCharData = getInstance(charset, character);
            for (int i = 0; i < rawCharData.size(); ++i) {
                byte[] imageData = getImageData(charset, character, i).getImageArgb();
                double[] inverseColors = getDoublesArray(imageData);
                Pair<String, double[]> pair = new Pair<>(character, inverseColors);
                trainDataList.add(pair);
            }
        }
        return trainDataList;
    }





    public static ImageData getImageData(String path, String letter, int position) throws IllegalArgumentException {
        RawCharData rawCharData = getInstance(path, letter);
        if (position < rawCharData.size()) {
            int size = 100;
            byte[] fullArray = (byte[])rawCharData.dataList.get(position);
            return new ImageData(Arrays.copyOfRange(fullArray, 0, size), 10, 10);
        } else {
            throw new IllegalArgumentException("Position out of index");
        }
    }

    public static Pair<String, Long> getAdditionalData(String path, String letter, int position) {
        long date = 0L;
        String name = "no name";
        RawCharData rawCharData = getInstance(path, letter);
        if (position < rawCharData.size()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap((byte[])rawCharData.dataList.get(position));

            try {
                byteBuffer.get(new byte[100]);
                date = byteBuffer.getLong();
                byte[] nameByte = new byte[byteBuffer.capacity() - 100 - 8];
                byteBuffer.get(nameByte);
                name = new String(nameByte, StandardCharsets.UTF_8);
            } catch (RuntimeException var9) {
                date = 0L;
                name = "no name";
            }
        }

        return new Pair(name, date);
    }

    public static double[] getDoublesArray(byte[] imageArray) {
        double[] doublesArray = new double[imageArray.length];

        for(int i = 0; i < imageArray.length; ++i) {
            doublesArray[i] = (double)(~imageArray[i] & 255);
        }
        return doublesArray;
    }

    public static int size(String path, String letter) {
        RawCharData rawCharData = getInstance(path, letter);
        return rawCharData.size();
    }

    private int size() {
        return this.dataList.size();
    }


    public static void add(String path, String letter, byte[] addArray, String name, long date) {
        RawCharData rawCharData = getInstance(path, letter);
        rawCharData.add(addArray, name, date);
    }

    private void add(byte[] addArray, String name, long date) {
        byte[] nameByte = name.getBytes(StandardCharsets.UTF_8);
        int size = 108 + nameByte.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(addArray);
        byteBuffer.putLong(date);
        byteBuffer.put(nameByte);
        this.dataList.add(byteBuffer.array());
    }

    public static void deleteItem(String path, String letter, int position) {
        RawCharData rawCharData = getInstance(path, letter);
        if (position < rawCharData.size() && position >= 0) {
            rawCharData.dataList.remove(position);
        }

    }

    public String toString() {
        String var10000 = this.character;
        return "RawCharData, letter: " + var10000 + ", path" + this.charset + ", size: " + this.dataList.size();
    }
}
