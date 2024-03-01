
package com.clientservernn.server.guiFX;

import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExceptionItem implements ComparableTo<Long> {
    private final ObjectProperty<Date> date;
    private final StringProperty source;
    private final ObjectProperty<Exception> exception;

    public ExceptionItem(Date date, String source, Exception exception) {
        this.date = new SimpleObjectProperty<>(date);
        this.source = new SimpleStringProperty(source);
        this.exception = new SimpleObjectProperty<>(exception);
    }

    public String getSource() {
        return (String)this.source.get();
    }

    public String getException() {
        return ((Exception)this.exception.get()).getMessage();
    }

    public Date getDate() {
        return (Date)this.date.get();
    }

    @Override
    public Long getCompareIndex() {
        return  this.getDate().getTime();
    }

}
