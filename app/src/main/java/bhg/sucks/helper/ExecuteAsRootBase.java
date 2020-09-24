package bhg.sucks.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Abstract class that encapsulates root access.
 * <p>
 * More or less copied from http://muzikant-android.blogspot.com/2011/02/how-to-get-root-access-and-execute.html
 * </p>
 */
public abstract class ExecuteAsRootBase {

    /**
     * Tests, if root commands can be run.
     *
     * @return <i>true</i>, if app has root access
     */
    public static boolean canRunRootCommands() {
        boolean retval;

        Process suProcess;
        try {
            suProcess = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
            return false;
        }

        try (DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
             BufferedReader osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));
        ) {
            // Getting the id of the current user to check if this is root
            os.writeBytes("id\n");
            os.flush();

            String currUid = osRes.readLine();
            boolean exitSu;
            if (null == currUid) {
                retval = false;
                exitSu = false;
                Log.d("ROOT", "Can't get root access or denied by user");
            } else if (currUid.contains("uid=0")) {
                retval = true;
                exitSu = true;
                Log.d("ROOT", "Root access granted");
            } else {
                retval = false;
                exitSu = true;
                Log.d("ROOT", "Root access rejected: " + currUid);
            }

            if (exitSu) {
                os.writeBytes("exit\n");
                os.flush();
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            retval = false;
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }

        return retval;
    }

    /**
     * Performs execution of the commands, see {@link #getCommandsToExecute()}.
     *
     * @return <i>true</i>, if the commands could be executed
     */
    public final boolean execute() {
        boolean ret = false;

        try {
            List<String> commands = getCommandsToExecute();
            if (null != commands && commands.size() > 0) {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        ret = true;
                    } else {
                        // Root access denied
                        ret = false;
                    }
                } catch (Exception ex) {
                    Log.e("ROOT", "Error executing root action", ex);
                }
            }
        } catch (IOException | SecurityException ex) {
            Log.w("ROOT", "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w("ROOT", "Error executing internal operation", ex);
        }

        return ret;
    }

    /**
     * Defines the shell commands to be run.
     */
    protected abstract List<String> getCommandsToExecute();

}
