package com.barbogogo.leedreader;

import java.io.IOException;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.leed.reader.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.NetworkOnMainThreadException;

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

    public static String htmlspecialchars(String src)
    {
        src = src.replace("&", "&amp;");
        src = src.replace("\"", "&quot;");
        src = src.replace("'", "&#039;");
        src = src.replace("<", "&lt;");
        src = src.replace(">", "&gt;");

        return src;
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

    public static String extractArticle(String article, int length)
    {
        String output = "";
        String pattern = "<([^><]*)>";
        Matcher matches2 = Pattern.compile(pattern).matcher(article);

        while (matches2.find())
        {
            output = matches2.replaceAll("");
        }

        String[] test = output.split(" ");

        output = "";

        for (int i = 0; i < length; i++)
        {
            if (test.length > i)
            {
                output += test[i] + " ";
            }
        }

        output = output.replaceAll("&Aacute;", "Á");
        output = output.replaceAll("&aacute;", "á");
        output = output.replaceAll("&Agrave;", "À");
        output = output.replaceAll("&agrave;", "à");
        output = output.replaceAll("&Acirc;", "Â");
        output = output.replaceAll("&acirc;", "â");
        output = output.replaceAll("&Auml;", "Ä");
        output = output.replaceAll("&auml;", "ä");
        output = output.replaceAll("&Atilde;", "Ã");
        output = output.replaceAll("&atilde;", "ã");
        output = output.replaceAll("&Aring;", "Å");
        output = output.replaceAll("&aring;", "å");
        output = output.replaceAll("&Aelig;", "Æ");
        output = output.replaceAll("&aelig;", "æ");
        output = output.replaceAll("&Ccedil;", "Ç");
        output = output.replaceAll("&ccedil;", "ç");
        output = output.replaceAll("&Eth;", "Ð");
        output = output.replaceAll("&eth;", "ð");
        output = output.replaceAll("&Eacute;", "É");
        output = output.replaceAll("&eacute;", "é");
        output = output.replaceAll("&Egrave;", "È");
        output = output.replaceAll("&egrave;", "è");
        output = output.replaceAll("&Ecirc;", "Ê");
        output = output.replaceAll("&ecirc;", "ê");
        output = output.replaceAll("&Euml;", "Ë");
        output = output.replaceAll("&euml;", "ë");
        output = output.replaceAll("&Iacute;", "Í");
        output = output.replaceAll("&iacute;", "í");
        output = output.replaceAll("&Igrave;", "Ì");
        output = output.replaceAll("&igrave;", "ì");
        output = output.replaceAll("&Icirc;", "Î");
        output = output.replaceAll("&icirc;", "î");
        output = output.replaceAll("&Iuml;", "Ï");
        output = output.replaceAll("&iuml;", "ï");
        output = output.replaceAll("&Ntilde;", "Ñ");
        output = output.replaceAll("&ntilde;", "ñ");
        output = output.replaceAll("&Oacute;", "Ó");
        output = output.replaceAll("&oacute;", "ó");
        output = output.replaceAll("&Ograve;", "Ò");
        output = output.replaceAll("&ograve;", "ò");
        output = output.replaceAll("&Ocirc;", "Ô");
        output = output.replaceAll("&ocirc;", "ô");
        output = output.replaceAll("&Ouml;", "Ö");
        output = output.replaceAll("&ouml;", "ö");
        output = output.replaceAll("&Otilde;", "Õ");
        output = output.replaceAll("&otilde;", "õ");
        output = output.replaceAll("&Oslash;", "Ø");
        output = output.replaceAll("&oslash;", "ø");
        output = output.replaceAll("&oelig;", "œ");
        output = output.replaceAll("&OElig;", "Œ");
        output = output.replaceAll("&szlig;", "ß");
        output = output.replaceAll("&Thorn;", "Þ");
        output = output.replaceAll("&thorn;", "þ");
        output = output.replaceAll("&Uacute;", "Ú");
        output = output.replaceAll("&uacute;", "ú");
        output = output.replaceAll("&Ugrave;", "Ù");
        output = output.replaceAll("&ugrave;", "ù");
        output = output.replaceAll("&Ucirc;", "Û");
        output = output.replaceAll("&ucirc;", "û");
        output = output.replaceAll("&Uuml;", "Ü");
        output = output.replaceAll("&uuml;", "ü");
        output = output.replaceAll("&Yacute;", "Ý");
        output = output.replaceAll("&yacute;", "ý");
        output = output.replaceAll("&yuml;", "ÿ");
        output = output.replaceAll("&copy;", "©");
        output = output.replaceAll("&reg;", "®");
        output = output.replaceAll("&trade;", "™");
        output = output.replaceAll("&amp;", "&");
        output = output.replaceAll("&lt;", "<");
        output = output.replaceAll("&gt;", ">");
        output = output.replaceAll("&euro;", "€");
        output = output.replaceAll("&cent;", "¢");
        output = output.replaceAll("&pound;", "£");
        output = output.replaceAll("&quot;", "\"");
        output = output.replaceAll("&lsquo;", "‘");
        output = output.replaceAll("&rsquo;", "’");
        output = output.replaceAll("&ldquo;", "“");
        output = output.replaceAll("&rdquo;", "”");
        output = output.replaceAll("&laquo;", "«");
        output = output.replaceAll("&raquo;", "»");
        output = output.replaceAll("&mdash;", "—");
        output = output.replaceAll("&ndash;", "–");
        output = output.replaceAll("&deg;", "°");
        output = output.replaceAll("&plusmn;", "±");
        output = output.replaceAll("&frac14;", "¼");
        output = output.replaceAll("&frac12;", "½");
        output = output.replaceAll("&frac34;", "¾");
        output = output.replaceAll("&times;", "×");
        output = output.replaceAll("&divide;", "÷");
        output = output.replaceAll("&alpha;", "α");
        output = output.replaceAll("&beta;", "ß");
        output = output.replaceAll("&infin;", "∞");
        output = output.replaceAll("&nbsp;", " ");

        output = output + "...";

        return output;
    }

    public static String extractImage(Context context, String article)
    {
        String output = "";

        String pattern = "<img ([^><]*) src=\"([^=\"><]*)\" ([^><]*)>";

        Matcher matches2 = Pattern.compile(pattern).matcher(article);

        if (matches2.find())
        {
            output = matches2.group(2);
        }

        if (output.isEmpty())
        {
            output = null;
        }

        // Bitmap outputBmp = getBitmapFromURL(context, output);

        return output;
    }

    public static Bitmap getBitmapFromURL(Context context, String src)
    {
        Bitmap myBitmap = null;

        URL url;
        try
        {
            url = new URL(src);
            myBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }
        catch (MalformedURLException e)
        {
            myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        }
        catch (IOException e)
        {
            myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        }
        catch (NetworkOnMainThreadException e)
        {
            myBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        }

        return myBitmap;
    }
}
