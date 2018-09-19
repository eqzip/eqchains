/**
 * EQCoin core - EQZIP's EQCoin core library
 * @copyright 2018 EQZIP Inc.  All rights reserved...
 * https://www.eqzip.com
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqzip.eqcoin.util;

import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Util {

	public final static int TWO = 2;

	public final static int SIXTEEN = 16;

	public final static int HUNDRED = 100;

	public final static String WINDOWS_PATH = "C:\\EQCOIN";

	public final static String MAC_PATH = "C:\\Program Files\\EQCOIN";

	public final static String LINUX_PATH = "C:\\Program Files\\EQCOIN";

	public static String PATH = null;

	public enum Os {
		WINDOWS, MAC, LINUX
	}

	private Util() {
	}

	public static void init(final Os os) {
		switch (os) {
		case MAC:
			PATH = MAC_PATH;
			break;
		case LINUX:
			PATH = LINUX_PATH;
			break;
		case WINDOWS:
		default:
			PATH = WINDOWS_PATH;
			break;
		}
	}

	public static byte[] dualSHA3_512(final byte[] data) {
		byte[] bytes = null;
		try {
			bytes = MessageDigest.getInstance("SHA3-512").digest(MessageDigest.getInstance("SHA3-512").digest(data));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

	public static byte[] multipleExtend(final byte[] data, final int multiple) {
		byte[] result = new byte[data.length * multiple];
		for (int i = 0; i < multiple; ++i) {
			for (int j = 0; j < data.length; ++j) {
				result[j + data.length * i] = data[j];
			}
		}
		return result;
	}

	public static byte[] updateNonce(final byte[] bytes, final long nonce) {
		System.arraycopy(Util.longToBytes(nonce), 0, bytes, 200, 8);
		return bytes;
	}

	public static String getGMTTime(final long timestamp) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(timestamp);
	}

	public static String getHexString(final byte[] bytes) {
		return bigIntegerTo512String(new BigInteger(1, bytes));
	}

	public static BigInteger getDefaultTarget() {
		return BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(424 + 4);
	}

	public static byte[] bigIntegerTo64Bytes(final BigInteger foo) {
		byte[] tmp = new byte[64];
		byte[] fooBytes = foo.toByteArray();
		// Because big-endian byte-order so fill 0 in the high position to fill the gap
		System.arraycopy(fooBytes, 0, tmp, tmp.length - fooBytes.length, fooBytes.length);
		return tmp;
	}

	public static BigInteger bytesToBigInteger(final byte[] foo) {
		return new BigInteger(foo);
	}

	public static String bigIntegerTo512String(final BigInteger foo) {
		return bigIntegerToFixedLengthString(foo, 512);
	}

	public static String bigIntegerToFixedLengthString(final BigInteger foo, final int len) {
		String tmp = foo.toString(16);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len / 4 - tmp.length(); ++i) {
			sb.append("0");
		}
		sb.append(tmp);
		return sb.toString();
	}

	public static int bytesToInt(final byte[] foo) {
		return foo[3] & 0xFF | (foo[2] & 0xFF) << 8 | (foo[1] & 0xFF) << 16 | (foo[0] & 0xFF) << 24;
	}

	public static byte[] intToBytes(final int foo) {
		return new byte[] { (byte) ((foo >> 24) & 0xFF), (byte) ((foo >> 16) & 0xFF), (byte) ((foo >> 8) & 0xFF),
				(byte) (foo & 0xFF) };
	}

	public static byte[] longToBytes(final long foo) {
		return ByteBuffer.allocate(8).putLong(foo).array();
	}

	public static long bytesToLong(final byte[] bytes) {
		return ByteBuffer.allocate(8).put(bytes, 0, bytes.length).flip().getLong();
	}

	public static boolean createPath(final String path) {
		boolean boolIsSuccessful = true;
		File dir = new File(path);
		if (!dir.isDirectory()) {
			boolIsSuccessful = dir.mkdir();
		}
		return boolIsSuccessful;
	}

	public static byte[] getSecureRandomBytes() {
		byte[] bytes = new byte[64];
		try {
			SecureRandom.getInstance("SHA1PRNG").nextBytes(bytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.info(e.toString());
		}
		return bytes;
	}

	public static byte[] longToBits(final long value) {
		return bigIntegerToBits(new BigInteger(1, longToBytes(value)));
	}

	public static long bitsToLong(final byte[] bits) {
		return bitsToBigInteger(bits).longValue();
	}

	/**
	 * Varbit is a series of consecutive bytes. Each byte has 7 significant digits,
	 * the highest digit of which is a continuous label. If it is 1, it means that
	 * the subsequent byte is still part of bytes. If it is 0, it means the current
	 * byte is the last byte of bytes.
	 * <p>
	 * 
	 * @param value the original value of relevant number
	 * @return byte[] the original number's Varbit
	 */
	public static byte[] bigIntegerToBits(final BigInteger value) {
		// Get the original binary sequence with the high digits on the left.
		String strFoo = value.toString(2);
		StringBuilder sb = new StringBuilder();
		sb.append(strFoo);
		int len = strFoo.length();
		// Insert a 1 every 7 digits from the low position.
		for (int i = 1; i < len; ++i) {
			if (i % 7 == 0) {
				sb.insert(len - i, '1');
			}
		}
		return new BigInteger(sb.toString(), 2).toByteArray();
	}

	public static BigInteger bitsToBigInteger(final byte[] bits) {
		BigInteger foo = new BigInteger(1, bits);
		String strFoo = foo.toString(2);
		StringBuilder sb = new StringBuilder().append(strFoo);
		int len = strFoo.length();
		for (int i = 1; i < strFoo.length(); ++i) {
			if (i % 8 == 0) {
				sb.deleteCharAt(len - i);
			}
		}
		return new BigInteger(sb.toString(), 2);
	}

	public static String dumpBytes(final byte[] bytes, final int radix) {
		return new BigInteger(1, bytes).toString(radix);
	}

	/**
	 * EQCCHA - EQCOIN complex hash algorithm used for calculate the hash of EQC
	 * block chain's header and address. Each input data will be expanded by a
	 * factor of 100.
	 * 
	 * @param bytes     The raw data for example EQC block chain's header or address
	 * @param isAddress If this is an address. If it is an address at the end use
	 *                  RIPEMD160 and RIPEMD128 to reduce the size of address
	 * @return Hash value processed by EQCCHA
	 */
	public static byte[] EQCCHA(final byte[] bytes, final boolean isAddress) {
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("SHA-256").digest(multipleExtend(bytes, HUNDRED));
			hash = MessageDigest.getInstance("SHA-384").digest(multipleExtend(hash, HUNDRED));
			hash = MessageDigest.getInstance("SHA-512").digest(multipleExtend(hash, HUNDRED));
			hash = RIPEMD160(multipleExtend(hash, HUNDRED));
			hash = RIPEMD128(multipleExtend(hash, HUNDRED));
			hash = MessageDigest.getInstance("SHA3-256").digest(multipleExtend(hash, HUNDRED));
			hash = MessageDigest.getInstance("SHA3-384").digest(multipleExtend(hash, HUNDRED));
			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(hash, HUNDRED));
			// Due to this is a address so here use RIPEMD160 and RIPEMD128 reduce the size
			// of address
			if (isAddress) {
				hash = RIPEMD160(multipleExtend(hash, HUNDRED));
				hash = RIPEMD128(multipleExtend(hash, HUNDRED));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}

	public static byte[] RIPEMD160(final byte[] bytes) {
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(bytes, 0, bytes.length);
		byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

	public static byte[] RIPEMD128(final byte[] bytes) {
		RIPEMD128Digest digest = new RIPEMD128Digest();
		digest.update(bytes, 0, bytes.length);
		byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

//	uint8_t crc8_itu(uint8_t *data, uint_len length)
//	{
//	    uint8_t i;
//	    uint8_t crc = 0;        // Initial value
//	    while(length--)
//	    {
//	        crc ^= *data++;        // crc ^= *data; data++;
//	        for ( i = 0; i < 8; i++ )
//	        {
//	            if ( crc & 0x80 )
//	                crc = (crc << 1) ^ 0x07;
//	            else
//	                crc <<= 1;
//	        }
//	    }
//	    return crc ^ 0x55;
//	}
//	
	
//	public static byte crc8(byte[] bytes) {
//		
//	}
	
	public static byte CRC8ITU(byte[] bytes) {
		byte crci = 0x00;
		for (int j = 0; j < bytes.length; j++) {
			crci ^= bytes[j] & 0xff;
			for (int i = 0; i < 8; i++) {
				if ((crci & 0x80) != 0) {
//					crci <<= 1;
//					crci ^= 0x07;
					crci = (byte) ((crci << 1) ^ 0x07);
				} else {
					crci <<= 1;
				}
			}
		}
		return (byte) (crci^0x55);
	}

}
