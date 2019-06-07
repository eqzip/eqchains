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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHeader implements EQCTypable {
	/*
	 * previous block hash |  target  | Root hash | 	 Height 	 |     timestamp 	 |       nonce  
   			 64 bytes	     4 bytes     64 bytes  lengthen(>=1bytes) lengthen(>=6bytes)  lengthen(<=4bytes)   
	 */
	private byte[]	preHash;
	private byte[]	target;
	private byte[]	rootHash;
	private ID height;
	private ID timestamp;
	private ID nonce;
	// The EQCHeader's size is lengthen
	private final int min_size = 139;
	private final static int MIN_TIMESTAMP_LEN = 6;
	private final static int MAX_NONCE_LEN = 4;
	private final static int TARGET_LEN = 4;
	
	public EQCHeader(ByteBuffer byteBuffer) throws NoSuchFieldException {
		parseEQCHeader(byteBuffer.array());
	}
	
	/**
	 * @param header
	 * @throws NoSuchFieldException 
	 */
	public EQCHeader(byte[] bytes) throws NoSuchFieldException {
		super();
		parseEQCHeader(bytes);
	}

	private void parseEQCHeader(byte[] bytes) throws NoSuchFieldException {
		EQCType.assertNotNull(bytes);
		preHash = new byte[Util.HASH_LEN];
		target = new byte[TARGET_LEN];
		rootHash = new byte[Util.HASH_LEN];
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		try {
			// Parse PreHash
			is.read(preHash);
			// Parse Target
			is.read(target);
			// Parse RootHash
			is.read(rootHash);
			// Parse Height
			height = new ID(EQCType.parseEQCBits(is));
			// Parse Timestamp
			timestamp = new ID(EQCType.parseEQCBits(is));
			// Parse Nonce
			nonce = new ID(EQCType.parseEQCBits(is));
			EQCType.assertNoRedundantData(is);
		} catch (IOException | NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During parseEQCHeader error occur: " + e.getMessage());
		}
	}
	
//	public static boolean isValid(byte[] bytes) {
//		byte validCount = 0;
//		byte[] preHash = new byte[Util.HASH_LEN];
//		byte[] target = new byte[TARGET_LEN];
//		byte[] rootHash = new byte[Util.HASH_LEN];
//		int result = 0;
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		byte[] data = null;
//		try {
//			// Parse PreHash
//			result = is.read(preHash);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse Target
//			result = is.read(target);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse RootHash
//			result = is.read(rootHash);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse Height
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				++validCount;
//			}
//			// Parse Timestamp
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//				++validCount;
//			}
//			// Parse Nonce
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				++validCount;
//			}
//		} catch (IOException | NoSuchFieldException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		
//		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
//	}
	
	/**
	 * 
	 */
	public EQCHeader() {
		super();
	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(preHash);
			os.write(target);
			os.write(rootHash);
			os.write(height.getEQCBits());
			os.write(timestamp.getEQCBits());
			os.write(nonce.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(getBytes());
	}
	
	/**
	 * @return the preHash
	 */
	public byte[] getPreHash() {
		return preHash;
	}
	/**
	 * @param preHash the preHash to set
	 */
	public void setPreHash(byte[] preHash) {
		this.preHash = preHash;
	}
	/**
	 * @return the target
	 */
	public byte[] getTarget() {
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(byte[] target) {
		this.target = target;
	}
	/**
	 * @return the rootHash
	 */
	public byte[] getRootHash() {
		return rootHash;
	}
	/**
	 * @param rootHash the rootHash to set
	 */
	public void setRootHash(byte[] rootHash) {
		this.rootHash = rootHash;
	}
	/**
	 * @return the timestamp
	 */
	public ID getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(ID timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}
	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(ID nonce) {
		this.nonce = nonce;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString() 
	 */
	@Override
	public String toString() {
		return 
				"{\n" +
				toInnerJson() +
				"\n}";
	}
	
	public String toInnerJson() {
		return "\"EQCHeader\":" + 
				"{\n" +
					"\"PreHash\":" + "\"" + Util.getHexString(preHash) + "\"" + ",\n" +
					"\"Target\":" + "\"" + Util.getHexString(Util.targetBytesToBigInteger(target).toByteArray()) + "\"" + ",\n" +
					"\"TargetBytes\":" + "\"" + Integer.toHexString(Util.bytesToInt(target)).toUpperCase() + "\"" + ",\n" +
					"\"RootHash\":" + "\"" + Util.dumpBytes(rootHash, 16) + "\"" + ",\n" +
					"\"Height\":" + "\"" + height + "\"" + ",\n" +
					"\"Timestamp\":" + "\"" + Util.getGMTTime(timestamp.longValue()) + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + Integer.toHexString(nonce.intValue()).toUpperCase() + "\"" + "\n" +
				"}";
	}

	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}
	
	public int getMinSize() {
		return min_size;
	}

	/**
	 * @return byte[] the eqcHeader's EQCCHA hash
	 */
	public byte[] getHash() {
		return Util.EQCCHA_MULTIPLE_DUAL_MIX(getBytes(), Util.HUNDREDPULS, true, false);
	}

	@Override
	public boolean isSanity() {
		if(preHash == null || target == null || rootHash == null || height == null || timestamp == null || nonce == null) {
			return false;
		}
		if(preHash.length != Util.HASH_LEN) {
			return false;
		}
		if(target.length != TARGET_LEN) {
			return false;
		}
		if(rootHash.length != Util.HASH_LEN) {
			return false;
		}
		if(timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN) {
			return false;
		}
		if(nonce.getEQCBits().length > MAX_NONCE_LEN) {
			return false;
		}
		return true;
	}

	public boolean isDifficultyValid(AccountsMerkleTree accountsMerkleTree) {
		if (!Arrays.equals(target, Util.cypherTarget(accountsMerkleTree.getHeight().getNextID()))) {
			return false;
		}
		// getHash()
		Log.info(Util.getHexString(getHash()));
		if (new BigInteger(1, getHash()).compareTo(Util.targetBytesToBigInteger(target)) > 0) {
			Log.info("falied");
			return false;
		}
		return true;
	}
	
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, byte[] rootHash) {
		if(!isSanity()) {
			Log.info("Sanity test failed.");
			return false;
		}
		if(!Arrays.equals(preHash, Util.DB().getEQCHeaderHash(accountsMerkleTree.getHeight()))) {
			Log.Error("PreHash is invalid.");
			return false;
		}
		if(!Arrays.equals(this.rootHash, rootHash)) {
			Log.Error("RootHash is invalid.");
			return false;
		}
		if(height.getPreviousID().compareTo(Util.DB().getEQCBlock(accountsMerkleTree.getHeight(), true).getEqcHeader().getHeight()) != 0) {
			Log.Error("Height should be the previous EQCBlock's next Height.");
			return false;
		}
		if(timestamp.compareTo(Util.DB().getEQCBlock(accountsMerkleTree.getHeight(), true).getEqcHeader().getTimestamp()) <= 0) {
			Log.Error("Timestamp should bigger than previous EQCBlock's timestamp.");
			return false;
		}
		if(timestamp.compareTo(new ID(System.currentTimeMillis())) > 0) {
			Log.Error("Timestamp should less than current GMT time.");
			return false;
		}
		return true;
	}

	/**
	 * @return the height
	 */
	public ID getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(ID height) {
		this.height = height;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
