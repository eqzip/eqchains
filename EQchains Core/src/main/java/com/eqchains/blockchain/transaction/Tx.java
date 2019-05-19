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

import com.eqchains.blockchain.transaction.Address.AddressShape;
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
public class Tx implements Comparator<Tx>, Comparable<Tx>, EQCTypable {
	protected Address address;
	protected long value;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
	 * verified.
	 */
	private final static byte VERIFICATION_COUNT = 2;
	
	public Tx() {
		address = new Address();
	}

	public Tx(byte[] bytes, Address.AddressShape addressShape) throws NoSuchFieldException, IOException {
		super();
		address = new Address();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		
		// Parse Address
		if(addressShape == Address.AddressShape.READABLE) {
			if ((data = EQCType.parseBIN(is)) != null) {
				address.setReadableAddress(EQCType.bytesToASCIISting(data));
			}
		}
		else if(addressShape == Address.AddressShape.ID) {
			if ((data = EQCType.parseEQCBits(is)) != null) {
				address.setID(new ID(data));
			}
		}
		
		// Parse Value
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			value = EQCType.eqcBitsToLong(data);
		}
	}

	public static boolean isValid(byte[] bytes, AddressShape addressShape) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return isValid(is, addressShape) && EQCType.isInputStreamEnd(is);
	}
	
	public static boolean isValid(ByteArrayInputStream is, AddressShape addressShape) throws NoSuchFieldException, IOException {
		byte[] data = null;
		byte validCount = 0;
		
		// Parse Address
		if(addressShape == Address.AddressShape.READABLE || addressShape == AddressShape.AI) {
			if ((data = EQCType.parseBIN(is)) != null && !EQCType.isNULL(data)) {
				++validCount;
			}
		}
		else if(addressShape == Address.AddressShape.ID) {
			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
				++validCount;
			}
		}
		
		// Parse Value
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
			++validCount;
		}
		
		return (validCount == VERIFICATION_COUNT);
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
	 */
	public void setValue(long value) {
		this.value = value;
	}
	
	public void updateValue(long value) {
		this.value += value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (int) (value ^ (value >>> 32));
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
		Tx other = (Tx) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (value != other.value)
			return false;
		return true;
	}
	
	public byte[] getBytes(Address.AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(address.getAddressShapeBin(addressShape));
			os.write(EQCType.longToEQCBits(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * Get the Txin's bytes for storage it in the EQC block chain
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return getBytes(Address.AddressShape.ID);
	}
	
	@Override
	public byte[] getBin(AddressShape addressShape) {
		byte[] bin = null;
		if(addressShape == AddressShape.ID) {
			bin = getBytes(addressShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(addressShape));
		}
		return bin;
	}
	
	/**
	 * Get the TxOut's BIN for storage it in the EQC block chain.
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}
	
	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(address == null) {
			return false;
		}
		if(this instanceof TxOut && value < Util.MIN_EQC) {
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

}
