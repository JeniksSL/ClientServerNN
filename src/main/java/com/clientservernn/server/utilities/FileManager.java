

package com.clientservernn.server.utilities;

import com.clientservernn.common.CharsetList;
import com.clientservernn.server.guiFX.ExceptionHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * The class {@code FileManager} contains methods for performing file operations
 * with data or extracting file information. The class has protection against
 * simultaneous reading and writing to file.
 *
 * @author  Yauheni Slabko
 * @since   1.0
 */

public final class  FileManager {

    /**
     * The constant holding current work directory as {@link String}
     */
    static final String directory = "src\\main\\resources\\com\\clientservernn\\server\\LetterData\\";
    /**
     * The constant holding current work directory as {@link Path}
     */
    static final Path dir=Paths.get(directory);

    /**
     * The {@link ArrayList} that contains all current reading and written files as {@link Path}.
     * Operation of reading {@code loadObject()} and writing {@code saveAsObject()} are synchronized by Path.
     */
    private static final ArrayList<Path> currentPathList=new ArrayList<>();


    private FileManager() {
    }

    /**
     * The method loads from {@code directory} information and creates a {@link HashMap<>} with
     * a description of each charset represented in {@code enum} {@link CharsetList} and the
     * characters stored in it.
     * If it is no such directories, creates it. If directories can not be created,
     * pass current charset, did not put it in {@link HashMap<>} and adds caught exception
     * to {@link ExceptionHandler}.
     *
     *
     * @return  a {@link HashMap<>} with {@link String} key - name of charset,
     * and  {@link File} array value - saved information about each character of charset.
     * If there are no characters in the charset, it returns value as empty array.
     */

    private static HashMap<String, File[]> loadMap() {
        HashMap<String, File[]> directoryMap = new HashMap<>();
        CharsetList[] charsetLists = CharsetList.values();
        for (CharsetList charsetList : charsetLists) {
            String subPath = directory + charsetList.name();
            Path paths = Paths.get(subPath);
            if (!Files.exists(paths)) {
                try {
                    Files.createDirectories(paths);
                } catch (IOException exception) {
                    ExceptionHandler.setException(FileManager.class.getName()+".getCharList(String "+paths+")",exception);
                    paths=null;
                }
            }
            if (paths!=null&&paths.toFile().isDirectory())        {
                File[] listOfFiles=paths.toFile().listFiles((dir, name) -> name.contains(".dat"));
                directoryMap.put(charsetList.name(), listOfFiles);
            }
        }
        return directoryMap;
    }


    /**
     * Using {@link HashMap<>} from {@code loadMap()} method
     * creates and returns a new {@link List<>} with all represented characters
     * as {@link String} corresponding {@code charset}.
     * @param charset the requested charset .
     * @return  the {@link List<>} of {@link String} characters corresponding requested {@code charset}.
     */
    public static List<String> getCharList(String charset) {
        String[] nameList;
        File[] fileList = loadMap().get(charset);
        nameList = Arrays.stream(fileList).map(File::getName).toArray(String[]::new);
        for(int i = 0; i < nameList.length; ++i) {
            int lastPoint = nameList[i].lastIndexOf(".");
            nameList[i] = nameList[i].substring(0, lastPoint);
        }
        return Arrays.stream(nameList).toList();
    }

    /**
     * Using {@link HashMap<>} from {@code loadMap()} method
     * find for requested {@code charset} the last modified file of
     * characters data and returns it last modification date as {@link String}.
     * @param charset the requested charset.
     * @return date {@link String} of last modification requested {@code charset}.
     */
    public static String lastModified(String charset) {
        long lastModified = 0L;
        File[] fileList = loadMap().get(charset);
        for (File file : fileList) {
            lastModified = Math.max(file.lastModified(), lastModified);
        }
        return (new Date(lastModified)).toString();
    }

    /**
     * Save {@code requested} object extends {@link Serializable} in sub path
     * {@code charset} relatively current {@code directory} with {@code name}
     * and extension ".dat". Access to fife are synchronized.
     *
     * @param charset the requested charset.
     * @param name the requested name.
     * @param requested the requested object.
     * @throws IOException if the {@code requested} object can not be written.
     */
    public static <T extends Serializable> void saveAsObject(String charset, String name, T requested) throws IOException {
        String extension=".dat";
        final Path current=Paths.get(dir.toString(),charset,name+extension);
        Optional<Path> pathOptional =currentPathList.stream().filter(path->path.compareTo(current)==0).findAny();
        if (pathOptional.isEmpty()) {
            currentPathList.add(current);
        }
        synchronized (current) {
            try ( FileOutputStream fos = new FileOutputStream(current.toFile())) {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(requested);
            }
        }
        currentPathList.remove(current);
    }

    /**
     * Save {@code requested} object extends {@link Iterable} <{@link byte[]}> in sub path
     * {@code charset} relatively current {@code directory} with {@code name}
     * and extension ".csv" as CSV file .
     *
     * @param charset the requested charset.
     * @param name the requested name.
     * @param requested the requested object.
     * @throws IOException if the {@code requested} object can not be written.
     */

    public static <T extends Iterable<byte[]>> void saveAsCSV(String charset, String name, T requested) throws IOException {
        String extension=".csv";
        Path current = Paths.get(dir.toString(),charset,name+extension);
        try(PrintWriter printWriter = new PrintWriter(current.toFile())){
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

    /**
     * Returns {@link Optional} with {@code requested} object extends {@link Serializable} from sub path
     * {@code charset} relatively current {@code directory} with {@code name}
     * and extension ".dat". If any exceptions is occurred return empty {@link Optional}.
     *
     * @param charset the requested charset.
     * @param name the requested name.
     * @param requested the requested object.
     * @return the {@link Optional} with requested object or empty.
     */
    public static <T extends Serializable> Optional<T> loadObject(String charset, String name, T requested) {
        String extension=".dat";
        final Path current=Paths.get(dir.toString(),charset,name+extension);
        Optional<Path> pathOptional =currentPathList.stream().filter(path->path.compareTo(current)==0).findAny();
        if (pathOptional.isEmpty()) {
            currentPathList.add(current);
        }
        synchronized (current) {
            try (FileInputStream fis = new FileInputStream(current.toFile())) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                requested = (T) ois.readObject();
            }catch (ClassNotFoundException | ClassCastException | IOException exception) {
                requested = null;
            }
        }
        currentPathList.remove(current);
        return Optional.ofNullable(requested);
    }
}
