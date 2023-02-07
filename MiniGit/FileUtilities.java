package MiniGit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class FileUtilities {

    /**
     * Returns a byte array of the specified object
     * */
    static byte[] serialize(Serializable object) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(object);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException e) {
            throw new Error("IO");
        }
    }

    /**
     * Returns a specified object of 'type' as read from the given file.
     * Return an object of type T read from FILE.
     *  May throw IllegalArgumentException in the case of problems. */
    static <T extends Serializable> T readFile(File file, Class<T> type) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            T result = type.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Writes the given object to the given file. */
    static void writeObject(File file, Serializable object) {
        writeContents(file, serialize(object));
    }

    /**
     * Writes the given byte Array to the given file.
     * */
    static void writeByteArray(File file, byte[] b) {
        writeContents(file, b);
    }

    /** Write the result of concatenating the bytes in CONTENTS to FILE,
     *  creating or overwriting it as needed.  Each object in CONTENTS may be
     *  either a String or a byte array.  Throws IllegalArgumentException
     *  in case of problems. */
    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Creates and returns a sha1 String based on a given byte[] or a String.
     * */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("Unable to convert given type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /**
     * Creates a new File with the given path and name
     * */
    public static File newFile(String existingPath, String fileName){
        File newFile = Paths.get(existingPath, fileName).toFile();
        return newFile;
    }

    /**
     * Serialises Object and performs a sha1 hash function on the byteArray, then returns the hash as a String
     */
    public static String saveObject(Serializable object, File parentDir) {
        byte[] byteArray = serialize(object);
        String sha1Hash = sha1(byteArray);

        File f = new File(parentDir, sha1Hash);
        writeByteArray(f, byteArray);
        return sha1Hash;
    }

    /**
     * Returns sha1Hash from the byteArray of some serializable Object
     * */
    public static String getSha1Hash(Serializable object) {
        byte[] byteArray = serialize(object);
        return sha1(byteArray);
    }

    /**
     * Returns sha1Hash from the byteArray
     * */
    public static String getSha1Hash(byte[] byteArray){
        return sha1(byteArray);
    }


    /**
     * Returns sha1Hash from the byteArray of some File
     * */
    public static <T extends Serializable> String getSha1HashFromFile(File file){
        byte[] ba = getByteArrayFromFile(file);
        return sha1(ba);
    }

    /**
     * Returns the byte[] of a given file
     * */
    static byte[] getByteArrayFromFile(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new file, can be used to create a file with multiple parents that do not yet exist.
     * */
    static File newFiles(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    /**
     * If file exists, returns the file, otherwise returns null
     * */
    public static File getFile(File directoryLocation, String fileName) {
        if (directoryLocation.isDirectory() && fileName != null) {

            File f = newFile(directoryLocation.getPath(), fileName);

            if (f.exists()) {
                return f;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * deletes directory denoted by the given File.
     * */
    public static void deleteDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                deleteDirectory(child);
            }
        }
        directory.delete();
    }

    /**
     * Deletes the file denoted by the given name if said file exists in the given parent directory,
     * deletes both files and directories.
     * */
    public static void deleteIfExists(File parentDirectory, String... nameOrNames){

        if (parentDirectory.exists() && parentDirectory.isDirectory()){
            for (String name: nameOrNames) {
                File f = new File(parentDirectory, name);
                if (f.exists()) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
    }

    /**
     * Deletes specified files, explicitly checks if file is a directory, if file is directory, it is not deleted
     * */
    public static void deleteFilesIfNotDirectory(File enclosingFolder, String... names){
        for (String name: names){
            File f = new File(enclosingFolder, name);
            if (f.exists() && !f.isDirectory()){
                f.delete();
            }
        }
    }

    /**
     * Gets the files and reads it into the given 'type'
     * */
    public static <T extends Serializable> T getAndReadObjectFile(File ParentDir,
                                                                  String fileName,
                                                                  Class<T> type){
        File f = getFile(ParentDir, fileName);
        return  readFile(f, type);
    }

    /** Return the entire contents of FILE as a byte array.  FILE must
     *  be a normal file.  Throws IllegalArgumentException
     *  in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Reads the content of a file and returns it as a string */
    static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }


    /**
     * Copies a file or directory to the specified target location
     * */
    public static void copy(File sourceFile, File targetLocation) {
        if (sourceFile.isDirectory()) {
            copyDirectory(sourceFile, targetLocation);
        } else {
            copyFile(sourceFile, targetLocation);
        }
    }

    /**
     * Copies a directory, and it's content to the specified target location
     * */
    static void copyDirectory(File source, File target) {
        if (!target.exists()) {
            target.mkdir();
        }

        File t = new File(target, source.getName());
        if (!t.exists()) {
            t.mkdir();
        }

        for (String f : source.list()) {
            copy(new File(source, f), new File(t, f));
        }
    }

    /**
     * Copies a File to the specified target location
     * */
    private static void copyFile(File source, File target) {
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)
        ) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
