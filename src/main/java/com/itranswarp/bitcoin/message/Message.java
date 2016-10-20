package com.itranswarp.bitcoin.message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.itranswarp.bitcoin.BitcoinConstants;
import com.itranswarp.bitcoin.BitcoinException;
import com.itranswarp.bitcoin.io.BitCoinInput;
import com.itranswarp.bitcoin.io.BitCoinOutput;
import com.itranswarp.cryptocurrency.common.Hash;

/**
 * P2P message:
 * https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
 * 
 * @author liaoxuefeng
 */
public abstract class Message {

	byte[] command;

	public Message(String cmd) {
		this.command = getCommandBytes(cmd);
	}

	public byte[] toByteArray() {
		byte[] payload = getPayload();
		return new BitCoinOutput().writeInt(BitcoinConstants.MAGIC) // magic
				.write(this.command) // command: char[12]
				.writeInt(payload.length) // length: uint32_t
				.write(getCheckSum(payload)) // checksum: uint32_t
				.write(payload) // payload:
				.toByteArray();
	}

	/**
	 * Parse stream as expected command message, and return payload.
	 */
	public static byte[] parsePayload(String command, BitCoinInput input) throws IOException {
		if (input.readInt() != BitcoinConstants.MAGIC) {
			throw new BitcoinException("Bad magic.");
		}
		byte[] cmd = new byte[12];
		input.readFully(cmd);
		String actualCommand = getCommandFrom(cmd);
		if (!command.equals(actualCommand)) {
			throw new BitcoinException("Unexpected command: expect " + command + " but actual " + actualCommand);
		}
		int payloadLength = input.readInt();
		byte[] expectedChecksum = new byte[4];
		input.readFully(expectedChecksum);
		byte[] payload = new byte[payloadLength];
		input.readFully(payload);
		// check:
		byte[] actualChecksum = getCheckSum(payload);
		if (!Arrays.equals(expectedChecksum, actualChecksum)) {
			throw new BitcoinException("Checksum failed.");
		}
		return payload;
	}

	protected abstract byte[] getPayload();

	static String getCommandFrom(byte[] cmd) {
		int n = cmd.length - 1;
		while (n >= 0) {
			if (cmd[n] == 0) {
				n--;
			} else {
				break;
			}
		}
		if (n <= 0) {
			throw new BitcoinException("Bad command bytes.");
		}
		byte[] b = Arrays.copyOfRange(cmd, 0, n);
		return new String(b, StandardCharsets.UTF_8);
	}

	static byte[] getCommandBytes(String cmd) {
		byte[] cmdBytes = cmd.getBytes();
		if (cmdBytes.length < 1 || cmdBytes.length > 12) {
			throw new IllegalArgumentException("Bad command: " + cmd);
		}
		byte[] buffer = new byte[12];
		System.arraycopy(cmdBytes, 0, buffer, 0, cmdBytes.length);
		return buffer;
	}

	static byte[] getCheckSum(byte[] payload) {
		byte[] hash = Hash.doubleSha256(payload);
		return Arrays.copyOfRange(hash, 0, 4);
	}
}
