//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.clientservernn.server.neuralNetwork;


import com.clientservernn.common.CharsetList;
import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.server.utilities.FileManager;
import com.clientservernn.server.utilities.RawCharData;

import java.util.*;

import javafx.util.Pair;

import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_HEIGHT;
import static com.clientservernn.dataTransfer.ImageDataUtil.DATA_WIDTH;

/**
 * The class {@code NetworkCommander} wraps neural network for given {@code charset} and
 * contains methods for performing operations with wrapped network.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */
public class NetworkCommander {


    /**
     * The {@code charset} of  {@code NetworkCommander} instance.
     *
     */
    private final CharsetList charset;

    /**
     * The {@link List} that contains all characters of given {@code charset}.
     *
     */
    private final List<String> characterList;
    /**
     * The wrapped {@link Network} with {@link String} type objects.
     *
     */
    private final Network<String> characterNetwork;

    /**
     * The boolean variable indicating the enabling to access network.
     *
     */
    private boolean access;

    /**
     * The date of last network training.
     *
     */
    private Date trainDate;

    /**
     * The current data for network training.
     *
     */
    List<Pair<String, double[]>> trainData;


    /**
     * Allocate the new {@code NetworkCommander} for given {@code charset}.
     * Data for {@code characterList} and {@code trainData} loads from
     * storage. Based on {@code characterList} size and standard {@code DATA_WIDTH}
     * and {@code  DATA_HEIGHT} dimensions from {@link com.clientservernn.dataTransfer.ImageDataUtil}
     * allocate wrapped neural network {@code characterNetwork}.
     *
     * @param  charset
     *         given charset.
     * @throws NullPointerException if {@code charset} is null.
     */
    public NetworkCommander(CharsetList charset) {
        Objects.requireNonNull(charset);
        this.charset = charset;
        this.characterList = FileManager.getCharList(charset.name());
        this.access = false;
        this.trainDate = new Date(0L);
        this.trainData = RawCharData.getTrainData(charset.name());
        this.characterNetwork = new Network<>(new int[]{DATA_WIDTH*DATA_HEIGHT, this.characterList.size()}, 0.1, Util::sigmoid, Util::derivativeSigmoid);

    }

    /**
     * Refreshes data in {@code characterList} by loading from storage.
     */
    public void refreshTrainData() {
        this.trainData = RawCharData.getTrainData(this.charset.name());
    }

    /**
     * Method that used as {@link java.util.function.Function} for interpreting
     * output of neural network work results.
     * @param  output
     *         results from neural network.
     * @return {@link HashMap} with all characters of {@code characterList}
     * as keys {@link String} and values of network output for each key character
     * as value {@link Double}.
     */
    public HashMap<String, Double> interpretOutputMap(double[] output) {
        HashMap<String, Double> hashMap = new HashMap<>();
        for(int i = 0; i < output.length; ++i) {
            hashMap.put(this.characterList.get(i), output[i]);
        }

        return hashMap;
    }


    /**
     * Trains a {@code characterNetwork} once based on available characters
     * from {@code characterList} and characters data from {@code trainData}.
     * Data of characters not presented in {@code characterList} from {@code trainData}
     * discarded and not used for training.
     */
    public void train() {
        List<double[]> dataSet = new ArrayList<>();
        List<double[]> resultArray = new ArrayList<>();
        Collections.shuffle(this.trainData);
        for (Pair<String, double[]> trainPair : this.trainData) {
            String character=trainPair.getKey();
            if (characterList.contains(character)) {
                dataSet.add(trainPair.getValue());
                double[] classification = new double[this.characterList.size()];
                Arrays.fill(classification, 0.0);
                classification[this.characterList.indexOf(trainPair.getKey())] = 1.0;
                resultArray.add(classification);
            }
        }
        Util.normalizeByFeatureScaling(dataSet);
        this.characterNetwork.train(dataSet, resultArray);
        this.trainDate = new Date();
    }

    /**
     * Puts data from {@code imageData} on {@code characterNetwork} input
     * and returns results interpreted {@link NetworkCommander} interpretOutputMap() method
     * as new {@link HashMap}.
     * @param  imageData checked standard {@link ImageData} instance.
     * @return {@link HashMap} with all characters of {@code characterList}
     * as keys {@link String} and values of network output for each key character
     * as value {@link Double}.
     * @throws NullPointerException if any param is null.
     * @throws IllegalArgumentException if {@code imageData} is not standard.
     */
    public HashMap<String, Double> checkAll(ImageData imageData) {
        Objects.requireNonNull(imageData);
        if (imageData.isStandard()) {
            byte[] imageARGB= imageData.getImageArgb();
            return this.characterNetwork.getCheck(imageARGB, this::interpretOutputMap);
        } else throw new IllegalArgumentException("ImageData not standard");

    }

    /**
     * Reverse access value of this {@code NetworkCommander}.
     */
    public void changeAccess() {
        this.access = !this.access;
    }

    /**
     * Returns {@code charset} of this {@code NetworkCommander}.
     * @return  {@code charset} of this {@code NetworkCommander}.
     */
    public CharsetList getCharset() {
        return this.charset;
    }

    /**
     * Returns {@code access} value of this {@code NetworkCommander}.
     * @return  value of {@code access}.
     */
    public boolean isAccess() {
        return this.access;
    }

    /**
     * Returns last training date of this {@code NetworkCommander}.
     * @return  last training date.
     */
    public Date getTrainDate() {
        return this.trainDate;
    }
    /**
     * Returns current {@code characterList} of this {@code NetworkCommander}.
     * @return  current {@code characterList}.
     */
    public List<String> getCharacterList() {
        return characterList;
    }


}
