/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;

import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Nov 12, 2018
 * @email 10509759@qq.com
 */
public class Index implements EQCTypable {
	private BigInteger version;
	private long totalSupply;
	private BigInteger totalAccountNumbers;
	private BigInteger totalTransactionNumbers;
//	private long accountsMerkleTreeRootListSize;
//	private Vector<byte[]> accountsMerkleTreeRootList;
//	private byte[] transactionsHash;
//	// Bin type 16 bytes use EQCCHA_MULTIPLE(final byte[] bytes, 1, true) generate
//	// it.
//	private byte[] signaturesHash = null;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	private final static byte VERIFICATION_COUNT = 4;
	
	public Index() {
		version = BigInteger.ZERO;
//		accountsMerkleTreeRootList = new Vector<byte[]>();
	}
	
	public Index(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		// Parse Version
		if ((data = EQCType.parseEQCBits(is)) != null) {
			version = Util.bytesToBigInteger(data);
		}
		
		// Parse totalSupply
		if ((data = EQCType.parseEQCBits(is)) != null) {
			totalSupply = EQCType.eqcBitsToLong(data);
		}

		// Parse totalAccountNumbers
		if ((data = EQCType.parseEQCBits(is)) != null) {
			totalAccountNumbers = EQCType.eqcBitsToBigInteger(data);
		}

		// Parse totalTransactionNumbers
		if ((data = EQCType.parseEQCBits(is)) != null) {
			totalTransactionNumbers = EQCType.eqcBitsToBigInteger(data);
		}

//		// Parse Accounts Merkle Tree's Root size & List
//		ARRAY array = null;
//		if ((array = EQCType.parseARRAY(is)) != null) {
////			parseAccountsMerkleTreeRootList(data);
//			accountsMerkleTreeRootListSize = array.length;
//			accountsMerkleTreeRootList = array.elements;
//		}
//		
//		// Parse transactionsHash
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			transactionsHash = data;
//		}

//		// Parse signaturesHash
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			signaturesHash = data;
//		}
	}
	
	
	
	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;

		// Parse Version
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

		// Parse totalSupply
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

		// Parse totalAccountNumbers
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

		// Parse totalTransactionNumbers
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}
//
//		// Parse Accounts Merkle Tree's Root size & List
//		ARRAY array = null;
//		if ((array = EQCType.parseARRAY(is)) != null) {
//			if(array.length > 0) {
//				++validCount;
//			}
//			if(array.length == array.elements.size()) {
//				++validCount;
//			}
//		}
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			Vector<byte[]> accountsMerkleTreeRootList = new Vector<byte[]>();
//			long accountsMerkleTreeRootListSize = 0;
//			ByteArrayInputStream iss = new ByteArrayInputStream(data);
//			byte[] value = null;
//			// Parse accountsMerkleTreeRootListSize
//			if ((value = EQCType.parseEQCBits(iss)) != null) {
//				accountsMerkleTreeRootListSize = Util.eqcBitsToLong(value);
//			}
//			// Parse accountsMerkleTreeRootList
//			while ((value = EQCType.parseBIN(iss)) != null) {
//				accountsMerkleTreeRootList.add(value);
//			}
//			
//			if(accountsMerkleTreeRootList.size() > 0) {
//				++validCount;
//			}
//			if(accountsMerkleTreeRootList.size() == accountsMerkleTreeRootListSize) {
//				++validCount;
//			}
//		}
//
//		// Parse transactionsHash
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			++validCount;
//		}

//		// Parse signaturesHash
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			++validCount;
//		}

		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

