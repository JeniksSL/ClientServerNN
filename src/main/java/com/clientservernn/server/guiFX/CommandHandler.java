

package com.clientservernn.server.guiFX;


import com.clientservernn.common.CharsetList;
import com.clientservernn.dataTransfer.Command;
import com.clientservernn.dataTransfer.DataTransfer;
import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.dataTransfer.ImageDataUtil;
import com.clientservernn.server.neuralNetwork.NetworkCommander;
import com.clientservernn.server.neuralNetwork.NetworkItem;
import com.clientservernn.server.utilities.FileManager;
import com.clientservernn.server.utilities.RawCharData;
import javafx.util.Pair;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

/**
 * The class {@code CommandHandler} represents class that manages
 * neural networks through the {@link NetworkCommander} and provides methods
 * from nested interface {@link HandlingMethods} for processing client requests.
 * When a handler method is selecting, checks the permission level of the client.
 * If the permission level is insufficient, it issues the appropriate method:
 * {@link HandlingMethods#lowUserPermission(DataTransfer)}.
 * @see NetworkCommander
 * @see NetworkItem
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */
public final class CommandHandler {

    /**
     * The {@link EnumMap} that contains all loaded {@code NetworkCommander} instances
     * with enum {@link CharsetList} keys.
     */
    public static EnumMap<CharsetList, NetworkCommander> networkList = new EnumMap<>(CharsetList.class);


    private CommandHandler() {
    }


    /**
     * Returns {@link ArrayList} with {@link NetworkItem}s that
     * represents all loaded NetworkCommanders.
     *
     * @return list of {@code NetworkItem}.
     */
    public static ArrayList<NetworkItem> getNetworkItems(){
        ArrayList<NetworkItem> networkItems=new ArrayList<>();
        for (NetworkCommander network : networkList.values()) {
            NetworkItem networkItem = new NetworkItem(network);
            networkItems.add(networkItem);
        }
        return networkItems;
    }

    /**
     * Creates a new {@code NetworkCommander} instance
     * of given {@code charset}, if it was not created yet.
     * And put this new instance in {@code networkList}
     *
     * @param  charset given {@code CharsetList} instance for a network creation.
     */
    public static void uploadNetwork(CharsetList charset) {
        if(!networkList.containsKey(charset)) {
            NetworkCommander networkCommander = new NetworkCommander(charset.name());
            networkList.put(charset, networkCommander);
        }
    }

    /**
     * Returns method from nested interface {@link HandlingMethods} for processing client request.
     * The method is selected according to the client's request {@link DataTransfer}s {@link Command} by
     * {@code private} method {@link CommandHandler#getMethod(String, Command)}.
     * If is requested method is not found returns null and adds {@link NoSuchMethodException} exception
     * to {@link ExceptionHandler}.
     *
     * @param  userName name of the client.
     * @param  dataTransferIn request received from the client.
     * @return {@link Method} according to the client's request
     * or {@code null} if no such method.
     */
    public static Method getResponseMethod(String userName, DataTransfer dataTransferIn) {
        Method method=null;
        try {
            method=getMethod(userName, dataTransferIn.getCommand());
        } catch (NoSuchMethodException e){
            ExceptionHandler.setException(dataTransferIn.toString(), e);
        }
        return method;
    }



