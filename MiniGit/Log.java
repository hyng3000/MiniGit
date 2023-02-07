package MiniGit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * The Log class generates a log String of Commits for presentation to the user.
 * */
public class Log {

    /**
     * Creates a Log String of the given Map<String, Commit> Object in the order
     * given by the Map (This may be ordered or unordered).
     * */
    public static ArrayList<String> createLog(Map<String, Commit> commitMap){
        String divider = "===\n";
        String commitId = "Could Not Get CommitID";
        String branch = "";
        String merge = "";
        String date = "Could Not Get Date";
        String message = "Could Not Get Date";
        String trackedFiles = "";
        String emptySpace = "\n";

        ArrayList<String> log = new ArrayList<>();

        for (String id: commitMap.keySet()){
            Commit commit = commitMap.get(id);

            commitId = "Commit: " + id + "\n";
            branch = "Branch: " + commit.branch + "\n";
            if (commit.getSecondParentSha1() != null){
                merge = "Merged: " + commit.getFirstParentSha1() + "\n" + "With: " + commit.getSecondParentSha1() + "\n";
            }
            date = "Date:   " + commit.getTimestamp()  + "\n";
            message = "\"" + commit.getMessage()  + "\"" + "\n";
            trackedFiles = commit.getTrackedFiles().toString() + "\n";

            log.add(divider + commitId + branch + merge + date + message+ emptySpace);
        }
        return log;
    }


    /**
     * Creates a log String of all Commits in the given directory, in no particular order.
     * */
    public static ArrayList<String> createGlobalLog(File commitsDir) {

        HashMap<String, Commit> map = new HashMap<>();
        for (File commit: commitsDir.listFiles()){
            map.put(commit.getName(), FileUtilities.readFile(commit, Commit.class));
        }

        return createLog(map);

    }
}
