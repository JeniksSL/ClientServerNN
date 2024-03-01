package com.clientservernn.client.additional;

import com.clientservernn.dataTransfer.DataTransfer;

public class Transfer implements Transferable {

    DataTransfer dataTransfer;

    public Transfer(DataTransfer dataTransfer) {
        this.dataTransfer = dataTransfer;
    }

    @Override
    public DataTransfer getTransferOut() {
        return dataTransfer;
    }

    @Override
    public void setTransferIn(DataTransfer dataTransferIn) {
        dataTransfer=null;
    }
}
