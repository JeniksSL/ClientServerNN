
package com.clientservernn.server.utilities;

import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.server.guiFX.ExceptionHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.util.Pair;

public final class RawCharData {
    private ArrayList<byte[]> dataList;
    private static final ArrayList<RawCharData> listOfClassInstances = new ArrayList();
    private final String letter;
    private final String path;
    private static long timerCount = 10000L;
    private Timer freeTimer;

    private RawCharData(String letter, String path) {
        this.letter = letter;
        this.path = path;
        this.dataList = new ArrayList<>();
        String subPath = path + "\\" + letter;
        this.dataList =  FileManager.loadObject(subPath, this.dataList).orElse(new ArrayList<>());
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

    private void setTimer(long millis) {
        final String thisObj = this.toString();
        if (millis <= 0L) {
            this.freeTimer.cancel();
            this.freeTimer = new Timer();
        } else {
            this.freeTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        System.out.println(thisObj);
                        RawCharData.this.free();
                    } catch (IOException var2) {
                        ExceptionHandler.setException(thisObj, var2);
                    }

                }
            }, millis);
        }

    }

    private void save() throws IOException {
        String subPath = this.path + "\\" + this.letter;
        FileManager.saveAsObject(subPath, this.dataList);
        FileManager.saveAsCSV(subPath, this.dataList);
    }

    private void free() throws IOException {
        String subPath = this.path + "\\" + this.letter;
        ArrayList<byte[]> savedList = FileManager.loadObject(subPath, this.dataList).orElse(new ArrayList<>());
        if (!this.compareDataList(savedList)) {
            try {
                this.save();
            } catch (IOException var4) {
                throw var4;
            }
        }

        listOfClassInstances.remove(this);
    }

    public static List<Pair<String, double[]>> getTrainData(String path) {
        ArrayList<Pair<String, double[]>> trainDataList = new ArrayList();
        Iterator var2 = FileManager.getCharList(path).iterator();

        while(var2.hasNext()) {
            String letter = (String)var2.next();
            RawCharData rawCharData = getInstance(path, letter);

            for(int i = 0; i < rawCharData.size(); ++i) {
                byte[] imageData = getImageData(path, letter, i).getImageArgb();
                double[] inverseColors = getDoublesArray(imageData);
                Pair<String, double[]> pair = new Pair(letter, inverseColors);
                trainDataList.add(pair);
            }
        }

        return trainDataList;
    }

    private static synchronized RawCharData getInstance(String path, String letter) {
        RawCharData data = new RawCharData(letter, path);
        long timer;
        if (!listOfClassInstances.isEmpty()) {

            for (RawCharData rowCharData : listOfClassInstances) {
                if (rowCharData.compareTo(letter, path)) {
                    rowCharData.setTimer(-1L);
                    data = rowCharData;
                }
            }
        }
        long variableTimer=timerCount/(listOfClassInstances.size()+1);
        data.setTimer(variableTimer);
        listOfClassInstances.add(data);
        return data;
    }

    private boolean compareTo(String letter, String path) {
        return this.letter.compareToIgnoreCase(letter) == 0 && this.path.compareToIgnoreCase(path) == 0;
    }

    private boolean compareDataList(ArrayList<byte[]> savedList) {
        boolean isEqual = savedList.size() == this.dataList.size();

        for(int i = 0; isEqual && i < this.dataList.size(); ++i) {
            isEqual = Arrays.compare((byte[])this.dataList.get(i), (byte[])savedList.get(i)) == 0;
        }

        return isEqual;
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
        String var10000 = this.letter;
        return "RawCharData, letter: " + var10000 + ", path" + this.path + ", size: " + this.dataList.size();
    }
}
