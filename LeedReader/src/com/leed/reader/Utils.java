package com.leed.reader;

import java.lang.StringBuilder;
import java.util.Formatter;


public class Utils
{
	public static String hex(byte[] src) {
		Formatter fmt = new Formatter(new StringBuilder(src.length*2));

		for(byte b: src) {
			fmt.format("%02x", b);
		}

		return fmt.toString();
	}
}

