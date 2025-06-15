package io.jungmini.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GlobalSessionIdGenerator {
	public static final String SERVER_ID;

	static {
		try {
			SERVER_ID = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public static String generateSessionId(String sessionId) {
		return SERVER_ID + "-" + sessionId;
	}
}
