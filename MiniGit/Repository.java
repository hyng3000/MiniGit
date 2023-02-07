package MiniGit;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static MiniGit.FileUtilities.*;


public class Repository {

    /*
    * CWD
    */
    static final File CURRENT_WORKING_DIRECTORY = new File(System.getProperty("user.dir")).getParentFile();
    /*
     .MiniGit DIRECTORY
     */
    static final File MiniGitDir = Paths.get(CURRENT_WORKING_DIRECTORY.getPath(), ".MiniGit").toFile();

    /*
    * Inside .MiniGit Directory
    * */
    static final File COMMITS_DIR = Paths.get(MiniGitDir.getPath(), "Commits").toFile();
    static final File STORED_FILE_DIR = Paths.get(MiniGitDir.getPath(),"Blobs").toFile();
    static final File STATE_DIR = Paths.get(MiniGitDir.getPath(),"State").toFile();
    static final File STAGING_AREA_DIR = Paths.get(MiniGitDir.getPath(),"StagingArea").toFile();
    static final File[] subDirectories = {COMMITS_DIR, STORED_FILE_DIR, STATE_DIR, STAGING_AREA_DIR};
    static final String MINI_GIT_STATE = "MiniGitState";
    static final String DEFAULT_BRANCH = "master";

    /*
    *
    * Repository Creation Methods.
    *
    * */
    Repository() {
        if (MiniGitDir.exists()){
            return;

        } else {
            createRepositoryDirectories();
            createStateFiles();
            initialCommit();
        }
    }

    /**Creates MiniGits required Repositories,
     * throws Security Exception in the event user settings prevent proper function
     * */
    static void createRepositoryDirectories() {
        try {
            MiniGitDir.mkdir();
            for (File file : subDirectories) {
                file.mkdir();
            }
        } catch (SecurityException e) {
            System.out.println("Security Settings Prevented Creation of MiniGit Directory");
        }
    }

    /**Creates MiniGitState() object which is always saved to record the programs current state*/
    private static MiniGitState createStateFiles() {
        return new MiniGitState(null, STATE_DIR.getPath(), MINI_GIT_STATE, DEFAULT_BRANCH);
    }

    /**
     * Returns true if all required Directories exist.
     * */
    static Boolean miniGitExists() {
        return MiniGitDir.exists() &&
                COMMITS_DIR.exists() &&
                STORED_FILE_DIR.exists() &&
                STATE_DIR.exists() &&
                STAGING_AREA_DIR.exists();
    }


    /*
     * MiniGit Commands Implementation.
     * */


    /*
     * Commit Command.
     * */


    /**
     * Creates and Writes an initial Commit Object.
     * */
    static String initialCommit() {
       Commit IC = CommitGraph.makeFirstCommit(DEFAULT_BRANCH);
       return writeCommit(IC);
    }

    /**
     * Creates a Commit object
     *  */
    static Commit createCommit(String parentSha1, String branch, String message, Map<String, String> stagedFiles, Set<String> unstagedFiles) {
        Commit parent = getHeadCommit();
        return CommitGraph.makeNewCommit(parent, parentSha1, branch, message, stagedFiles, unstagedFiles);
    }

    /**Saves Commit Object to REPOSITORY.COMMITS_DIR */
    static String writeCommit(Commit commit) {
        String commitID = saveObject(commit, COMMITS_DIR);
        MiniGitState state = getMiniGitState();
        state.setNewHead(commitID);
        return commitID;
    }

    /**
     * Creates and Writes a Commit Object to REPOSITORY.COMMITS_DIR.
     * */
    static String commit(String message){
        Map<String, String> stagedFiles = StoredFiles.getStoredFilesAsMap(STAGING_AREA_DIR);
        MiniGitState state = getMiniGitState();
        Commit c = createCommit(state.getHead(), state.getBranch(), message, stagedFiles, state.getUnstagedFiles());
        String commitSha1 = writeCommit(c);
        StoredFiles.copyStoredFiles(getStagedFile(), STORED_FILE_DIR);
        emptyStagingArea();
        return commitSha1;
    }


