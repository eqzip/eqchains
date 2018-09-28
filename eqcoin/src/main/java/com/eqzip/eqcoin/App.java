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
package com.eqzip.eqcoin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Vector;

import com.eqzip.eqcoin.blockchain.Address;
import com.eqzip.eqcoin.blockchain.EQCHeader;
import com.eqzip.eqcoin.keystore.Account;
import com.eqzip.eqcoin.keystore.AddressTool;
import com.eqzip.eqcoin.keystore.EQCPublicKey;
import com.eqzip.eqcoin.keystore.Keystore;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.Os;
import com.eqzip.eqcoin.util.Base58;
import com.eqzip.eqcoin.util.CRC8ITU;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public class App {
	public static void main(String[] args) {
		Thread.currentThread().setPriority(10);
		Util.init(Os.WINDOWS);
		Log.info("Default target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes())));
		
		byte[] by = Util.bigIntegerToTargetBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()));//new BigInteger("1525de600000000000000000000000", 16));
		Log.info("hex: " + Integer.toHexString(Util.bytesToInt(by)));
		Log.info("target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger(by)));
		
//    	Log.info("r: " + new SecureRandom("abc".getBytes()).nextLong());
//		Log.info(Util.bigIntegerTo512String(Util.getDefaultTarget()));
//		String text = "In teaching others we teach ourselves";
//		for(int i=0; i<100; ++i) {
//			Log.info("count: " + i);
//			testECPubKeySignature((byte) Keystore.P521, text+=text);
//		}
//		Log.Error(Util.bigIntegerTo512String(new BigInteger("8efa4a3a6bbb63868387395106900b9a6fc9aabb0cb5f2fb20964830cb26244b11b13d6f14a0c807fb4265557d3e7166351bfcb67bebd86fe4d3c1e5a1911753", 16)));
//		byte[] a = Util.bigIntegerToBits(BigInteger.valueOf(188888));
//		BigInteger b = Util.bitsToBigInteger(a);
//		Address address = new Address(new SerialNumber(BigInteger.valueOf(188888)), "32h9PPspxFSASmpkXWXwvNcWx", Util.getSecureRandomBytes());
//		Address abc = new Address(address.getBytes());
//		Log.info(abc.toString());
//		if(address.equals(abc)) {
//			Log.info("equal");
//		}
//		else {
//			Log.info("not equal");
//		}
//		int i=0;
//    	testEC((byte)1);
//    	testEC((byte)2);
//    	Log.info("abc");
//    	byte[] random = Util.getSecureRandomBytes();
//    	String s58 = Base58.encode(random);
//    	byte[] r58 = null;
//    	try {
//			r58 = Base58.decode(s58);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//    	if(Arrays.equals(random, r58)) {
//    		Log.info("array equal");
//    	}
//    	else {
//    		Log.info("array not equal");
//    	}
//    	String s = Address.generateAddress(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.HUNDRED, true), Address.V1);
//    	if (Address.verifyAddress(s)) {
//			Log.info("address " + s + " verify passed");
//		}
//		else {
//			Log.info("address " + s + " verify failed");
//		}
//    	try {
//			byte[] by = Base58.decode(s.substring(0, 1));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	byte[] bytes = BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray();
//    	byte[] bytes1 = new BigInteger(1, bytes).toByteArray();
//    	byte[] bytes2 = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toByteArray();
//    	byte[] bytes3 = BigInteger.valueOf(Long.MAX_VALUE).toByteArray();
//    	byte[] bytes4 = BigInteger.valueOf(128).toByteArray();
//    	Log.info(Util.dumpBytesBigEndianHex(bytes4));
//    	Log.info(Util.dumpBytesBigEndianBinary(bytes4));
//    	testCreateAccount();
//    	testCRC8ITU();
//    	testRIPEMD();
//    	Util.longToBits(256);
//    	byte[] bytes = Util.longToBits(123456789);
//    	long l = Util.bitsToLong(bytes);
//    	Log.info(l+"");
//    	CRC8 crc8 = new CRC8();
//    	crc8.updateCRC8(Util.longToBytes(1234567), 0, Util.longToBytes(1234567).length);
//    	byte crc = crc8.checksum();
//		System.out.println("" + Integer.toHexString(0x00ff & crc));
//    	Log.info(Util.dumpBytes(new BigInteger("123456789012345", 10).toByteArray(), 16));
//    	testBigIntegerToBits();
//    	bytes = Util.longToBits(255);
//    	l = Util.bitsToLong(bytes);
//    	bytes = Util.longToBits(128);
//    	l = Util.bitsToLong(bytes);
//    	bytes = Util.longToBits(127);
//    	l = Util.bitsToLong(bytes);
//    	Log.info(Util.dumpBytes(Util.longToBits(255)));
//    	SerialNumber sn = new SerialNumber(BigInteger.valueOf(123456));
//    	byte[] bytes1 = sn.getBits();
//    	byte[] bytes2 = Util.longToBits(123456);
//    	testSN();
//    	long l = Util.bitsToLong(Util.longToBits(1111));
//    	Log.info(""+l);
//    	Log.info(new BigInteger(1, Util.longToBits(1111)).toString(2));
//    	if(Util.createPath(Util.PATH)) {
//    		Log.info("successful");
//    	}
//    	else {
//    		Log.info("failed");
//    	}
//    	for(int i=0; i<1; ++i)
//    	Log.info("abcde");
//    	testBase58();
//    	testKeystore();
		testBlockchain();
//		testHashTime();
//    	testLongToBytes();
//    	testTargetToBytes();
//    	testSignBigIntegerPadingZero();
//    	caluateTarget();
	}

	private static void testHashTime() {
		EQCHeader header = new EQCHeader();
		header.setNonce(1);
		header.setPreHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
		header.setTarget(Util.getDefaultTargetBytes());
		header.setTxHash(Util.dualSHA3_512(Util.getSecureRandomBytes()));
		header.setTimestamp(System.currentTimeMillis());
		Log.info(header.toString());
		long c0 = System.currentTimeMillis();
		for (int i = 0; i < 100; ++i) {
			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.MILLIAN, false);
		}
		long c1 = System.currentTimeMillis();
		Log.info("average time:" + (c1-c0)/100);
	}

	private static void testECPubKeySignature(byte type, String text) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == 1) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == 2) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			ECPublicKey ecPublicKey = (ECPublicKey) pubKey;
			Log.info(pubKey.toString() + " public key's len: " + pubKey.getEncoded().length + " ec x: " + ecPublicKey.getW().getAffineX().toByteArray().length);
			EQCPublicKey eqPublicKey = new EQCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			Log.info("Compress Public Key's len: " + eqPublicKey.getCompressedPublicKeyEncoded().length + "\nPublic Key:" + eqPublicKey.getCompressedPublicKeyEncoded()[0]);
			
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(privKey);
//			String text = "In teaching others we teach ourselves";
			System.out.println("Text len: " + text.length());
			byte[] baText = text.getBytes("UTF-8");
			ecdsa.update(baText);
			byte[] baSignature = ecdsa.sign();
			Log.info("signature' len: " + baSignature.length);
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	private static void testEC(byte type) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == 1) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == 2) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			if (pubKey instanceof ECPublicKey) {
				Log.info("ECPublicKey");
			} else {
				Log.info("Not ECPublicKey");
			}
			Log.info("getAlgorithm: " + pubKey.getAlgorithm() + " getFormat: " + pubKey.getFormat());

			System.out.println(privKey.toString());
			System.out.println(pubKey.toString());
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(privKey);
			String text = "In teaching others we teach ourselves";
			System.out.println("Text: " + text);
			byte[] baText = text.getBytes("UTF-8");
			ecdsa.update(baText);
			byte[] baSignature = ecdsa.sign();
			System.out.println("Signature: 0x" + (new BigInteger(1, baSignature).toString(16)).toUpperCase());
			Signature signature;
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
//			pubKey = new sun.security.ec.ECPublicKeyImpl(baSignature);
			EQCPublicKey eqPublicKey = new EQCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			byte[] compressedPubkey = eqPublicKey.getCompressedPublicKeyEncoded();
			Log.info("compressedPubkey: " + Util.dumpBytes(compressedPubkey, 10) + " len: " + compressedPubkey.length);// (compressedPubkey));
			eqPublicKey.setECPoint(compressedPubkey);
