package sir.barchable.clash;

import com.beust.jcommander.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Common entry point for the command line tools.
 *
 * @author Sir Barchable
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @ParametersDelegate
    private Env env = new Env();

    private ProxyCommand proxyCommand = new ProxyCommand();
    private ServerCommand serverCommand = new ServerCommand();
    private DumpCommand dumpCommand = new DumpCommand();

    @Parameters(commandDescription = "Run the clash proxy")
    public static class ProxyCommand {
        @Parameter(names = {"-s", "--save"}, description = "Save messages to the 'villages' directory")
        private boolean save;

        @Parameter(names = {"-n", "--name-server"}, description = "Name server to read up-stream server address from")
        private String nameServer = "8.8.8.8";

        public boolean getSave() {
            return save;
        }

        public String getNameServer() {
            return nameServer;
        }
    }

    @Parameters(commandDescription = "Run the clash server")
    public static class ServerCommand {
        @Parameter(names = {"--home"}, required = true, description = "Home village file")
        private File homeFile;

        @Parameter(names = {"--loadout"}, description = "Name of loadout to apply")
        private String loadout;

        public File getHomeFile() {
            return homeFile;
        }

        public String getLoadout() {
            return loadout;
        }
    }

    @Parameters(commandDescription = "Decode captured tcp dumps")
    public static class DumpCommand {
        @Parameter(names = {"-h", "--hex"}, description = "Dump messages as hex")
        private boolean dumpHex;

        @Parameter(names = {"-j", "--json"}, description = "Dump messages as json")
        private boolean dumpJson;

        public boolean getDumpHex() {
            return dumpHex;
        }

        public boolean getDumpJson() {
            return dumpJson;
        }
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        JCommander commander = new JCommander(main);

        commander.addCommand("proxy", main.proxyCommand);
        commander.addCommand("server", main.serverCommand);
        commander.addCommand("dump", main.dumpCommand);

        try {
            commander.parse(args);
            main.run(commander.getParsedCommand());
        } catch (ParameterException e) {
            if (e.getMessage() != null) {
                System.err.println(e.getMessage());
            }
            commander.usage();
        } catch (Exception e) {
            System.err.println("Oops: " + e);
        }
    }

    private void run(String command) throws IOException, InterruptedException {
        ClashServices services = new ClashServices(env);

        if (command == null) {
            throw new ParameterException("Missing command");
        }

        switch (command) {
            case "proxy":
                ClashProxy proxy = new ClashProxy(services, proxyCommand);
                proxy.run();
                break;

            case "server":
                ClashServer server = new ClashServer(services, serverCommand);
                server.run();
                break;

            case "dump":
                Dump dump = new Dump(services, dumpCommand);
                dump.run();
                break;

            default:
                throw new ParameterException("Unknown command '" + command + "'");
        }
    }
}