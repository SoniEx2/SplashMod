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

/**
 * @author soniex2
 */
@Mod(modid = "splash", name = "Splash", version = "1.0.0")
public class Splash {
    private static final Random rand = new Random();

    private class ModSplashes {
        public final List<String> splashes;
        public final String modid;

        public ModSplashes(String modid, List<String> splashes) {
            this.modid = modid;
            this.splashes = splashes;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ArrayList<ModSplashes> splashes = new ArrayList<ModSplashes>();
        for (ModContainer mod : Loader.instance().getModList()) {
            String modid = mod.getModId();
            InputStream is = getClass().getResourceAsStream("/assets/splash/splashes/" + modid + ".txt");
            if (is == null) continue;
            ArrayList<String> modSplashes = new ArrayList<String>();
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
                String s;
                while ((s = r.readLine()) != null) {
                    s = s.trim();
                    if (!s.isEmpty())
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
                splashes.add(new ModSplashes(modid, modSplashes));
            }
        }
        if (!splashes.isEmpty()) {
            ModSplashes splash = splashes.get(rand.nextInt(splashes.size()));
            String s = splash.splashes.get(rand.nextInt(splash.splashes.size()));
            s = s.replace("%n", "\n").replace("%%", "%");
            for (String s1 : s.split("\n"))
                if (!s1.isEmpty())
                    LogManager.getLogger(splash.modid).info(s1);
        }
    }
}