//			eqPublicKey.setECPoint(pubKey.getEncoded());
			Log.info(Util.dumpBytesBigEndianBinary(pubKey.getEncoded()));
			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			ECPublicKey abc = (ECPublicKey) pubKey;
//			abc.getW().getAffineX()
			signature.initVerify(eqPublicKey);
			signature.update(baText);
			boolean result = signature.verify(baSignature);
			System.out.println("Valid: " + result);

		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException
				| InvalidKeyException | SignatureException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	private static void testCreateAccount() {
//		Account acc = new Account();
//    	acc.setAddress("abc");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(new byte[64]);
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("abcd");
//    	Keystore.getInstance().createAccount(acc);
//    	Account acc1 = new Account();
//    	acc1.setAddress("abc");
//    	acc1.setBalance(1000000000);
//    	acc1.setPrivateKey(new byte[64]);
//    	acc1.setPwdHash(Util.getSecureRandomBytes());
//    	acc1.setUserName("abcd");
//    	if(acc.equals(acc1)) {
//    		Log.info("equal");
//    	}
//    	Keystore.getInstance().createAccount(acc1);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("a");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("a");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("b");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("b");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
	}

	private static void testKeystore() {
		Account acc = new Account();
		acc.setAddress("abc");
		acc.setBalance(1000000000);
		acc.setPrivateKey(new byte[64]);
		acc.setPwdHash(new byte[64]);
		acc.setUserName("abcd");
//    	Keystore.getInstance().updateAccount();
//    	Keystore.getInstance().createAccount(acc);
	}

	private static void testBase58() {
		byte[] address = new byte[18];
		for (int i = 0; i < 1; ++i) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(0);
			ByteArrayInputStream is = new ByteArrayInputStream(address);
			Log.info("os0:\n" + Util.dumpBytesBigEndianHex(os.toByteArray()));
			StringBuilder sb = new StringBuilder();
//    	sb.append("00");
			BigInteger pubKeyHash = new BigInteger(1,
					Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.HUNDRED, true));

			Log.info("pubKeyHash:\n" + Util.dumpBytesBigEndianHex(pubKeyHash.toByteArray()));
			try {
				os.write(pubKeyHash.toByteArray());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(pubKeyHash.toString(16));
			sb.append(CRC8ITU.update(os.toByteArray()));
			Log.info("os1:\n" + Util.dumpBytesBigEndianHex(os.toByteArray()));
			BigInteger mod = new BigInteger(1, os.toByteArray());
			if (mod.mod(BigInteger.valueOf(0x7)).compareTo(BigInteger.ZERO) == 0) {
				Log.info("crc check passed.");
			} else {
				Log.info("crc check failed.");
			}
			byte[] version = new byte[1];
			version[0] = 1;
			Log.info(Base58.encode(new byte[] { 0 }));
			String add = Base58.encode(version)
					+ Base58.encode(ByteBuffer.wrap(os.toByteArray(), 1, os.toByteArray().length - 1).array());
			Log.info(add);
//    	String str = sb.toString();
//    	Log.info(str);
//    	byte[] bytes = new BigInteger(str, 16).toByteArray();
//    	Log.info(Base58.encode(new BigInteger(str, 16).toByteArray()) + " len: " + Base58.encode(new BigInteger(str, 16).toByteArray()).length());
//    	Log.info(Base58.encode1(new BigInteger(str, 16).toByteArray()));
		}
	}

	private static void testCRC8ITU() {
		byte[] bytes = "123456789".getBytes();
		short b = CRC8ITU.update(bytes);
		Log.info(Integer.toHexString(CRC8ITU.update(bytes) & 0xff));
	}

	private static void testRIPEMD() {
		Log.info(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16));
		Log.info(Util.dumpBytes(Util.RIPEMD128("abc".getBytes()), 16));
