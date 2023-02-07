package MiniGit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static MiniGit.FileUtilities.*;
import static java.nio.file.Files.copy;

public class StoredFiles {

    /*
    *
    * A MiniGit Format File is a file saved in Name(Parent Directory) -> Sha1(Child File) Format
    *
    * */

    StoredFiles(File blobsDirectory) {
        blobsDirectory = blobsDirectory;
    }

    /**
     * Determines if a given file exists, saved in MiniGits Name(Directory) -> Sha1(File) Format,
     * This method only confirms a Sha1 File exists, and does not check its validity
     * */
    static boolean existsInMiniGitFormat(File parentDirectory, String name) {
        File f = getFile(parentDirectory, name);
        if (f.isDirectory()) {
            File[] content = f.listFiles();
            for (File file : content) {
                if (file.isFile() && file.getName().length() == 40) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Determines if a duplicate file exists, saved in MiniGits Name(Directory) -> Sha1(File) Format,
     * This method differs to existsInMiniGitFormat() in that it compates both the 'name' directory and it's contents,
     * the name and content must match to return true.
     * */
    static boolean sameMiniGitFormatFileExists(File directoryToSearch, File nameSha1FormatFile) {

        if (nameSha1FormatFile.exists() && nameSha1FormatFile.isDirectory()) {
            File targetFile = new File(directoryToSearch, nameSha1FormatFile.getName());

            if (targetFile.exists() && targetFile.isDirectory()) {
                // Set of Each
                Set<String> blobContent = Set.of(nameSha1FormatFile.list());
                Set<String> targetFileContent = Set.of(targetFile.list());

                // If the sets are the same, the blobs are equal
                if (targetFileContent.equals(blobContent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches the specified directory for a directory of the same name as 'regularFile',
     *  containing a file named after the sha1 of calculated from 'regularFile
     *  '*/
    static boolean savedInMiniGitFileFormat(File directoryToSearch, File regularFile) {

        File storedFileOfSameName = new File(directoryToSearch, regularFile.getName());

        if (storedFileOfSameName.exists() && storedFileOfSameName.isDirectory()){
            String contentSha1 = getSha1HashFromFile(regularFile);
            if (new File(storedFileOfSameName, contentSha1).exists()){
                    return true;
            }
        }
        return false;
    }


    /**
     * Writes a file to MiniGit Format,
     * Returns the newly calculated Sha1 Hash.
     * */
    static String writeFileToMiniGitFileFormat(File fileToWrite, File parentDirectory){

        String name = fileToWrite.getName();
        byte[] content = FileUtilities.readContents(fileToWrite);
        String sha1 = FileUtilities.getSha1Hash(content);

        File nameDirectory = new File(parentDirectory, name);
        nameDirectory.mkdir();

        File sha1File = new File(nameDirectory, sha1);
        writeByteArray(sha1File, content);
        return sha1;
    }

    /**
     * Writes a file to MiniGit Format, deleting the old 'name' directory and its content, before writing the new File.
     * Returns the newly calculated Sha1 Hash.
     * */
    static String overwriteMiniGitFormatFile(File fileToWrite, File newLocation){

        String name = fileToWrite.getName();
        byte[] content = FileUtilities.readContents(fileToWrite);
        String sha1 = FileUtilities.getSha1Hash(content);

        File newDirectory = new File(newLocation, name);
        FileUtilities.deleteDirectory(newDirectory); // <-- <-- <-- deletes previous dir and content,
                                                                    // without having to know the exact sha1
        newDirectory.mkdir();

        File sha1File = new File(newDirectory, sha1);
        writeByteArray(sha1File, content);
        return sha1;

    }

    /**
     * Writes a given string to a file saved in MiniGit Format. Returns the newly calculated Sha1 Hash.
     * */
    static String writeStringToMiniGitFormatFile(String name, String content, File enclosingDirectory){


        File nameAsDirectory = new File(enclosingDirectory, name);

        if (!nameAsDirectory.exists()) {
            nameAsDirectory.mkdir();
        }

        return saveObject(content, nameAsDirectory);
    }

    /**
     * Copies files stored in MiniGit Format to the new specified location
     * */
    static void copyStoredFiles(File[] blobs, File newParentDirectory){
        for (File blob : blobs) {
            FileUtilities.copyDirectory(blob, newParentDirectory);
        }
    }


    /**
     * Copies a file stored in MiniGit Format to a new specified location.
     * */
    static void copyStoredFile(File blob, File newParentDirectory){
        FileUtilities.copyDirectory(blob, newParentDirectory);
    }


    /**
     * Takes a blob and writes it as a regular file, the new files name is the former blob directory name,
     * the sha1 of this file is discarded.
     * */
    public static void copyMiniGitFormatToRegularFiles(File originalLocation, Map<String, String> filenameToSha1Map, File newLocation){
        for (String fileName : filenameToSha1Map.keySet()) {
            File f = newFiles(originalLocation, fileName, filenameToSha1Map.get(fileName));
            FileUtilities.copy(f, new File(newLocation, fileName));
        }
    }

    /**
     * Returns a map<String, String> of File names and their respective Sha1 value,
     * This Method only returns one Sha1 String per name,
     * and therefore only suitable for uses where each name directory contains one Sha1 File.
     * */
    static Map<String, String> getStoredFilesAsMap(File location){
        File[] files = location.listFiles();
        if (files == null || files.length == 0) {
            return new HashMap<>();
        } else {
            Map<String, String> storedFiles = new HashMap<>();
            for (File file : files) {
                assert Objects.requireNonNull(file.list()).length == 1;
                storedFiles.put(file.getName(), Objects.requireNonNull(file.list())[0]);
            }
            return storedFiles;
        }
    }

}
