package com.itranswarp.bitcoin;

public final class BitcoinConstants {

	public static final int MAGIC = 0xd9b4bef9;

	public static final int PORT = 8333;

	public static final int PROTOCOL_VERSION = 60002;

	public static final long NETWORK_SERVICES = 1L;

	public static final long NODE_ID = (long) (Math.random() * Long.MAX_VALUE);

}
