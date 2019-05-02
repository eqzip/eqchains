/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-2019 EQCOIN Foundation Inc.  All rights reserved...
 * CC BY-NC-ND( * Copyright of all works released by EQCOIN Foundation or jointly released by EQCOIN Foundation 
 * with cooperative partners are owned by EQCOIN Foundation and entitled to protection
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to take any legal
 * action and pursue any right or remedy available under applicable law.)
 * https://www.EQCOIN Foundation.com
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
package com.eqchains.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32C;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections.functors.SwitchClosure;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.symmetric.Threefish;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Trim;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.EQCBlock;
import com.eqchains.blockchain.EQCBlockChain;
import com.eqchains.blockchain.EQCHeader;
import com.eqchains.blockchain.Index;
import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.Root;
import com.eqchains.blockchain.TransactionsHeader;
import com.eqchains.blockchain.AccountsMerkleTree.Filter;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxIn;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.configuration.Configuration;
import com.eqchains.crypto.EQCPublicKey;
import com.eqchains.crypto.MerkleTree;
import com.eqchains.keystore.Keystore;
import com.eqchains.keystore.Keystore.ECCTYPE;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.rpc.avro.Cookie;
import com.eqchains.rpc.avro.Status;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.test.Test;
import com.eqchains.util.Util.AddressTool.AddressType;
import com.eqchains.util.Util.AddressTool.P2SHAddress.Peer;


/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Util {

	/*
	 * Singularity - EQC's basic unit of measure. 1 EQC = 10000 singularity
	 */
	public final static long ABC = 10000;
	
	public final static byte[] MAGIC_HASH = UnsignedBiginteger(new BigInteger("200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5", 16)).toByteArray();

	public final static byte[] SINGULARITY_HASH = {};

	public final static long MAX_EQC = 210000000000L * ABC;

	public final static long MIN_EQC = 50L * ABC;
	
//	public final static long SINGULARITY_TOTAL_SUPPLY = 16800000 * ABC;

	public final static long MINER_TOTAL_SUPPLY = 42000000000L * ABC;
	
	public final static long EQCOIN_FOUNDATION_TOTAL_SUPPLY = 168000000000L * ABC;

	public final static int BLOCK_INTERVAL = 600000;

	public final static int TARGET_INTERVAL = 10000;

	public final static long MINER_COINBASE_REWARD = 25 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);
	
	public final static long EQC_FOUNDATION_COINBASE_REWARD = 100 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);
	
	public final static long COINBASE_REWARD = MINER_COINBASE_REWARD + EQC_FOUNDATION_COINBASE_REWARD;

	public final static BigInteger MAX_COINBASE_HEIGHT = BigInteger.valueOf(MAX_EQC / COINBASE_REWARD);

	public final static int TXFEE_UNIT = 10;

	public final static int ZERO = 0;

	public final static int ONE = 1;

	public final static int TWO = 2;

	public final static int SIXTEEN = 16;

	public final static int HUNDREDPULS = 101;
	
	public final static int THOUSANDPLUS = 1001;

	public final static int HUNDRED_THOUSAND = 100000;

	public final static int MILLIAN = 1000000;

	public final static int ONE_MB = 1048576;
	
	public final static int MAX_NONCE = 268435455;
	
	public final static int HASH_LEN = 64;

//	public final static String WINDOWS_PATH = "C:/EQCOIN";
//
//	public final static String MAC_PATH = "C:/Program Files/EQCOIN";
//
//	public final static String LINUX_PATH = "C:/Program Files/EQCOIN";
	/*
	 * Set the default PATH value WINDOWS_PATH
	 */
	private static String CURRENT_PATH = System.getProperty("user.dir");
	public static String PATH = "C:" + File.separator + "EQCOIN";// System.getProperty("user.dir") + File.separator +
																	// "EQCOIN";
//	static {
//		PATH = System.getProperty("user.dir") + "/EQCOIN";
//	}
	
	public static final String MAGIC_PATH = ".\\src\\main\\QuidditchHelixFlashForward";

	public static final String KEYSTORE_PATH = PATH + File.separator + "EQCoin.keystore";

	public static final String KEYSTORE_PATH_BAK = PATH + File.separator + "EQCoin.keystore.bak";

	public static final String LOG_PATH = PATH + File.separator + "log.txt";

	public final static String AVRO_PATH = PATH + File.separator + "AVRO";

	public final static String BLOCK_PATH = PATH + File.separator + "BLOCK/";

	public final static String EQC_SUFFIX = ".eqc";
	
	public final static String DB_PATH = PATH + File.separator + "DB";
	
	public final static String H2_PATH = DB_PATH + File.separator + "H2";
	
	public final static String ROCKSDB_PATH = DB_PATH + File.separator + "ROCKSDB";

	public final static String H2_DATABASE_NAME = H2_PATH + File.separator + "EQC";

	/*  @see 压缩公钥&签名长度规范
		EC长度 压缩公钥长度(bytes)  签名长度(bytes)
		P256 		33 				70、71、72
		P521 		67 			  137、138、139
	 */
	public final static int P256_BASIC_SIGNATURE_LEN = 73;

	public final static int P521_BASIC_SIGNATURE_LEN = 140;

	public final static int P256_BASIC_PUBLICKEY_LEN = 34;

	public final static int P521_BASIC_PUBLICKEY_LEN = 68;
	
	public final static int BASIC_SERIAL_NUMBER_LEN = 5;
	
	public final static int BASIC_VALUE_NUMBER_LEN = 8;

	public final static int INIT_ADDRESS_SERIAL_NUMBER = 1;

	public final static String PROTOCOL_VERSION = "0.0.1";

	private static Cookie cookie = null;

	private static Status status = null;
	
	public static final int DEFAULT_TIMEOUT = 3000;
	
	public static final int MAX_ADDRESS_LEN = 51;
	
	public static final int MIN_ADDRESS_LEN = 41;
	
	public static final int MAX_ADDRESS_AI_LEN = 33;
	
	public static final int MAX_T3_ADDRESS_CODE_LEN = 213;
	
	public static final int CRC32C_LEN = 4;

	public static final int MAX_DIFFICULTY_MULTIPLE = 4;
	
	public static final BigInteger EUROPA = UnsignedBiginteger(BigInteger.valueOf(1008));
	
	public static final byte[] NULL_HASH = UnsignedBiginteger(new BigInteger("C333A8150751C675CDE1312860731E54818F95EDC1563839501CE5F486DE1C79EA6675EECA26833E41341B5B5D1E72800CBBB13AE6AA289D11ACB4D4413B1B2D", 16)).toByteArray();
	
	public static ID [] FIBONACCI = {
			new ID(1597),
			new ID(2584),
			new ID(4181),
			new ID(6765),
			new ID(10946),
			new ID(17711),
			new ID(28657),
			new ID(46368),
			new ID(75025),
			new ID(121393)
	};
	
	public enum STATUS {
		OK, ERROR
	}

//	static {
//		init(OS.WINDOWS); 
//	}

	public enum OS {
		WINDOWS, MAC, LINUX
	}

	public enum PERSISTENCE {
		AVRO, H2, RPC
	}
	
	public enum DB {
		H2, ROCKSDB
	}

	private Util() {
	}

	public static void init() {
		System.setProperty("sun.net.client.defaultConnectTimeout", "3000");  
		createDir(PATH);
//		createDir(AVRO_PATH);
		createDir(H2_PATH);
		createDir(BLOCK_PATH);
		if (!Configuration.getInstance().isInitSingularityBlock()
				&& Keystore.getInstance().getUserAccounts().size() > 0/* Will Remove when Cold Wallet ready */) {
			EQCBlock eqcBlock = gestationSingularityBlock();
			EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
			EQCBlockChainRocksDB.getInstance().saveEQCBlock(eqcBlock);
//			Address address = eqcBlock.getTransactions().getAddressList().get(0);
//			if(!EQCBlockChainH2.getInstance().isAddressExists(address)) {
//				EQCBlockChainH2.getInstance().appendAddress(address, SerialNumber.ZERO);
//			}
//			EQCBlockChainH2.getInstance().addAllTransactions(eqcBlock);// .addTransaction(eqcBlock.getTransactions().getTransactionList().get(0),
																		// SerialNumber.ZERO, 0);
			EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.ZERO));
			EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.ZERO));
			Configuration.getInstance().updateIsInitSingularityBlock(true);
		}
