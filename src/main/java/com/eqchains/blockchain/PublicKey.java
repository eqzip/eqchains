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
package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * The PublicKey contains the compressed public key corresponding to the
 * specific address. The public key can be verified with any transaction
 * corresponding to this address.
 * 
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class PublicKey implements EQCTypable {
	/** 
	 * Current PublicKey's relevant address' ID.
	 */
	private ID id = null;
	/**
	 * The No. Address relevant public key which is Bin type.
	 */
	private byte[] publicKey = null;
	/**
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private final static byte VERIFICATION_COUNT = 2;
	
	private boolean isNew;

	public PublicKey() {
		super();
		isNew = false;
	}

	public PublicKey(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data;

		// Parse PublicKey's serial number
		if ((data = EQCType.parseEQCBits(is)) != null) {
			id = new ID(data);
		}

		// Parse publicKey
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			publicKey = data;
		}
	}

	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data;
		byte validCount = 0;

		// Parse PublicKey's serial number
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

		// Parse publicKey
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			++validCount;
		}
		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
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
			os.write(id.getEQCBits());
			Log.info("publicKey raw data len: " + publicKey.length);
			os.write(EQCType.bytesToBIN(publicKey));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public int getBillingSize() {
		int size = 0;
		size += Util.BASIC_SERIAL_NUMBER_LEN;
		if(publicKey == null) {
			throw new NullPointerException("Publickey shouldn't be null.");
		}
		size += EQCType.bytesToBIN(publicKey).length;
		size += EQCType.getEQCTypeOverhead(size);
	    return size;
	}
	
	/**
	 * @return the publickeyID
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @param publickeyID the publickeyID to set
	 */
	public void setID(ID id) {
		this.id = id;
	}

	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
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
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length == 0) {
			if(id == null || publicKey == null) {
				return false;
			}
		}
		else if(addressShape.length == 1) {
			if(addressShape[0] == AddressShape.ID) {
				if(id == null) {
					return false;
				}
			}
			else {
				if(publicKey == null) {
					return false;
				}
			}
		}
		return true;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(publicKey);
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
		PublicKey other = (PublicKey) obj;
		if (!Arrays.equals(publicKey, other.publicKey))
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
		return "\"Publickey\":" + "{\n" + "\"ID\":" + "\"" + ((id == null) ? null : id) + "\"" + ",\n"
				+ "\"Publickey\":" + "\"" + Util.dumpBytes(publicKey, 16) + "\""
				+ "\n" + "}";
	}
	
}
