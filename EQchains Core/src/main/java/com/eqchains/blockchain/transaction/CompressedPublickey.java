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
package com.eqchains.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * The PublicKey contains the compressed public key corresponding to the
 * specific address. The public key can be verified with any transaction
 * corresponding to this address.
 * 
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class CompressedPublickey implements EQCTypable {
	/**
	 * The No. Address relevant public key which is BINX type.
	 */
	private byte[] compressedPublickey = null;
	private boolean isNew;

	/**
	 * Publickey relevant ID used to verify Publickey
	 */
	private ID id;
	
	public CompressedPublickey() {
		super();
		isNew = false;
	}

	public CompressedPublickey(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parsePublickey(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public CompressedPublickey(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parsePublickey(is);
	}
	
	private void parsePublickey(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		// Parse publicKey
		compressedPublickey = EQCType.parseBIN(is);
	}

	/**
	 * Get the PublicKey's bytes for storage it in the EQC block chain.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(compressedPublickey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public int getBillingSize() {
		int size = 1; // One byte for BINX
		size += compressedPublickey.length;
	    return size;
	}

	/**
	 * @return the compressedPublickey
	 */
	public byte[] getCompressedPublickey() {
		return compressedPublickey;
	}

	/**
	 * @param compressedPublickey the compressedPublickey to set
	 */
	public void setCompressedPublickey(byte[] compressedPublickey) {
		this.compressedPublickey = compressedPublickey;
	}

	/**
	 * Get the PublicKey's BIN for storage it in the EQC block chain.
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}

	/**
	 * @return the isNew
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	@Override
	public boolean isSanity() {
		if (compressedPublickey == null) {
			return false;
		}
		if(compressedPublickey.length != Util.P256_PUBLICKEY_LEN && compressedPublickey.length != Util.P521_PUBLICKEY_LEN) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(compressedPublickey);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompressedPublickey other = (CompressedPublickey) obj;
		if (!Arrays.equals(compressedPublickey, other.compressedPublickey))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" + toInnerJson() + "\n}";
	}

	public String toInnerJson() {
		return "\"Publickey\":" + "{\n"
				+ "\"Publickey\":" + "\"" + Util.dumpBytes(compressedPublickey, 16) + "\""
				+ "\n" + "}";
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		Account account = accountsMerkleTree.getAccount(new Passport(AddressTool.AIToAddress(AddressTool.publickeyToAI(compressedPublickey))), true);
		if(!account.isPublickeyExists()) {
			if(!AddressTool.verifyAddressPublickey(account.getPassport().getReadableAddress(), compressedPublickey)) {
				return false;
			}
		}
		else {
			if(!Arrays.equals(account.getPublickey().getCompressedPublickey(), compressedPublickey)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isNULL() {
		return compressedPublickey == null;
	}

	/**
	 * @return the id
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setID(ID id) {
		this.id = id;
	}
	
}