//    	assertNotNull(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16), "userAttribute");
	}

	private static void testBigIntegerToBits() {

		// 127 = ‭01111111‬
		Log.info(Util.dumpBytes(Util.longToBytes(127l), 16) + "\n" + Util.dumpBytes(Util.longToBits(127l), 16) + "\n"
				+ Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(127l)).toByteArray(), 16));
		// 128 = ‭10000000‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(128)) + "\n" + Util.dumpBytes(Util.longToBits(128)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(128)).toByteArray()));
//    	// 255 = ‭11111111‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(255)) + "\n" + Util.dumpBytes(Util.longToBits(255)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(255)).toByteArray()));
//    	// 4095 = ‭111111111111‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(4095)) + "\n" + Util.dumpBytes(Util.longToBits(4095)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(4095)).toByteArray()));
//    	// 1234 = ‭‭010011010010‬‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(1234)) + "\n" + Util.dumpBytes(Util.longToBits(1234)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(1234)).toByteArray()));
//    	// 12345 = ‭‭0011000000111001‬‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(12345)) + "\n" + Util.dumpBytes(Util.longToBits(12345)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(12345)).toByteArray()));
//    	// ‭Integer.MAX_VALUE‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(Integer.MAX_VALUE-1)) + "\n" + Util.dumpBytes(Util.longToBits(Integer.MAX_VALUE-1)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(Integer.MAX_VALUE-1)).toByteArray()));
		// 1844674407370955161 =
		// ‭0001100110011001100110011001100110011001100110011001100110011001
