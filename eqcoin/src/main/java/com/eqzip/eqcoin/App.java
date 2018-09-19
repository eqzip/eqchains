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

import java.math.BigInteger;
import java.util.Base64;
import java.util.Vector;
import com.eqzip.eqcoin.blockchain.EQCHeader;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.Os;
import com.eqzip.eqcoin.util.Base58;
import com.eqzip.eqcoin.util.CRC8;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.SerialNumber;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public class App 
{
    public static void main( String[] args )
    {
    	Thread.currentThread().setPriority(10);
    	Util.init(Os.WINDOWS);
    	testCRC8ITU();
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
    	testBase58();
//    	testBlockchain();
//    	testLongToBytes();
//    	testTargetToBytes();
//    	testSignBigIntegerPadingZero();
//    	caluateTarget();
    }
    
    private static void testBase58() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("00");
//    	for(int i=0; i<17; ++i) {
//    		sb.append("f2");
//    	}
    	sb.append(new BigInteger(1, Util.RIPEMD160(Util.intToBytes(1))).toString(16));
    	sb.append("ffffffff");
    	String str = sb.toString();
    	Log.info(str);
    	byte[] bytes = new BigInteger(str, 16).toByteArray();
    	Log.info(Base58.encode(new BigInteger(str, 16).toByteArray()) + " len: " + Base58.encode(new BigInteger(str, 16).toByteArray()).length());
//    	Log.info(Base58.encode1(new BigInteger(str, 16).toByteArray()));
    }
    
    private static void testCRC8ITU() {
    	Log.info(Integer.toHexString(Util.CRC8ITU(Util.intToBytes(123))));
    	CRC8    crc8 = new CRC8();
    	byte[] bytes = BigInteger.valueOf(123l).toByteArray();
//        crc8.update(bytes,0,bytes.length);
    	
        Log.info(Long.toHexString(crc8.Calculate(bytes, (byte)0x107, (byte)0, (byte)0x55, true, false)));
    }
    
    private static void testRIPEMD() {
    	Log.info(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16));
    	Log.info(Util.dumpBytes(Util.RIPEMD128("abc".getBytes()), 16));
//    	assertNotNull(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16), "userAttribute");
    }
    
    private static void testBigIntegerToBits() {
    	
    	// 127 = ‭01111111‬
    	Log.info(Util.dumpBytes(Util.longToBytes(127l), 16) + "\n" + Util.dumpBytes(Util.longToBits(127l), 16) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(127l)).toByteArray(), 16));
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
    	// 1844674407370955161 = ‭0001100110011001100110011001100110011001100110011001100110011001
