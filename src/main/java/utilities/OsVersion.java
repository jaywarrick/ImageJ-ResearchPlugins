//  OsVersion.java
//  MicroFluidicHT_Tools
//
//  Created by erwin berthier on 7/4/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package utilities;

public final class OsVersion {
	
	public static final String OS_NAME = System.getProperty("os.name");
	
	private static boolean startsWith(String str, String prefix)
	{
		return str != null && str.startsWith(prefix);
	}
	
	private static boolean startsWithIgnoreCase(String str, String prefix)
	{
		return str != null && str.toUpperCase().startsWith(prefix.toUpperCase());
	}
	
	/**
	 * True if this is FreeBSD.
	 */
	public static final boolean IS_FREEBSD = startsWithIgnoreCase(OS_NAME, "FreeBSD");
	
	/**
	 * True if this is Linux.
	 */
	public static final boolean IS_LINUX = startsWithIgnoreCase(OS_NAME, "Linux");
	
	/**
	 * True if this is HP-UX.
	 */
	public static final boolean IS_HPUX = startsWithIgnoreCase(OS_NAME, "HP-UX");
	
	/**
	 * True if this is AIX.
	 */
	public static final boolean IS_AIX = startsWithIgnoreCase(OS_NAME, "AIX");
	
	/**
	 * True if this is SunOS.
	 */
	public static final boolean IS_SUNOS = startsWithIgnoreCase(OS_NAME, "SunOS");
	
	/**
	 * True if this is OS/2.
	 */
	public static final boolean IS_OS2 = startsWith(OS_NAME, "OS/2");
	
	/**
	 * True if this is the Mac OS X.
	 */
	public static final boolean IS_OSX = startsWith(OS_NAME, "Mac") && OS_NAME.endsWith("X");
	
	/**
	 * True if this is Windows.
	 */
	public static final boolean IS_WINDOWS = startsWith(OS_NAME, "Windows");
	
	/**
	 * True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc).
	 */
	public static final boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS;
}