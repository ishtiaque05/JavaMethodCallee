package util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;

import java.io.File;
import java.io.IOException;

public class GitHelper {
    public static void checkoutCMD(String commit, String repoPath) {
        try {
            Git git = Git.open(new File(repoPath + "/.git"));
            git.checkout().setName(commit).call();
            git.clean();
            git.reset().setMode( ResetCommand.ResetType.HARD ).call();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
        } catch (CheckoutConflictException e) {
            e.printStackTrace();
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RefNotFoundException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
