package com.barbogogo.leedreader;

import java.lang.StringBuilder;
import java.util.Formatter;

public class Utils
{
    public static String hex(byte[] src)
    {
        Formatter fmt = new Formatter(new StringBuilder(src.length * 2));

        for (byte b : src)
        {
            fmt.format("%02x", b);
        }

        String hex = fmt.toString();
        fmt.close();
        
        return hex;
    }

    public static int versionCompare(String actualVersion, String serverVersion)
    {
        int versionCompare = 0;

        actualVersion = actualVersion.replace("Beta", "").trim();
        serverVersion = serverVersion.replace("Beta", "").trim();

        String[] vals1 = serverVersion.split("\\.");
        String[] vals2 = actualVersion.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
        {
            i++;
        }

        if (i < vals1.length && i < vals2.length)
        {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            versionCompare = diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }
        else
            versionCompare = vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;

        return versionCompare;
    }

}