    /*
     * Add Command.
     * */


    /**
     * Stages a file for addition to the next Commit.
     * */
    static void add(String fileName) {
        add(new File(CURRENT_WORKING_DIRECTORY, fileName));
    }

    /**
     * Stages multiple files for addition to the next Commit.
     * */
    static void addMultiple(String... fileName){
        for (String name: fileName){
            add(new File(CURRENT_WORKING_DIRECTORY, name));
        }
    }

    /**
     * Determines which Add() method is applicable to the given arguments, then calls that method.
     * */
    public static void addSwitch(String[] args, int length){
        try {
            if (length > 2) {
                addMultiple(args);
            } else if (length == 1) {
                add(args[0]);
            }
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }


    /**
     * Stages a file if shouldStage() method returns true.
     * */
    private static void add(File f){
        if (f.exists() && shouldStage(f)) {
                stageFile(f);
            }
    }


    /*
     * rm Command.
     * */


    /**
     * Deletes a File from MiniGit Repository, the file will not be tracked in future commits,
     * however will be available in historic Commits.
     * */
    public static void rm(String fileName){
        deleteIfExists(CURRENT_WORKING_DIRECTORY, fileName);
        deleteIfExists(STAGING_AREA_DIR, fileName);
        getMiniGitState().stageForRemoval(fileName);
    }


    /*
     * Log Commands.
     * */


    /**
     * Prints a log for all Commits in the current Commits history as per its branch.
     * Log is printed in order with the newest Commits First.
     * */
    static void log(){
        MiniGitState state = getMiniGitState();
        LinkedHashMap<String, Commit> map = CommitGraph.getCommitHistory(state.getHead(), getHeadCommit(), COMMITS_DIR);
        Log.createLog(map).forEach(System.out::println);
    }

    /**
     * Prints log for all Commits on all branches, this log is printed in no guaranteed order.
     * */
    static void globalLog(){
        Log.createGlobalLog(COMMITS_DIR).forEach(System.out::println);
    }


    /*
     * Find Command.
     * */


    /**
     * Searches for Commit files by 'message'
     * */
    static void find(String message){
        ArrayList<String> list = CommitGraph.searchByMessage(message, COMMITS_DIR);
        list.forEach(System.out::println);
    }


    /*
     * Status Command.
     * */


    /**
    * Prints the current status of the MiniGit Repository
    */
    static void status(){
        MiniGitState state = getMiniGitState();

        Set<String> branches = state.getAllBranchNames();
        Set<String> removedFiles = state.getUnstagedFiles();
        Set<String> stagedFiles = Set.of(getStagedFileNames());

        System.out.println("=== Branches ===");
        branches.forEach(System.out::println);
        System.out.println("");

        System.out.println("=== Staged Files ===");
        stagedFiles.forEach(System.out::println);
        System.out.println("");

        System.out.println("=== Removed Files ===");
        removedFiles.forEach(System.out::println);
        System.out.println("");

    }


    /*
     * Checkout Commands.
     * */


    /**
     * Looks up the specified file in the head commit object and writes it to the working directory,
     * if there is a file of the same name, it will be overwritten.
     * The file is not staged.
     * */
    static void checkout(String fileName) {
        String commitID = getMiniGitState().getHead();
        checkoutByCommitID(commitID, fileName);
    }

    /**
     * Looks up the specified file in the specified commit object and writes it to the working directory,
     * if there is a file of the same name, it will be overwritten.
     * The newly written file is not staged.
     * */
    static void checkoutByCommitID(String commitID, String fileName) {
        Commit commit = getCommit(commitID);
        if (commit == null){
            System.out.println("Commit ID not found");
            return;
        }

        String sha1 = commit.lookUpTrackedFile(fileName);
        if (sha1 == null){
            System.out.println("File not found in specified Commit");
        }

        File sha1File = newFiles(STORED_FILE_DIR, fileName, sha1);
        if (sha1File.exists()){
            FileUtilities.copy(sha1File, new File(CURRENT_WORKING_DIRECTORY, fileName));
        } else {
            System.out.println("File as found in Commit does not exist in StoredFiles Directory");
        }
    }

    /**
     * Looks up the specified branch and writes its files to the working directory,
     * if there is a file of the same name, it will be overwritten.
     * The staging area is cleared. Any file tracked in the previous commit,
     * but not tracked in the new head commit will be deleted.
     * */
    static void checkoutBranch(String name) {
        MiniGitState state = getMiniGitState();
        Commit currentCommit = getCommit(state.getHead());
        assert currentCommit != null;
        // get the files tracked by the current commit -> they will be deleted
        Set<String> currentlyTrackedFiles = currentCommit.getTrackedFiles().keySet();
        if (untrackedFiles(CURRENT_WORKING_DIRECTORY, currentlyTrackedFiles)){
            System.out.println("There is an untracked file in the directory. Checkout command cannot continue.");
            return;
        }

        // If we switch to a valid branch
        if (state.setBranchAsHead(name)) {
            Commit destinationCommit = getCommit(state.getHead()); // get the commit
            assert destinationCommit != null;
            Map<String, String> newTrackedFiles = destinationCommit.getTrackedFiles(); // get the files it tracks

            // Delete old files
            deleteFilesIfNotDirectory(CURRENT_WORKING_DIRECTORY, currentlyTrackedFiles.toArray(new String[0]));

            // Copy in new Files
            StoredFiles.copyMiniGitFormatToRegularFiles(STORED_FILE_DIR, newTrackedFiles, CURRENT_WORKING_DIRECTORY);
        }
    }

    /**
     * Determines and calls the correct checkout method by inspecting the supplied args
     * */
    static void checkoutSwitch(String[] args, int length){
        if (length == 2){
            checkoutByCommitID(args[0], args[1]);
        } else {
            MiniGitState state = getMiniGitState();
            if (state.getAllBranchNames().contains(args[0])) {
                checkoutBranch(args[0]);
            } else {
                checkout(args[0]);
            }
        }
    }


    /*
     * Branch Commands.
     * */


    /**
     * Adds a new branch to MiniGit, this command does not move the head pointer to the new branch. (See Checkout())
     * */
    static void branch(String name){
        MiniGitState state = getMiniGitState();
        state.addNewBranch(name);
    }

    /**
     * removes a branch from MiniGit, if the given branch is the current branch, no branch is removed.
     * */
    static void rmBranch(String name){
        MiniGitState state = getMiniGitState();
        state.removeBranch(name);
    }


    /*
     * Reset Command.
     * */


    /**
     * Reverts the MiniGit Repository to the state as remembered by the given commit.
     * Any Files tracked in the current commit, that have been modified or created since the specified commit will be deleted.
     * */
    static void reset(String commitID) {
        Commit destinationCommit = getCommit(commitID);

        if (destinationCommit != null) {
            MiniGitState state = getMiniGitState(); // Get State
            Commit currentCommit = getCommit(state.getHead()); // Get Current Commit
            assert currentCommit != null; // We know the commit exists
            Set<String> currentlyTrackedFiles = currentCommit.getTrackedFiles().keySet(); //Get the tracked Files in the current commit
            if (untrackedFiles(CURRENT_WORKING_DIRECTORY, currentlyTrackedFiles)){
                System.out.println("There is an untracked file in the directory. Reset command cannot continue.");
                return;
            }

            state.setNewHead(commitID); // Move Head to the new CommitID
            Map<String, String> newTrackedFiles = destinationCommit.getTrackedFiles(); // Get the tracked Files in the new commit

            // delete the file tracked by the old commit, copy in the files tracked by the new commit
            deleteFilesIfNotDirectory(CURRENT_WORKING_DIRECTORY, currentlyTrackedFiles.toArray(new String[0]));
            StoredFiles.copyMiniGitFormatToRegularFiles(STORED_FILE_DIR, newTrackedFiles, CURRENT_WORKING_DIRECTORY);
        }
    }


    /*
     * Merge Command.
     * */


    /**
     * Merges files from the specified branch to the current branch according to the merge rules.
     * */
    static void merge(String branch){

        MiniGitState state = getMiniGitState();

        String currentId = state.getHead();
        Commit currentCommit = getCommit(currentId);
        String otherBranchId = state.getBranchCommitID(branch);

        if (otherBranchId == null) {
            System.out.println("Invalid Branch");
            return;
        }

        Commit otherBranchCommit = getCommit(otherBranchId);

        String splitPointID = CommitGraph.findFirstSplitPoint(
                currentCommit,
                currentId,
                otherBranchCommit,
                otherBranchId,
                COMMITS_DIR);

        if (splitPointID.equals(otherBranchId)){
            System.out.println("Specified branch is ancestor of current branch.");
            return;
        }

        if (splitPointID.equals(currentId)){
            checkoutBranch("branch");
            System.out.println("Current branch is ancestor of given branch," +
                    " fast forward to given branch has been performed.");
            return;
        }

        Commit newCommit = CommitGraph.merge(
                currentCommit,
                currentId,otherBranchCommit,
                otherBranchId,
                getCommit(splitPointID),
                STORED_FILE_DIR);

        String newCommitID = writeCommit(newCommit);
        reset(newCommitID);
    }

    /*
    *
    * Helper Methods
    *
    * */

    /**
     * Returns the state file of class MiniGitState() that represents MiniGits current state.
     * */
    static MiniGitState getMiniGitState() {
        try {
            return getAndReadObjectFile(STATE_DIR, MINI_GIT_STATE, MiniGitState.class);
        } catch (Exception e){
            System.out.println("Unable to read current state");
            System.exit(0);
        }
        return null;
    }

    /**Returns the current head Commit*/
    public static Commit getHeadCommit(){
        return getCommit(getMiniGitState().getHead());
    }

    /**Returns the head commit as saved in the MiniGitState file, if no state file exists, or head is null, return null*/
    static Commit getCommit(String fileName) {
        return CommitGraph.getCommit(COMMITS_DIR, fileName);
    }

    /**
     * Returns true if a file does not already exist in Repository.STORED_FILE_DIR AND it has not already been staged.
     * */
    private static Boolean shouldStage(File file) {
        // a file should be staged if it does not exist in the blobs dir OR the staging Area
        if (!StoredFiles.savedInMiniGitFileFormat(STORED_FILE_DIR, file) && !StoredFiles.sameMiniGitFormatFileExists(STAGING_AREA_DIR, file));
        return true;
    }

    /**
     * Writes a file to the staging area, overwrites and previous entry of the same name.
     * */
    public static void stageFile(File f){
        StoredFiles.overwriteMiniGitFormatFile(f, STAGING_AREA_DIR);
    }

    /**
     * Returns a File[] from the Repository.STAGING_AREA_DIR
     * */
    private static File[] getStagedFile(){
        return STAGING_AREA_DIR.listFiles();
    }

    /**
     * Returns a String[] of files names from the Repository.STAGING_AREA_DIR
     * */
    static String[] getStagedFileNames(){
        return STAGING_AREA_DIR.list();
    }

    /**
     * Empties the Staging Area Directory as specified by Repository.STAGING_AREA_DIR
     * */
    private static void emptyStagingArea(){
        File[] files = STAGING_AREA_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                FileUtilities.deleteDirectory(file);
            }
        }
    }

    /**
     * Returns true if a file is found in the given file location that is not present in the given tracked files.
     * Directories and Hidden Files are excluded from the search.
     * */
    private static boolean untrackedFiles(File location, Set<String> trackedFiles) {

        try {
            for (File f : location.listFiles()) {
                if (!f.isDirectory() && !f.isHidden() && !trackedFiles.contains(f.getName())) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Unable to determine tracked files");
        }

        return false;
    }


}
