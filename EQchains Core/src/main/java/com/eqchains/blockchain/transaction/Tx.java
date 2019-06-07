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
import java.util.Comparator;

import com.eqchains.blockchain.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.serialization.EQCAddressShapeInheritable;
import com.eqchains.serialization.EQCAddressShapeTypable;
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
public class Tx implements Comparator<Tx>, Comparable<Tx>, EQCAddressShapeTypable, EQCAddressShapeInheritable {
	protected Address address;
	protected long value;
	
	public Tx() {
		address = new Address();
	}

	public Tx(byte[] bytes, Address.AddressShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}

	public Tx(ByteArrayInputStream is, Address.AddressShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		parseBody(is, addressShape);
	}
	
	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
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
	public byte[] getBytes(AddressShape addressShape) {
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
	public byte[] getBin(AddressShape addressShape) {
		byte[] bin = null;
		// Due to the EQCBits itself is embedded in the key-value pair so here need use getBytes
		if(addressShape == AddressShape.ID) {
			bin = getBytes(addressShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(addressShape));
		}
		return bin;
	}
	
	@Override
	public boolean isSanity(AddressShape addressShape) {
		if(address == null) {
			return false;
		}
		if(value < Util.MIN_EQC) {
			return false;
		}
		if(!address.isSanity(addressShape)) {
			return false;
		}
		else {
			if(!address.isGood()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Tx o) {
		// TODO Auto-generated method stub
		return address.getReadableAddress().compareTo(o.getAddress().getReadableAddress());
	}

	@Override
	public int compare(Tx o1, Tx o2) {
		// TODO Auto-generated method stub
		return o1.getAddress().getReadableAddress().compareTo(o2.getAddress().getReadableAddress());
	}

	@Override
	public void parseBody(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		// Parse Address
		if(addressShape == Address.AddressShape.READABLE) {
			address = new Address(EQCType.parseBIN(is));
		}
		else if(addressShape == Address.AddressShape.ID) {
			address = new Address();
			address.setID(new ID(EQCType.parseEQCBits(is)));
		}
		
		// Parse Value
		value = EQCType.eqcBitsToLong(EQCType.parseEQCBits(is));
		
	}

	@Override
	public byte[] getHeaderBytes(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(address.getBin(addressShape));
			os.write(EQCType.longToEQCBits(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

}