//		cookie = new Cookie();
//		cookie.setIp(getIP());
//		cookie.setVersion(PROTOCOL_VERSION);
//		if (cookie.getIp().length() == 0) {
//			Log.Error("During get IP error occur please check your network");
//		} else {
//			Log.info(cookie.toString());
//		}
//		status = new Status();
//		status.setCookie(cookie);
//		status.setCode(STATUS.OK.ordinal());
//		status.setMessage("");
	}

//	private static void init(final OS os) {
//		switch (os) {
//		case MAC:
//			PATH = MAC_PATH;
//			break;
//		case LINUX:
//			PATH = LINUX_PATH;
//			break;
//		case WINDOWS:
//		default:
//			PATH = WINDOWS_PATH;
//			break;
//		}
//	}

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
		byte[] result = null;
		BigInteger begin = new BigInteger(1, data);
		BigInteger divisor = begin.divide(BigInteger.valueOf(multiple));
		BigInteger end = begin.multiply(BigInteger.valueOf(multiple).multiply(FIBONACCI[9]).multiply(divisor));
		
		int bufferLen = end.toByteArray().length * multiple;
		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLen);
		for (int i = 0; i < multiple; ++i) {
			byteBuffer.put(begin.multiply(BigInteger.valueOf(i).multiply(divisor).multiply(FIBONACCI[i%10])).toByteArray());
		}
		byteBuffer.flip();
		if(byteBuffer.remaining() == bufferLen) {
//			Log.info("multipleExtend equal: " + bufferLen);
			result = byteBuffer.array();
		}
		else {
//			Log.info("multipleExtend not equal");
			result = new byte[byteBuffer.remaining()];
			byteBuffer.get(result);
//			Log.info(Util.dumpBytes(Util.CRC32C(result), 16));
//			Log.info("Len: " + result.length);
		}
		
		
//		for (int i = 0; i < multiple; ++i) {
//			for (int j = 0; j < data.length; ++j) {
//				result[j + data.length * i] = data[j];
//			}
//		}
		return result;
	}

	public static byte[] updateNonce(final byte[] bytes, final int nonce) {
		System.arraycopy(Util.intToBytes(nonce), 0, bytes, 140, 4);
		return bytes;
	}

	public static String getGMTTime(final long timestamp) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(timestamp);
	}

	public static String getHexString(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
//		return	bigIntegerTo128String(new BigInteger(1, bytes));
		return	bigIntegerTo512String(new BigInteger(1, bytes));
	}
	
//	public static BigInteger getDefaultTarget() {
//		return BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
//				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(60);
//	}

	public static byte[] getDefaultTargetBytes() {
//		return new byte[] { 0x68, (byte) 0xda, (byte) 0xab, (byte) 0xcd };
		return new byte[] { (byte) 0xF4, (byte) 0x4F, (byte) 0xAB, (byte) 0xCD };
	}

	public static BigInteger targetBytesToBigInteger(byte[] foo) {
		int target = bytesToInt(foo);
//		long l = target;
//		int i = target >>> 23;
//		int j = target << 9;
//		Log.info(Util.dumpBytes(Util.intToBytes(target >>> 23), 2));
//		Log.info("" + Util.dumpBytes(Util.intToBytes(target >>>23), 16));
//		Log.info(Util.dumpBytes(foo, 16));
//		return BigInteger.valueOf(Long.valueOf(target & 0x00ffffff)).shiftLeft((target & 0xff000000) >>> 24);
//		Log.info("" + (target << 9));
//		Log.info(bigIntegerTo512String(BigInteger.valueOf(Long.valueOf((target << 9) >>> 9)).shiftLeft(target >>> 23)));
		return UnsignedBiginteger(BigInteger.valueOf(Long.valueOf((target << 9) >>> 9))).shiftLeft(target >>> 23);
	}

	public static byte[] bigIntegerToTargetBytes(BigInteger foo) {
		byte[] bytes = UnsignedBiginteger(foo).toByteArray();
		byte[] target;
		int offset;
		// Exists Leading zero
		if ((bytes[0] == 0) && (bytes[1] < 0)) {
			target = new byte[] { bytes[1], bytes[2], bytes[3], bytes[4] };
			offset = (bytes.length - 1) * 8 - 23;
		} 
		// Doesn't exists Leading zero
		else {
			target = new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] };
			offset = bytes.length * 8 - 23;
		}
//		Log.info("" + offset);
//		Log.info("" + (offset & 0x1FF));
		return intToBytes((bytesToInt(target) >>> 9) | ((offset & 0x1FF) << 23));
