package MiniGit;

/**Entry Point of MiniGit - your favourite lightweight version control system.
 * @author Hamish Young
 * */
public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String command = args[0];
        String[] commandArgs = getCommandArgs(args, args.length);
        int length =  commandArgs.length;

        if (!repoAlreadyExists() && !commandIsInit(command)){
            System.out.println("MiniGit has not been initialised in this directory.");
            System.exit(0);
        }

        switch (command) {
            case "init":
                if (repoAlreadyExists()) {
                    System.out.println("A MiniGit version-control system already exists in the current directory.");
                } else {
                    Repository r = new Repository();
                }
                break;

            case "commit":
                if (!oneArgumentMax(length)) {
                    printInvalidArgsWarning();
                } else {
                    Repository.commit(commandArgs[0]);
                }
                break;

            case "add":
                if (!oneArgumentMax(length)) {
                    printInvalidArgsWarning();
                } else {
                    Repository.addSwitch(commandArgs, length);
                }
                break;

            case "rm":
                if (!oneArgumentMax(length)) {
                    printInvalidArgsWarning();
                } else {
                    Repository.rm(commandArgs[0]);
                }
                break;

            case "log":
                Repository.log();
                break;

            case "global-log":
                Repository.globalLog();
                break;

            case "find":
                if (!oneArgumentMax(length)) {
                    printInvalidArgsWarning();
                } else {
                    Repository.find(commandArgs[0]);
                }
                break;

            case "status":
                Repository.status();
                break;

            case "checkout":
                if (!correctArgsNumber(1,2 , length)){
                    printInvalidArgsWarning();
                } else {
                    Repository.checkoutSwitch(commandArgs, length);
                }
                break;

            case "branch":
                if (!oneArgumentMax(length)) {
                    printInvalidArgsWarning();
                } else {
                    Repository.branch(commandArgs[0]);
                }
                break;

            case "rm-branch":
                if (!oneArgumentMax(length)){
                    printInvalidArgsWarning();
                } else {
                    Repository.rmBranch(commandArgs[0]);
                }
                break;

            case "reset":
                if (!oneArgumentMax(length)){
                    printInvalidArgsWarning();
                } else {
                    Repository.reset(commandArgs[0]);
                }
                break;

            case "merge":
                if (!oneArgumentMax(length)){
                    printInvalidArgsWarning();
                } else {
                    Repository.merge(commandArgs[0]);
                }
                break;
        }
    }


    private static String[] getCommandArgs(String[] args, int argsLength){
        if (argsLength == 1){
            return new String[0];
        }
        String[] methodArgs = new String[argsLength - 1];
        System.arraycopy(args, 1, methodArgs, 0, argsLength - 1);
        return methodArgs;
    }

    private static boolean correctArgsNumber(int min, int max, int actual){
        return actual >= min && actual <= max;
    }

    private static boolean oneArgumentMax(int actual){
        return correctArgsNumber(1, 1, actual);
    }

    private static void printInvalidArgsWarning(){
        System.out.println("Invalid Arguments");
    }

    private static Boolean repoAlreadyExists(){
        return Repository.miniGitExists();
    }

    private static Boolean commandIsInit(String command){
        return command.equals("init");
    }
}
