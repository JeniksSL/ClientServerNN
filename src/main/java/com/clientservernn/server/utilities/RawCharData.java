
package com.clientservernn.server.utilities;

import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.dataTransfer.ImageDataUtil;
import com.clientservernn.server.guiFX.ExceptionHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javafx.util.Pair;

import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_HEIGHT;
import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_WIDTH;

/**
 * The class {@code RawCharData} contains methods for performing operations
 * with data of character images. For one character can be instantiated only one
 * object {@code RawCharData} and it can not be used outside class.
 *
 * @author  Yauheni Slabko
 * @see ExceptionHandler
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
                    System.out.println(thisObj+", timer set: "+timerSet+" ms");
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
     * Returns the new {@link List} with {@link Pair} of {@link String} key represents
     * characters of {@code charset} and value {@link double[]} represents refactored
     * by {@code getDoublesArray()} method data of character image saved as {@code imageArgb}
     * of {@link ImageData}.
     * {@code List} contains all available characters and its data {@code Pairs}.
     *
     * @param  charset
     *         requested charset.
     *
     * @return  {@code List} that contains {@code Pairs} of all available characters and its refactored
     * image data for given {@code charset}.
     *  @throws NullPointerException if {@code charset} is null.
     */
    public static List<Pair<String, double[]>> getTrainData(String charset) throws NullPointerException {
        Objects.requireNonNull(charset);
        ArrayList<Pair<String, double[]>> trainDataList = new ArrayList<>();
        for (String character : FileManager.getCharList(charset)) {
            RawCharData rawCharData = getInstance(charset, character);
            for (int i = 0; i < rawCharData.size(); ++i) {
                byte[] imageData = getImageData(charset, character, i).getImageArgb();
                double[] inverseColors = getDoublesArrayRefactored(imageData);
                Pair<String, double[]> pair = new Pair<>(character, inverseColors);
                trainDataList.add(pair);
            }
        }
        return trainDataList;
    }

    /**
     * Returns the new standard {@link ImageData} recovered from saved in {@code datalist}
     * byte array at given {@code index} of {@code RawCharData} corresponds given {@code charset}
     * and {@code character}.
     * If new {@code ImageData} is not standard by any casualties, returns new standard {@link ImageData}
     * with fully white image, and adds exception to {@link ExceptionHandler}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     * @param  index
     *         index of byte array in {@code datalist}.
     *
     * @return  the new standard {@link ImageData} stored in corresponds {@code RawCharData} under
     * given index.
     *  @throws NullPointerException if any param is null.
     *  @throws IndexOutOfBoundsException if index is out of {@code dataList} size.
     *
     */
    public static ImageData getImageData(String charset, String character, int index) throws IndexOutOfBoundsException, NullPointerException {
        RawCharData rawCharData = getInstance(charset, character);
        Objects.checkIndex(index,rawCharData.size());
        int standardSize = DATA_WIDTH*DATA_HEIGHT;
        byte[] fullArray = rawCharData.dataList.get(index);
        ImageData standardImageData;
        try {
            standardImageData= new ImageData(Arrays.copyOfRange(fullArray, 0, standardSize), DATA_WIDTH, DATA_HEIGHT);
        } catch (IllegalArgumentException exception) {
            standardImageData= ImageDataUtil.getStandardWhite();
            String source=ExceptionHandler.getSource(RawCharData.class,"getImageData()",String.class, charset, String.class, character, Integer.class,index);
            ExceptionHandler.setException(source, exception);
        }
        return standardImageData;
    }

    /**
     * Returns the additional data about {@link ImageData} that is name of user and creation date, stored
     * in {@code datalist} at given {@code index} of {@code RawCharData}
     * corresponds given {@code charset} and {@code character}. Data returns as {@link Pair}
     * with key {@link String} name  and value {@link Long} date in milliseconds since Unix Epoch.
     * If no date available returns new the following kind {@code Pair<"no name", 0L>}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     * @param  index
     *         index of byte array in {@code datalist}.
     *
     * @return  the additional data about {@link ImageData} as {@link Pair}
     *        with key {@link String} name and value {@link Long} date, stored
     *        in corresponds {@code RawCharData} under given index.
     *        If no date available returns {@code Pair<"no name", 0L>}.
     *  @throws NullPointerException if any param is null.
     *  @throws IndexOutOfBoundsException if index is out of {@code dataList} size.
     *
     */
    public static Pair<String, Long> getAdditionalData(String charset, String character, int index) throws IndexOutOfBoundsException, NullPointerException  {
        long date;
        String name;
        RawCharData rawCharData = getInstance(charset, character);
        Objects.checkIndex(index,rawCharData.size());
        ByteBuffer byteBuffer = ByteBuffer.wrap(rawCharData.dataList.get(index));
        try {
            byteBuffer.get(new byte[DATA_WIDTH*DATA_HEIGHT]);
            date = byteBuffer.getLong();
            byte[] nameByte = new byte[byteBuffer.capacity() - byteBuffer.position()];
            byteBuffer.get(nameByte);
            name = new String(nameByte, StandardCharsets.UTF_8);
        } catch (RuntimeException exception) {
            date = 0L;
            name = "no name";
        }
        return new Pair<>(name, date);
    }

    /**
     * Returns {@code double} array represents refactored initial {@code imageArray}
     * with same length. Initial {@code imageArray} represents {@code imageARGB} {@link ImageData}
     *
     * @param   imageArray
     *         initial byte array.
     *
     * @return  @code double} array represents refactored initial {@code imageArray}.
     * @throws NullPointerException if {@code imageArray} is null.
     *
     */
    public static double[] getDoublesArrayRefactored(byte[] imageArray) throws NullPointerException {
        Objects.requireNonNull(imageArray);
        //The network input is an array of double.
        double[] doublesArray = new double[imageArray.length];
        //In imageArray white color is 0xFF, and black is 0x00. In this case max signal (black)
        //has min value. For uses in neural network data should be modified in such a way, that
        //max signal (black) will have a max value. For this used operation of logical bitwise no (~).
        for(int i = 0; i < imageArray.length; ++i) {
            doublesArray[i] = (~imageArray[i] & 0xFF);
        }
        return doublesArray;
    }

    /**
     * Returns size of {@code datalist} of {@code RawCharData}
     * corresponds given {@code charset} and {@code character}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     *
     * @return  Returns size of {@code datalist} of {@code RawCharData}
     * corresponds given {@code charset} and {@code character}.
     *  @throws NullPointerException if any param is null.
     *
     */
    public static int size(String charset, String character) {
        RawCharData rawCharData = getInstance(charset, character);
        return rawCharData.dataList.size();
    }

    /**
     * Returns size of {@code datalist} of this {@code RawCharData} instance.
     * @return  Returns size of {@code datalist} of this {@code RawCharData} instance.
     */
    private int size() {
        return this.dataList.size();
    }

    /**
     * Adds the {@link ImageData} and the additional data about it (name of user and creation date), to
     * {@code datalist} of {@code RawCharData} corresponds given {@code charset} and {@code character}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     * @param  imageData
     *         imageData to add.
     * @param  name
     *         name of user.
     * @param  date
     *         creation date in milliseconds since Unix Epoch.
     *  @throws NullPointerException if any param is null.
     *  @throws IllegalArgumentException if {@code imageData} is not standard.
     *
     */
    public static void add(String charset, String character, ImageData imageData, String name, long date) {
        RawCharData rawCharData = getInstance(charset, character);
        byte[] storedArray=convertToStoredArray(imageData, name,date);
        rawCharData.dataList.add(storedArray);
    }

    /**
     * Convert the {@link ImageData} and the additional data about it (name of user and creation date), to
     * byte array of kind that stored in {@code datalist} of {@code RawCharData}.
     *
     * @param  imageData
     *         imageData to add.
     * @param  name
     *         name of user.
     * @param  date
     *         creation date in milliseconds since Unix Epoch.
     * @return byte array that contains all data about standard {@link ImageData}.
     *  @throws NullPointerException if any param is null.
     *  @throws IllegalArgumentException if {@code imageData} is not standard.
     *
     */
    private static byte[] convertToStoredArray (ImageData imageData, String name, long date){
        Objects.requireNonNull(imageData);
        Objects.requireNonNull(name);
        if (!imageData.isStandard()) {
            IllegalArgumentException exception=new IllegalArgumentException("Not standard data");
            String source=ExceptionHandler.getSource(RawCharData.class,"add()", ImageData.class,Arrays.toString(imageData.getImageArgb()));
            ExceptionHandler.setException(source,exception);
            throw exception;
        }
        byte[] nameByte = name.getBytes(StandardCharsets.UTF_8);
        int size = DATA_WIDTH*DATA_HEIGHT+Long.BYTES + nameByte.length;
        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.put(imageData.getImageArgb());
        byteBuffer.putLong(date);
        byteBuffer.put(nameByte);
        return byteBuffer.array();
    }

    /**
     * Removes byte array of data about given {@code imageData} with additional data
     * from {@code datalist} of {@code RawCharData} corresponds given {@code charset} and {@code character}.
     * If there is no such byte array in {@code imageData}, then no changes in {@code imageData}.
     *
     * @param  charset
     *         requested charset.
     * @param  character
     *         requested character (letter).
     * @param  imageData
     *         imageData to add.
     * @param  name
     *         name of user.
     * @param  date
     *         creation date in milliseconds since Unix Epoch.
     *  @throws NullPointerException if any param is null.
     *  @throws IllegalArgumentException if {@code imageData} is not standard.
     *
     */

    public static void deleteItem(String charset, String character, ImageData imageData, String name, long date) {
        RawCharData rawCharData = getInstance(charset, character);
        byte[] storedArray=convertToStoredArray(imageData, name,date);
        rawCharData.dataList.remove(storedArray);
    }

    @Override
    public String toString() {
        return "RawCharData, character: " + this.character + ", charset" + this.charset + ", size: " + this.dataList.size();
    }
}