//		return intToBytes((bytesToInt(target) >>> 9) | (((((offset * 8) == 512)?511:(offset * 8)) & 0x1FF) << 23));
//		if (bytes.length <= 3) {
//			return intToBytes(foo.intValue() & 0x00FFFFFF);
//		} else {
//			byte[] target;
//			int offset;
//			if ((bytes[0] == 0) && (bytes[1] < 0)) {
//				target = new byte[] { 0, bytes[1], bytes[2], bytes[3] };
//				offset = bytes.length - 4;
//			} else {
//				target = new byte[] { 0, bytes[0], bytes[1], bytes[2] };
//				offset = bytes.length - 3;
//			}
//			return intToBytes((bytesToInt(target) & 0x00FFFFFF) | (((offset * 8) & 0xFF) << 24));
//		}
		
		
//		String target = foo.toString(2);
//		if(target.length() <= 24) {
//			int value = new BigInteger(target, 2).intValue();
//			return intToBytes(value & 0x00FFFFFF);
//		}
//		else {
//			int value = new BigInteger(target.substring(0, 24), 2).intValue();
//			int a = (value & 0x00FFFFFF);
//			int d = (target.length() - 24) & 0xFF;
//			int e = d << 24;
//			int b = (((target.length() - 24) & 0xFF) << 24);
//			int c = a | b;
//			return intToBytes((value & 0x00FFFFFF) | (((target.length() - 24) & 0xFF) << 24));
//		}
	}

	public static byte[] bigIntegerTo64Bytes(final BigInteger foo) {
		byte[] tmp = new byte[64];
		byte[] fooBytes = foo.toByteArray();
		if (fooBytes.length == 65) {
			System.arraycopy(fooBytes, 1, tmp, 0, tmp.length);
		} else {
			// Because big-endian byte-order so fill 0 in the high position to fill the gap
			System.arraycopy(fooBytes, 0, tmp, tmp.length - fooBytes.length, fooBytes.length);
		}
		return tmp;
	}

	public static byte[] bigIntegerTo16Bytes(final BigInteger foo) {
		byte[] tmp = new byte[16];
		byte[] fooBytes = foo.toByteArray();
		if (fooBytes.length == 17) {
			System.arraycopy(fooBytes, 1, tmp, 0, tmp.length);
		} else {
			// Because big-endian byte-order so fill 0 in the high position to fill the gap
			System.arraycopy(fooBytes, 0, tmp, tmp.length - fooBytes.length, fooBytes.length);
		}
		return tmp;
	}

	public static BigInteger bytesToBigInteger(final byte[] foo) {
		return new BigInteger(foo);
	}

	public static String bigIntegerTo512String(final BigInteger foo) {
		return bigIntegerToFixedLengthString(foo, 512).toUpperCase();
	}

	public static String bigIntegerTo128String(final BigInteger foo) {
		return bigIntegerToFixedLengthString(foo, 128);
	}

	public static String bigIntegerToFixedLengthString(final BigInteger foo, final int len) {
		String tmp = foo.toString(16);
//		Log.info(tmp.length() + "  " + tmp);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len / 4 - tmp.length(); ++i) {
			sb.append("0");
		}
		sb.append(tmp);
		return sb.toString();
	}

	public static byte[] shortToBytes(final short foo) {
//		return ByteBuffer.allocate(2).putLong(foo).array();
		return new byte[] { (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static short bytesToShort(final byte[] bytes) {
//		return ByteBuffer.allocate(2).put(bytes, 0, bytes.length).flip().getShort();
		return (short) (bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8);
	}

	public static byte[] intToBytes(final int foo) {
		return new byte[] { (byte) ((foo >> 24) & 0xFF), (byte) ((foo >> 16) & 0xFF), (byte) ((foo >> 8) & 0xFF),
				(byte) (foo & 0xFF) };
//		return ByteBuffer.allocate(4).putInt(foo).array();
	}

	public static byte intToByte(final int foo) {
		return (byte) (foo & 0xFF);
	}

	public static byte[] intTo2Bytes(final int foo) {
		return new byte[] { (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static byte[] intTo3Bytes(final int foo) {
		return new byte[] { (byte) ((foo >> 16) & 0xFF), (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static int bytesToInt(final byte[] bytes) {
		int foo = 0;
		if (bytes.length == 1) {
			foo = (bytes[0] & 0xFF);
		} else if (bytes.length == 2) {
			foo = (bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8);
		} else if (bytes.length == 3) {
			foo = (bytes[2] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF) << 16);
		} else if (bytes.length == 4) {
			foo = (bytes[3] & 0xFF | (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24);
		}
		return foo;
//		return ByteBuffer.allocate(4).put(bytes, 0, bytes.length).flip().getInt();
	}

	public static byte[] longToBytes(final long foo) {
		return ByteBuffer.allocate(8).putLong(foo).array();
	}

	public static long bytesToLong(final byte[] bytes) {
		return ByteBuffer.allocate(8).put(bytes, 0, bytes.length).flip().getLong();
	}

	public static boolean createDir(final String dir) {
		boolean boolIsSuccessful = true;
		File file = new File(dir);
		if (!file.isDirectory()) {
			boolIsSuccessful = file.mkdir();
			Log.info("Create directory " + dir + boolIsSuccessful);
		} else {
			if (file.isDirectory()) {
				Log.info(dir + " already exists.");
			} else {
				Log.Error("Create directory " + dir + " failed and this directory doesn't exists.");
			}
		}
		return boolIsSuccessful;
	}

	public static byte[] getSecureRandomBytes() {
		byte[] bytes = new byte[64];
		try {
			SecureRandom.getInstanceStrong().nextBytes(bytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.info(e.toString());
		}
		return bytes;
	}

	public static String dumpBytes(final byte[] bytes, final int radix) {
		return new BigInteger(1, bytes).toString(radix).toUpperCase();
	}

	/**
	 * EQCCHA_MULTIPLE - EQCOIN complex hash algorithm used for calculate the hash
	 * of EQC block chain's header and address. Each input data will be expanded by
	 * a factor of multiple.
	 * 
	 * @param bytes      The raw data for example EQC block chain's header or
	 *                   address
	 * @param multiple   The input data will be expanded by a factor of multiple
	 * @param isCompress If this is an address or signatures. Then at the end use
	 *                   SHA3-256 to reduce the size of it
	 * @return Hash value processed by EQCCHA
	 */
	public static byte[] EQCCHA_MULTIPLE(final byte[] bytes, int multiple, boolean isCompress) {
		byte[] hash = null;
//		Log.info("Len: " + bytes.length);
		try {
			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(bytes, multiple));		
			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(hash, multiple));
			// Due to this is an address or signature so here use SHA3-256 reduce the size of it
			if (isCompress) {
				hash = SHA3_256(multipleExtend(hash, multiple));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return hash;
	}
	
	/**
	 * EQCCHA - EQCOIN complex hash algorithm used for calculate the hash
	 * of EQC block chain's address.
	 * 
	 * @param bytes      The raw data for example EQC block chain's header or
	 *                   address
	 * @param isCompress If this is an address or signatures. Then at the end use
	 *                   SHA3-256 to reduce the size of it
	 * @return Hash value processed by EQCCHA
	 */
	public static byte[] EQCCHA(final byte[] bytes, int multiple, boolean isCompress) {
		byte[] hash = null;
//		Log.info("Len: " + bytes.length);
		try {
			hash = MessageDigest.getInstance("SHA3-512").digest(bytes);		
			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(hash, multiple));
			// Due to this is an address or signature so here use SHA3-256 reduce the size of it
			if (isCompress) {
				hash = SHA3_256(multipleExtend(hash, multiple));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return hash;
	}
	
	public static byte[] EQCCHA_MULTIPLE_FIBONACCI_MERKEL(final byte[] bytes, int multiple, boolean isCompress) {
		Vector<byte[]> ten = new Vector<byte[]>();
		BigInteger begin = new BigInteger(1, bytes);
		BigInteger divisor = begin.divide(BigInteger.valueOf(multiple));
		MerkleTree merkleTree = null;
		for(int i=0; i<10; ++i) {
			ten.add(EQCCHA_MULTIPLE(begin.multiply(BigInteger.valueOf(i).multiply(divisor).multiply(FIBONACCI[i])).toByteArray(), multiple, false));
//			Log.info("i: " + i + " len: " + ten.get(i).length);
		}
		merkleTree = new MerkleTree(ten);
		merkleTree.generateRoot();
		return EQCCHA_MULTIPLE(merkleTree.getRoot(), multiple, isCompress);
	}
	
	public static byte[] SHA3_256(byte[] bytes) {
		byte[] result = null;
		try {
			result =  MessageDigest.getInstance("SHA3-256").digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result;
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

	public static String dumpBytesBigEndianHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
//			if (i % 8 == 0) {
//				sb.append(" ");
//			}
			sb.append(Integer.toHexString(bytes[i]).toUpperCase());
		}
		return sb.toString();
	}

	public static String dumpBytesBigEndianBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(binaryString(Integer.toBinaryString(bytes[i] & 0xFF)));
		}
		return sb.toString();
	}

	public static String dumpBytesLittleEndianHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(Integer.toHexString(bytes[i] & 0xFF));
		}
		return sb.toString();
	}

	public static String dumpBytesLittleEndianBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(Integer.toBinaryString(bytes[i] & 0xFF));
		}
		return sb.toString();
	}

	/**
	 * Add leading zero when the original binary string's length is less than 8.
	 * <p>
	 * For example when foo is 101111 the output is 00101111.
	 * 
	 * @param foo This value is a string of ASCII digitsin binary (base 2) with no
	 *            extra leading 0s.
	 * @return Fixed 8-bit long binary number with leading 0s.
	 */
	public static String binaryString(String foo) {
		if (foo.length() == 8) {
			return foo;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 8 - foo.length(); ++i) {
				sb.append(0);
			}
			sb.append(foo);
			return sb.toString();
		}
	}

	public static byte[] AESEncrypt(byte[] bytes, String password) {
		byte[] result = null;
		try {
			KeyGenerator kgen;
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(256, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			result = cipher.doFinal(bytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getLocalizedMessage());
		}
		return result;
	}

	public static byte[] AESDecrypt(byte[] bytes, String password) {
		byte[] result = null;
		try {
			KeyGenerator kgen;
//			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding","SunJCE");
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(256, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			result = cipher.doFinal(bytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result;
	}

	public static byte[] reverseBytes(final byte[] bytes) {
		byte[] foo = new byte[bytes.length];
		for (int i = 0; i <= foo.length - 1; ++i) {
			foo[i] = bytes[bytes.length - 1 - i];
		}
		return foo;
	}

	public static EQCBlockChain getEQCBlockChain(PERSISTENCE persistence) {
		EQCBlockChain eqcBlockChain;
		switch (persistence) {
//		case AVRO:
//			eqcBlockChain = EQCBlockChainAvro.getInstance();
//			break;
		case H2:
		default:
			eqcBlockChain = EQCBlockChainH2.getInstance();
			break;
		}
		return eqcBlockChain;
	}

	public static EQCBlockChain getEQCBlockChain() {
		return EQCBlockChainH2.getInstance();
	}

	public static AddressType getAddressType(ID addressSerialNumber) {
		return AddressTool
				.getAddressType(getEQCBlockChain(PERSISTENCE.H2).getAddress(addressSerialNumber).getReadableAddress());
	}

	public static ECPrivateKey getPrivateKey(byte[] privateKeyBytes, AddressType addressType) {
		ECPrivateKey privateKey = null;
		try {
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
			if (addressType == AddressType.T1) {
				parameters.init(new ECGenParameterSpec(Keystore.SECP256R1));
			} else if (addressType == AddressType.T2) {
				parameters.init(new ECGenParameterSpec(Keystore.SECP521R1));
			}
			ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
			ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(privateKeyBytes), ecParameterSpec);
			privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(ecPrivateKeySpec);
		} catch (Exception e) {
			Log.Error(e.toString());
		}
		return privateKey;
	}

	public static boolean verifySignature(AddressType addressType, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
		return verifySignature(transaction.getPublickey().getPublicKey(), transaction.getSignature(), addressType, transaction, TXIN_HEADER_HASH, SN);
	}

	public static boolean verifySignature(byte[] publickey, byte[] userSignature, AddressType addressType, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
		boolean isTransactionValid = false;
		Signature signature = null;

//		// Verify Address
//		if (!AddressTool.verifyAddress(transaction.getTxIn().getAddress().getAddress(),
//				transaction.getPublickey().getPublicKey())) {
//			Log.Error("Transaction's TxIn's Address error.");
//			return isTransactionValid;
//		}

		// Verify Signature
		try {
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (addressType == AddressType.T1) {
				eccType = ECCTYPE.P256;
			} else if (addressType == AddressType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCPublicKey eqPublicKey = new EQCPublicKey(eccType);
			// Create EQPublicKey according to java Publickey
			eqPublicKey.setECPoint(publickey);
			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			signature.initVerify(eqPublicKey);
//			signature.update(EQCBlockChainH2.getInstance().getBlockHeaderHash(
//					EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getAddress())));
			signature.update(TXIN_HEADER_HASH);
			if(SN.length == 1) {
				signature.update(intToBytes(SN[0]));
			}
			signature.update(transaction.getBytes(Address.AddressShape.AI));
			isTransactionValid = signature.verify(userSignature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}
	
	public static byte[] signTransaction(AddressType addressType, byte[] privateKey, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
		byte[] sign = null;
//		Signature signature = null;
		try {
//			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
//			ECCTYPE eccType = null;
//			if (addressType == AddressType.T1) {
//				eccType = ECCTYPE.P256;
//			} else if (addressType == AddressType.T2) {
//				eccType = ECCTYPE.P521;
//			}
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, addressType));
//			// Add current Transaction's relevant TxIn's Address's EQC block height which
//			// record the TxIn's Address.
//			ecdsa.update(EQCBlockChainH2.getInstance().getBlockHeaderHash(
//					EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getAddress())));
			ecdsa.update(TXIN_HEADER_HASH);
			if(SN.length == 1) {
				ecdsa.update(intToBytes(SN[0]));
			}
			ecdsa.update(transaction.getBytes(Address.AddressShape.AI));
			sign = ecdsa.sign();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return sign;
	}

	/**
	 * @author Xun Wang
	 * @date Sep 24, 2018
	 * @email 10509759@qq.com
	 */
	public static class AddressTool {
		public final static int T1_PUBLICKEY_LEN = 33;
		public final static int T2_PUBLICKEY_LEN = 67;

		public enum AddressType {
			T1, T2, T3
		}

		private AddressTool() {
		}
	
		/**
		 * @param bytes compressed PublicKey. Each input will be extended 100 times
		 *              using EQCCHA_MULTIPLE
		 * @param type  EQC Address’ type
		 * @return EQC address
		 */
		public static String generateAddress(byte[] publicKey, AddressType type) {
			byte[] publickey_hash = EQCCHA_MULTIPLE_FIBONACCI_MERKEL(publicKey, HUNDREDPULS, true);
			return _generateAddress(publickey_hash, type);
		}
		
		private static String _generateAddress(byte[] publickey_hash, AddressType type) {
			byte[] type_publickey_hash = null;
			byte[] CRC32C = null;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			// Calculate (type + PublickeyHash)'s CRC32C
			try {
				os.write(type.ordinal());
//				Log.info("AddressType: " + type.ordinal());
				os.write(publickey_hash);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			try {
				type_publickey_hash = os.toByteArray();
				CRC32C = CRC32C(type_publickey_hash);
				os = new ByteArrayOutputStream();
				os.write(CRC32C);
				os.write(type.ordinal());
				os.write(CRC32C);
				os.write(publickey_hash);
				os.write(CRC32C);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			byte[] crc32c = CRC32C(multipleExtend(os.toByteArray(), HUNDREDPULS));
			// Generate address Base58(type) + Base58((HASH + (type + HASH)'s CRC32C))
			try {
				os = new ByteArrayOutputStream();
				os.write(publickey_hash);
				os.write(crc32c);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
		}
		
		public static boolean verifyAddressPublickey(String address, byte[] publickey) {
			byte[] hidden_address = null;
			byte[] publickey_hash = null;
			try {
				publickey_hash = EQCCHA_MULTIPLE_FIBONACCI_MERKEL(publickey, HUNDREDPULS, true);
				hidden_address = Base58.decode(address.substring(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return Arrays.equals(publickey_hash, Arrays.copyOf(hidden_address, hidden_address.length - CRC32C_LEN));
		}
		
		public static byte[] addressToAI(String address) {
			byte[] bytes = null;
			ByteArrayOutputStream os = null;
			try {
				os = new ByteArrayOutputStream();
				os.write(Base58.decode(address.substring(0, 1)));
				bytes = Base58.decode(address.substring(1));
				os.write(bytes, 0, bytes.length - CRC32C_LEN);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return os.toByteArray();
		}

		public static String AIToAddress(byte[] bytes) {
			AddressType addressType = AddressType.T1;
			if (bytes[0] == 0) {
				addressType = AddressType.T1;
			} else if (bytes[0] == 1) {
				addressType = AddressType.T2;
			} 
//			else if (bytes[0] == 2) {
//				addressType = AddressType.T3;
//			}
			else {
				throw new UnsupportedOperationException("Unsupport type: " + bytes[0]);
			}
			
			return _generateAddress(Arrays.copyOfRange(bytes, 1, bytes.length), addressType);
		}
		
		public static boolean isAddressFormatValid(String address) {
			byte[] bytes = null;
			String addressContent = null, subString = null;
			char[] addressChar = null;
			try {
				if(address.length() < MIN_ADDRESS_LEN || address.length() > MAX_ADDRESS_LEN) {
					return false;
				}
				addressChar = address.toCharArray();
				if(addressChar[0] != '1' && addressChar[0] != '2') {
					return false;
				}
				for(char alphabet : addressChar) {
					if(!Base58.isBase58Char(alphabet)) {
						return false;
					}
				}
//				subString = address.substring(1);
//				bytes = Base58.decode(subString);
//				addressContent = Base58.encode(bytes);
//				if(!addressContent.equals(subString)) {
//					return false;
//				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			
			return true;
		}
		
		public static boolean verifyAddressCRC32C(String address) {
			byte[] bytes = null;
			byte[] crc32c = new byte[CRC32C_LEN];
			byte[] CRC32C = null;
			byte[] CRC32CC = null;
			byte[] type_publickey_hash = null;
			
			try {
				bytes = Base58.decode(address.substring(1));
				System.arraycopy(bytes, bytes.length - CRC32C_LEN, crc32c, 0, CRC32C_LEN);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(Base58.decode(address.substring(0, 1)));
				os.write(bytes, 0, bytes.length - CRC32C_LEN);
				try {
					type_publickey_hash = os.toByteArray();
					CRC32CC = CRC32C(type_publickey_hash);
					os = new ByteArrayOutputStream();
					os.write(CRC32CC);
					os.write(Base58.decode(address.substring(0, 1)));
					os.write(CRC32CC);
					os.write(bytes, 0, bytes.length - CRC32C_LEN);
					os.write(CRC32CC);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CRC32C = CRC32C(multipleExtend(os.toByteArray(), HUNDREDPULS));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return Arrays.equals(crc32c, CRC32C);
		}
		
//		/**
//		 * @param bytes compressed PublicKey. Each input will be extended 100 times
//		 *              using EQCCHA_MULTIPLE
//		 * @param type  EQC Address’ type
//		 * @return EQC address
//		 */
//		@Deprecated 
//		public static String generateAddress(byte[] publicKey, AddressType type) {
//			byte[] bytes = EQCCHA_MULTIPLE(publicKey, Util.HUNDRED, true);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			// Calculate (type | trim(HASH))'s CRC8ITU
//			os.write(type.ordinal());
//			Log.info("AddressType: " + type.ordinal());
//			try {
//				os.write(trim(bytes));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			byte crc = CRC8ITU.update(os.toByteArray());
//			// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//			// CRC8ITU))
//			os = new ByteArrayOutputStream();
//			try {
//				os.write(trim(bytes));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			os.write(crc);
//			return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
//		}
//		
//		@Deprecated 
//		public static boolean verifyAddress(String address, byte[] publickey) {
//			byte[] bytes = null;
//			byte crc = 0;
//			byte CRC = 0;
//			byte[] publicKey_hash = trim(EQCCHA_MULTIPLE(publickey, HUNDRED, true));
//			try {
//				bytes = Base58.decode(address.substring(1));
//				crc = bytes[bytes.length - 1];
//				ByteArrayOutputStream os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				os.write(bytes, 0, bytes.length - 1);
//				CRC = CRC8ITU.update(os.toByteArray());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return (crc == CRC) && Arrays.equals(publicKey_hash, Arrays.copyOf(bytes, bytes.length - 1));
//		}
//		
//		@Deprecated 
//		public static byte[] addressToAI(String address) {
//			byte[] bytes = null;
//			ByteArrayOutputStream os = null;
//			try {
//				os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				bytes = Base58.decode(address.substring(1));
//				os.write(bytes, 0, bytes.length - 1);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return os.toByteArray();
//		}
//
//		@Deprecated 
//		public static String AIToAddress(byte[] bytes) {
//			AddressType addressType = AddressType.T1;
//			if (bytes[0] == 1) {
//				addressType = AddressType.T1;
//			} else if (bytes[0] == 2) {
//				addressType = AddressType.T2;
//			} else if (bytes[0] == 3) {
//				addressType = AddressType.T3;
//			}
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			// Calculate (type | trim(HASH))'s CRC8ITU
//			os.write(addressType.ordinal());
//			try {
//				os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			byte crc = CRC8ITU.update(os.toByteArray());
//			// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//			// CRC8ITU))
//			os = new ByteArrayOutputStream();
//			try {
//				os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			os.write(crc);
//			return Base58.encode(new byte[] { (byte) addressType.ordinal() }) + Base58.encode(os.toByteArray());
//		}
//		
//		@Deprecated 
//		public static boolean verifyAddress(String address) {
//			byte[] bytes = null;
//			byte crc = 0;
//			byte CRC = 0;
//			try {
//				bytes = Base58.decode(address.substring(1));
//				crc = bytes[bytes.length - 1];
//				ByteArrayOutputStream os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				os.write(bytes, 0, bytes.length - 1);
//				CRC = CRC8ITU.update(os.toByteArray());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return (crc == CRC);
//		}

		public static byte[] trim(final byte[] bytes) {
			int i = 0;
			for (; i < bytes.length; ++i) {
				if (bytes[i] != 0) {
					break;
				}
			}
			int j = bytes.length - 1;
			for (; j > 0; --j) {
				if (bytes[j] != 0) {
					break;
				}
			}
			byte[] trim = new byte[j - i + 1];
			System.arraycopy(bytes, i, trim, 0, trim.length);
			return trim;
		}

		public static AddressType getAddressType(String address) {
			byte type = 0;
			AddressType addressType = AddressType.T1;
			try {
				type = Base58.decodeToBigInteger(address.substring(0, 1)).byteValue();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			if (type == 0) {
				addressType = AddressType.T1;
			} else if (type == 1) {
				addressType = AddressType.T2;
			} else if (type == 2) {
				addressType = AddressType.T3;
			}
			return addressType;
		}

		/**
		 * @author Xun Wang
		 * @date Feb 1, 2019
		 * @email 10509759@qq.com
		 */
		public static class P2SHAddress implements EQCTypable {
			public final static int MAX_PEER_NUMBERS = 3;
			private BigInteger version;
			private byte[] code;
			private String address;
			private Vector<Peer> peerList;
			
			private void init() {
				peerList = new Vector<Peer>();
			}
			
			public P2SHAddress() {
				init();
				version = BigInteger.ZERO;
			}
			
			public P2SHAddress(byte[] code) throws NoSuchFieldException, IOException {
				init();
				this.code = code;
				ARRAY peers = null;
				peers = EQCType.parseARRAY(code);
				for(byte[] peer : peers.elements) {
					peerList.add(new Peer(peer));
				}
				if(!isValid()) {
					return;
				}
			}
			
			public void addPeer(Peer ...peers) {
				if((null == peers) || (peers.length > MAX_PEER_NUMBERS || peers.length < 0)) {
					throw new IllegalStateException("P2SH address's peer's numbers should between [1-3].");
				}
				for(Peer peer:peers) {
					peerList.add(peer);
				}
			}
			
			public void generate() {
				Vector<byte[]> bytes = new Vector<byte[]>();
				for(Peer peer:peerList) {
					bytes.add(peer.getBytes());
				}
				if(!isValid()) {
					return;
				}
				code = EQCType.bytesArrayToARRAY(bytes);
				address = AddressTool.generateAddress(code, AddressType.T3);
			}
			
			public boolean isValid() {
				boolean isValid = true;
				if(!checkPeerSize()) {
					isValid = false;
					throw new ArrayIndexOutOfBoundsException("P2SH address's Peer's numbers should between [1-3].");
				}
				if(!checkSN()) {
					isValid = false;
					throw new IllegalStateException("P2SH address's Peer's SN should between [1-3] and continuously grow in steps of 1.");
				}
				if(!checkPeerAddressSize()) {
					isValid = false;
					throw new ArrayIndexOutOfBoundsException("P2SH address's Peer's address' numbers should between [1-3].");
				}
				return isValid;
			}
			
			public boolean checkSN() {
				boolean isValid = true;
				int initValue = 1;
				for(Peer peer : peerList) {
					if(peer.getPeerSN() != initValue++) {
						isValid = false;
						break;
					}
				}
				return isValid;
			}
			
			public boolean checkPeerSize() {
				boolean isValid = true;
				if(peerList.size() == 0 || peerList.size() > MAX_PEER_NUMBERS) {
					isValid = false;
				}
				return isValid;
			}
			
			public boolean checkPeerAddressSize() {
				boolean isValid = true;
				for(Peer peer : peerList) {
					if(peer.getAddressList().size() == 0 || peer.getAddressList().size() > MAX_PEER_NUMBERS) {
						isValid = false;
						break;
					}
				}
				return isValid;
			}
			
			/**
			 * @return the code
			 */
			public byte[] getCode() {
				return code;
			}

			/**
			 * @return the address
			 */
			public String getAddress() {
				return address;
			}
			
			/**
			 * @return the peerList
			 */
			public Vector<Peer> getPeerList() {
				return peerList;
			}

			/**
			 * @return the version
			 */
			public BigInteger getVersion() {
				return version;
			}

			/**
			 * @param version the version to set
			 */
			public void setVersion(BigInteger version) {
				this.version = version;
			}
			
			public static class Peer implements EQCTypable{
				private int peerSN;
				private Vector<String> addressList;
				private long timestamp;
				/*
				 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
				 */
				private final static byte VERIFICATION_COUNT = 3;
				
				public Peer() {
					addressList = new Vector<String>();
				}
				
				public Peer(byte[] bytes) throws NoSuchFieldException, IOException {
					addressList = new Vector<String>();
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;

					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						peerSN = new ID(data).intValue();
					}

					// Parse Array
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						// Parse Peers
						ARRAY peers = EQCType.parseARRAY(array.elements.get(0));
						for(byte[] peer:peers.elements) {
							addressList.add(AddressTool.AIToAddress(peer));
						}
						// Parse Timestamp
						if(!EQCType.isNULL(array.elements.get(1))) {
							timestamp = Util.bytesToLong(array.elements.get(1));
						}
						else {
							timestamp = 0;
						}
					}
				}
				
				public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;
					byte validCount = 0;
					
					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						int iPeerSN = new ID(data).intValue();
						if((iPeerSN >= 0) && (iPeerSN <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
					}

					// Parse Array
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						// Parse Peers
						ARRAY peers = EQCType.parseARRAY(array.elements.get(0));
						if((peers.length == peers.elements.size()) && (peers.elements.size() > 0 && peers.elements.size() <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
						// Parse Timestamp
						if(EQCType.isNULL(array.elements.get(1)) || (array.elements.get(1).length > 0)) {
							++validCount;
						}
					}
					
					return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
				}
				
				/**
				 * @return the peerSN
				 */
				public int getPeerSN() {
					return peerSN;
				}
				/**
				 * @param peerSN the peerSN to set
				 */
				public void setPeerSN(int peerSN) {
					if(peerSN > MAX_PEER_NUMBERS || peerSN <= 0) {
						throw new IllegalStateException("P2SH address's peer's SN should between [1-3].");
					}
					this.peerSN = peerSN;
				}
				/**
				 * @return the timestamp
				 */
				public long getTimestamp() {
					return timestamp;
				}
				/**
				 * @param timestamp the timestamp to set
				 */
				public void setTimestamp(long timestamp) {
					if(timestamp !=0 && timestamp < System.currentTimeMillis()) {
						throw new IllegalStateException("P2SH address's peer's timestamp should exceed current time.");
					}
					this.timestamp = timestamp;
				}
				
				public void addAddress(String ...addresses){
					if(addresses.length > MAX_PEER_NUMBERS || addresses.length == 0) {
						throw new IllegalStateException("P2SH address's peer's address number should between [1-3].");
					}
					for(String address:addresses) {
						if(!AddressTool.verifyAddressCRC32C(address)) {
							addressList.clear();
							throw new IllegalStateException("P2SH address's peer's address format is wrong can't pass the verify.");
						}
						addressList.add(address);
					}
				}
				
				@Override
				public byte[] getBytes() {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
						os.write(EQCType.longToEQCBits(peerSN));
						Vector<byte[]> bytes = new Vector<byte[]>();
						for(String address:addressList) {
							Log.info(address + " AI's len: " + Util.AddressTool.addressToAI(address).length);
							bytes.add(Util.AddressTool.addressToAI(address));
						}
						byte[] peers = EQCType.bytesArrayToARRAY(bytes);
						bytes.clear();
						bytes.add(peers);
						if(timestamp != 0) {
							Log.info("Timestamp's len: " + Util.longToBytes(timestamp).length);
							bytes.add(Util.longToBytes(timestamp));
						}
						else {
							bytes.add(EQCType.bytesToBIN(null));
						}
						os.write(EQCType.bytesArrayToARRAY(bytes));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
					return os.toByteArray();
				}
				
				@Override
				public byte[] getBin() {
					return EQCType.bytesToBIN(getBytes());
				}

				/**
				 * @return the addressList
				 */
				public Vector<String> getAddressList() {
					return addressList;
				}

				@Override
				public boolean isSanity(AddressShape... addressShape) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public byte[] getBytes(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public byte[] getBin(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}
				
			}
			
			public static class PeerPublickeys implements EQCTypable {
				private int publickeySN;
				private Vector<byte[]> publickeyList;
				/*
				 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
				 */
				private final static byte VERIFICATION_COUNT = 2;
				
				public PeerPublickeys() {
					publickeyList = new Vector<byte[]>();
				}
				
				public PeerPublickeys(byte[] bytes) throws NoSuchFieldException, IOException {
					publickeyList = new Vector<byte[]>();
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;

					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						publickeySN = new ID(data).intValue();
					}

					// Parse Publickeys
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						for(byte[] publickey:array.elements) {
							publickeyList.add(publickey);
						}
					}
				}
				
				public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;
					byte validCount = 0;
					
					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						int iPeerSN = new ID(data).intValue();
						if((iPeerSN >= 0) && (iPeerSN <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
					}

					// Parse Publickeys
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						if((array.length == array.elements.size()) && (array.elements.size() > 0 && array.elements.size() <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
					}
					
					return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
				}
				
				/**
				 * @return the publickeySN
				 */
				public int getPublickeySN() {
					return publickeySN;
				}
				
				/**
				 * @param publickeySN the publickeySN to set
				 */
				public void setPublickeySN(int publickeySN) {
					if(publickeySN > MAX_PEER_NUMBERS || publickeySN <= 0) {
						throw new IllegalStateException("P2SH Publickey's Publickey's SN should between [1-3].");
					}
					this.publickeySN = publickeySN;
				}
				
				public void addPublickey(byte[] ...publickeys){
					if(publickeys.length > MAX_PEER_NUMBERS || publickeys.length == 0) {
						throw new IllegalStateException("P2SH address's Publickey's Publickey number should between [1-3].");
					}
					for(byte[] publickey:publickeys) {
						publickeyList.add(publickey);
					}
				}
				
				@Override
				public byte[] getBytes() {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
						os.write(EQCType.longToEQCBits(publickeySN));
						Vector<byte[]> bytes = new Vector<byte[]>();
						for(byte[] publickey:publickeyList) {
							bytes.add(publickey);
						}
						os.write(EQCType.bytesArrayToARRAY(bytes));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
					return os.toByteArray();
				}
				
				@Override
				public byte[] getBin() {
					return EQCType.bytesToBIN(getBytes());
				}

				/**
				 * @return the publickeyList
				 */
				public Vector<byte[]> getPublickeyList() {
					return publickeyList;
				}

				@Override
				public boolean isSanity(AddressShape... addressShape) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public byte[] getBytes(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public byte[] getBin(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}
				
			}
			
			public static class PeerSignatures implements EQCTypable {
				private int signatureSN;
				private Vector<byte[]> signatureList;
				/*
				 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
				 */
				private final static byte VERIFICATION_COUNT = 2;
				
				public PeerSignatures() {
					signatureList = new Vector<byte[]>();
				}
				
				public PeerSignatures(byte[] bytes) throws NoSuchFieldException, IOException {
					signatureList = new Vector<byte[]>();
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;

					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						signatureSN = new ID(data).intValue();
					}

					// Parse Signatures
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						for(byte[] signature : array.elements) {
							signatureList.add(signature);
						}
					}
				}
				
				public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
					ByteArrayInputStream is = new ByteArrayInputStream(bytes);
					byte[] data = null;
					byte validCount = 0;
					
					// Parse SN
					if ((data = EQCType.parseEQCBits(is)) != null) {
						int iSignatureSN = new ID(data).intValue();
						if((iSignatureSN >= 0) && (iSignatureSN <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
					}

					// Parse Signatures
					ARRAY array = null;
					if ((array = EQCType.parseARRAY(is)) != null) {
						if((array.length == array.elements.size()) && (array.elements.size() > 0 && array.elements.size() <= MAX_PEER_NUMBERS)) {
							++validCount;
						}
					}
					
					return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
				}
				
				/**
				 * @return the publickeySN
				 */
				public int getSignatureSN() {
					return signatureSN;
				}
				
				/**
				 * @param signatureSN the signatureSN to set
				 */
				public void setSignatureSN(int signatureSN) {
					if(signatureSN > MAX_PEER_NUMBERS || signatureSN <= 0) {
						throw new IllegalStateException("P2SH Signature's Signature's SN should between [1-3].");
					}
					this.signatureSN = signatureSN;
				}
				
				public void addSignature(byte[] ...signatures){
					if(signatures.length > MAX_PEER_NUMBERS || signatures.length == 0) {
						throw new IllegalStateException("P2SH address's Publickey's Publickey number should between [1-3].");
					}
					for(byte[] signature : signatures) {
						signatureList.add(signature);
					}
				}
				
				@Override
				public byte[] getBytes() {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					try {
						os.write(EQCType.longToEQCBits(signatureSN));
						Vector<byte[]> bytes = new Vector<byte[]>();
						for(byte[] signature : signatureList) {
							bytes.add(signature);
						}
						os.write(EQCType.bytesArrayToARRAY(bytes));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
					return os.toByteArray();
				}
				
				@Override
				public byte[] getBin() {
					return EQCType.bytesToBIN(getBytes());
				}

				/**
				 * @return the signatureList
				 */
				public Vector<byte[]> getSignatureList() {
					return signatureList;
				}

				@Override
				public boolean isSanity(AddressShape... addressShape) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public byte[] getBytes(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public byte[] getBin(AddressShape addressShape) {
					// TODO Auto-generated method stub
					return null;
				}
				
			}

			@Override
			public byte[] getBytes() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public byte[] getBin() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isPeerPublickeysValid(PeerPublickeys peerPublickeys) {
				boolean isValid = true;
				Peer peer = peerList.get(peerPublickeys.getPublickeySN() - 1);
				if(peerPublickeys.getPublickeyList().size() != peer.getAddressList().size()) {
					Log.Error("VerifyPublickey size verify failed please check your size");
					isValid = false;
				}
				else {
					for(int i=0; i<peer.getAddressList().size(); ++i) {
						if(!AddressTool.verifyAddressPublickey(peer.getAddressList().get(i), peerPublickeys.getPublickeyList().get(i))) {
							isValid = false;
							Log.Error("VerifyPublickey address verify failed please check your Publickey");
							break;
						}
					}
				}
				return isValid;
			}

			@Override
			public boolean isSanity(AddressShape... addressShape) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public byte[] getBytes(AddressShape addressShape) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public byte[] getBin(AddressShape addressShape) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}
		
	}

	public static Transaction generateCoinBaseTransaction(Address address, ID height, AccountsMerkleTree accountsMerkleTree) {
		Transaction transaction = new CoinbaseTransaction();
		TxOut eqcFoundationTxOut = new TxOut();
		TxOut minerTxOut = new TxOut();
		minerTxOut.setAddress(address);
		eqcFoundationTxOut.setAddress(Util.DB().getAddress(ID.ONE));
		if (height.compareTo(Util.MAX_COINBASE_HEIGHT) < 0) {
			if(accountsMerkleTree.isAccountExists(address, true)) {
				transaction.setNonce(accountsMerkleTree.getAccount(address).getNonce().getNextID());
			}
			else {
				transaction.setNonce(ID.ONE);
			}
			eqcFoundationTxOut.setValue(Util.EQC_FOUNDATION_COINBASE_REWARD);
			minerTxOut.setValue(Util.MINER_COINBASE_REWARD);
		} else {
			transaction.setNonce(accountsMerkleTree.getAccount(ID.ONE).getNonce().getNextID());
			eqcFoundationTxOut.setValue(0);
			minerTxOut.setValue(0);
		}
		transaction.addTxOut(eqcFoundationTxOut);
		transaction.addTxOut(minerTxOut);
		return transaction;
	}

	public static EQCBlock gestationSingularityBlock() {
		saveEQCBlockTailHeight(ID.ZERO);
		// Create AccountsMerkleTree
		AccountsMerkleTree accountsMerkleTree = new AccountsMerkleTree(ID.ZERO, new Filter(EQCBlockChainRocksDB.ACCOUNT_MINERING_TABLE));
		
		// Create EQC block
		EQCBlock eqcBlock = new EQCBlock();

		// Create TransactionsHeader
		TransactionsHeader transactionsHeader = new TransactionsHeader();
//		transactionsHeader.setSignaturesHash(null);
//		eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);

		// Create Transaction
		Transaction transaction = new CoinbaseTransaction();
		TxOut txOut = new TxOut();
		txOut.getAddress().setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		txOut.getAddress().setID(ID.ONE);
		txOut.setValue(EQC_FOUNDATION_COINBASE_REWARD);
		txOut.setNew(true);
		transaction.addTxOut(txOut);
		Account account = new Account();
		account.setAddress(txOut.getAddress());
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(EQC_FOUNDATION_COINBASE_REWARD);
		account.setBalanceUpdateHeight(ID.ZERO);
		accountsMerkleTree.saveAccount(account);
		accountsMerkleTree.increaseTotalAccountNumbers();
		
		txOut = new TxOut();
		txOut.getAddress().setReadableAddress(Keystore.getInstance().getUserAccounts().get(1).getReadableAddress());
		txOut.getAddress().setID(ID.TWO);
		txOut.setValue(MINER_COINBASE_REWARD);
		txOut.setNew(true);
		transaction.addTxOut(txOut);
		account = new Account();
		account.setAddress(txOut.getAddress());
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(MINER_COINBASE_REWARD);
		account.setBalanceUpdateHeight(ID.ZERO);
		account.setNonce(ID.ONE);
		accountsMerkleTree.saveAccount(account);
		accountsMerkleTree.increaseTotalAccountNumbers();
		transaction.setNonce(ID.ONE);
		eqcBlock.getTransactions().addTransaction(transaction);
		
		// Add new address in address list
//		if (!eqcBlock.getTransactions().isAddressExists(address.getAddress())) {
//			eqcBlock.getTransactions().getAddressList().addElement(address);
//		}

		accountsMerkleTree.buildAccountsMerkleTree();
		accountsMerkleTree.generateRoot();
		accountsMerkleTree.merge();

//		// Create Index
//		Index index = new Index();
//		index.setTotalSupply(cypherTotalSupply(SerialNumber.ZERO));
//		index.setTotalAccountNumbers(BigInteger.TWO);
//		index.setTotalTransactionNumbers(BigInteger.ONE);
//		index.setAccountsMerkleTreeRootList(accountsMerkleTree.getAccountsMerkleTreeRootList());
//		index.setTransactionsHash(eqcBlock.getTransactions().getHash());

		// Create Root
		Root root = new Root();
//		root.setIndexHash(index.getHash());
		root.setTotalSupply(cypherTotalSupply(ID.ZERO));
		root.setTotalAccountNumbers(ID.TWO);
		root.setTotalTransactionNumbers(ID.ONE);
		root.setAccountsMerkelTreeRoot(accountsMerkleTree.getRoot());
		root.setTransactionsMerkelTreeRoot(eqcBlock.getTransactionsMerkelTreeRoot());

		// Create EQC block header
		EQCHeader header = new EQCHeader();
		header.setPreHash(MAGIC_HASH);
		header.setTarget(Util.getDefaultTargetBytes());
		header.setHeight(ID.ZERO);
		header.setTimestamp(new ID(0));
		header.setNonce(ID.ZERO);
		header.setRootHash(root.getHash());
		eqcBlock.setEqcHeader(header);

//		eqcBlock.setIndex(index);
		eqcBlock.setRoot(root);

		return eqcBlock;
	}

	public static long cypherTotalSupply(ID height) {
		if (height.compareTo(MAX_COINBASE_HEIGHT) < 0) {
			return (EQC_FOUNDATION_COINBASE_REWARD + MINER_COINBASE_REWARD) * (height.longValue() + 1);
		} else {
			return MAX_EQC;
		}
	}

	public static byte[] cypherTarget(ID height) {
		byte[] target = null;
		BigInteger oldDifficulty;
		BigInteger newDifficulty;
		if (height.longValue() < 9) {
			return getDefaultTargetBytes();
		}
		ID serialNumber_end = new ID(height.subtract(BigInteger.ONE));
		ID serialNumber_begin = new ID(height.subtract(BigInteger.valueOf(10)));
		if (height.longValue() % 10 != 0) {
//			Log.info(serialNumber_end.toString());
			target = EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTarget();
//			Log.info(Util.bigIntegerTo128String(Util.targetBytesToBigInteger(target)));
		} else {
			Log.info(
					"Old target: "
							+ Util.bigIntegerTo512String(Util.targetBytesToBigInteger(
									EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTarget()))
							+ "\r\naverge time: "
							+ (EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTimestamp().longValue()
									- EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_begin).getTimestamp().longValue())
									/ 9);
			oldDifficulty = Util
					.targetBytesToBigInteger(EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTarget());
			newDifficulty = oldDifficulty
					.multiply(BigInteger
							.valueOf((EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTimestamp().longValue()
									- EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_begin).getTimestamp().longValue())))
					.divide(BigInteger.valueOf(9 * Util.BLOCK_INTERVAL));
			// Compare if old difficulty divide new difficulty is bigger than MAX_DIFFICULTY_MULTIPLE
			if(oldDifficulty.divide(newDifficulty).compareTo(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE)) > 0) {
				Log.info("Due to old difficulty divide new difficulty(" + Util.bigIntegerTo512String(newDifficulty) +") = " + oldDifficulty.divide(newDifficulty).toString() + " is bigger than MAX_DIFFICULTY_MULTIPLE so here just divide MAX_DIFFICULTY_MULTIPLE");
				newDifficulty = oldDifficulty.divide(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE));
			}
			if (Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(newDifficulty) >= 0) {
				Log.info("New target: " + Util.bigIntegerTo512String(newDifficulty));
				target = Util.bigIntegerToTargetBytes(newDifficulty);
			} else {
				Log.info("New target: " + Util.bigIntegerTo512String(newDifficulty)
						+ " but due to it's bigger than the default target so still use default target.");
				target = Util.getDefaultTargetBytes();
			}
		}
		return target;
	}

	

	public static PublicKey getPublicKey(ID serialNumber, EQCBlock eqcBlock) {
		PublicKey publicKey = null;
		publicKey = EQCBlockChainH2.getInstance().getPublicKey(serialNumber);
		if (publicKey == null) {
			Vector<PublicKey> publicKeyList = eqcBlock.getTransactions().getNewPublicKeyList();
			for (PublicKey publicKey2 : publicKeyList) {
				if (publicKey2.equals(serialNumber)) {
					publicKey = publicKey2;
					break;
				}
			}
		}
		return publicKey;
	}

	public static String getAddress(ID serialNumber, EQCBlock eqcBlock) {
		Address address = null;
		address = EQCBlockChainH2.getInstance().getAddress(serialNumber);
		if (address == null) {
			Vector<Address> addressList = eqcBlock.getTransactions().getNewAccountList();
			for (Address address2 : addressList) {
				if (address2.equals(serialNumber)) {
					address = address2;
					break;
				}
			}
		}
		return (address == null) ? null : address.getReadableAddress();
	}

//	public static long getBillingFee(Transaction transaction, TXFEE_RATE rate, SerialNumber height){
//		int qos = 0;
//		switch (rate) {
//		case POSTPONE0:
//			qos = 6;
//			break;
//		case POSTPONE20:
//			qos = 4;
//			break;
//		case POSTPONE40:
//			qos = 2;
//			break;
//		case POSTPONE60:
//			qos = 1;
//			break;
//		default:
//			qos = 4;
//			break;
//		}
//
//		long txNumbersIn24 = EQCBlockChainH2.getInstance()
//				.getTransactionNumbersIn24hours(transaction.getTxIn().getAddress(), height);
//		Log.info("txNumbersIn24: " + txNumbersIn24);
//		return (txNumbersIn24 % 10 + 1) * (transaction.getMaxBillingSize() / TXFEE_UNIT) * qos;
//	}
	
//	public static TXFEE_RATE getQOS(Transaction transaction, long billingFee, SerialNumber height) {
//		TXFEE_RATE txfee_rate = null;
//		int qos = 1;
//		long txNumbersIn24 = EQCBlockChainH2.getInstance()
//				.getTransactionNumbersIn24hours(transaction.getTxIn().getAddress(), height);
//		Log.info("txNumbersIn24: " + txNumbersIn24);
//		qos = (int) (billingFee / ((txNumbersIn24 % 10 + 1) * (transaction.getMaxBillingSize() / TXFEE_UNIT)));
//		switch (qos) {
//		case 1000:
//			txfee_rate = TXFEE_RATE.POSTPONE0;
//			break;
//		case 100:
//			txfee_rate = TXFEE_RATE.POSTPONE20;
//			break;
//		case 10:
//			txfee_rate = TXFEE_RATE.POSTPONE40;
//			break;
//		case 1:
//			txfee_rate = TXFEE_RATE.POSTPONE60;
//			break;
//		}
//		return txfee_rate;
//	}

	public static byte[] getMerkleTreeRoot(Vector<byte[]> bytes) {
		MerkleTree merkleTree = new MerkleTree(bytes);
		merkleTree.generateRoot();
		return merkleTree.getRoot();
	}

	public static boolean isAddressFormatValid(String address) {
		Log.info("address' length: " + address.length());
		String mode = "^[1-3][1-9A-Za-z]{20,25}";
		Pattern pattern = Pattern.compile(mode);
		Matcher matcher = pattern.matcher(address);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isTXValueValid(String address) {
		Log.info("address' length: " + address.length());
		String mode = "^[0-9]+(.[0-9]{1,4})?$";// "^[1-9][0-9]*|[1-9][0-9]*\\.[0-9]{0,4}";
		Pattern pattern = Pattern.compile(mode);
		Matcher matcher = pattern.matcher(address);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static long getBalance(String address) {
		Address strAddress = new Address();
		strAddress.setReadableAddress(address);
		strAddress.setID(EQCBlockChainH2.getInstance().getAddressID(strAddress));
		return EQCBlockChainH2.getInstance().getBalance(strAddress);
	}

	public static String getIP() {
		InputStream ins = null;
		String ip = "";
		try {
			URL url = new URL("http://www.cip.cc/");
			URLConnection con = url.openConnection();
			ins = con.getInputStream();
			InputStreamReader isReader = new InputStreamReader(ins, "utf-8");
			BufferedReader bReader = new BufferedReader(isReader);
			StringBuffer webContent = new StringBuffer();
			String str = null;
			while ((str = bReader.readLine()) != null) {
				webContent.append(str);
			}
			int start = webContent.indexOf("IP	: ") + 5;
			int end = webContent.indexOf("地址	:");
			ip = webContent.substring(start, end);
		} catch (Exception e) {
			Log.Error(e.toString());
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.toString());
				}
			}
		}
		return ip;
	}

	public static Cookie getCookie() {
		return cookie;
	}

	public static void updateCookie() {
		cookie.setIp(getIP());
	}

	public static Status getStatus() {
		return status;
	}

	public static Status getStatus(STATUS _status, String message) {
		status.setCode(_status.ordinal());
		status.setMessage(message);
		return status;
	}

	public static long getNTPTIME() {
		NTPUDPClient timeClient = new NTPUDPClient();
		String timeServerUrl = "time.windows.com";
		InetAddress timeServerAddress;
		TimeStamp timeStamp = null;
		try {
			timeClient.setDefaultTimeout(DEFAULT_TIMEOUT);
			timeServerAddress = InetAddress.getByName(timeServerUrl);
			TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
			timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Log.info("Current time: " + dateFormat.format(timeStamp.getDate()));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return (timeStamp!=null)?timeStamp.getDate().getTime():0;
	}

	public static boolean isTimeCorrect() {
		boolean boolCorrect = true;
		if(Math.abs(System.currentTimeMillis() - getNTPTIME()) >= DEFAULT_TIMEOUT) {
			boolCorrect = false;
		}
		return boolCorrect;
	}
	
	public static boolean isNetworkAvailable() {
		boolean boolIsNetworkAvailable = false;
		InputStream ins = null;
		String ip = "";
		try {
			URL url = new URL("http://www.bing.com");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(DEFAULT_TIMEOUT);
			httpURLConnection.connect();
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				boolIsNetworkAvailable = true;
			}
			httpURLConnection.disconnect();
		} catch (Exception e) {
			Log.Error(e.toString());
		} 
		return boolIsNetworkAvailable;
	}
	
	public static byte[] getBlockHeaderHash(Transaction transaction) {
		return EQCBlockChainH2.getInstance().getEQCHeaderHash(
				EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getAddress()));
	}
	
	public static byte[] CRC32C(byte[] bytes) {
		CRC32C crc32c = new CRC32C();
		crc32c.update(bytes);
//		Log.info(dumpBytes(longToBytes(crc32c.getValue()), 16) + " Len: " + longToBytes(crc32c.getValue()).length);
//		Log.info(dumpBytes(intToBytes((int) crc32c.getValue()), 16) + " Len: " + intToBytes((int) crc32c.getValue()).length);
//		Log.info("" + crc32c.getValue());
		return intToBytes((int) (crc32c.getValue() & 0xFFFFFFFF));
	}
	
	public static EQCBlockChain DB(DB db) {
		EQCBlockChain eqcBlockChain = null;
		if(db == DB.H2) {
			eqcBlockChain = EQCBlockChainH2.getInstance();
		}
		else if(db == DB.ROCKSDB) {
			eqcBlockChain = EQCBlockChainRocksDB.getInstance();
		}
		return eqcBlockChain;
	}
	
	public static EQCBlockChain DB() {
		return DB(DB.ROCKSDB);
	}
	
	public static BigInteger UnsignedBiginteger(BigInteger foo) {
		BigInteger value = foo;
		if(foo.signum() == -1) {
			value = new BigInteger(1, foo.toByteArray());
		}
		return value;
	}
	
	public static void cypherSingularityEQCBlockPreHash() {
		File file = new File(MAGIC_PATH);
		Vector<byte[]> vector = new Vector<>();
		MerkleTree merkleTree = null;
		for (File photo : file.listFiles()) {
			try {
				vector.add(new FileInputStream(photo).readAllBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Vector<byte[]> reverse = new Vector<>();
		for(int i=vector.size()-1; i>=0; --i) {
			reverse.add(vector.get(i));
		}
		merkleTree = new MerkleTree(reverse);
		merkleTree.generateRoot();
		Log.info("Root: " + dumpBytes(merkleTree.getRoot(), 16));
		Log.info("Magic: " + dumpBytes(EQCCHA_MULTIPLE_FIBONACCI_MERKEL(merkleTree.getRoot(), HUNDRED_THOUSAND, false), 16));
		Log.info(new BigInteger(1, EQCCHA_MULTIPLE_FIBONACCI_MERKEL(merkleTree.getRoot(), HUNDRED_THOUSAND, false)).toString());
	}

	public static ID fibonacci(long number) {
		ID a = ID.ONE, b = ID.ONE, c = ID.ZERO;
		if(number <= 2) {
			return ID.ONE;
		}
		for(int i=3; i<=number; ++i) {
			c = a.add(b);
			a = b;
			b = c;
		}
		return c;
	}

	public static String getSizeJson(int size) {
		return 
		"{\n" +
		"\"Size\":\"" + size + "\"" +
		"\n}";
	}
	
	public static void saveEQCBlockTailHeight(ID height) {
		EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(height);
		EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(height);
	}
	
}
