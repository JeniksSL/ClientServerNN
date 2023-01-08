

package com.clientservernn.server.utilities;

import com.clientservernn.common.PathList;
import com.clientservernn.dataTransfer.ImageData;
import com.clientservernn.server.guiFX.ExceptionHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * The class {@code FileManager} contains methods for performing file operations
 * with data or extracting file information.
 *
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public final class  FileManager {
    static final String directory = "src\\main\\resources\\com\\clientservernn\\server\\LetterData\\";

    private FileManager() {
    }

    /**
     * The method loads from {@code directory} information and creates a {@link HashMap<>} with
     * a description of each charset represented in {@code enum} {@link PathList} and the
     * characters stored in it.
     * If it is no such directories, creates it.
     *
     *
     * @return  a {@link HashMap<>} with {@link String} key - name of charset,
     * and  {@link File} array value - saved information about each character of charset.
     * If there are no characters in the charset, it returns value as empty array.
     * @throws  IOException
     *          If directories can not be created.
     */


    private static HashMap<String, File[]> loadMap() throws IOException {
        HashMap<String, File[]> directoryMap = new HashMap<>();
        PathList[] pathLists = PathList.values();

        for (PathList pathList : pathLists) {
            String subPath = directory + pathList.name();
            Path paths = Paths.get(subPath);
            if (!Files.exists(paths)) {
                Files.createDirectories(paths);
            }
            if (paths.toFile().isDirectory())        {
                File[] listOfFiles=paths.toFile().listFiles((dir, name) -> name.contains(".dat"));
                directoryMap.put(pathList.name(), listOfFiles);
            }
        }
        return directoryMap;
    }

    public static List<String> getCharList(String path) {
        String[] nameList;
        try {
            File[] fileList = loadMap().get(path);
            nameList = Arrays.stream(fileList).map(File::getName).toArray(String[]::new);
        } catch (IOException e) {
            nameList=new String[0];
            ExceptionHandler.setException(FileManager.class.getName()+".getCharList(String "+path+")",e);
        }
        for(int i = 0; i < nameList.length; ++i) {
            int lastPoint = nameList[i].lastIndexOf(".");
            nameList[i] = nameList[i].substring(0, lastPoint);
        }
        return Arrays.stream(nameList).toList();
    }

    public static String lastModified(String path) {
        long lastModified = 0L;
        try {
            File[] fileList = loadMap().get(path);
            for (File file : fileList) {
                lastModified = Math.max(file.lastModified(), lastModified);
            }
        } catch (IOException e){
            ExceptionHandler.setException(FileManager.class.getName()+".lastModified(String "+path+")",e);
        }
        return (new Date(lastModified)).toString();
    }

    public static <T extends Serializable> void saveAsObject(String subPath, T requested) throws IOException {
        FileOutputStream fos = new FileOutputStream(directory + subPath + ".dat");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(requested);
    }

    public static <T extends AbstractCollection<byte[]>> void saveAsCSV(String subPath, T requested) throws IOException {
        try(PrintWriter printWriter = new PrintWriter(directory + subPath + ".csv")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(requested.getClass().getName());
            stringBuilder.append(",");

            for (byte[] byteArray : requested) {
                for (byte number : byteArray) {
                    stringBuilder.append(number);
                    stringBuilder.append(",");
                }
                stringBuilder.append(",");
                stringBuilder.append("\n");
            }
            printWriter.write(stringBuilder.toString());
        }


    }

    public static <T extends Serializable> Optional<T> loadObject(String subPath, T aClass) {
        T object;
        try (FileInputStream fis = new FileInputStream(directory + subPath + ".dat")) {

            ObjectInputStream ois = new ObjectInputStream(fis);
            object = (T) ois.readObject();

        }catch (ClassNotFoundException | ClassCastException | IOException exception) {
            object = null;
        }
        return Optional.ofNullable(object);
    }
}
