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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author soniex2
 */
@Mod(modid = "splash", name = "Splash", version = "0.1.2")
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

        @Override
        public String toString() {
            return "{modid=" + modid + ",logname=" + logname + ",splashes=" + splashes + "}";
        }
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent event) {

        final ArrayList<ModSplashes> splashes = new ArrayList<ModSplashes>();

        final Set<Class> classes = new HashSet<Class>();

        for (final ModContainer mod : Loader.instance().getModList()) {
            classes.add(mod.getClass());
            if (mod.getMod() != null) {
                classes.add(mod.getMod().getClass());
            }
        }

        for (final ModContainer mod : Loader.instance().getModList()) {
            final Set<String> modSplashes = new HashSet<String>();
            String logname = null;
            for (final Class clazz : classes) {
                final String filename = "/assets/splash/splashes/" + mod.getModId() + ".txt";
                final InputStream is = clazz.getResourceAsStream(filename);
                if (is == null) continue;

                if (logname == null)
                    logname = mod.getModId();
                BufferedReader r = null;

                try {
                    r = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
                    String s;

                    s = r.readLine();
                    if (s != null && s.trim()/* NOTE: this doesn't change the value of "s" */.startsWith("#!")
                            && s.indexOf('!') + 1 <= s.length()) {
                        logname = processEscapes(s.substring(s.indexOf('!') + 1));
                    } else if (s != null) {
                        s = s.trim();
                        if (!s.startsWith("#") && !s.isEmpty())
                            modSplashes.add(s);
                    }

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
            }
            if (!modSplashes.isEmpty()) {
                splashes.add(new ModSplashes(mod.getModId(), logname, new ArrayList<String>(modSplashes)));
            }
        }

        System.out.println(splashes);

        if (!splashes.isEmpty()) {

            final ModSplashes splash = splashes.get(rand.nextInt(splashes.size()));
            final String s = processEscapes(splash.splashes.get(rand.nextInt(splash.splashes.size())));

            for (final String s1 : s.split("\n"))
                if (!s1.isEmpty())
                    LogManager.getLogger(splash.logname).info(s1);

        }
    }

    private String processEscapes(final String s) {
        final Matcher m = p.matcher(s);
        final StringBuilder sb = new StringBuilder();

        int last = 0;
        while (m.find()) {
            sb.append(s.substring(last, m.start()));

            final String s2;
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

        return sb.toString();
    }
}
