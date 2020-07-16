package org.virayer.sqlcompletion.util;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @author Virayer
 * @date 2020/7/15
 */
public class JarPathUtil {
    public static String getPath() {
        String jarWholePath = JarPathUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
        } catch (UnsupportedEncodingException e) { System.out.println(e.toString()); }
        String jarPath = new File(jarWholePath).getParentFile().getAbsolutePath();
        return jarPath;

    }
}