//	private void parseAccountsMerkleTreeRootList(byte[] bytes) throws NoSuchFieldException, IOException {
//		accountsMerkleTreeRootList = new Vector<byte[]>();
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		byte[] data = null;
//		// Parse accountsMerkleTreeRootListSize
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			accountsMerkleTreeRootListSize = Util.eqcBitsToLong(data);
//		}
//		// Parse accountsMerkleTreeRootList
//		while ((data = EQCType.parseBIN(is)) != null) {
//			accountsMerkleTreeRootList.add(data);
//		}
//	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bigIntegerToEQCBits(version));
			os.write(EQCType.longToEQCBits(totalSupply));
			os.write(EQCType.bigIntegerToEQCBits(totalAccountNumbers));
			os.write(EQCType.bigIntegerToEQCBits(totalTransactionNumbers));
//			os.write(EQCType.bytesArrayToARRAY(accountsMerkleTreeRootList));
//			os.write(EQCType.bytesToBIN(transactionsHash));
//			os.write(EQCType.bytesToBIN(signaturesHash));
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

	/**
	 * @return the totalSupply
	 */
	public long getTotalSupply() {
		return totalSupply;
	}

	/**
	 * @param totalSupply the totalSupply to set
	 */
	public void setTotalSupply(long totalSupply) {
		this.totalSupply = totalSupply;
	}

	/**
	 * @return the totalAccountNumbers
	 */
	public BigInteger getTotalAccountNumbers() {
		return totalAccountNumbers;
	}

	/**
	 * @param totalAccountNumbers the totalAccountNumbers to set
	 */
	public void setTotalAccountNumbers(BigInteger totalAccountNumbers) {
		this.totalAccountNumbers = totalAccountNumbers;
	}

	/**
	 * @return the totalTransactionNumbers
	 */
	public BigInteger getTotalTransactionNumbers() {
		return totalTransactionNumbers;
	}

	/**
	 * @param totalTransactionNumbers the totalTransactionNumbers to set
	 */
	public void setTotalTransactionNumbers(BigInteger totalTransactionNumbers) {
		this.totalTransactionNumbers = totalTransactionNumbers;
	}

//	/**
//	 * @return the accountsMerkleTreeRootList
//	 */
//	public Vector<byte[]> getAccountsMerkleTreeRootList() {
//		return accountsMerkleTreeRootList;
//	}

//	/**
//	 * @param accountsMerkleTreeRootList the accountsMerkleTreeRootList to set
//	 */
//	public void setAccountsMerkleTreeRootList(Vector<byte[]> accountsMerkleTreeRootList) {
//		this.accountsMerkleTreeRootList = accountsMerkleTreeRootList;
//		this.accountsMerkleTreeRootListSize = accountsMerkleTreeRootList.size();
//	}

//	/**
//	 * @return the transactionsHash
//	 */
//	public byte[] getTransactionsHash() {
//		return transactionsHash;
//	}
//
//	/**
//	 * @param transactionsHash the transactionsHash to set
//	 */
//	public void setTransactionsHash(byte[] transactionsHash) {
//		this.transactionsHash = transactionsHash;
//	}

//	/**
//	 * @return the signaturesHash
//	 */
//	public byte[] getSignaturesHash() {
//		return signaturesHash;
//	}
//
//	/**
//	 * @param signaturesHash the signaturesHash to set
//	 */
//	public void setSignaturesHash(byte[] signaturesHash) {
//		this.signaturesHash = signaturesHash;
//	}
	
	public byte[] getHash() {
		return  Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(getBytes(), Util.HUNDRED_THOUSAND, false);
	}
	
//	public byte[] getAccountsMerkleTreeRootListRoot() {
//		return Util.EQCCHA_MULTIPLE(Util.getMerkleTreeRoot(accountsMerkleTreeRootList), Util.HUNDRED_THOUSAND, false);
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Index [version=" + version + ", totalSupply=" + totalSupply + ", totalAccountNumbers="
				+ totalAccountNumbers + ", totalTransactionNumbers=" + totalTransactionNumbers
				+ ", accountsMerkleTreeRootListSize=" /*+ accountsMerkleTreeRootListSize + ", accountsMerkleTreeRootList="
				+ accountsMerkleTreeRootList + ", transactionsHash=" + Arrays.toString(transactionsHash)*/ + "]";
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
