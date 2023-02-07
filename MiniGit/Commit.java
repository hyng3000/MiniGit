package MiniGit;

import java.io.Serializable;
import java.util.*;

public class Commit implements Serializable {

    private final String firstParentSha1;
    private String secondParentSha1 = null;
    String branch;
    private final String timestamp;
    private final Map<String, String> trackedFiles;
    private final String message;

    Commit(String branch){
        message = "initial commit";
        timestamp = new Date(0).toString(); // 0 Milliseconds since epoch
        firstParentSha1 = null;
        trackedFiles = new HashMap<>();
        this.branch = branch;
        /*
        Makes the initial commit
        This commit contains no files, has the message 'inital commit'
        timestamp is 00:00:00 UTC, Thursday 1 january 1970
         */
    }

    Commit(String parentSha1, String branch, String message, Map<String,String> blobs){
        this.message = message;
        this.timestamp = new Date().toString();
        this.firstParentSha1 = parentSha1;
        this.trackedFiles = Objects.requireNonNullElseGet(blobs, HashMap::new);
        this.branch = branch;
        }

    Commit(String firstParentSha1, String secondParentSha1, String branch, String message, Map<String,String> blobs){
        this.message = message;
        this.timestamp = new Date().toString();
        this.firstParentSha1 = firstParentSha1;
        this.secondParentSha1 = secondParentSha1;
        this.trackedFiles = Objects.requireNonNullElseGet(blobs, HashMap::new);
        this.branch = branch;
    }

    /**
     * returns a map<File name, Sha1Hash> of trackedFiles in this commit,
     * if no files are tracked, returns an empty hashmap<>
     * */
    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    /**Returns the corresponding Sha1 String value from the map of tracked files stored in the commit object.
     *  If no value exists, returns null */
    public String lookUpTrackedFile(String fileName){
        return trackedFiles.get(fileName);
    }

    /**
     * Returns the ID of the first parent -> A Commit may have two parents in the event of a merge. First parent will
     * always hold a value unless this is the initial commit, whihc has no parent
     * */
    public String getFirstParentSha1() {
        return firstParentSha1;
    }

    /**
     * Returns the ID of the second parent  -> A Commit may have two parents in the event of a merge. Second Parent
     * may or may not be null.
     * */
    public String getSecondParentSha1(){
        return secondParentSha1;
    }

    /**
     * Returns the commit message.
     * */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the timestamp generated on this commits initialization.
     * */
    public String getTimestamp() {
        return this.timestamp;
    }

    /**
     * checks for equality between this commit and some other commit.
     * */
    public Boolean equals(Commit other) {
        return FileUtilities.getSha1Hash(this).equals(FileUtilities.getSha1Hash(other));
    }

    /**
     * Returns the branch.
     * */
    public String getBranch(){
        return branch;
    }
}
