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
package com.eqzip.eqcoin.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqzip.eqcoin.blockchain.Address.AddressShape;
import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
import com.eqzip.eqcoin.serialization.EQCTypable;
import com.eqzip.eqcoin.serialization.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.ID;
import com.eqzip.eqcoin.util.Util;
import com.eqzip.eqcoin.util.Util.AddressTool;
import com.eqzip.eqcoin.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Address implements EQCTypable {
	/*
	 * AddressShape enum which expressed two types of addresses String and Serial
	 * Number. <p> ADDRESS String used for RPC for example send the Transaction's
	 * bytes to EQC Transaction network or used for signature the Transaction. <p>
	 * id Serial Number used for EQC block chain for example save the
	 * Transaction's bytes into AVRO file.
	 */
	public enum AddressShape {
		READABLE, ID, AI
	}
	private ID id = null;
	private String address = null;
//	private final String email = null; // not support in mvp status
	private byte[] code = null;
	
	/**
	 * If code isn't null the use this field to save the codeHash.
	 * Also use codeHash to get the getBytesWithCodeHash.
	 */
	private byte[] codeHash = null;
	
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	private final static byte VERIFICATION_COUNT = 3;

	/**
	 * @param id
	 * @param address
	 * @param code
	 */
	public Address(ID id, String address, byte[] code) {
		super();
		this.id = id;
		this.address = address;
		setCode(code);
	}

	public Address() {

	}
	
	public Address(String address) {
		this.address = address;
//		this.id = EQCBlockChainH2.getInstance().getAddressID(this);
	}

	/**
	 * Create Address according to the bytes from EQC block chain's avro storage.
	 * @param bytes
	 * @throws IOException 
	 * @throws NoSuchFieldException 
	 */
	public Address(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;

		// Parse ID
		if ((data = EQCType.parseEQCBits(is)) != null) {
			id = new ID(data);
		}

		// Parse Address
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			address = Util.AddressTool.AIToAddress(data);
		}

		// Parse Code
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			setCode(data);
		}
	}

	public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;

		// Parse ID
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

		// Parse Address
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			++validCount;
		}
		
		// Check if include the code in the address
		if (!EQCType.isInputStreamEnd(is)) {
			// Parse Code
			data = null;
			if ((data = EQCType.parseBIN(is)) != null) {
				++validCount;
			}
		}
		else {
			// Without code just update the validCount for verify the result
			++validCount;
		}
		
		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}

	/**
	 * Get the Address' whole bytes include Address' Serial Number, Address' string value and relevant code(Optional). 
	 * For storage Address in EQC block chain.
	 * @return byte[] 
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(id.getEQCBits());
			os.write(EQCType.bytesToBIN(Util.AddressTool.addressToAI(address)));
			if (code != null) {
				os.write(EQCType.bytesToBIN(code));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * Get the Address' whole bytes include Address' Serial Number, Address' string value and relevant code Hash(Optional). 
	 * For storage Address in EQC block chain.
	 * @return byte[] 
	 */
	public byte[] getBytesWithCodeHash() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(id.getEQCBits());
			os.write(EQCType.bytesToBIN(Util.stringToASCIIBytes(address)));
			if (codeHash != null) {
				os.write(EQCType.bytesToBIN(codeHash));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * Get the address' whole Bin include Address' Serial Number, Address' string value and relevant code(Optional). 
	 * For storage Address in EQC block chain.
	 * @return byte[] 
	 */
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}
	
	/**
	 * Get the Address' whole AIBytes include Address' Serial Number, Address' AI(1 byte version + 16 bytes EQCCHA's hash) value and relevant code(Optional). 
	 * For storage Address in EQC block chain.
	 * @return byte[] 
	 */
	public byte[] getAIBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(id.getEQCBits());
			os.write(EQCType.bytesToBIN(getAddressAI()));
			if (code != null) {
				os.write(EQCType.bytesToBIN(code));
			}
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
		size += EQCType.bytesToBIN(Util.AddressTool.addressToAI(address)).length;
		if (code != null) {
			size += EQCType.bytesToBIN(code).length;
		}
	    return EQCType.getEQCTypeOverhead(size);
	}
	
	/**
	 * Get the address' whole AIBin include Address' Serial Number, Address' AI(1 byte version + 16 bytes EQCCHA's hash) value and relevant code(Optional). 
	 * For storage Address in EQC block chain.
	 * @return byte[] 
	 */
	public byte[] getAIBin() {
		return EQCType.bytesToBIN(getAIBytes());
	}
	
	/**
	 * Get the address' bytes which is Address' Serial Number or Address' string value. 
	 * For create the Transaction for storage it in the EQC block chain when addressShape is id or
	 * for create the Transaction for send it to the EQC miner network when addressShape is ADDRESS.
	 * @param addressShape
	 * @return byte[]
	 */
	public byte[] getBytes(Address.AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if(addressShape == Address.AddressShape.ID) {
				os.write(id.getEQCBits());
			}
			else if(addressShape == Address.AddressShape.READABLE) {
				os.write(EQCType.stringToBIN(address));
			}
			else if(addressShape == Address.AddressShape.AI) {
				os.write(EQCType.bytesToBIN(Util.AddressTool.addressToAI(address)));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public AddressType getType() {
		return Util.AddressTool.getAddressType(address);
	}

	/**
	 * @return the ID
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @param ID the ID to set
	 */
	public void setID(ID id) {
		this.id = id;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the code
	 */
	public byte[] getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(byte[] code) {
		this.code = code;
		calculateCodeHash(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + Arrays.hashCode(code);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		Address other = (Address) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (!Arrays.equals(code, other.code))
			return false;
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}

	public String toInnerJson() {
		return "\"Address\":" + "{\n" + "\"sn\":" + "\"" + ((id == null)?null:id.longValue()) + "\"" + ",\n"
				+ "\"address\":" + "\"" + address + "\"" + ",\n" + "\"code\":" + "\"" + Util.getHexString(code) + "\""
				+ "\n" + "}";
	}
	
	public boolean isGood() {
		return isGood(null);
	}
	
	public boolean isGood(PublicKey publickey) {
		AddressTool.AddressType addressType = Util.AddressTool.getAddressType(address);
		
		if((address == null) || (id == null)) {
			return false;
		}
		
		// Check Address length is valid
		if(address.length() > Util.MAX_ADDRESS_LEN || address.length() < Util.MIN_ADDRESS_LEN) {
			return false;
		}
		
		// Check Address type, CRC32 checksum and generated from Publickey is valid
		if(addressType == AddressType.T1 || addressType == AddressType.T2) {
			if(!AddressTool.verifyAddress(address)) {
				return false;
			}
			if(publickey != null) {
				if(!AddressTool.verifyAddress(address, publickey.getPublicKey())) {
					return false;
				}
			}
		}
		// The others Address Type is invalid
		else {
			return false;
		}
		
		// Check if Address' Serial Number is valid which should >= 1
		if(id.compareTo(ID.ONE) < 0) {
			return false;
		}
		
//		else if(addressType == AddressType.T3) {
//			if(code == null) {
//				isValid = false;
//			}
//			else {
//				if(code.length > Util.MAX_T3_ADDRESS_CODE_LEN) {
//					isValid = false;
//				}
//			}
//			if(!AddressTool.verifyAddress(address) || !AddressTool.verifyAddress(address, code)) {
//				isValid = false;
//			}
//		}
		return true;
	}

	/**
	 * @return the codeHash
	 */
	public byte[] getCodeHash() {
		return codeHash;
	}

	/**
	 * @param codeHash the codeHash to set
	 */
	public void setCodeHash(byte[] codeHash) {
		this.codeHash = codeHash;
	}
	
	public byte[] calculateCodeHash(byte[] bytes) {
		if(bytes != null) {
			codeHash = Util.EQCCHA_MULTIPLE(bytes, Util.ONE, true);
		}
		return codeHash;
	}
	
	public byte[] getAddressAI() {
		return Util.AddressTool.addressToAI(address);
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length == 0) {
			if(address == null) {
				return false;
			}
			if(getType() != AddressType.T1 || getType() != AddressType.T2) {
				return false;
			}
			if(id == null) {
				return false;
			}
		}
		else if(addressShape.length == 1) {
			if(addressShape[0] == AddressShape.AI || addressShape[0] == AddressShape.READABLE) {
				if(address == null) {
					return false;
				}
			}
			else {
				if(id == null) {
					return false;
				}
			}
		}
		else {
			return false;
		}
		return true;
	}

}
