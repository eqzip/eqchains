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
package com.eqchains.blockchain.account;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.serialization.EQCAddressShapeTypable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Passport implements EQCAddressShapeTypable {
	/*
	 * AddressShape enum which expressed three types of addresses Readable, ID and
	 * AI. <p> Readable Address used for RPC for example send the Transaction's
	 * bytes to EQC Transaction network. <p> AI address used for signature the
	 * Transaction. <p> ID used for EQC blockchain for example save the
	 * Transaction's bytes into EQC blockchain.
	 */
	public enum AddressShape {
		READABLE, AI, ID,
	}

	private ID id = null;
	private String readableAddress = null;
	private byte[] code = null;

	/**
	 * @param id
	 * @param address
	 * @param code
	 */
	public Passport(ID id, String address, byte[] code) {
		super();
		this.id = id;
		this.readableAddress = address;
		this.code = code;
	}

	public Passport() {
	}
	
	public Passport(String readableAddress) {
		this.readableAddress = readableAddress;
	}

	/**
	 * Create Address according to the bytes from EQC blockchain's Transactions' newAccountList.
	 * 
	 * @param bytes
	 * @throws IOException
	 * @throws NoSuchFieldException
	 */
	public Passport(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseAddress(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public Passport(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parseAddress(is);
	}

	private void parseAddress(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		// Parse ID
		id = new ID(EQCType.parseEQCBits(is));
		
		// Parse Address
		readableAddress = Util.AddressTool.AIToAddress(EQCType.parseBIN(is));

//		// Parse Code
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			setCode(data);
//		}
	}
	
	/**
	 * Get the Address' whole bytes include Address' ID, Address' AI
	 * value and relevant code(Optional). For storage Address in newAccountList.
	 * 
	 * @return byte[]
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(id.getEQCBits());
			os.write(EQCType.bytesToBIN(AddressTool.addressToAI(readableAddress)));
//			if (code != null) {
//				os.write(EQCType.bytesToBIN(code));
//			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * Get the address' whole Bin include Address' ID, Address' AI
	 * value and relevant code(Optional). For storage Address in AddressList.
	 * 
	 * @return byte[]
	 */
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	public int getBillingSize() {
		int size = 0;
		size += Util.BASIC_SERIAL_NUMBER_LEN;
		size += EQCType.bytesToBIN(Util.AddressTool.addressToAI(readableAddress)).length;
		if (code != null) {
			size += EQCType.bytesToBIN(code).length;
		}
		size += EQCType.getEQCTypeOverhead(size);
		return size;
	}

	public AddressType getAddressType() {
		return Util.AddressTool.getAddressType(readableAddress);
	}
	
	/**
	 * @return the ID
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @param ID the ID to set
	 * @throws NoSuchFieldException 
	 */
	public void setID(ID id) {
		this.id = id;
	}

	/**
	 * @return the readableAddress
	 */
	public String getReadableAddress() {
		return readableAddress;
	}

	/**
	 * @param readableAddress the readableAddress to set
	 */
	public void setReadableAddress(String readableAddress) {
		this.readableAddress = readableAddress;
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
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readableAddress == null) ? 0 : readableAddress.hashCode());
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
		Passport other = (Passport) obj;
		if (readableAddress == null) {
			if (other.readableAddress != null)
				return false;
		} else if (!readableAddress.equals(other.readableAddress))
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
		return "\"Passport\":" + "{\n" + "\"ID\":" + ((id == null) ? null : "\"" + id + "\"") + ",\n"
				+ "\"ReadableAddress\":" + ((readableAddress == null)?null:"\"" + readableAddress + "\"") + ",\n" + "\"Code\":" + ((code == null)?null:"\"" + Util.getHexString(code) + "\"")
				+ "\n" + "}";
	}

	public boolean isGood() {
		return isGood(null);
	}

	public boolean isGood(CompressedPublickey publickey) {
		if (readableAddress == null) {
			return false;
		}
		
		AddressTool.AddressType addressType = Util.AddressTool.getAddressType(readableAddress);

		// Check Address length is valid
		if (readableAddress.length() > Util.MAX_ADDRESS_LEN || readableAddress.length() < Util.MIN_ADDRESS_LEN) {
			return false;
		}

		// Check Address type, CRC32 checksum and generated from Publickey is valid
		if (addressType == AddressType.T1 || addressType == AddressType.T2) {
			if (!AddressTool.verifyAddressCRC32C(readableAddress)) {
				return false;
			}
			if (publickey != null) {
				if (!AddressTool.verifyAddressPublickey(readableAddress, publickey.getCompressedPublickey())) {
					return false;
				}
			}
		}
		// The others Address Type is invalid
		else {
			return false;
		}

		return true;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		// Here exists one bug need check if code is null due to in mvp phase the code should be null
		if (getAddressType() != AddressType.T1 && getAddressType() != AddressType.T2) {
			return false;
		}
		if (addressShape == null) {
			if (readableAddress == null) {
				return false;
			}
			if (id == null) {
				return false;
			}
			// Check if Address' Serial Number is valid which should >= 1
			if(id.compareTo(ID.ONE) < 0) {
				return false;
			}
		} else {
			if (addressShape == AddressShape.AI || addressShape == AddressShape.READABLE) {
				if (readableAddress == null) {
					return false;
				}
			} else if(addressShape == AddressShape.ID) {
				if (id == null) {
					return false;
				}
				// Check if Address' Serial Number is valid which should >= 1
				if(id.compareTo(ID.ONE) < 0) {
					return false;
				}
			} 
		} 
		
		return true;
	}

	public byte[] getAddressAI() {
		return AddressTool.addressToAI(readableAddress);
	}
	
	/**
	 * Get the Address' bytes according to it's AddressShape(READABLE, AI, ID). For
	 * create the Transaction for storage it in the EQC block chain when
	 * addressShape is ID or for create the Transaction for send it to the EQC miner
	 * network when addressShape is READABLE.
	 * 
	 * @param addressShape
	 * @return byte[]
	 */
	public byte[] getBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (addressShape == Passport.AddressShape.ID) {
				os.write(id.getEQCBits());
			} else if (addressShape == Passport.AddressShape.READABLE) {
//				Log.info(Util.dumpBytes(EQCType.stringToASCIIBytes(readableAddress), 16));
				os.write(EQCType.stringToASCIIBytes(readableAddress));
			} else if (addressShape == Passport.AddressShape.AI) {
				os.write(AddressTool.addressToAI(readableAddress));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * Get the Address' bin according to it's AddressShape(READABLE, AI, ID). For
	 * create the Transaction for storage it in the EQC block chain when
	 * addressShape is ID or for create the Transaction for send it to the EQC miner
	 * network when addressShape is READABLE.
	 * 
	 * @param addressShape
	 * @return byte[]
	 */
	public byte[] getBin(AddressShape addressShape) {
		byte[] bin = null;
		// Due to EQCBits bytes is BIN type also so here just get it
		if(addressShape == AddressShape.ID) {
			bin = getBytes(addressShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(addressShape));
		}
		return bin;
	}
	
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean compare(Passport passport) {
		if(!id.equals(passport.getID())) {
			return false;
		}
		if(!readableAddress.equals(passport.getReadableAddress())) {
			return false;
		}
		if(code != null) {
			return false;
		}
		if(passport.getCode() != null) {
			return false;
		}
		return true;
	}
	
}