    /**
     * Returns method from nested interface {@link HandlingMethods} for processing client request.
     * The method is selected according to the client's request {@link DataTransfer}s {@link Command}.
     * When a handler method is selecting, checks the permission level of the client.
     * If the permission level is insufficient, it returns the appropriate method:
     *  {@link HandlingMethods#lowUserPermission(DataTransfer)}.
     *
     * @param  userName name of the client.
     * @param  command command according to which the method is searched.
     * @return {@link Method} according to the client's request.
     * @throws NoSuchMethodException if requested method is not found.
     */
    private static Method getMethod(String userName, Command command) throws NoSuchMethodException {
       UserPermission userPermission;
       //To simplify, the access level of the client is checked based on its name.
        //Default client name is "unsigned" that corresponds to the lowest permission level.
        if (userName.equalsIgnoreCase(UserPermission.ADMINISTRATOR.name())) {
            userPermission=UserPermission.ADMINISTRATOR;
        } else if (userName.equalsIgnoreCase(UserPermission.UNSIGNED.name())){
            userPermission=UserPermission.UNSIGNED;
        } else {
            userPermission=UserPermission.SIGNED;
        }

        Method method= HandlingMethods.class.getMethod(command.name().toLowerCase(Locale.ROOT), DataTransfer.class);
        UserRestrictions userRestrictions=method.getAnnotation(UserRestrictions.class);
        if (userRestrictions!=null&&userPermission.ordinal()<userRestrictions.permissionType().ordinal()){
            method= HandlingMethods.class.getMethod("lowUserPermission", DataTransfer.class);
        }
        return  method;
    }

    /**
     * The nested interface that provides methods for processing client requests.
     * Names of methods responds enum {@link Command} values in lower case.
     *
     * @author  Yauheni Slabko
     * @since   1.0
     */
    private  interface HandlingMethods {

        /**
         * Returns {@link DataTransfer} with confirmation that the name is accepted
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with confirmation that the name is accepted.
         */
        @UserRestrictions
         static DataTransfer user_data(DataTransfer dataTransferIn) {
            String[] username = dataTransferIn.getMessage();
            return new DataTransfer(null, Command.USER_DATA, username);
        }

        /**
         * Returns {@link DataTransfer} with confirmation that the client is disconnected.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with confirmation that the client is disconnected.
         */
        @UserRestrictions
         static DataTransfer disconnect(DataTransfer dataTransferIn) {
            return new DataTransfer(null, Command.DISCONNECT, (String)null);
        }

        /**
         * Returns {@link DataTransfer} with confirmation that the command is executed.
         * Adds or deletes correspond {@link ImageData} instance and additional information
         * about it that stored in {@link RawCharData} class.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with confirmation that the command is executed.
         */
        @UserRestrictions(permissionType = UserPermission.ADMINISTRATOR)
         static DataTransfer edit(DataTransfer dataTransferIn) {
            String[] message = dataTransferIn.getMessage();
            CharsetList charsetList = CharsetList.valueOf(message[0]);
            String letter = message[1];
            String name = message[2];
            long date = Long.parseLong(message[3]);
            ImageData imageData = ImageDataUtil.getStandard(dataTransferIn.getImageData());
            int position = dataTransferIn.getPosition();
            if (position < 0) {
                RawCharData.add(charsetList.name(), letter, imageData, name, date);
            } else {
                RawCharData.deleteItem(charsetList.name(), letter, imageData, name, date);
            }
            return new DataTransfer(null, Command.EDIT, (String[])null);
        }

        /**
         * Returns {@link DataTransfer} with {@code message},
         * that is array of all requested characters.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with with {@code message},
         * that is array of all requested characters.
         */
        @UserRestrictions(permissionType = UserPermission.SIGNED)
        static DataTransfer learn(DataTransfer dataTransferIn) {
            String[] message = dataTransferIn.getMessage();
            CharsetList charsetList = CharsetList.valueOf(message[0]);
            message = FileManager.getCharList(charsetList.name()).toArray(String[]::new);
            return new DataTransfer(null, Command.LEARN, message);
        }

