package MiniGit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A serializable class that represents the current state of the MiniGit Program at any given time.
 * Each Method that mutates the state in some way calls Save() and the clas sis written to the given name and location
 * as specified on initialization.
 * */
public class MiniGitState extends SavableState {
    private String head;
    private String branch;
    private HashMap<String, String> branches;
    private Set<String> stagedToRemove;

    MiniGitState(String head, String filePath, String name, String defaultBranch) {
        super(name, filePath);
        this.head = head;
        branches = new HashMap<>();
        branches.put(defaultBranch, null);
        branch = defaultBranch;
        this.stagedToRemove = new HashSet<>();
        this.save();
    }

    /**
     * Returns a string containing the Sha1 Commit ID of the current head commit.
     * */
    public String getHead(){
        return head;
    }

    /**
     * Moves the head pointer to the newly specified Commit.
     * The branch pointer of the current branch is updated to reflect the change as well.
     * */
    public void setNewHead(String toCommit) {
        head = toCommit;
        updateBranchID(branch);
        save();
    }


    /**
     * Adds a file name to the stagedToRemove Set<String>.
     * */
    public void stageForRemoval(String fileName){
        stagedToRemove.add(fileName);
        save();
    }

    /**
     * Returns a Set<String> of files staged for removal.
     * */
    public Set<String> getUnstagedFiles(){
        return stagedToRemove;
    }

    /**
     * Updates the branch pointer of the given branch to the current head commit.
     * */
    private void updateBranchID(String branch) {
        branches.put(branch, head);
    }

    /**
     * Returns a String of the current branch name
     * */
    public String getBranch(){
        return branch;
    }

    /**
     * Returns a String of the Commit Id of the given branch names pointer.
     * */
    public String getBranchCommitID(String name){
        String commitId = branches.get(name);
        if (commitId != null) {
            return commitId;
        } else {
            System.out.println("Specified branch " + name + " not found.");
        }
        return null;
    }

    /**
     * Returns a Set<String> of branch names.
     * */
    public Set<String> getAllBranchNames(){
        return branches.keySet();
    }

    /**
     * Adds a new branch to the branches Map<String,String> with the given name as the key,
     * and the current head as the value.
     * */
    public void addNewBranch(String name){
        if (name != null) {
            this.branches.put(name, head);
            this.save();
        }
    }

    /**
     * Moves the head pointer to the specified branch head.
     * */
    public boolean setBranchAsHead(String branch){
        if (branches.containsKey(branch)){
            this.branch = branch;
            this.head = branches.get(branch);
            save();
            return true;
        } else {
            System.out.println("Branch: \"" + branch + "\" does not exist.");
            return false;
        }
    }

    /**
     * Removes the specified branch from the branches map.
     * */
    public void removeBranch(String name){

        if (branches.containsKey(name)){
             if (!branch.equals(name)) {

                 branches.remove(name);
                 save();
             } else {
                 System.out.println("Current branch cannot be removed.");
             }
        } else {
            System.out.println("Branch: \"" + branch + "\" does not exist.");
        }
    }
}
