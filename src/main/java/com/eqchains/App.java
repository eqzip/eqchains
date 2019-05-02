/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-2019 EQCOIN Foundation Inc.  All rights reserved...
 * CC BY-NC-ND(https://creativecommons.org/licenses/by-nc-nd/4.0/deed.en)
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
package com.eqchains;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.avro.io.BinaryData;
import org.bouncycastle.asn1.dvcs.Data;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.EQCBlock;
import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.AccountsMerkleTree.Filter;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.configuration.Configuration;
import com.eqchains.keystore.Keystore;
import com.eqchains.keystore.UserAccount;
import com.eqchains.keystore.Keystore.ECCTYPE;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB.TABLE;
import com.eqchains.serialization.EQCType;
import com.eqchains.service.MinerService;
import com.eqchains.test.Test;
import com.eqchains.util.Base58;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public class App {
	public static void main(String[] args) {
		Thread.currentThread().setPriority(10);
//		EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.valueOf(0)));
//		EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.valueOf(0)));
//		MinerService.getInstance().start();
//		Test.testTransaction1();
//		EQCBlockChainRocksDB.getInstance().dumpEQCBlock();
//		EQCBlockChainRocksDB.getInstance().dumpAccount();
//		EQCBlockChainRocksDB.getInstance().dumpTable(TABLE.ACCOUNT);
//		EQCBlockChainRocksDB.getInstance().dumpTable(TABLE.ACCOUNT_AI);
//		Log.info(EQCBlockChainRocksDB.getInstance().getTableItemNumbers(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_AI)).toString());
//		EQCBlock eqcBlock = EQCBlockChainRocksDB.getInstance().getEQCBlock(new ID(4), false);
//		Log.info(eqcBlock.toString());
		
//		Test.testMultiExtendLen();
//		Log.info(Util.dumpBytes(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(new byte[]{0}, Util.HUNDRED, false), 16));
//		Util.cypherSingularityEQCBlockPreHash();
//		Test.testDisplayBase58();
		
//		AccountsMerkleTree accountsMerkleTree = new AccountsMerkleTree(new ID(28), new Filter(EQCBlockChainRocksDB.ACCOUNT_MINERING_TABLE));
//		accountsMerkleTree.buildAccountsMerkleTree();
//		accountsMerkleTree.generateRoot();
//		Log.info(Util.dumpBytes(accountsMerkleTree.getRoot(), 16));
		
//		Util.init(OS.WINDOWS);
//		for(int i=0; i<10; ++i)
//		Test.testUserAccount();
//		Test.testAIToAddress();
//		EQCBlockChainH2.getInstance().dropTable();
//		EQCBlockChainRocksDB.getInstance().dropTable();
//		Configuration.getInstance().updateIsInitSingularityBlock(false);
//		Util.init();
//		try {
//			byte[] bytes = EQCBlockChainRocksDB.get(TABLE.ACCOUNT_AI, new BigInteger("1FB2C896CBB5B44593C3225E6C4E67F1D0A77D5860EDDB424B7A409278CD2F644", 16).toByteArray());
//			ID id = new ID(bytes);
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Test.testTakeSnapshot();
//		EQCBlockChainH2.getInstance().deleteAccountSnapshot(ID.TWO, false);
//		EQCBlockChainRocksDB.getInstance().dumpEQCBlock();
//		Util.cypherSingularityEQCBlockPreHash();
//		Log.info(new BigInteger("4BFC0CA5060236AA7AB0B19D0D49B18495AED275BBCA000EC1CFB05774CA25DFDBFD0C5C56A13BD9C46D80E43B00B9620D503227BB64D0549AB2970B253CE913", 16).toString());
//		Test.testTarget1();
//		Log.info("Len: " + Util.UnsignedBiginteger(new BigInteger("200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5", 16)).toByteArray().length);
//		Log.info(Util.dumpBytes(Util.UnsignedBiginteger(new BigInteger("200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5", 16)).toByteArray(), 16));
//		Test.testAccountHashTime();
		
//		long length = new ID(Util.UnsignedBiginteger(new BigInteger("33644764876431783266621612005107543310302148460680063906564769974680081442166662368155595513633734025582065332680836159373734790483865268263040892463056431887354544369559827491606602099884183933864652731300088830269235673613135117579297437854413752130520504347701602264758318906527890855154366159582987279682987510631200575428783453215515103870818298969791613127856265033195487140214287532698187962046936097879900350962302291026368131493195275630227837628441540360584402572114334961180023091208287046088923962328835461505776583271252546093591128203925285393434620904245248929403901706233888991085841065183173360437470737908552631764325733993712871937587746897479926305837065742830161637408969178426378624212835258112820516370298089332099905707920064367426202389783111470054074998459250360633560933883831923386783056136435351892133279732908133732642652633989763922723407882928177953580570993691049175470808931841056146322338217465637321248226383092103297701648054726243842374862411453093812206564914032751086643394517512161526545361333111314042436854805106765843493523836959653428071768775328348234345557366719731392746273629108210679280784718035329131176778924659089938635459327894523777674406192240337638674004021330343297496902028328145933418826817683893072003634795623117103101291953169794607632737589253530772552375943788434504067715555779056450443016640119462580972216729758615026968443146952034614932291105970676243268515992834709891284706740862008587135016260312071903172086094081298321581077282076353186624611278245537208532365305775956430072517744315051539600905168603220349163222640885248852433158051534849622434848299380905070483482449327453732624567755879089187190803662058009594743150052402532709746995318770724376825907419939632265984147498193609285223945039707165443156421328157688908058783183404917434556270520223564846495196112460268313970975069382648706613264507665074611512677522748621598642530711298441182622661057163515069260029861704945425047491378115154139941550671256271197133252763631939606902895650288268608362241082050562430701794976171121233066073310059947366875"))).multiply(new ID(Util.UnsignedBiginteger(new BigInteger("33644764876431783266621612005107543310302148460680063906564769974680081442166662368155595513633734025582065332680836159373734790483865268263040892463056431887354544369559827491606602099884183933864652731300088830269235673613135117579297437854413752130520504347701602264758318906527890855154366159582987279682987510631200575428783453215515103870818298969791613127856265033195487140214287532698187962046936097879900350962302291026368131493195275630227837628441540360584402572114334961180023091208287046088923962328835461505776583271252546093591128203925285393434620904245248929403901706233888991085841065183173360437470737908552631764325733993712871937587746897479926305837065742830161637408969178426378624212835258112820516370298089332099905707920064367426202389783111470054074998459250360633560933883831923386783056136435351892133279732908133732642652633989763922723407882928177953580570993691049175470808931841056146322338217465637321248226383092103297701648054726243842374862411453093812206564914032751086643394517512161526545361333111314042436854805106765843493523836959653428071768775328348234345557366719731392746273629108210679280784718035329131176778924659089938635459327894523777674406192240337638674004021330343297496902028328145933418826817683893072003634795623117103101291953169794607632737589253530772552375943788434504067715555779056450443016640119462580972216729758615026968443146952034614932291105970676243268515992834709891284706740862008587135016260312071903172086094081298321581077282076353186624611278245537208532365305775956430072517744315051539600905168603220349163222640885248852433158051534849622434848299380905070483482449327453732624567755879089187190803662058009594743150052402532709746995318770724376825907419939632265984147498193609285223945039707165443156421328157688908058783183404917434556270520223564846495196112460268313970975069382648706613264507665074611512677522748621598642530711298441182622661057163515069260029861704945425047491378115154139941550671256271197133252763631939606902895650288268608362241082050562430701794976171121233066073310059947366875")))
//		).toByteArray().length;
//		Log.info("l: " + length);
//		for(int i=17; i<27; ++i)
//		Log.info(i+  " : " + Util.fibonacci(10000000+i));
		Test.testHashTime();
//		Log.info(Keystore.getInstance().getUserAccounts().get(1).getAddress() + " Len: " + Keystore.getInstance().getUserAccounts().get(1).getAddress().length());
//		Log.info(EQCType.bytesToASCIISting(EQCType.stringToASCIIBytes(Keystore.getInstance().getUserAccounts().get(1).getAddress())));
//		Log.info(Util.dumpBytes(EQCType.stringToASCIIBytes(Keystore.getInstance().getUserAccounts().get(1).getAddress()), 16) + " Len: " + EQCType.stringToASCIIBytes(Keystore.getInstance().getUserAccounts().get(1).getAddress()).length);
//		
//		EQCBlockChainH2.getInstance().getTransactionListInPool();
//		try {
//			Log.info("" + new ID(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, Keystore.getInstance().getUserAccounts().get(1).getAddress().getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Util.fibonacci(2);
//		Util.cypherSingularityEQCBlockPreHash();
//		Log.info(Util.dumpBytes(Util.EQCCHA_MULTIPLE(new byte[]{0}, 100, false), 16));
		
//		for(int i=0; i<10; ++i)
//		EQCBlockChainRocksDB.dropTable(EQCBlockChainRocksDB.createTable("abc".getBytes()));
		
//		Test.testSingularBlockBytes();
//		EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(ID.ONE);
//		
//		PublicKey publicKey = new PublicKey();
//		publicKey.setID(ID.ONE);
//		publicKey.setPublicKey("abc".getBytes());
//		EQCBlockChainH2.getInstance().savePublicKey(publicKey, ID.ONE);
		
//		Test.testH2Account();
//		Test.testTimestamp();
//		Test.testRocksDB();
//		Test.testRocksDB1();
//		Test.destoryRocksDB();
//		Test.testNonce();
//		Test.testRocksDBAccount();
//		Test.testDisplayAccount();
//		Test.testMisc();
//		Test.testBiginteger();
//		CoinbaseTransaction coinbaseTransaction = new CoinbaseTransaction();
//		coinbaseTransaction.isSanity();
//		ID id = new ID(1);
//		Log.info(id + "  " + id.signum());
//		
//		ID id2 = new ID(1);
//		if(id == id2) {
//			Log.info("id == id2");
//		}
//		if(id.equals(id2)) {
//			Log.info("id.equals(id2)");
//		}
//		
//		Address address = new Address();
//		address.setReadableAddress("ab");
//		Address address2 = new Address();
//		address2.setReadableAddress("ab");
//		
//		if(address.equals(address2)) {
//			Log.info("equal");
//		}
//		Test.testVerifyAddress();
//		Test.testBase582();
		
//		Test.dumpEQCBlock();
//		Test.dumpAccount();
//		Test.testCF4();
		
//		ArrayList<String> acb = new ArrayList<>();
//		acb.add("a");
//		acb.add("b");
//		acb.add("c");
//		acb.add("d");
//		acb.remove(2);acb.re
//		acb.remove(3);
		
//		EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.valueOf(0)));
//		EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.valueOf(0)));
//		MinerService.getInstance().start();
//		Test.testDisplayEQCBlock(new SerialNumber(93));
//		Test.testDisplayAllAccount();
//		Log.info(Util.dumpBytes(Util.longToEQCBits(127), 2));
//		EQCBlockChainRocksDB.clearTable(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT_MINERING));
//		EQCBlockChainRocksDB.clearTable(EQCBlockChainRocksDB.getTableHandle(TABLE.ACCOUNT));
//		Log.info(Util.dumpBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).toByteArray(), 16));
//		Test.testKeystore();
//		Test.testDisplayKeystore();
//		Log.info(Base58.encode(Util.intToBytes(58)));
//		Test.testSb();
//		Test.testCRC32C();
//		Test.testBigintegerLeadingzero();
//		Test.testMinAndMaxAddress();
//		Util.CRC32C("abcddd".getBytes());
//		String cmd = "  cmd /c time 22:35:00";
//		    try {
//				Runtime.getRuntime().exec(cmd);
//				Log.info(cmd);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		Test.testSignTransaction();
//		Test.testP2SH();
//		Log.info("" + Keystore.getInstance().getUserAccounts().size());
//		Log.info(Util.getGMTTime(System.currentTimeMillis()));
//		Log.info(Util.getGMTTime(Util.getNTPTIME()));
//		Test.testAddressFormat();
//		Test.testValue();
//		Log.info(Util.getIP());
//		Log.info("Len: " + "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".length());
//		EQCBlockChainH2.getInstance().setEQCBlockTailHeight(new SerialNumber(BigInteger.valueOf(0)));
//		try {
//			
//			Log.info("" + Base58.decode("2111111111111111111111111111111111111111").length + " Len: " + "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz".length());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Test.testMinAndMaxAddress();
//		MinerService.getInstance().start();
//		Log.info(java.security.Security.getProperty( "securerandom.strongAlgorithms" ));
//		SecureRandom.getInstanceStrong()
//		 byte[] data = Util.SHA3_256("abc".getBytes());
//		 Log.info("Len: " + data.length);
//		 byte[] newdata = new byte[32];//new byte[192];
//		 for(int i=0; i<1; ++i) {
//			 System.arraycopy(data, 0, newdata, i*data.length, data.length);
//		 }
//		 Log.info(Util.dumpBytes(newdata, 16));
//		 ByteArrayOutputStream out = new ByteArrayOutputStream();
//	        GZIPOutputStream gzip;
//	        try {
//	            gzip = new GZIPOutputStream(out);
//	            gzip.write(newdata);
//	            gzip.finish();
//	            gzip.close();
//	        } catch (IOException e) {
//	            Log.Error("gzip compress error." + e);
//	        }

//	        BrotliInputStream 
//		    BrotliStreamCompressor brotliStreamCompressor = new BrotliStreamCompressor(Brotli.DEFAULT_PARAMETER);
//		    byte[] compressedBuffer = brotliStreamCompressor.compressArray(data, true);
//		    Log.info("compressedBuffer Len: " + out.toByteArray().length);
//		Test.testCreateAddressTime();
//		Test.testMultiExtendTime();
//		Test.testHashTime();
//		Test.testBytesToBIN();
//		String abc = "abc";
//		for(int i=0; i<1; ++i) {
//			abc += abc;
//			Test.testGeneratePublicKey(ECCTYPE.P256, abc);
//		}
//		Test.testVerifyPublicKey(ECCTYPE.P256);
//		Test.testSpendCoinBase();
//		EQCBlockChainH2.getInstance().setEQCBlockTailHeight(new SerialNumber(BigInteger.valueOf(0)));
//		MinerService.getInstance().start();
//		if(Util.verifyEQCBlock(EQCBlockChainH2.getInstance().getEQCBlock(new SerialNumber(BigInteger.ONE), true))) {
//			Log.info("passed");
//		}
//		Address address = new Address(Keystore.getInstance().getAccounts().get(0).getAddress());
//		Log.info("balance: " + EQCBlockChainH2.getInstance().getBalance(address));
//		Log.info("len:" + Util.bigIntegerToEQCBits(BigInteger.valueOf(25000000000l)).length);
//		Test.testEQCBlock();
//		Address address = new Address();
//		address.setAddress("32h9PPspxFSASmpkXWXwvNdWx");
//		address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//		EQCBlockChainH2.getInstance().appendAddress(address, new SerialNumber(BigInteger.ZERO));
////		address.setCode(Util.getDefaultTargetBytes());
//		for(int i=0; i<100; ++i)
//		EQCBlockChainH2.getInstance().appendAddress(address, new SerialNumber(BigInteger.ONE));
////		EQCBlockChainH2.getInstance().deleteAddress(address);
//		EQCBlockChainH2.getInstance().deleteAddressFromHeight(new SerialNumber(BigInteger.ONE));
//		if(EQCBlockChainH2.getInstance().isAddressExists(address)) {
//			Log.info("exitst");
//		}
//		else {
//			Log.info("not");
//		}
//		Test.testEC(ECCTYPE.P256);
//		Test.testSignTransaction();
//		Test.testHashTime();
//		EQCBlockChainH2.getInstance().dropTable();
//		BigInteger difficulty = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
//		Log.info(Util.bigIntegerTo128String(difficulty));
//		Log.info(Util.bigIntegerTo128String(difficulty.divide(BigInteger.valueOf(10000000000000l))));
//		EQCBlock sin = Util.getSingularityBlock();
//		EQCBlockChainH2.getInstance().saveEQCBlock(Util.getSingularityBlock());
//		EQCBlock eqcBlock = EQCBlockChainH2.getInstance().getEQCBlock(new SerialNumber(BigInteger.ZERO), true);
//		Log.info("abc");
//		Test.testToString();
//		Test.testKeystore();
//		Test.testMultiTransaction();
//		byte[] en = Util.AESEncrypt("abc".getBytes(), "abc");
//		Log.info(new String(en));
//		Log.info(new String(Util.AESDecrypt(en, "abc")));;
//		
//		Log.info(Keystore.getInstance().getAccounts().get(0).toString());
//		Account account = Keystore.getInstance().getAccounts().get(0);
//		String abc = "abc";
//		if(account.isPasswordCorrect("abc")) {
//			Log.info("password correct");
//		}
//		byte[] sign = account.signTransaction("abc", abc.getBytes());
//		if(account.verifyTransaction("abc", abc.getBytes(), sign)) {
//			Log.info("sign passed");
//		}
//		else {
//			Log.info("sign failed");
//		}
		
//		testKeystore();
		
//		Log.info("Default target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes())));
//		
//		byte[] by = Util.bigIntegerToTargetBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()));//new BigInteger("1525de600000000000000000000000", 16));
//		Log.info("hex: " + Integer.toHexString(Util.bytesToInt(by)));
//		Log.info("target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger(by)));
//		
//		Log.info("Len: " + new BigInteger("1FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16).toByteArray().length);
//		byte[] number = new byte[17]; 
//		for(int i=0; i<number.length; ++i) {
//			number[i] = (byte) 0xff;
//		}
//		Log.info(Util.dumpBytes(number, 16));
//		Log.info(Util.AddressTool.generateAddress(number, AddressType.T1));
//		Log.info("" + Util.AddressTool.generateAddress(number, AddressType.T1).length());
//		Log.info(Util.AddressTool.generateAddress(number, AddressType.T2));
//		Log.info(Util.AddressTool.generateAddress(number, AddressType.T3));
//		Log.info(Base58.encode(number) + " Len: " + Base58.encode(number).length());
//		for(UserAccount userAccount : Keystore.getInstance().getUserAccounts()) {
//			Log.info(userAccount.getAddress() + " len: " + userAccount.getAddress().length());
//		}
		
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
//		testBlockchain();
//		Test.testHashTime();
//    	testLongToBytes();
//    	testTargetToBytes();
//    	testSignBigIntegerPadingZero();
//    	caluateTarget();
//		Test.testTarget();
	}

}
