package MiniGit;

import java.io.File;
import java.io.Serializable;
import static MiniGit.FileUtilities.*;


/**
 * The SavableState class is designed to allow inheritors to save themselves to the given filepath
 * with the given name without having to implement these methods themselves.
 * */
public class SavableState implements Serializable {

    private String name;
    private String filePath;

    SavableState(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    /**
     * Saves self to the file path, and name given on initialization.
     * */
    public void save() {
        File stateFile = newFile(filePath, name);
        if (stateFile.exists()){
            stateFile.delete();
        }
        writeObject(stateFile, this);
    }

}

