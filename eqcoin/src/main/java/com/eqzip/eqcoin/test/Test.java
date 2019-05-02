/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
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
package com.eqzip.eqcoin.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Vector;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.MutableColumnFamilyOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.eqzip.eqcoin.blockchain.Account;
import com.eqzip.eqcoin.blockchain.Account.Publickey;
import com.eqzip.eqcoin.blockchain.AccountsMerkleTree;
import com.eqzip.eqcoin.blockchain.transaction.Address;
import com.eqzip.eqcoin.blockchain.transaction.Transaction;
import com.eqzip.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqzip.eqcoin.blockchain.transaction.TxIn;
import com.eqzip.eqcoin.blockchain.transaction.TxOut;
import com.eqzip.eqcoin.blockchain.transaction.operation.UpdateAddressOperation;
import com.eqzip.eqcoin.configuration.Configuration;
import com.eqzip.eqcoin.blockchain.transaction.Address.AddressShape;
import com.eqzip.eqcoin.blockchain.transaction.OperationTransaction;
import com.eqzip.eqcoin.blockchain.transaction.Transaction.TXFEE_RATE;
import com.eqzip.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqzip.eqcoin.blockchain.EQCBlock;
import com.eqzip.eqcoin.blockchain.EQCBlockChain;
import com.eqzip.eqcoin.blockchain.EQCHeader;
import com.eqzip.eqcoin.blockchain.Transactions;
import com.eqzip.eqcoin.blockchain.AccountsMerkleTree.Filter;
import com.eqzip.eqcoin.crypto.EQCPublicKey;
import com.eqzip.eqcoin.keystore.Keystore;
import com.eqzip.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqzip.eqcoin.keystore.UserAccount;
import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
import com.eqzip.eqcoin.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqzip.eqcoin.persistence.rocksdb.EQCBlockChainRocksDB.TABLE;
import com.eqzip.eqcoin.serialization.EQCType;
import com.eqzip.eqcoin.util.Base58;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.ID;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.AddressTool;
import com.eqzip.eqcoin.util.Util.AddressTool.AddressType;


/**
 * @author Xun Wang
 * @date Oct 15, 2018
 * @email 10509759@qq.com
 */
