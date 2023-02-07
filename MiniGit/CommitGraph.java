package MiniGit;

import java.io.File;
import java.util.*;

import static MiniGit.FileUtilities.getFile;
import static MiniGit.FileUtilities.readFile;

public class CommitGraph {

    /**
     * Creates a Commit Object suited to being an initial Commit.
     * */
    public static Commit makeFirstCommit(String branch) {
        return new Commit(branch);
    }

    /**
     * Creates a Commit Object containing the given information. The Files tracked by this commit are determined by
     * examining the parents tracked files, the currently staged files, and the files staged for removal.
     * No duplicates are allowed, any file staged is added, any file staged for removal is removed,
     * and all other files tracked in the parent commit are added.
     *
     * Returns a Commit Object.
     * */
    public static Commit makeNewCommit(Commit parent, String parentSha1, String branch, String message, Map<String, String> stagedBlobs, Set<String> unstagedFiles) {
        HashMap<String, String> parentBlobs = new HashMap<>();
        if (parent != null) {
            parentBlobs = (HashMap<String, String>) parent.getTrackedFiles();
            parentBlobs.entrySet().removeIf(entry -> unstagedFiles.contains(entry.getKey()));

            parentBlobs.putAll(stagedBlobs);
        }
        return new Commit(parentSha1, branch, message, parentBlobs);
    }


    /**
     * Returns a linkedHashMap of all files in a Commits history
     * */
    public static LinkedHashMap<String, Commit> getCommitHistory(String commitId, Commit commit, File commitsDir) {
        LinkedHashMap<String, Commit> commitMap = new LinkedHashMap<>();
        commitMap.put(commitId, commit);

        String parentID = commit.getFirstParentSha1();
        while (parentID != null) {
            Commit c = FileUtilities.readFile(new File(commitsDir, parentID), Commit.class);
            commitMap.put(parentID, c);
            parentID = c.getFirstParentSha1();
        }
        return commitMap;
    }

    /**
     * Searches through all Commits for any Commits with the specified message and returns them as an ArrayList<String>
     * */
    public static ArrayList<String> searchByMessage(String message, File location) {

        ArrayList<String> idList = new ArrayList<>();
        for (File commitFile : location.listFiles()) {
            String thisMessage = FileUtilities.readFile(commitFile, Commit.class).getMessage();
            if (thisMessage.equals(message)) {
                idList.add(commitFile.getName());
            }
        }
        return idList;
    }


    /**
     * Determines the first common ancestor of the specified commits.
     * Returns the Commit File of said ancestor.
     * */
    static String findFirstSplitPoint(Commit currentCommit,
                                             String currentID,
                                             Commit otherBranchCommit,
                                             String otherBranchID,
                                             File location) {

        if (currentID.equals(otherBranchID)) {
            System.out.println("Branch is current, unable to merge.");
            return null;
        }

        Set<String> allCommitsSoFar = new HashSet<>();
        allCommitsSoFar.add(currentID);
        allCommitsSoFar.add(otherBranchID);

        String split = null;

        while (split == null) {
            String currentBranch_NextNodeID = currentCommit.getFirstParentSha1();
            String otherBranch_NextNodeID = otherBranchCommit.getFirstParentSha1();

            if (currentBranch_NextNodeID != null) {
                if (allCommitsSoFar.contains(currentBranch_NextNodeID)) {
                    split = currentBranch_NextNodeID;
                    break;
                } else {
                    allCommitsSoFar.add(currentBranch_NextNodeID);
                    currentCommit = getCommit(location, currentBranch_NextNodeID);
                }
            }

            /* Slightly more efficient implementation -> less readable.
            if (currentBranch_NextNodeID != null){
                if (allCommitsSoFar.add(currentBranch_NextNodeID)){
                    currentCommit = getCommit(location, currentBranch_NextNodeID);
                } else {
                    split = currentBranch_NextNodeID;
                    break;
                }
           } */

            if (otherBranch_NextNodeID != null) {
                if (allCommitsSoFar.contains(otherBranch_NextNodeID)) {
                    split = otherBranch_NextNodeID;
                    break;
                } else {
                    allCommitsSoFar.add(otherBranch_NextNodeID);
                    otherBranchCommit = getCommit(location, otherBranch_NextNodeID);
                }
            }

            // Escape Route if it all goes wrong.
            if (currentBranch_NextNodeID == null && otherBranch_NextNodeID == null) {
                System.out.println("No common ancestor found. Commit history is damaged or incomplete.");
                return null;
            }

        }
        return split;
    }

    /**
     * Returns a Commit with the given commitId in the given parent Directory or null.
     * */
    static Commit getCommit(File parentDirectory, String commitID) {
        File f = getFile(parentDirectory, commitID);
        if (f != null) {
            Commit c = readFile(f, Commit.class);
            return c;
        }
        System.out.println("No Such Commit ID");
        return null;
    }

    /**
     * Merges two files of different branches, handling any conflict that arises.
     * */
    public static Commit merge(Commit currentCommit,
                       String currentID,
                       Commit otherBranchCommit,
                       String otherBranchID,
                       Commit splitPoint,
                       File storedFileLocation
                       ){

        Merger m = new Merger(splitPoint, currentCommit, otherBranchCommit, storedFileLocation);

        Map<String, String> newCommitFiles = m.getMergedFiles();
        String message = m.getMessage();

        return new Commit(currentID, otherBranchID, currentCommit.branch, message, newCommitFiles);
    }

}








