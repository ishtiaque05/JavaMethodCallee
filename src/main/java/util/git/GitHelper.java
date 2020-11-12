package util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;

import java.io.File;
import java.io.IOException;

public class GitHelper {
    public static void checkoutCMD(String commit, String repoPath) throws Exception {
        try {
//            ProcessBuilder processBuilder = new ProcessBuilder();
//            processBuilder.command("bash", "-c", "cd "+ repoPath, "git reset --hard", "git clean -xdf");
//            Process process = processBuilder.start();
//            process.waitFor();
            Git git = Git.open(new File(repoPath + "/.git"));
            git.reset().setMode( ResetCommand.ResetType.HARD ).call();
            git.clean().setCleanDirectories(true).setForce(true);
            git.checkout().setName(commit).setForced(true).call();
            git.clean().setCleanDirectories(true).setForce(true);
            git.reset().setMode( ResetCommand.ResetType.HARD ).call();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error");
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
            throw new Exception("Error");
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
            throw new Exception("Error");
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
            throw new Exception("Error");
        } catch (RefNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Error");
        } catch (GitAPIException e) {
            e.printStackTrace();
            throw new Exception("Error");
        }
    }
}