//    	Log.info(Util.dumpBytes(Util.longToBytes(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.longToBits(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(Long.MAX_VALUE)).toByteArray()));‬
    }
    
    private static void testSN() {
    	SerialNumber addressSN = new SerialNumber(BigInteger.ZERO);
    	Vector<SerialNumber> vec = new Vector<SerialNumber>();
    	vec.add(addressSN);
    	for(int i=1; i<1000; ++i) {
    		vec.add(vec.get(i-1).getNextSN());
    		if(vec.get(i).isNextSN(vec.get(i-1)))
    			Log.info("isNextSN：" + " current: " + vec.get(i).getSerialNumber().longValue() + " previous:" + vec.get(i-1).getSerialNumber().longValue() + " bits: " + Util.dumpBytes(vec.get(i).getBits(), 2));
    	}
    }
    
    private static void testBlockchain() {
    	System.out.println("testBlockchain");
    	EQCHeader header = new EQCHeader();
    	header.setNonce(1);
    	header.setPreHash(Util.dualSHA3_512(Util.getSecureRandomBytes()));
    	header.setTarget(Util.bigIntegerTo64Bytes(Util.getDefaultTarget()));
    	header.setTxHash(Util.dualSHA3_512(Util.getSecureRandomBytes()));
    	header.setTimestamp(System.currentTimeMillis());
//    	System.out.println(header.toString());
    	Vector<EQCHeader> vec = new Vector<EQCHeader>();
    	vec.add(header);
    	BigInteger target = Util.getDefaultTarget();
    	long time0 = System.currentTimeMillis();
    	long time1;
    	int lCount = 0;
    	long Totaltime = 0;
    	long i = 0;
    	byte[] bytes;
        while(true) {
        	BigInteger hash = new BigInteger(1, Util.EQCCHA((bytes=Util.updateNonce(vec.get(lCount).getBytes(), ++i)), false));//Util.dualSHA3_512(Util.multipleExtend((bytes=Util.updateNonce(vec.get(lCount).getBytes(), ++i)), 100)));
//        	System.out.println("hash: " + Util.bigIntegerTo512String(hash));
        	if(hash.compareTo(target) == -1) {
//        		time1 = System.currentTimeMillis();
        		Log.info("EQC Block No."+ lCount + " Find use: " + (System.currentTimeMillis()-vec.get(lCount).getTimestamp()) + " ms, details:");
        		vec.set(lCount, new EQCHeader(bytes));
        		Log.info(vec.get(lCount).toString());
        		header = new EQCHeader();
        		header.setNonce(0);
        		header.setPreHash(Util.bigIntegerTo64Bytes(hash));
        		++lCount;
        		if(lCount%10 != 0) {
        			header.setTarget(vec.get(lCount-1).getTarget());
        		}
        		else {
        			Log.info("Old target: " + Util.bigIntegerTo512String(Util.bytesToBigInteger(vec.get(lCount-1).getTarget())) + "\r\naverge time: " + (vec.get(lCount-1).getTimestamp()-vec.get(lCount-10).getTimestamp())/10);
        			target = target.multiply(BigInteger.valueOf((vec.get(lCount-1).getTimestamp()-vec.get(lCount-10).getTimestamp()))).divide(BigInteger.valueOf(100000));
        			Log.info("New target: " + Util.bigIntegerTo512String(target));
        			header.setTarget(Util.bigIntegerTo64Bytes(target));
        		}
        		header.setTxHash(Util.dualSHA3_512(Util.getSecureRandomBytes()));
        		header.setTimestamp(System.currentTimeMillis());
        		vec.add(header);
        		i = 0;
        		if(lCount == 2000) {
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
        Log.info("averge time: " + (vec.get(vec.size()-1).getTimestamp()-vec.get(0).getTimestamp())/lCount + " total time: " + (vec.get(vec.size()-1).getTimestamp()-vec.get(0).getTimestamp()) + " count:" + lCount);
    }
    
    private static void testLongToBytes() {
    	byte[] foo = Util.longToBytes(Long.MAX_VALUE);
    	long lValue = Util.bytesToLong(foo);
    	System.out.println("lValue: " + lValue);
    }
    
    private static void testTarget() {
    	
    	BigInteger a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(512-60-17));
    	System.out.println(a.toString(16));
    	a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3));
    	System.out.println(a.toString(16));
    	a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3)).add(BigInteger.valueOf(Long.parseLong("21", 16)));
    	System.out.println(a.toString(16));
    	a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8).add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(424);//.multiply(BigInteger.valueOf(2).pow(3)).add(BigInteger.valueOf(Long.parseLong("21", 16)));
    	System.out.println(a.shiftRight(512-a.bitLength()).toString(16)+ " len: " + a.bitLength());
    	
    }
    
    private static void testSignBigIntegerPadingZero() {
    	
    	BigInteger negative_number = BigInteger.valueOf(Long.MAX_VALUE + 1); 
    	byte[] negative_bytes = negative_number.toByteArray();
    	System.out.println("negative_number:" + negative_number.toString() + " negative_bytes' len: " + negative_bytes.length);
    	
    	BigInteger positive_number = new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray());
    	byte[]  positive_bytes = positive_number.toByteArray();
    	System.out.println("positive_number:" + positive_number.toString() + " positive_bytes' len: " + positive_bytes.length);
    	
    	BigInteger number = BigInteger.valueOf(Long.MAX_VALUE+1);//.add(BigInteger.ONE);
    	byte[]  number_bytes = number.toByteArray();
    	System.out.println("number:" + number.toString() + " number_bytes' len: " + number_bytes.length);
    	
    	BigInteger number1 = BigInteger.valueOf(128).shiftLeft(80);
    	byte[]  number_bytes1 = number1.toByteArray();
    	System.out.println("number1:" + number1.toString(16) + " number_bytes1' len: " + number_bytes1.length);
    	
    	BigInteger number2 = new BigInteger(1, number_bytes1);
    	byte[]  number_bytes2 = number2.toByteArray();
    	System.out.println("number2:" + number2.toString(16) + " number_bytes2' len: " + number_bytes2.length);
    	
    	BigInteger number3 = new BigInteger(number_bytes1);
    	byte[]  number_bytes3 = number3.toByteArray();
    	System.out.println("number3:" + number3.toString(16) + " number_bytes3' len: " + number_bytes3.length);
    	
    }
    
    private static void testTargetToBytes() {
    	
    	// Display default target
    	BigInteger target = Util.getDefaultTarget();
    	System.out.println("Default target's length: " + Util.getDefaultTarget().toByteArray().length);
    	// Display 512 bit length target
    	byte[] tmp = Util.bigIntegerTo64Bytes(target);
    	System.out.print("512 bit bytes' len: " + tmp.length + "\n");
    	
    	System.out.println(Util.bytesToBigInteger(tmp).toString(16));
    	System.out.println(Util.bigIntegerTo512String(Util.getDefaultTarget()));
    	
    }
    
    private static void caluateTarget() {
    	
    	BigInteger target = Util.getDefaultTarget();
    	long time0 = System.currentTimeMillis();
    	long time1;
    	long lCount = 0;
    	long Totaltime = 0;
    	long i = 10000;
        while(true) {
        	BigInteger hash = new BigInteger(1, Util.dualSHA3_512(Util.multipleExtend((""+i++).getBytes(), 100)));
//        	logger.info(tmp.toString(16));
        	
        	if(hash.compareTo(target) == -1) {
        		time1 = System.currentTimeMillis();
        		System.out.println("i: "+ lCount + " Find use: " + (time1-time0) + " ms\n");
        		Totaltime += (time1-time0);
        		time0 = time1;
        		++lCount;
        		if(lCount == 100) {
        			break;
        		}
        		System.out.println(hash.toString(2));
        		System.out.println(" len: " + hash.toString(2).length() + " i: " +i);
        		System.out.println(" len: " + hash.toString(2).length() + " i: " + (i-1) + "\n" + Base64.getEncoder().encodeToString(Util.dualSHA3_512(Util.multipleExtend((""+(i-1)).getBytes(), 1))));
        		if(lCount%10 == 0) {
        			System.out.println("Old target: " + target.toString(16) + "averge time: " + Totaltime/10);
        			target = target.multiply(BigInteger.valueOf(Totaltime)).divide(BigInteger.valueOf(100000));
        			Totaltime = 0;
        			System.out.println("New target: " + target.toString(16));
        		}
        	}
        }
        System.out.println("averge time: " + Totaltime/lCount + " total time: " + Totaltime + " count:" + lCount);
    	
    }
    
}