public class Test {
	public static void testSignTransaction() {
		TransferTransaction transaction = new TransferTransaction();
		TxIn txIn = new TxIn();
		Address address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		address.setID(new ID(BigInteger.ZERO));
		txIn.setAddress(address);
		txIn.setValue(25*Util.ABC);
		transaction.setTxIn(txIn);
		address = new Address();
		address.setReadableAddress("abc");
		address.setID(new ID(BigInteger.TWO));
		TxOut txOut = new TxOut();
		txOut.setAddress(address);
		txOut.setValue(24*Util.ABC);
		transaction.addTxOut(txOut);
		transaction.setNonce(ID.ONE);
		
		byte[] privateKey = Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPrivateKey(), "abc");
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPublicKey(), "abc");
		byte[] sign = Util.signTransaction(transaction.getTxIn().getAddress().getType(), privateKey, transaction, new byte[4]);
		transaction.setSignature(sign);
		com.eqzip.eqcoin.blockchain.PublicKey publicKey2 = new com.eqzip.eqcoin.blockchain.PublicKey();
		publicKey2.setPublicKey(publickey);
		transaction.setPublickey(publicKey2);
		boolean result = Util.verifySignature(transaction.getTxIn().getAddress().getType(), transaction, new byte[4]);
		if(result) {
			Log.info("verify passed");
		}
		else {
			Log.info("verify failed");
		}
	}
	
	public static void testHashTime() {
		EQCHeader header = new EQCHeader();
		header.setNonce(ID.ONE);
		header.setPreHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, false));
		header.setTarget(Util.getDefaultTargetBytes());
		header.setRootHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), Util.ONE, false));
		header.setHeight(ID.ZERO);
		header.setTimestamp(new ID(System.currentTimeMillis()));
		Log.info(header.toString());
		long c0 = System.currentTimeMillis();
		int n = 10;
		for (int i = 0; i < n; ++i) {
			Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(header.getBytes(), Util.HUNDREDPULS, false);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1-c0) + " average time:" + (double)(c1-c0)/n);
	}
	
	public static void testMultiExtendTime() {
//		EQCHeader header = new EQCHeader();
//		header.setNonce(ID.ONE);
//		header.setPreHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, true));
//		header.setTimestamp(new ID(System.currentTimeMillis()));
//		Log.info(header.toString());
		long c0 = System.currentTimeMillis();
		int n = 100;
		for (int i = 0; i < n; ++i) {
			Util.multipleExtend(Util.getSecureRandomBytes(), Util.MILLIAN);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1-c0) + "average time:" + (double)(c1-c0)/n);
	}

	public static void testECPubKeySignature(ECCTYPE type, String text) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == ECCTYPE.P521) {
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
	
	public static void testEC(ECCTYPE type) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			if (pubKey instanceof ECPublicKey) {
				Log.info("ECPublicKey, len: " + pubKey.getEncoded().length);
			} else {
				Log.info("Not ECPublicKey");
			}
			Log.info("getAlgorithm: " + pubKey.getAlgorithm() + " getFormat: " + pubKey.getFormat());

			Log.info(privKey.toString());
			Log.info(pubKey.toString());
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ECPrivateKey ecPrivateKey = (ECPrivateKey) privKey;
			ecdsa.initSign(Util.getPrivateKey(ecPrivateKey.getS().toByteArray(), AddressType.T1));//privKey);
			String text = "In teaching others we teach ourselves";
			System.out.println("Text: " + text);
			byte[] baText = text.getBytes("UTF-8");
//			ecdsa.update(Util.EQCCHA_MULTIPLE(Util.getDefaultTargetBytes(), 1, true));
			ecdsa.update(baText);
			byte[] baSignature = ecdsa.sign();
			System.out.println("Signature: 0x" + (new BigInteger(1, baSignature).toString(16)).toUpperCase() + "\n Len: " + baSignature.length);
			Signature signature;
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
//			pubKey = new sun.security.ec.ECPublicKeyImpl(baSignature);
			EQCPublicKey eqPublicKey = new EQCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			byte[] compressedPubkey = eqPublicKey.getCompressedPublicKeyEncoded();
			Log.info("compressedPubkey: " + Util.dumpBytes(compressedPubkey, 10) + " len: " + compressedPubkey.length);// (compressedPubkey));
			eqPublicKey = new EQCPublicKey(type);
			eqPublicKey.setECPoint(compressedPubkey);
//			eqPublicKey.setECPoint(pubKey.getEncoded());
			Log.info(Util.dumpBytesBigEndianBinary(pubKey.getEncoded()));
			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			ECPublicKey abc = (ECPublicKey) pubKey;
			Log.info("getAlgorithm: " + abc.getAlgorithm() + " getFormat: " + abc.getFormat());
//			abc.getW().getAffineX()
			signature.initVerify(eqPublicKey);
//			signature.update(Util.EQCCHA_MULTIPLE(Util.getDefaultTargetBytes(), 1, true));
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

	public static void test() {
//		Account acc = new Account();
//    	acc.setAddress("abc");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(new byte[64]);
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("abcd");
//    	Keystore.getInstance().(acc);
//    	Account acc1 = new Account();
//    	acc1.setAddress("abc");
//    	acc1.setBalance(1000000000);
//    	acc1.setPrivateKey(new byte[64]);
//    	acc1.setPwdHash(Util.getSecureRandomBytes());
//    	acc1.setUserName("abcd");
//    	if(acc.equals(acc1)) {
//    		Log.info("equal");
//    	}
//    	Keystore.getInstance().(acc1);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("a");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("a");
//    	Keystore.getInstance().(acc);
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

	public static void testKeystore() {
		UserAccount account;
		for(int i=0; i<10; ++i) {
			account = Keystore.getInstance().createUserAccount("nju2006", "abc", ECCTYPE.P521);
//			if(account.getAddress().length() > 51 || account.getAddress().length() < 49) {
				Log.info(account.getReadableAddress() + " len: " + account.getReadableAddress().length());
//			}
		}
		Log.info("end");
	}
	
	public static void testH2Account() {
		Address address = new Address(Keystore.getInstance().getUserAccounts().get(2).getReadableAddress());
		EQCBlockChainH2.getInstance().isAddressExists(address);
	}
	
	public static void testAIToAddress() {
		Log.info(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		Log.info(Util.AddressTool.AIToAddress(Util.AddressTool.addressToAI(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress())));
	}
	
	public static void testUserAccount() {
		UserAccount account;
		account = Keystore.getInstance().createUserAccount("nju2006", "abc", ECCTYPE.P521);
		Log.info(account.toString());
//		Log.info(Keystore.getInstance().getUserAccounts().get(0).toString());
	}
	
	public static void testBytesToBIN() {
		byte[] bytes = EQCType.bytesToBIN(new byte[16]);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		try {
			byte[] bytes1 = EQCType.parseBIN(is);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testBase58() {
		byte[] address = new byte[18];
		for (int i = 0; i < 1; ++i) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(0);
			ByteArrayInputStream is = new ByteArrayInputStream(address);
			Log.info("os0:\n" + Util.dumpBytesBigEndianHex(os.toByteArray()));
			StringBuilder sb = new StringBuilder();
//    	sb.append("00");
			BigInteger pubKeyHash = new BigInteger(1,
					Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDREDPULS, true));

			Log.info("pubKeyHash:\n" + Util.dumpBytesBigEndianHex(pubKeyHash.toByteArray()));
			try {
				os.write(pubKeyHash.toByteArray());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(pubKeyHash.toString(16));
//			sb.append(CRC8ITU.update(os.toByteArray()));
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

//	public static void testCRC8ITU() {
//		byte[] bytes = "123456789".getBytes();
//		short b = CRC8ITU.update(bytes);
//		Log.info(Integer.toHexString(CRC8ITU.update(bytes) & 0xff));
//	}

	public static void testRIPEMD() {
		Log.info(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16));
		Log.info(Util.dumpBytes(Util.RIPEMD128("abc".getBytes()), 16));
//    	assertNotNull(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16), "userAttribute");
	}

	public static void testBigIntegerToBits() {

		// 127 = ‭01111111‬
		Log.info(Util.dumpBytes(Util.longToBytes(127l), 16) + "\n" + Util.dumpBytes(EQCType.longToEQCBits(127l), 16) + "\n"
				+ Util.dumpBytes(EQCType.eqcBitsToBigInteger(EQCType.longToEQCBits(127l)).toByteArray(), 16));
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

	public static void testSN() {
		ID addressSN = new ID(BigInteger.ZERO);
		Vector<ID> vec = new Vector<ID>();
		vec.add(addressSN);
		for (int i = 1; i < 1000; ++i) {
			vec.add(vec.get(i - 1).getNextID());
			if (vec.get(i).isNextID(vec.get(i - 1)))
				Log.info("isNextSN：" + " current: " + vec.get(i).longValue() + " previous:"
						+ vec.get(i - 1).longValue() + " bits: "
						+ Util.dumpBytes(vec.get(i).getEQCBits(), 2));
		}
	}

//	public static void testBlockchain() {
//		System.out.println("testBlockchain");
//		Log.info("Default target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger((Util.getDefaultTargetBytes()))));
//		
//		// 0.avro
//		EQCBlock eqcBlock = new EQCBlock();
//		
//		EQCHeader header = new EQCHeader();
//		header.setNonce(1);
//		header.setPreHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header.setTimestamp(System.currentTimeMillis());
////    	System.out.println(header.toString());
//		
//		TransactionsHeader transactionsHeader = new TransactionsHeader();
//		eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
//		Transaction transaction = new  Transaction();
//		Address address = new Address();
//		address.setAddress(Keystore.getInstance().getUserAccounts().get(0).getAddress());
//		address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//		TxOut txOut = new TxOut();
//		txOut.setAddress(address);
//		txOut.setValue(25*Util.ABC);
//		transaction.addTxOut(txOut);
//		eqcBlock.addTransaction(transaction);
//		
//		header.setRootHash(eqcBlock.getTransactionsHash());
//		eqcBlock.setEqcHeader(header);
//		
//		EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
//		
//		Vector<EQCBlock> vec = new Vector<EQCBlock>();
//		vec.add(eqcBlock);
//		
//		// 1.avro
//		EQCBlock eqcBlock1 = new EQCBlock();
//		
//		EQCHeader header1 = new EQCHeader();
//		header1.setNonce(1);
//		header1.setPreHash(vec.get(0).getEqcHeader().getEqcHeaderHash());
//		header1.setTarget(Util.getDefaultTargetBytes());
////		header1.setTxHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header1.setTimestamp(System.currentTimeMillis());
////    	System.out.println(header.toString());
//		
//		TransactionsHeader transactionsHeader1 = new TransactionsHeader();
//		transactionsHeader1.setHeight(SerialNumber.ZERO);
//		eqcBlock1.getTransactions().setTransactionsHeader(transactionsHeader1);
//		Transaction transaction1 = new  Transaction();
//		Address address1 = new Address();
//		address1.setAddress(Keystore.getInstance().getUserAccounts().get(0).getAddress());
//		address1.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//		TxOut txOut1 = new TxOut();
//		txOut1.setAddress(address1);
//		txOut1.setValue(25*Util.ABC);
//		transaction1.addTxOut(txOut1);
//		eqcBlock1.addTransaction(transaction1);
//		
//		header1.setTxHash(eqcBlock1.getTransactionsHash());
//		eqcBlock1.setEqcHeader(header1);
//		
////		EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock1);
//		vec.add(eqcBlock1);
//		
//		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
//		long time0 = System.currentTimeMillis();
//		long time1;
//		int lCount = 1;
//		long Totaltime = 0;
//		long i = 0;
//		byte[] bytes;
//		while (true) {
//			BigInteger hash = new BigInteger(1, Util
//					.EQCCHA_MULTIPLE((bytes = Util.updateNonce(vec.get(lCount).getEqcHeader().getBytes(), ++i)), Util.MILLIAN, true));// Util.dualSHA3_512(Util.multipleExtend((bytes=Util.updateNonce(vec.get(lCount).getBytes(),
//																														// ++i)),
//																														// 100)));
////        	System.out.println("hash: " + Util.bigIntegerTo128String(hash));
//			if (hash.compareTo(target) <= 0) {
////        		time1 = System.currentTimeMillis();
//				Log.info("EQC Block No." + lCount + " Find use: "
//						+ (System.currentTimeMillis() - vec.get(lCount).getEqcHeader().getTimestamp()) + " ms, details:");
//				// Update the relevant EQCHeader to the right one
//				//vec.set(lCount, new EQCHeader(bytes));
//				vec.get(lCount).setEqcHeader(new EQCHeader(bytes));
//				Log.info(vec.get(lCount).getEqcHeader().toString());
//				
//				EQCBlockChainH2.getInstance().saveEQCBlock(vec.get(lCount));
////				if(lCount == 0) {
////					EQCBlock eqcBlock = new EQCBlock();
////					eqcBlock.setEqcHeader(vec.get(lCount));
////					TransactionsHeader transactionsHeader = new TransactionsHeader();
////					transactionsHeader.setHeight(BigInteger.ZERO);
////					eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
////					Transaction transaction = new  Transaction();
////					Address address = new Address();
////					address.setAddress(Keystore.getInstance().getUserAccounts().get(0).getAddress());
////					address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
////					TxOut txOut = new TxOut();
////					txOut.setAddress(address);
////					txOut.setValue(25*Util.ABC);
////					transaction.addTxOut(txOut);
////					eqcBlock.addTransaction(transaction);
////					EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
////				}
////				else {
//					
//					
////				}
//				
//				header = new EQCHeader();
//				header.setNonce(0);
//				header.setPreHash(vec.get(lCount).getEqcHeader().getEqcHeaderHash());
//				
//				eqcBlock = new EQCBlock();
//				
//				transactionsHeader = new TransactionsHeader();
//				transactionsHeader.setHeight(new SerialNumber(vec.get(lCount).getTransactions().getTransactionsHeader().getHeight().getSerialNumber().add(BigInteger.ONE)));
//				eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
//				transaction = new  Transaction();
//				address = new Address();
//				address.setAddress(Keystore.getInstance().getUserAccounts().get(0).getAddress());
//				address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//				txOut = new TxOut();
//				txOut.setAddress(address);
//				txOut.setValue(25*Util.ABC);
//				transaction.addTxOut(txOut);
//				eqcBlock.addTransaction(transaction);
//				
//				++lCount;
//				if (lCount % 10 != 0) {
//					header.setTarget(vec.get(lCount - 1).getEqcHeader().getTarget());
//				} else {
//					Log.info("Old target: "
//							+ Util.bigIntegerTo128String(Util.targetBytesToBigInteger(vec.get(lCount - 1).getEqcHeader().getTarget()))
//							+ "\r\naverge time: "
//							+ (vec.get(lCount - 1).getEqcHeader().getTimestamp() - vec.get(lCount - 10).getEqcHeader().getTimestamp()) / 10);
//					target = target
//							.multiply(BigInteger.valueOf(
//									(vec.get(lCount - 1).getEqcHeader().getTimestamp() - vec.get(lCount - 10).getEqcHeader().getTimestamp())))
//							.divide(BigInteger.valueOf(90000));
//					if(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(target) >= 0)
//					{
//						Log.info("New target: " + Util.bigIntegerTo128String(target));
//						header.setTarget(Util.bigIntegerToTargetBytes(target));
//					}
//					else {
//						Log.info("New target: " + Util.bigIntegerTo128String(target) + " but due to it's less than the default target so still use default target.");
//						header.setTarget(Util.getDefaultTargetBytes());
//					}
//				}
//				header.setTxHash(eqcBlock.getTransactionsHash());
//				header.setTimestamp(System.currentTimeMillis());
//				eqcBlock.setEqcHeader(header);
//				vec.add(eqcBlock);
//				i = 0;
//				if (lCount == 2000) {
//					break;
//				}
////        		System.out.println(hash.toString(2));
////        		System.out.println(" len: " + hash.toString(2).length() + " i: " +i);
////        		System.out.println(" len: " + hash.toString(2).length() + " i: " + (i-1) + "\n" + Base64.getEncoder().encodeToString(Util.dualSHA3_512(Util.multipleExtend((""+(i-1)).getBytes(), 1))));
////        		if(lCount%10 == 0) {
////        			System.out.println("Old target: " + target.toString(16) + "averge time: " + Totaltime/10);
////        			target = target.multiply(BigInteger.valueOf(Totaltime)).divide(BigInteger.valueOf(100000));
//////        			Totaltime = 0;
////        			System.out.println("New target: " + target.toString(16));
////        		}
//			}
//		}
//		Log.info("averge time: " + (vec.get(vec.size() - 1).getEqcHeader().getTimestamp() - vec.get(0).getEqcHeader().getTimestamp()) / lCount
//				+ " total time: " + (vec.get(vec.size() - 1).getEqcHeader().getTimestamp() - vec.get(0).getEqcHeader().getTimestamp()) + " count:"
//				+ lCount);
//	}

	public static void testLongToBytes() {
		byte[] foo = Util.longToBytes(Long.MAX_VALUE);
		long lValue = Util.bytesToLong(foo);
		System.out.println("lValue: " + lValue);
	}

	public static void testTarget() {

//		BigInteger a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16))
//				.multiply(BigInteger.valueOf(2).pow(512 - 60 - 17));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3))
//				.add(BigInteger.valueOf(Long.parseLong("21", 16)));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
//				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(424);// .multiply(BigInteger.valueOf(2).pow(3)).add(BigInteger.valueOf(Long.parseLong("21",
//																					// 16)));
//		System.out.println(a.shiftRight(512 - a.bitLength()).toString(16) + " len: " + a.bitLength());
		
		Log.info(Util.dumpBytes(Util.getDefaultTargetBytes(), 16));
		Log.info(Util.bigIntegerTo512String(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes())));

//		Util.bigIntegerToTargetBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()));
		
		Util.bigIntegerToTargetBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).divide(BigInteger.valueOf(10)));
		
	}

	public static void testSignBigIntegerPadingZero() {

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

	public static void testTargetToBytes() {

		// Display default target
		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
		System.out.println("Default target's length: " + Util.getDefaultTargetBytes().length);
//		// Display 512 bit length target
//		byte[] tmp = Util.bigIntegerTo64Bytes(target);
//		System.out.print("128 bit bytes' len: " + tmp.length + "\n");

//		System.out.println(Util.bytesToBigInteger(tmp).toString(16));
//		System.out.println(Util.bigIntegerTo128String(Util.getDefaultTargetBytes()));

	}

	public static void caluateTarget() {

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
	
	public static void testEQCBlock() {
		for (int i = 0; i < 2; ++i) {
			EQCBlock eqcBlock = EQCBlockChainH2.getInstance().getEQCBlock(new ID(BigInteger.valueOf(i)),
					true);
			Log.info(eqcBlock.toString());
		}
//		eqcBlock = EQCBlockChainH2.getInstance().getEQCBlock(new SerialNumber(BigInteger.valueOf(1)), true);
//		Log.info(eqcBlock.toString());
	}
	
	public static void testToString() {
		TxIn txIn = new TxIn();
		Address address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		address.setID(new ID(BigInteger.ZERO));
		txIn.setAddress(address);
		txIn.setValue(25 * Util.ABC);
		Log.info(txIn.toString());
		Log.info(address.toString());
		// Create Transaction
		TransferTransaction transaction = new TransferTransaction();
		TxOut txOut = new TxOut();
		txOut.setAddress(address);
		txOut.setValue(25 * Util.ABC);
		transaction.setTxIn(txIn);
		transaction.addTxOut(txOut);
		Log.info(transaction.toString());
		Transactions transactions = new Transactions();
		transactions.addTransaction(transaction);
		transactions.addTransaction(transaction);
		transactions.addTransaction(transaction);
		Log.info(transactions.toString());
		EQCBlock eqcBlock = Util.gestationSingularityBlock();
		Log.info(eqcBlock.toString());
		eqcBlock.setTransactions(transactions);
		Log.info(eqcBlock.toString());
	}
	
	public static void testSpendCoinBase() {
		try {
		TransferTransaction transaction;
		TxIn txIn;
		TxOut txOut;
		Address address;
		transaction = new TransferTransaction();
		txIn = new TxIn();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
//		address.setSerialNumber(serialNumber);
		txIn.setAddress(address);
		
		txOut = new TxOut();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(1).getReadableAddress());
//		address.setSerialNumber((serialNumber = serialNumber.getNextSerialNumber()));
		txOut.setAddress(address);
		txOut.setValue(24*Util.ABC);
		transaction.setTxIn(txIn);
		transaction.addTxOut(txOut);
		
		byte[] privateKey = Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPrivateKey(), "abc");
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPublicKey(), "abc");
		
		com.eqzip.eqcoin.blockchain.PublicKey publicKey1 = new com.eqzip.eqcoin.blockchain.PublicKey();
		publicKey1.setPublicKey(publickey);
		transaction.setPublickey(publicKey1);
		publicKey1.setID(ID.ZERO);
//		EQCBlockChainH2.getInstance().appendPublicKey(publicKey1, SerialNumber.ZERO);
		
		// TxFee
		transaction.setTxFeeLimit(Transaction.TXFEE_RATE.POSTPONE0);
		Log.info("TxIn value:" + txIn.getValue());
		Log.info("TxFeeLimit: " + transaction.getTxFeeLimit());
		
		if(TransferTransaction.isValid(transaction.getBytes(Address.AddressShape.READABLE), AddressShape.READABLE)) {
			Log.info("Right format");
		}
		else {
			Log.info("Bad format");
		}
//		Log.info(transaction.toString());
//		byte[] bytes = transaction.getBytes(AddressShape.ADDRESS);
//		Transaction transaction1 = new Transaction(transaction.getBytes(AddressShape.ADDRESS), AddressShape.ADDRESS);
//		Log.info(transaction1.toString());
		
	
		
		byte[] sign = Util.signTransaction(transaction.getTxIn().getAddress().getType(), privateKey, transaction, new byte[4]);
		transaction.setSignature(sign);
		
		if(Util.verifySignature(transaction.getTxIn().getAddress().getType(), transaction, new byte[4])) {
			Log.info("Passed");
		}
		Log.info(transaction.toString());
		EQCBlockChainH2.getInstance().addTransactionInPool(transaction);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
//		Vector<Transaction> transactions = EQCBlockChainH2.getInstance().getTransactionListInPool();
//		
//		try {
//			if(transactions.get(0).isValid(10000000)) {
//				Log.info("Passed2");
//			}
//			else {
//				Log.info("Failed");
//			}
//		} catch (NoSuchFieldException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void testMultiTransaction() {
		// Create Transaction
		Transactions transactions;
		TransferTransaction transaction;
		TxIn txIn;
		TxOut txOut;
		Address address;
		ID serialNumber = new ID(BigInteger.ZERO);
		transactions = new Transactions();
		
		transaction = new TransferTransaction();
		txIn = new TxIn();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		address.setID(serialNumber);
		txIn.setAddress(address);
		txIn.setValue(25*Util.ABC);
		txOut = new TxOut();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(1).getReadableAddress());
		address.setID((serialNumber = serialNumber.getNextID()));
		txOut.setAddress(address);
		txOut.setValue(24*Util.ABC);
		transaction.setTxIn(txIn);
		transaction.addTxOut(txOut);
		transactions.addTransaction(transaction);
		
		transaction = new TransferTransaction();
		txIn = new TxIn();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(2).getReadableAddress());
		address.setID((serialNumber = serialNumber.getNextID()));
		txIn.setAddress(address);
		txIn.setValue(25*Util.ABC);
		txOut = new TxOut();
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(3).getReadableAddress());
//		Log.info("a:" + address.getAddress());
		address.setID((serialNumber = serialNumber.getNextID()));
		txOut.setAddress(address);
		txOut.setValue(12*Util.ABC);
		transaction.setTxIn(txIn);
		transaction.addTxOut(txOut);
		
		// Add new TxOut
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(4).getReadableAddress());
//		Log.info("b:" + address.getAddress());
		address.setID((serialNumber = serialNumber.getNextID()));
		txOut = new TxOut();
		txOut.setAddress(address);
		txOut.setValue(12*Util.ABC);
		transaction.addTxOut(txOut);
		
		// Add new TxOut
		address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(5).getReadableAddress());
