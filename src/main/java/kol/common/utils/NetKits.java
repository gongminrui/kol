package kol.common.utils;

public class NetKits {
	//v2ray default proxy setting
	public static void setProxyNet() {
		System.setProperty("proxySet", "true");
		System.setProperty("proxyHost", "217.0.0.1");
		System.setProperty("proxyPort", "10808");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyPort", "10809");
	}
}
