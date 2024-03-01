package com.clientservernn.client.additional;

import com.clientservernn.dataTransfer.DataTransfer;


public interface Transferable {

    int loadedAtStart=0;
    DataTransfer getTransferOut();
    void setTransferIn(DataTransfer dataTransferIn);
    default boolean isReceive(){
        return false;
    }


}
