package com.github.soniex2.splash;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author soniex2
 */
@Mod(modid = "splash", name = "Splash", version = "0.1.1")
public class Splash {
    private static final Random rand = new Random();

    private static final Pattern p = Pattern.compile("%(.)", Pattern.DOTALL);

    private class ModSplashes {
        public final List<String> splashes;
        public final String modid;
        private final String logname;

        public ModSplashes(String modid, String logname, List<String> splashes) {
            this.modid = modid;
            this.logname = logname;
            this.splashes = splashes;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ArrayList<ModSplashes> splashes = new ArrayList<ModSplashes>();
        for (ModContainer mod : Loader.instance().getModList()) {
            String logname = mod.getModId();
            String filename = "/assets/splash/splashes/" + mod.getModId() + ".txt";
            InputStream is = getClass().getResourceAsStream(filename);
            if (is == null) continue;
            ArrayList<String> modSplashes = new ArrayList<String>();
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
                String s;
                /*
                START Logname Flag (#!len:name on first line).
                Used by BuildCraft and a few other mods.
                TODO: rewrite it to be more intuitive
                 */
                s = r.readLine();
                boolean flag = false;
                if (s != null && s.trim()/* NOTE: this doesn't change the value of "s" */.startsWith("#!")) {
                    if (s.indexOf('!') + 1 <= s.length()) {
                        String[] split = s.substring(s.indexOf('!') + 1).split(":", 2);
                        if (split.length >= 2) {
                            try {
                                int i = Integer.parseInt(split[0]);
                                if (i > 0) {
                                    StringBuilder sb = new StringBuilder(i).append(split[1]);
                                    while (sb.length() < i) {
                                        s = r.readLine();
                                        if (s != null) {
                                            sb.append("\n").append(s);
                                        } else {
                                            break;
                                        }
                                    }
                                    if (sb.length() >= i) {
                                        flag = true;
                                        sb.setLength(i);
                                        logname = sb.toString();
                                    } else {
                                        event.getModLog().error("Errors were found while parsing file " + filename + ". " +
                                                "Could not parse logname. Defaulting to " + logname);
                                    }
                                } else {
                                    event.getModLog().error("Errors were found while parsing file " + filename + ". " +
                                            "Invalid logname length. Using default logname: " + logname);
                                }
                            } catch (NumberFormatException e) {
                                event.getModLog().error("Errors were found while parsing file " + filename + ". " +
                                        "Invalid logname length. Using default logname: " + logname, e);
                            }
                        } // else treat it like it's a comment
                    } // else treat it like it's a comment
                }
                if (!flag && s != null) {
                    s = s.trim();
                    if (!s.startsWith("#") && !s.isEmpty())
                        modSplashes.add(s);
                }
                /*
                END Logname Flag.
                 */
                while ((s = r.readLine()) != null) {
                    s = s.trim();
                    if (!s.startsWith("#") && !s.isEmpty())
                        modSplashes.add(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            if (!modSplashes.isEmpty()) {
                splashes.add(new ModSplashes(mod.getModId(), logname, modSplashes));
            }
        }
        if (!splashes.isEmpty()) {
            ModSplashes splash = splashes.get(rand.nextInt(splashes.size()));
            String s = splash.splashes.get(rand.nextInt(splash.splashes.size()));
            Matcher m = p.matcher(s);
            StringBuilder sb = new StringBuilder();
            int last = 0;
            while (m.find()) {
                sb.append(s.substring(last, m.start()));
                String s2;
                switch (m.group(1).charAt(0)) {
                    case 'n':
                        s2 = "\n";
                        break;
                    default:
                        s2 = m.group(1);
                }
                sb.append(s2);
                last = m.end();
            }
            sb.append(s.substring(last));
            s = sb.toString();
            for (String s1 : s.split("\n"))
                if (!s1.isEmpty())
                    LogManager.getLogger(splash.logname).info(s1);
        }
    }
}
