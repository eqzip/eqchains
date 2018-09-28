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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Xun Wang
 * @date 9-21-2018
 * @email 10509759@qq.com
 */
public class EQCType {
	
	/**
	 * Fixeddata stores a byte array whose length is up to 31 bytes.
	 * 001xxxxx | data	  
	 */
	public final static byte MAX_DATA_LEN = 31;
	public final static byte FIXEDDATA = 0x20;
	public final static byte FIXEDDATA_MASK = (byte) 0xE0;
	public final static byte FIXEDDATA_VALUE_MASK = (byte) 0x1F;
	
	/**
	 * Bin 8 stores a byte array whose length is up to (2^8)-1 bytes.
	 * 0x1  |XXXXXXXX|  data  |
	 * XXXXXXXX is a 8-bit unsigned integer which represents the length of data
	 */
	public final static byte BIN8 = 0x1;
	public final static int MAX_BIN8_VALUE = 255;
	
	/**
	 * Bin 16 stores a byte array whose length is up to (2^16)-1 bytes.
	 * 0x1  |XXXXXXXX|XXXXXXXX|  data  |
	 * XXXXXXXX|XXXXXXXX is a 16-bit unsigned integer which represents the length of data
	 */
	public final static byte BIN16 = 0x2;
	public final static int MAX_BIN16_VALUE = 65535;
	
	/**
	 * Bin 24 stores a byte array whose length is up to (2^24)-1 bytes.
	 * 0x1  |XXXXXXXX|XXXXXXXXXXXXXXXX||  data  |
	 * XXXXXXXX|XXXXXXXX|XXXXXXXX is a 24-bit unsigned integer which represents the length of data
	 */
	public final static byte BIN24 = 0x3;
	public final static int MAX_BIN24_VALUE = 16777215;
	
	/**
	 * Bin 32 stores a byte array whose length is up to (2^32)-1 bytes.
	 * 0x1  |XXXXXXXX|XXXXXXXX|XXXXXXXX|  data  |
	 * XXXXXXXX|XXXXXXXX|XXXXXXXX is a 32-bit unsigned integer which represents the length of data
	 */
	public final static byte BIN32 = 0x4;
	public final static long MAX_BIN32_VALUE = 4294967295l;
	
	public final static byte BITS = (byte) 0x80;
	
	/**
	 * Convert String to fixeddata using StandardCharsets.US_ASCII charset
	 * @param foo The String which will be convert to fixeddata
	 * @return
	 */
	public static byte[] stringToFixedData(final String foo) {
		return bytesToFixedData(foo.getBytes(StandardCharsets.US_ASCII));
//		if(foo.length() > MAX_DATA_LEN) {
//			throw new IllegalArgumentException("String's length shouldn't exceed 31.");
//		}
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		try {
//			os.write((byte) (FIXEDDATA | (foo.length()&0xFF)));
//			Log.info(Util.dumpBytesBigEndianBinary(new byte[] {(byte) (FIXEDDATA | (foo.length()&0xFF))}));
//			os.write(foo.getBytes(StandardCharsets.US_ASCII));
//			Log.info(Util.dumpBytesBigEndianBinary(foo.getBytes(StandardCharsets.US_ASCII)));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return os.toByteArray();
	}
	
	public static byte[] bytesToFixedData(final byte[] foo) {
		if(foo.length > MAX_DATA_LEN) {
			throw new IllegalArgumentException("Byte array's length shouldn't exceed 31.");
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write((byte) (FIXEDDATA | (foo.length&0xFF)));
			Log.info(Util.dumpBytesBigEndianBinary(new byte[] {(byte) (FIXEDDATA | (foo.length&0xFF))}));
			os.write(foo);
			Log.info(Util.dumpBytesBigEndianBinary(foo));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public static byte parseEQCType(final int type) {
		byte foo = (byte)type;
		byte result = 0;
		if((foo&FIXEDDATA_MASK) == FIXEDDATA) {
			result = FIXEDDATA;
		}
		else if((foo&BITS) == BITS) {
			result = BITS;
		}
		return result;
	}
	
	public static boolean isFixedData(final int type) {
		return (parseEQCType(type) == FIXEDDATA);
	}
	
	public static boolean isBin(final int type) {
		byte foo = (byte)type;
		return ((foo == BIN16) || (foo == BIN24) || (foo == BIN32) || (foo == BIN8));
	}
	
	public static int getBinLen(final int type) {
		byte foo = (byte)type;
		int len = 0;
		if(foo == BIN16) {
			len = 2;
		}
		else if(foo == BIN24) {
			len = 3;
		}
		else if(foo == BIN32) {
			len = 4;
		}
		else if(foo == BIN8) {
			len = 1;
		}
		return len;
	}
	
	public static int getBinDataLen(final int type, byte[] bytes) {
		byte foo = (byte)type;
		int len = 0;
		if(foo == BIN16) {
			len = Util.bytesToShort(bytes) & 0xffff;
		}
		else if(foo == BIN24) {
			len = Util.bytesToInt(bytes);
		}
		else if(foo == BIN32) {
			len = Util.bytesToInt(bytes);
		}
		else if(foo == BIN8) {
			len = bytes[0] & 0xff;
		}
		return len;
	}
	
	public static int parseFixedDataLen(final int type) {
		return type & FIXEDDATA_VALUE_MASK;
	}
	
	public static byte[] bytesToBin(byte[] bytes) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(bytes.length <= MAX_BIN8_VALUE) {
			try {
				os.write(BIN8);
				os.write(bytes.length);
				os.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(bytes.length <= MAX_BIN16_VALUE) {
			try {
				os.write(BIN16);
				os.write(bytes.length);
				os.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(bytes.length <= MAX_BIN32_VALUE) {
			try {
				os.write(BIN32);
				os.write(bytes.length);
				os.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return os.toByteArray();
	}
	
}
