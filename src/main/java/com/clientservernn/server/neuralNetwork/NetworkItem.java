
package com.clientservernn.server.neuralNetwork;

import com.clientservernn.common.CharsetList;
import com.clientservernn.server.utilities.FileManager;
import java.util.Date;
import java.util.Objects;

import javafx.beans.property.*;

/**
 * The class  {@code NetworkItem} represents {@code NetworkCommander} instance for
 * displaying in {@link javafx.collections.ObservableList} of {@link javafx.scene.control.TableView}.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public class NetworkItem {

    //Properties of {@code NetworkCommander} instance.
    public final IntegerProperty numberItems;
    public final ObjectProperty<CharsetList> charset;
    public final BooleanProperty access;

    /**
     * The {@link String} representation of last network training date.
     * If date is 0 in milliseconds since Unix Epoch, sets as "Not trained".
     * Also, if {@code NetworkCommander} instance is at training process, training date set as progress of training process.
     */
    public final StringProperty trainDate;
    public final StringProperty lastModified;


    //Getters for properties.
    public int getNumberItems() {
        return this.numberItems.get();
    }

    public CharsetList getCharset() {
        return this.charset.get();
    }

    public boolean isAccess() {
        return this.access.get();
    }


    public String getTrainDate() {
        return this.trainDate.get();
    }

    public String getLastModified() {
        return this.lastModified.get();
    }

    /**
     * Allocate the new {@code NetworkItem} for given {@link NetworkCommander} instance.        *
     * @param  networkCommander
     *         given networkCommander instance.
     * @throws NullPointerException if {@code networkCommander} is null.
     */
    public NetworkItem(NetworkCommander networkCommander) {
        Objects.requireNonNull(networkCommander);
        this.charset = new SimpleObjectProperty<CharsetList>(networkCommander.getCharset());
        this.numberItems = new SimpleIntegerProperty(networkCommander.getCharacterList().size());
        this.access = new SimpleBooleanProperty(networkCommander.isAccess());
        Date date = networkCommander.getTrainDate();
        String dateStr = date.compareTo(new Date(0L)) > 0 ? date.toString() : "Not trained";
        this.trainDate = new SimpleStringProperty(dateStr);
        this.lastModified = new SimpleStringProperty(FileManager.lastModified(this.charset.get().name()));
    }


    public void setProgress(String progress) {
        this.trainDate.set(progress);
    }



}
