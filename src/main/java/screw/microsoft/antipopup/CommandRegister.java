package screw.microsoft.antipopup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.bukkit.Bukkit.getLogger;

public class CommandRegister implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1
        && sender instanceof ConsoleCommandSender) {
            switch (args[0]) {
                case "setup" -> {
                    try {
                        FileInputStream in = new FileInputStream("server.properties");
                        Properties props = new Properties();
                        props.load(in);
                        if (Boolean.parseBoolean(props.getProperty("enforce-secure-profile"))) {
                            getLogger().info("enforce-secure-profile got disabled.");
                            props.setProperty("enforce-secure-profile", String.valueOf(false));
                            in.close();
                            FileOutputStream out = new FileOutputStream("server.properties");
                            props.store(out, "");
                            out.close();
                        } else {
                            getLogger().info("AntiPopup has been already set up.");
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            }

        }
        return false;
    }
}