        /**
         * Returns {@link DataTransfer} with requested {@link ImageData} instance and
         * additional information about it.
         * If index of {@link DataTransfer#getPosition()} is negative
         * returns {@link DataTransfer} only with number of stored requested character
         * samples.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with data obout image or number of
         * stored requested character samples.
         */
        @UserRestrictions(permissionType = UserPermission.SIGNED)
        static DataTransfer upload(DataTransfer dataTransferIn) {
            DataTransfer dataTransferOut;
            String[] message = dataTransferIn.getMessage();
            CharsetList charsetList = CharsetList.valueOf(message[0]);
            String letter = message[1];
            int position = dataTransferIn.getPosition();
            if (position < 0) {
               dataTransferOut = new DataTransfer(null, Command.UPLOAD, RawCharData.size(charsetList.name(), letter), message);
            } else {
                try {
                    ImageData imageData = RawCharData.getImageData(charsetList.name(), letter, position);
                    Pair<String, Long> stringLongPair = RawCharData.getAdditionalData(charsetList.name(), letter, position);
                    String name = stringLongPair.getKey();
                    long date = stringLongPair.getValue();
                    message = new String[]{charsetList.name(), letter, name, String.valueOf(date)};
                    dataTransferOut = new DataTransfer(imageData, Command.UPLOAD, position, message);
                } catch (Exception exception) {
                    dataTransferOut = new DataTransfer(null, Command.EXCEPTION, exception.getMessage());
                    ExceptionHandler.setException(dataTransferIn.toString(),exception);
                }
            }
            return dataTransferOut;
        }

        /**
         * Performs recognition of given {@link ImageData} from received {@code dataTransfer}.
         * If charset is indicated recognition performs only in network of given charset,
         * else in all loaded networks.
         * Returns {@link DataTransfer} with message about recognition result.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with message about recognition result.
         */
        @UserRestrictions
        static DataTransfer recognize(DataTransfer dataTransferIn) {
            String charset= dataTransferIn.getMessage(0);
            HashMap<String, Double> map=new HashMap<>();
            ImageData imageData = ImageDataUtil.getStandard(dataTransferIn.getImageData());
            if (Arrays.stream(CharsetList.values()).anyMatch(charsetList -> charsetList.name().equalsIgnoreCase(charset))) {
                CharsetList charsetList = CharsetList.valueOf(charset);
                //recognizing in given network
                map = (networkList.get(charsetList)).checkAll(imageData);
            } else { //recognizing all loaded networks
                for (NetworkCommander network:networkList.values()) {
                    HashMap<String, Double> tempMap=  network.checkAll(imageData);
                    map.putAll(tempMap);
                }
            }

            DecimalFormat decimalFormat=new DecimalFormat("0.###E0" );
            double max = 0.0;
            ArrayList<String>  message = new ArrayList<>();
            Iterator<String> iterator = map.keySet().iterator();
            String probCharacter="";

            while(iterator.hasNext()) {
                String key = iterator.next();
                double prob = map.get(key);
                if (prob>4.5569512622227484E-305){
                    message.add(key + ", " + decimalFormat.format(prob));
                }
                if (prob > max) {
                    max = prob;
                    probCharacter = key;
                }
            }

            String maxString=decimalFormat.format(max);
            if (max > 0.9) {
                probCharacter = "It is definitely letter: " + probCharacter + ", probability is: " + maxString;
            } else if (max > Float.MIN_VALUE) {
                probCharacter = "It is can be letter: " + probCharacter + ", probability is: " + maxString;
            } else if (max > Double.MIN_VALUE*Long.MAX_VALUE) {
                probCharacter = "There is exist small probability, that letter is: " + probCharacter + ", probability is: " + maxString;
            } else {
                probCharacter = "I do not know that, probability is: " + maxString;
            }
            message.add(0, probCharacter);

            return new DataTransfer(null, Command.RECOGNIZE, message.toArray(String[]::new));
        }

        /**
         * If user does not have the required level permission, this method is returned.
         * @param  dataTransferIn request received from the client.
         * @return {@link DataTransfer} with {@link Command#EXCEPTION} and message
         * about low user permission level.
         */
        static DataTransfer lowUserPermission(DataTransfer dataTransferIn) {
            String message=" Low user permission, for command: "+dataTransferIn.getCommand();
            return new DataTransfer(null, Command.EXCEPTION, message);
        }

    }
}
