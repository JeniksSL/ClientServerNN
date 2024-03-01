
package com.clientservernn.server.guiFX;

import com.clientservernn.dataTransfer.Command;
import com.clientservernn.dataTransfer.DataTransfer;

import java.net.Socket;
import java.util.Date;

import javafx.beans.property.*;

public class ClientItem implements ComparableTo<Integer> {
    private final StringProperty ipAddress;
    private final StringProperty name;
    private final ObjectProperty<Date> date;
    private final ObjectProperty<DataTransfer> lastTransfer;
    private final IntegerProperty hash;

    public ClientItem(Socket socket) {
        this.ipAddress = new SimpleStringProperty(socket.getInetAddress().getHostAddress());
        this.name = new SimpleStringProperty("unsigned");
        this.date = new SimpleObjectProperty<>(new Date());
        this.lastTransfer = new SimpleObjectProperty<>(new DataTransfer(null, Command.USER_DATA, (String)null));
        this.hash=new SimpleIntegerProperty(this.name.get().hashCode());
    }
    public ClientItem(ClientItem clientItem) {
        this.ipAddress = clientItem.ipAddress;
        this.name = clientItem.name;
        this.date = clientItem.date;
        this.lastTransfer = clientItem.lastTransfer;
        this.hash=clientItem.hash;
    }


    public String getIpAddress() {
        return (String)this.ipAddress.get();
    }

    public String getName() {
        return (String)this.name.get();
    }

    public Date getDate() {
        return (Date)this.date.get();
    }

    public DataTransfer getLastTransfer() {
        return (DataTransfer)this.lastTransfer.get();
    }

    public void setName(String name) {
        this.name.set(name);
        this.hash.set(name.hashCode());
    }
    public void setLastTransfer(DataTransfer lastTransfer) {
        this.lastTransfer.set(lastTransfer);
    }

    public Integer getHash() {
        return this.hash.get();
    }

    @Override
    public int hashCode() {
        return this.hash.get();
    }

    @Override
    public Integer getCompareIndex () {
        return this.hashCode();
    }
}