//				Log.info("b:" + address.getAddress());
		address.setID((serialNumber = serialNumber.getNextID()));
		txOut = new TxOut();
		txOut.setAddress(address);
		txOut.setValue((long)(0.9 * Util.ABC));
		transaction.addTxOut(txOut);
				
//		Log.info("txout number: " + transaction.getTxOutNumber());
		transactions.addTransaction(transaction);
		
		Log.info(transactions.toString());
		
	}
	
	public static void testVerifyPublicKey(ECCTYPE type) {
			KeyPairGenerator kpg;
				try {
					kpg = KeyPairGenerator.getInstance("EC", "SunEC");
					ECGenParameterSpec ecsp = null;
					if (type == ECCTYPE.P256) {
						ecsp = new ECGenParameterSpec("secp256r1");
					} else if (type == ECCTYPE.P521) {
						ecsp = new ECGenParameterSpec("secp521r1");
					}
					kpg.initialize(ecsp);
					KeyPair kp = kpg.genKeyPair();
					PrivateKey privKey = kp.getPrivate();
					PublicKey pubKey = kp.getPublic();
					ECPublicKey ecPublicKey = (ECPublicKey) pubKey;
					Log.info(Util.dumpBytes(ecPublicKey.getEncoded(), 16));
					EQCPublicKey eqPublicKey = new EQCPublicKey(type);
					// Create EQPublicKey according to java pubkey
					eqPublicKey.setECPoint((ECPublicKey) pubKey);
					eqPublicKey.setECPoint(eqPublicKey.getCompressedPublicKeyEncoded());
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchProviderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	public static void testValue() {
		String address = "1";
		if(Util.isTXValueValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "1.1";
		if(Util.isTXValueValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "MwG";
		if(Util.isTXValueValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "1.1110";
		if(Util.isTXValueValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
	}
	
	public static void testAddressFormat() {
		String address = "1w6WJRsMFEcGVEqXMwGmLHWW";
		if(Util.isAddressFormatValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "4w6WJRsMFEcGVEqXMwGmLHWW";
		if(Util.isAddressFormatValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "1w6WJRsMFEcGVEqXMwG";
		if(Util.isAddressFormatValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
		address = "1w6WJRsMFEcGVEq0XMwGmLHW";
		if(Util.isAddressFormatValid(address)) {
			Log.info(address + " Passed.");
		}
		else {
			Log.info(address + " Failed.");
		}
	}
	
	public static void testP2SH() {
//		P2SHAddress pAddress = new P2SHAddress();
//		Peer peer1 = new Peer(), peer2 = new Peer(), peer3 = new Peer();
//		peer1.setPeerSN(1);
//		peer1.setTimestamp(Time.UTC(2019, 2, 6, 10, 0, 0));
//		peer1.addAddress(Keystore.getInstance().getUserAccounts().get(1).getAddress(), 
//				Keystore.getInstance().getUserAccounts().get(2).getAddress(),
//				Keystore.getInstance().getUserAccounts().get(3).getAddress());
//		
//		peer2.setPeerSN(2);
//		peer2.setTimestamp(Time.UTC(2029, 2, 2, 24, 0, 0));
//		peer2.addAddress(Keystore.getInstance().getUserAccounts().get(4).getAddress(), 
//				Keystore.getInstance().getUserAccounts().get(5).getAddress(),
//				Keystore.getInstance().getUserAccounts().get(6).getAddress());
//		
//		peer3.setPeerSN(3);
//		peer3.setTimestamp(Time.UTC(2029, 2, 2, 24, 0, 0));
//		peer3.addAddress(Keystore.getInstance().getUserAccounts().get(7).getAddress(), 
//				Keystore.getInstance().getUserAccounts().get(8).getAddress(),
//				Keystore.getInstance().getUserAccounts().get(9).getAddress());
//		
//		pAddress.addPeer(peer1, peer2, peer3);
//		pAddress.generate();
//		Log.info(pAddress.getAddress());
//		Log.info("Code len: " + pAddress.getCode().length);
//		
//		Address address = new Address();
//		address.setAddress(pAddress.getAddress());
//		address.setSerialNumber(SerialNumber.ZERO);
//		address.setCode(pAddress.getCode());
//		
//		TransferTransaction transaction = new TransferTransaction();
//		TxIn txIn = new TxIn();
//		txIn.setAddress(address);
//		transaction.setTxIn(txIn);
//		transaction.setNonce(BigInteger.valueOf(1l));
//		TxOut txOut = new TxOut();
//		address = new Address();
//		address.setAddress(Keystore.getInstance().getUserAccounts().get(1).getAddress());
//		txOut.setAddress(address);
//		txOut.setValue(50 * Util.ABC);
//		transaction.addTxOut(txOut);
//		
//		int sn = 1;
//		PeerPublickeys peerPublickeys2 = new PeerPublickeys();
//		peerPublickeys2.setPublickeySN(sn);
//		peerPublickeys2.addPublickey(Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+1).getPublicKey(), "abc"),
//				Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+2).getPublicKey(), "abc"),
//				Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+3).getPublicKey(), "abc"));
//		com.eqzip.eqcoin.blockchain.PublicKey publicKey = new com.eqzip.eqcoin.blockchain.PublicKey();
//		publicKey.setPublicKey(peerPublickeys2.getBytes());
//		transaction.setPublickey(publicKey);
//		
//		transaction.setTxFeeLimit(TXFEE_RATE.POSTPONE0);
//		
//		PeerSignatures peerSignatures2 = new PeerSignatures();
//		peerSignatures2.setSignatureSN(sn);
//		peerSignatures2.addSignature(Util.signTransaction(Keystore.getInstance().getUserAccounts().get((sn-1)*3+1).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+1).getPrivateKey(), "abc"), transaction, new byte[16], sn),
//				Util.signTransaction(Keystore.getInstance().getUserAccounts().get((sn-1)*3+2).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+2).getPrivateKey(), "abc"), transaction, new byte[16], sn),
//				Util.signTransaction(Keystore.getInstance().getUserAccounts().get((sn-1)*3+3).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get((sn-1)*3+3).getPrivateKey(), "abc"), transaction, new byte[16], sn)
//				);
//		transaction.setSignature(peerSignatures2.getBytes());
//		
//		if(transaction.verifyPublickey()) {
//			Log.info("verifyPublickey passed");
//		}
//		else {
//			Log.info("verifyPublickey failed");
//		}
//		
//		if(transaction.verifySignature()) {
//			Log.info("verifySignature passed");
//		}
//		else {
//			Log.info("verifySignature failed");
//		}
//		
//		if(transaction.verify()) {
//			Log.info("verify passed");
//		}
//		else {
//			Log.info("verify failed");
//		}
//		
	}
	
	public static void testMinAndMaxAddress() {
		byte[] bytes = new byte[32];
		bytes[0] = 1;
		bytes[31] = 1;
		Log.info("32 bytes 0: " + Base58.encode(bytes));
		Log.info(Util.AddressTool.generateAddress(bytes, AddressType.T1) + " len: " + Util.AddressTool.generateAddress(bytes, AddressType.T1).length());
		for(int i=0; i<bytes.length; ++i) {
			bytes[i] = (byte) 0xff;
		}
		Log.info("32 bytes ff: " + Base58.encode(bytes));
		Log.info(Util.AddressTool.generateAddress(bytes, AddressType.T1) + " len: " + Util.AddressTool.generateAddress(bytes, AddressType.T1).length());
	}
	
	public static void testDisplayKeystore() {
		Vector<UserAccount> userAccounts = Keystore.getInstance().getUserAccounts();
		for(UserAccount userAccount : userAccounts) {
			Log.info(userAccount.toString());
		}
	}
	
	public static void testInterface() {
		EQCBlockChain eqcBlockChain = EQCBlockChainH2.getInstance();
	}
	
	public static void testRocksDB() {
		try {
			System.gc();
			byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, false);
			long begin = System.currentTimeMillis();
			Log.info("" + begin);
			for(int i=0; i<10000000; ++i) {
				EQCBlockChainRocksDB.put(TABLE.ACCOUNT, new ID(BigInteger.valueOf(i)).getEQCBits(), bytes);
			}
			long end = System.currentTimeMillis();
			Log.info("Total put time: " + (end - begin) + " ms");
			begin = System.currentTimeMillis();
			Log.info("" + Util.dumpBytes(EQCBlockChainRocksDB.get(TABLE.ACCOUNT, ID.ZERO.getEQCBits()), 16));
			Log.info("" + begin);
			for(int i=0; i<10000000; ++i) {
				EQCBlockChainRocksDB.get(TABLE.ACCOUNT, new ID(BigInteger.valueOf(i)).getEQCBits());
			}
			end = System.currentTimeMillis();
			Log.info("Total get time: " + (end - begin) + " ms");
			EQCBlockChainRocksDB.getInstance().close();
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testRocksDB1() {
			System.gc();
			final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(false);
			org.rocksdb.Options options = new Options().setCompressionType(CompressionType.NO_COMPRESSION).setCreateIfMissing(true);
			ColumnFamilyHandle columnFamilyHandle;
			try {
				RocksDB.destroyDB(Util.ROCKSDB_PATH, new Options());
				RocksDB rocksDB = RocksDB.open(options, Util.ROCKSDB_PATH);
				final ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions().setCompressionType(CompressionType.NO_COMPRESSION);
				final List<ColumnFamilyDescriptor> columnFamilyDescriptors = Arrays.asList(
				        new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyOptions),
				        new ColumnFamilyDescriptor(EQCBlockChainRocksDB.EQCBLOCK_TABLE, columnFamilyOptions),
				        new ColumnFamilyDescriptor(EQCBlockChainRocksDB.ACCOUNT_TABLE, columnFamilyOptions),
				        new ColumnFamilyDescriptor(EQCBlockChainRocksDB.ACCOUNT_MINERING_TABLE, columnFamilyOptions),
				        new ColumnFamilyDescriptor(EQCBlockChainRocksDB.MISC_TABLE, columnFamilyOptions)
				    );
				
				columnFamilyHandle = rocksDB.createColumnFamily(columnFamilyDescriptors.get(1));
				rocksDB.setOptions(columnFamilyHandle, MutableColumnFamilyOptions.builder().setCompressionType(CompressionType.NO_COMPRESSION).build());

				byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, false);
				long begin = System.currentTimeMillis();
				Log.info("" + begin);
				for(int i=0; i<10000000; ++i) {
					rocksDB.put(columnFamilyHandle, new ID(BigInteger.valueOf(i)).getEQCBits(), bytes);
					
				}
				long end = System.currentTimeMillis();
				Log.info("Total put time: " + (end - begin) + " ms");
				begin = System.currentTimeMillis();
//				Log.info("" + Util.dumpBytes(rocksDB.get(columnFamilyHandles.get(1), SerialNumber.ZERO.getEQCBits()), 16));
				Log.info("" + begin);
				for(int i=0; i<10000000; ++i) {
					rocksDB.get(columnFamilyHandle, new ID(BigInteger.valueOf(i)).getEQCBits());
				}
				end = System.currentTimeMillis();
				rocksDB.close();
				Log.info("Total get time: " + (end - begin) + " ms");
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void destoryRocksDB() {
		try {
			RocksDB.destroyDB(Util.ROCKSDB_PATH, new Options());
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testTimestamp() {
		Log.info("Current timestamp's length: " + new ID(BigInteger.valueOf(System.currentTimeMillis())).getEQCBits().length);
	}
	
	public static void testNonce() {
		Log.info("268435455 len: " + new ID(268435455).getEQCBits().length);
	}
	
	public static void testRocksDBAccount() {
		Account account = new Account();
		Address address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		address.setID(ID.TWO);
		account.setAddress(address);
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(500000);
		account.setBalanceUpdateHeight(ID.ZERO);
		EQCBlockChainRocksDB.getInstance().saveAccount(account);
		account = EQCBlockChainRocksDB.getInstance().getAccount(ID.TWO);
		Log.info(account.getAddress().toString());
//		RocksIterator rocksIterator = EQCBlockChainRocksDB.getInstance().getRocksDB().newIterator(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT));
//		rocksIterator.seekToFirst();
//		while(rocksIterator.isValid()) {
//			account = null;
//			try {
//				account = new Account(rocksIterator.key());
//			} catch (NoSuchFieldException | IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Log.info(account.getAddress().toString());
//			rocksIterator.next();
//		}
	}
	
	public static void testDisplayAccount() {
		Account account = EQCBlockChainRocksDB.getInstance().getAccount(ID.TWO);
		Log.info(account.getAddress().toString());
		account = EQCBlockChainRocksDB.getInstance().getAccount(new ID(3));
		Log.info(account.getAddress().toString());
	}
	
	public static void testDisplayEQCBlock(ID height) {
		Log.info(EQCBlockChainRocksDB.getInstance().getEQCBlock(height, false).getRoot().toString());
	}
	
	public static void testDisplayAllAccount() {
		BigInteger serialNumber = EQCBlockChainRocksDB.getInstance().getTotalAccountNumbers(EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight());
		for(int i=Util.INIT_ADDRESS_SERIAL_NUMBER; i<serialNumber.longValue()+Util.INIT_ADDRESS_SERIAL_NUMBER; ++i) {
			Log.info(EQCBlockChainRocksDB.getInstance().getAccount(new ID(i)).toString());
		}
	}
	
	public static void testMisc() {
//		TransferTransaction transaction = new TransferTransaction();
//		transaction.getBytes();
//		transaction.getMaxTxFeeLimit();
		Log.info("" + TransactionType.COINBASE);
		Log.info("" + TransactionType.COINBASE.ordinal());
	}
	
	public static void testBigintegerLeadingzero() {
		Log.info(BigInteger.valueOf(-Long.MAX_VALUE).toString(10));
		Log.info("Len: " + BigInteger.valueOf(-Long.MAX_VALUE).toByteArray().length);
		Log.info("Len: " + BigInteger.valueOf(Long.MAX_VALUE).toByteArray().length);
		Log.info("Len: " + BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray().length);
		Log.info("Len: " + new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray()).toByteArray().length);
		Log.info(BigInteger.valueOf(Long.MAX_VALUE).toString(10));
		Log.info(Util.dumpBytes(BigInteger.valueOf(Long.MAX_VALUE).toByteArray(), 16));
		Log.info(BigInteger.valueOf(Long.MAX_VALUE+1).toString(10));
		Log.info(Util.dumpBytes(BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray(), 16));
		byte[] bytes = BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray();
		Log.info(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray()).toString(10));
		Log.info(Util.dumpBytes(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+1).toByteArray()).toByteArray(), 16));
		bytes = new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+123).toByteArray()).toByteArray();
		Log.info(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+2).toByteArray()).toString(10));
		Log.info(Util.dumpBytes(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE+2).toByteArray()).toByteArray(), 16));
		Log.info(new BigInteger("-9223372036854775808", 10).toString(10));
		Log.info(Util.dumpBytes(new BigInteger("9223372036854775808", 10).toByteArray(), 16));
	}
	
	public static void testSingularBlockBytes() {
		Configuration.getInstance().updateIsInitSingularityBlock(false);
		EQCBlock eqcBlock = Util.gestationSingularityBlock();
		Log.info(eqcBlock.toString());
		EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
		EQCBlockChainRocksDB.getInstance().saveEQCBlock(eqcBlock);
		eqcBlock = EQCBlockChainRocksDB.getInstance().getEQCBlock(eqcBlock.getHeight(), false);
		Log.info(eqcBlock.toString());
	}
	
	public static void testVerifyAddress() {
		byte[] privateKey = Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPrivateKey(), "abc");
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPublicKey(), "abc");
		String address = AddressTool.generateAddress(publickey, AddressType.T2);
		Log.info(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		Log.info(address);
		if(AddressTool.verifyAddressPublickey(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress(), publickey)){
			Log.info("Publickey verify passed");
		}
		else {
			Log.info("Publickey verify failed");
		}
		if(AddressTool.verifyAddressCRC32C(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress())) {
			Log.info("crc passed");
		}
		else {
			Log.info("crc failed");
		}
	}
	
	public static void testBase582() {
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(0).getPublicKey(), "abc");
		Log.info(Base58.encode(publickey));
		try {
			if(Arrays.equals(publickey, Base58.decode(Base58.encode(publickey)))) {
				Log.info("passed");
			}
			else {
				Log.info("failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testCF() {
		String data;
		ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor("abc".getBytes());
		try {
			
			ColumnFamilyHandle columnFamilyHandle = EQCBlockChainRocksDB.getRocksDB().createColumnFamily(columnFamilyDescriptor);
//			data = "a";
//			EQCBlockChainRocksDB.getRocksDB().put(columnFamilyHandle, data.getBytes(), data.getBytes());
			data = "b";
			EQCBlockChainRocksDB.getRocksDB().put(columnFamilyHandle, data.getBytes(), data.getBytes());
			
//			Log.info("" + new String(EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandle, data.getBytes())));
//			Log.info("" + new String(EQCBlockChainRocksDB.getRocksDB().get(data.getBytes())));
			EQCBlockChainRocksDB.getRocksDB().dropColumnFamily(columnFamilyHandle);
			Log.info("" + new String(EQCBlockChainRocksDB.getRocksDB().get(columnFamilyHandle, "b".getBytes())));
//			Log.info("" + new String(EQCBlockChainRocksDB.getRocksDB().get(data.getBytes())));
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testCF2() {
		ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor("abc".getBytes());
		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			Log.info("ab");
//			EQCBlockChainRocksDB.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getInstance().close();
//			EQCBlockChainRocksDB.dropTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING).close();
//			Thread.sleep(1000);
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info("abc");
//			EQCBlockChainRocksDB.getInstance().close();
			
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, data.getBytes())));
////			EQCBlockChainRocksDB.dropTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getRocksDB().delete(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING), data.getBytes());
//			
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, data.getBytes())));
			
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void testCF3() {
		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			EQCBlockChainRocksDB.clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, "b".getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void testCF4() {
		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			EQCBlockChainRocksDB.dropTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info(new String(EQCBlockChainRocksDB.get(TABLE.ACCOUNT_MINERING, "b".getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void testTakeSnapshot() {
		Account account = new Account();
		Address address = new Address();
		address.setID(ID.ZERO);
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		account.setAddress(address);
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(150000);
		account.setBalanceUpdateHeight(ID.ZERO);
		EQCBlockChainH2.getInstance().saveAccountSnapshot(account, ID.ZERO);
		
		address.setID(ID.ZERO);
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		account.setAddress(address);
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(150001);
		account.setBalanceUpdateHeight(ID.ONE);
		EQCBlockChainH2.getInstance().saveAccountSnapshot(account, ID.ONE);
		
		address.setID(ID.ZERO);
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
		account.setAddress(address);
		account.setAddressCreateHeight(ID.ZERO);
		account.setBalance(150002);
		account.setBalanceUpdateHeight(ID.TWO);
		EQCBlockChainH2.getInstance().saveAccountSnapshot(account, ID.TWO);
		
		Log.info(EQCBlockChainH2.getInstance().getAccountSnapshot(ID.ZERO, ID.ONE).toString());
	}
	
	public static void testTarget1() {
		if(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(new BigInteger("200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5", 16))>0){
			Log.info("Passed");
		}
	}
	
	public static void testTransaction() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(1);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(2);
		TransferTransaction transaction = new TransferTransaction();
		TxIn txIn = new TxIn();
		txIn.setAddress(new Address(userAccount.getReadableAddress()));
		transaction.setTxIn(txIn);
		TxOut txOut = new TxOut();
		txOut.setAddress(new Address(userAccount1.getReadableAddress()));
		txOut.setValue(50*Util.ABC);
		transaction.addTxOut(txOut);
		transaction.setNonce(EQCBlockChainRocksDB.getInstance().getAccount(txIn.getAddress().getAddressAI()).getNonce().getNextID());
		
		byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
		byte[] publickey =  Util.AESDecrypt(userAccount.getPublicKey(), "abc");
		com.eqzip.eqcoin.blockchain.PublicKey publicKey2 = new com.eqzip.eqcoin.blockchain.PublicKey();
		publicKey2.setPublicKey(publickey);
		transaction.setPublickey(publicKey2);
		transaction.cypherTxInValue(TXFEE_RATE.POSTPONE0);
		Log.info("getMaxBillingSize: " + transaction.getMaxBillingSize());
		Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
		Log.info("getQosRate: " + transaction.getQosRate());
		Log.info("getQos: " + transaction.getQos());
		
		Signature ecdsa = null;
		try {
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, transaction.getTxIn().getAddress().getType()));
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		transaction.sign(ecdsa, EQCBlockChainRocksDB.getInstance().getEQCHeaderHash(EQCBlockChainRocksDB.getInstance().getAccount(userAccount.getAddressAI()).getAddressCreateHeight()), publickey);
		EQCBlockChainH2.getInstance().addTransactionInPool(transaction);
//		AccountsMerkleTree accountsMerkleTree = new AccountsMerkleTree(EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight(), new Filter(EQCBlockChainRocksDB.ACCOUNT_MINERING_TABLE));
//		publicKey2.setID(accountsMerkleTree.getAddressID(transaction.getTxIn().getAddress()));
//		transaction.getTxIn().getAddress().setID(accountsMerkleTree.getAddressID(transaction.getTxIn().getAddress()));
//		if(transaction.verify(accountsMerkleTree)){
//			Log.info("passed");
//		}
//		else {
//			Log.info("failed");
//		}
	}
	
	public static void testTransaction1() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(1);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(2);
		TxIn txIn = new TxIn();
		txIn.setAddress(new Address(userAccount.getReadableAddress()));
		
		byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
		byte[] publickey =  Util.AESDecrypt(userAccount.getPublicKey(), "abc");
		Signature ecdsa = null;
		try {
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, txIn.getAddress().getType()));
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OperationTransaction operationTransaction = new OperationTransaction();
		com.eqzip.eqcoin.blockchain.PublicKey publicKey2 = new com.eqzip.eqcoin.blockchain.PublicKey();
		publicKey2.setPublicKey(publickey);
		operationTransaction.setPublickey(publicKey2);
		UpdateAddressOperation updateAddressOperation = new UpdateAddressOperation();
		UserAccount userAccount2 = Keystore.getInstance().getUserAccounts().get(3);
		updateAddressOperation.setAddress(new Address(userAccount2.getReadableAddress()));
		operationTransaction.setOperation(updateAddressOperation);
		operationTransaction.setTxIn(txIn);
		operationTransaction.setNonce(EQCBlockChainRocksDB.getInstance().getAccount(txIn.getAddress().getAddressAI()).getNonce().getNextID());
		operationTransaction.cypherTxInValue(TXFEE_RATE.POSTPONE0);
		Log.info("getMaxBillingSize: " + operationTransaction.getMaxBillingSize());
		Log.info("getTxFeeLimit: " + operationTransaction.getTxFeeLimit());
		Log.info("getQosRate: " + operationTransaction.getQosRate());
		Log.info("getQos: " + operationTransaction.getQos());
		operationTransaction.sign(ecdsa, EQCBlockChainRocksDB.getInstance().getEQCHeaderHash(EQCBlockChainRocksDB.getInstance().getAccount(txIn.getAddress().getAddressAI()).getAddressCreateHeight()), publickey);
		EQCBlockChainH2.getInstance().addTransactionInPool(operationTransaction);
		
	}

	public static void testAccountHashTime() {
		Account account = new Account();
		Address address = new Address();
		address.setReadableAddress(Keystore.getInstance().getUserAccounts().get(1).getReadableAddress());
		address.setID(ID.ONE);
		account.setAddress(address);
		account.setAddressCreateHeight(ID.ONE);
		account.setBalance(50*Util.ABC);
		account.setBalanceUpdateHeight(ID.ONE);
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(1).getPublicKey(), "abc");
		Publickey publicKey2 = new Publickey();
		publicKey2.setPublickey(publickey);
		publicKey2.setPublickeyCreateHeight(ID.ONE);
		account.setPublickey(publicKey2);
		
		long c0 = System.currentTimeMillis();
		int n = 10;
		for (int i = 0; i < n; ++i) {
			account.getHash();
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1-c0) + " average time:" + (double)(c1-c0)/n);
	}
	
	public static void testMultiExtendLen() {
//		BigInteger number = new BigInteger(1, Util.getSecureRandomBytes());
////		Log.info("Len:" + number.pow(1579).toByteArray().length);
////		Log.info("Len:" + number.pow(1580).toByteArray().length);
//		for(int i=0; i<100; ++i)
//		Log.info("Len: " + new BigInteger(1, Util.getSecureRandomBytes()).pow(1000).toByteArray().length);
		
		BigInteger a = new BigInteger(1, Util.getSecureRandomBytes());//BigInteger.ONE.toByteArray());
		BigInteger b = a.pow(2);
		BigInteger c = null;
		for(int i=3; i<10000; ++i) {
//			c = a.multiply(b);
//			a = b;
//			b = c;
//			a = a.multiply(a);
			a = a.pow(i);
			Log.info("i: " + i );//+ " a: " + a + " b: " + b);
		}
		Log.info("emn" + c.toByteArray().length);
	}
	
	public static void testMultiExtendLen1() {
		BigInteger aBigInteger = new BigInteger(1, Util.getSecureRandomBytes());
		for(int i=0; i< 100; ++i) {
			aBigInteger = aBigInteger.multiply(aBigInteger);
			Log.info(Util.dumpBytes(aBigInteger.toByteArray(), 2));
		}
	}
	
	public static void testDisplayBase58() {
		final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
		for(int i=0; i<ALPHABET.length(); ++i) {
			Log.info("i: " + i + " " + ALPHABET.charAt(i) + " value: " + (byte)ALPHABET.charAt(i));
		}
	}
	
	public static void testSb() {
		StringBuffer sb = new StringBuffer();
		sb.append("abc");
		sb.insert(0, "d");
		Log.info(sb.toString());
	}
	
	public static void testCRC32C() {
		for(int i=0; i<1000; ++i) {
			if(Util.dumpBytes(Util.CRC32C(Util.intToBytes(i)), 16).endsWith("00")) {
				Log.info(Util.dumpBytes(Util.CRC32C(Util.intToBytes(i)), 16) + " Len: " + Util.CRC32C(Util.intToBytes(i)).length);
			}
			if(Util.dumpBytes(Util.CRC32C(Util.intToBytes(i)), 16).length() <= 6) {
				Log.info(Util.dumpBytes(Util.CRC32C(Util.intToBytes(i)), 16) + " Len: " + Util.CRC32C(Util.intToBytes(i)).length);
			}
		}
	}
	
	public static void testCreateAddressTime() {
//		EQCHeader header = new EQCHeader();
//		header.setNonce(ID.ONE);
//		header.setPreHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, true));
//		header.setTimestamp(new ID(System.currentTimeMillis()));
//		Log.info(header.toString());
		byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(1).getPublicKey(), "abc");
		Log.info("Publickey Len: " + publickey.length);
		long c0 = System.currentTimeMillis();
		int n = 100;
		for (int i = 0; i < n; ++i) {
//			Util.multipleExtend(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
			Util.AddressTool.generateAddress(publickey, AddressType.T1);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1-c0) + " average time:" + (double)(c1-c0)/n);
	}
	
	public static void testAddressCRC32C() {
		Log.info(Keystore.getInstance().getUserAccounts().get(2).getReadableAddress());
		if(AddressTool.verifyAddressCRC32C(Keystore.getInstance().getUserAccounts().get(2).getReadableAddress())) {
			Log.info("Passed");
		}
		
	}
	
}
