/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
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
import java.util.Comparator;

import com.eqchains.blockchain.accountsmerkletree.PassportsMerkleTree;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.serialization.EQCLockShapeInheritable;
import com.eqchains.serialization.EQCLockShapeTypable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class Tx implements Comparator<Tx>, Comparable<Tx>, EQCLockShapeTypable, EQCLockShapeInheritable {
	protected Lock key;
	protected long value;
	
	public Tx() {
		key = new Lock();
	}

	public Tx(byte[] bytes, Lock.LockShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}

	public Tx(ByteArrayInputStream is, Lock.LockShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		parseBody(is, addressShape);
	}
	
	/**
	 * @return the Passport
	 */
	public Lock getKey() {
		return key;
	}

	/**
	 * @param Lock the Passport to set
	 */
	public void setKey(Lock passport) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 * @throws NoSuchFieldException 
	 */
	public void setValue(long value) {
		this.value = value;
	}
	
	public void addValue(long value) {
		this.value += value;
	}
	
	public void subtractValue(long value) {
		this.value -= value;
	}

	/**
	 * When AddressShpae is ID
	 * Get the Txin's bytes for storage it in the EQC block chain
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	public byte[] getBytes(LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getBodyBytes(addressShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((address == null) ? 0 : address.hashCode());
//		result = prime * result + ((value == null) ? 0 : value.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Tx other = (Tx) obj;
//		if (address == null) {
//			if (other.address != null)
//				return false;
//		} else if (!address.equals(other.address))
//			return false;
//		if (value == null) {
//			if (other.value != null)
//				return false;
//		} else if (!value.equals(other.value))
//			return false;
//		return true;
//	}

	@Override
	public byte[] getBin(LockShape addressShape) {
		byte[] bin = null;
		// Due to the EQCBits itself is embedded in the key-value pair so here need use getBytes
		if(addressShape == LockShape.ID) {
			bin = getBytes(addressShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(addressShape));
		}
		return bin;
	}
	
	@Override
	public boolean isSanity(LockShape addressShape) {
		if(key == null) {
			return false;
		}
//		if(value < Util.MIN_EQC) {
//			return false;
//		}
		if(!key.isSanity(addressShape)) {
			return false;
		}
		else {
			if(!key.isGood()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Tx o) {
		// TODO Auto-generated method stub
		return key.getReadableLock().compareTo(o.getKey().getReadableLock());
	}

	@Override
	public int compare(Tx o1, Tx o2) {
		// TODO Auto-generated method stub
		return o1.getKey().getReadableLock().compareTo(o2.getKey().getReadableLock());
	}

	@Override
	public void parseBody(ByteArrayInputStream is, LockShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		// Parse Address
		if(addressShape == Lock.LockShape.READABLE) {
			key = new Lock(EQCType.bytesToASCIISting(EQCType.parseBIN(is)));
		}
		else if(addressShape == Lock.LockShape.ID) {
			key = new Lock();
			key.setID(new ID(EQCType.parseEQCBits(is)));
		}
		
		// Parse Value
		value = EQCType.eqcBitsToLong(EQCType.parseEQCBits(is));
	}

	@Override
	public byte[] getHeaderBytes(LockShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes(LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(key.getBin(addressShape));
			os.write(EQCType.longToEQCBits(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public boolean isValid(PassportsMerkleTree accountsMerkleTree, LockShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is, LockShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	public boolean compare(Tx tx) {
		if(!key.compare(tx.getKey())) {
			return false;
		}
		if(value != tx.getValue()) {
			return false;
		}
		return true;
	}
	
}
