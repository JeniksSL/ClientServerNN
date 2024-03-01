package com.clientservernn.client.guiFX;

import com.clientservernn.dataTransfer.ImageData;
import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.util.Date;

public class ImageTable extends ImageData {

    private final IntegerProperty serial;
    private final StringProperty letter;
    private final ObjectProperty<Changes> change;
    private final StringProperty date;
    private final StringProperty name;
    private long dateTime;

    public enum Changes{No, Add, Delete }


    public ImageTable(String letter, String name, Long date, Integer number, ImageData imageData) {
        super(imageData);
        this.letter = new SimpleStringProperty(letter);
        this.date = new SimpleStringProperty(new Date(date).toString());
        dateTime=date;
        this.name = new SimpleStringProperty(name);
        this.serial=new SimpleIntegerProperty(number);
        this.change=new SimpleObjectProperty<Changes>(Changes.No);

    }
    public void setAsAdd() {
        if (change.get().equals(Changes.Add)) {
            change.set(Changes.No);
        } else {change.set(Changes.Add);}
    }
    public void setAsDelete(){
        if (change.get().equals(Changes.Delete)) {
            change.set(Changes.No);
        } else {change.set(Changes.Delete);}
    }
    public String getName() {
        return name.get();
    }

    public String getLetter(){
        return letter.get();
    }
    public long getLongTime(){
        return dateTime;
    }

    public String getDate(){return date.get();}
    public Integer getSerial(){
        return serial.get();
    }
    public Image getImage(){
        return super.getImage();
    }
    public Changes getChange(){
        return change.get();
    }
    public ImageData getImageData(){
        return new ImageData(this);
    }

}
