
package com.clientservernn.common;

/**
 * This enum contains dialog types for {@link com.clientservernn.client.guiFX.DialogHandler}
 * and for {@link com.clientservernn.server.guiFX.DialogHandler} classes.

 * @author  Yauheni Slabko
 * @since   1.0
 */

public enum Dialogues {
    SERVER_START,
    SERVER_STOP,
    START_NETWORK,
    LOAD_NETWORK,
    TRAIN_NETWORK,
    CONNECT,
    UPLOAD,
    DOWNLOAD,
    CHARSET,
    USER_DATA;

    private Dialogues() {
    }
}