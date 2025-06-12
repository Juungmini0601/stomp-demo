package io.jungmini.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerIdHolder {

	public static final String SERVER_ID;

	static {
		try {
			SERVER_ID = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