//    	Log.info(Util.dumpBytes(Util.longToBytes(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.longToBits(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(Long.MAX_VALUE)).toByteArray()));‬
	}

	private static void testSN() {
		SerialNumber addressSN = new SerialNumber(BigInteger.ZERO);
		Vector<SerialNumber> vec = new Vector<SerialNumber>();
		vec.add(addressSN);
		for (int i = 1; i < 1000; ++i) {
			vec.add(vec.get(i - 1).getNextSN());
			if (vec.get(i).isNextSN(vec.get(i - 1)))
				Log.info("isNextSN：" + " current: " + vec.get(i).getSerialNumber().longValue() + " previous:"
						+ vec.get(i - 1).getSerialNumber().longValue() + " bits: "
						+ Util.dumpBytes(vec.get(i).getBits(), 2));
		}
	}

	private static void testBlockchain() {
		System.out.println("testBlockchain");
		Log.info("Default target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger((Util.getDefaultTargetBytes()))));
		EQCHeader header = new EQCHeader();
		header.setNonce(1);
		header.setPreHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
		header.setTarget(Util.getDefaultTargetBytes());
		header.setTxHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
		header.setTimestamp(System.currentTimeMillis());
//    	System.out.println(header.toString());
		Vector<EQCHeader> vec = new Vector<EQCHeader>();
		vec.add(header);
		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
		long time0 = System.currentTimeMillis();
		long time1;
		int lCount = 0;
		long Totaltime = 0;
		long i = 0;
		byte[] bytes;
		while (true) {
			BigInteger hash = new BigInteger(1, Util
					.EQCCHA_MULTIPLE((bytes = Util.updateNonce(vec.get(lCount).getBytes(), ++i)), Util.MILLIAN, true));// Util.dualSHA3_512(Util.multipleExtend((bytes=Util.updateNonce(vec.get(lCount).getBytes(),
																														// ++i)),
																														// 100)));
//        	System.out.println("hash: " + Util.bigIntegerTo128String(hash));
			if (hash.compareTo(target) <= 0) {
//        		time1 = System.currentTimeMillis();
				Log.info("EQC Block No." + lCount + " Find use: "
						+ (System.currentTimeMillis() - vec.get(lCount).getTimestamp()) + " ms, details:");
				vec.set(lCount, new EQCHeader(bytes));
				Log.info(vec.get(lCount).toString());
				header = new EQCHeader();
				header.setNonce(0);
				header.setPreHash(Util.bigIntegerTo16Bytes(hash));
				++lCount;
				if (lCount % 10 != 0) {
					header.setTarget(vec.get(lCount - 1).getTarget());
				} else {
					Log.info("Old target: "
							+ Util.bigIntegerTo128String(Util.targetBytesToBigInteger(vec.get(lCount - 1).getTarget()))
							+ "\r\naverge time: "
							+ (vec.get(lCount - 1).getTimestamp() - vec.get(lCount - 10).getTimestamp()) / 10);
					target = target
							.multiply(BigInteger.valueOf(
									(vec.get(lCount - 1).getTimestamp() - vec.get(lCount - 10).getTimestamp())))
							.divide(BigInteger.valueOf(90000));
					if(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(target) >= 0)
					{
						Log.info("New target: " + Util.bigIntegerTo128String(target));
						header.setTarget(Util.bigIntegerToTargetBytes(target));
					}
					else {
						Log.info("New target: " + Util.bigIntegerTo128String(target) + " but due to it's less than the default target so still use default target.");
						header.setTarget(Util.getDefaultTargetBytes());
					}
				}
				header.setTxHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
				header.setTimestamp(System.currentTimeMillis());
				vec.add(header);
				i = 0;
				if (lCount == 2000) {
					break;
				}
//        		System.out.println(hash.toString(2));
//        		System.out.println(" len: " + hash.toString(2).length() + " i: " +i);
//        		System.out.println(" len: " + hash.toString(2).length() + " i: " + (i-1) + "\n" + Base64.getEncoder().encodeToString(Util.dualSHA3_512(Util.multipleExtend((""+(i-1)).getBytes(), 1))));
//        		if(lCount%10 == 0) {
//        			System.out.println("Old target: " + target.toString(16) + "averge time: " + Totaltime/10);
//        			target = target.multiply(BigInteger.valueOf(Totaltime)).divide(BigInteger.valueOf(100000));
////        			Totaltime = 0;
//        			System.out.println("New target: " + target.toString(16));
//        		}
			}
		}
		Log.info("averge time: " + (vec.get(vec.size() - 1).getTimestamp() - vec.get(0).getTimestamp()) / lCount
				+ " total time: " + (vec.get(vec.size() - 1).getTimestamp() - vec.get(0).getTimestamp()) + " count:"
				+ lCount);
	}

	private static void testLongToBytes() {
		byte[] foo = Util.longToBytes(Long.MAX_VALUE);
		long lValue = Util.bytesToLong(foo);
		System.out.println("lValue: " + lValue);
	}

	private static void testTarget() {

		BigInteger a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16))
				.multiply(BigInteger.valueOf(2).pow(512 - 60 - 17));
		System.out.println(a.toString(16));
		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3));
		System.out.println(a.toString(16));
		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3))
				.add(BigInteger.valueOf(Long.parseLong("21", 16)));
		System.out.println(a.toString(16));
		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(424);// .multiply(BigInteger.valueOf(2).pow(3)).add(BigInteger.valueOf(Long.parseLong("21",
																					// 16)));
		System.out.println(a.shiftRight(512 - a.bitLength()).toString(16) + " len: " + a.bitLength());

	}

	private static void testSignBigIntegerPadingZero() {

		BigInteger negative_number = BigInteger.valueOf(Long.MAX_VALUE + 1);
		byte[] negative_bytes = negative_number.toByteArray();
		System.out.println(
				"negative_number:" + negative_number.toString() + " negative_bytes' len: " + negative_bytes.length);

		BigInteger positive_number = new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray());
		byte[] positive_bytes = positive_number.toByteArray();
		System.out.println(
				"positive_number:" + positive_number.toString() + " positive_bytes' len: " + positive_bytes.length);

		BigInteger number = BigInteger.valueOf(Long.MAX_VALUE + 1);// .add(BigInteger.ONE);
		byte[] number_bytes = number.toByteArray();
		System.out.println("number:" + number.toString() + " number_bytes' len: " + number_bytes.length);

		BigInteger number1 = BigInteger.valueOf(128).shiftLeft(80);
		byte[] number_bytes1 = number1.toByteArray();
		System.out.println("number1:" + number1.toString(16) + " number_bytes1' len: " + number_bytes1.length);

		BigInteger number2 = new BigInteger(1, number_bytes1);
		byte[] number_bytes2 = number2.toByteArray();
		System.out.println("number2:" + number2.toString(16) + " number_bytes2' len: " + number_bytes2.length);

		BigInteger number3 = new BigInteger(number_bytes1);
		byte[] number_bytes3 = number3.toByteArray();
		System.out.println("number3:" + number3.toString(16) + " number_bytes3' len: " + number_bytes3.length);

	}

	private static void testTargetToBytes() {

		// Display default target
		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
		System.out.println("Default target's length: " + Util.getDefaultTargetBytes().length);
//		// Display 512 bit length target
//		byte[] tmp = Util.bigIntegerTo64Bytes(target);
//		System.out.print("128 bit bytes' len: " + tmp.length + "\n");

//		System.out.println(Util.bytesToBigInteger(tmp).toString(16));
//		System.out.println(Util.bigIntegerTo128String(Util.getDefaultTargetBytes()));

	}

	private static void caluateTarget() {

		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
		long time0 = System.currentTimeMillis();
		long time1;
		long lCount = 0;
		long Totaltime = 0;
		long i = 10000;
		while (true) {
			BigInteger hash = new BigInteger(1, Util.dualSHA3_512(Util.multipleExtend(("" + i++).getBytes(), 100)));
//        	logger.info(tmp.toString(16));

			if (hash.compareTo(target) == -1) {
				time1 = System.currentTimeMillis();
				System.out.println("i: " + lCount + " Find use: " + (time1 - time0) + " ms\n");
				Totaltime += (time1 - time0);
				time0 = time1;
				++lCount;
				if (lCount == 100) {
					break;
				}
				System.out.println(hash.toString(2));
				System.out.println(" len: " + hash.toString(2).length() + " i: " + i);
				System.out.println(" len: " + hash.toString(2).length() + " i: " + (i - 1) + "\n" + Base64.getEncoder()
						.encodeToString(Util.dualSHA3_512(Util.multipleExtend(("" + (i - 1)).getBytes(), 1))));
				if (lCount % 10 == 0) {
					System.out.println("Old target: " + target.toString(16) + "averge time: " + Totaltime / 10);
					target = target.multiply(BigInteger.valueOf(Totaltime)).divide(BigInteger.valueOf(100000));
					Totaltime = 0;
					System.out.println("New target: " + target.toString(16));
				}
			}
		}
		System.out.println("averge time: " + Totaltime / lCount + " total time: " + Totaltime + " count:" + lCount);

	}

}
