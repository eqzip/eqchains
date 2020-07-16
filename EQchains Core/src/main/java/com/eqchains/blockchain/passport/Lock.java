/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package com.eqchains.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqchains.blockchain.accountsmerkletree.PassportsMerkleTree;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.serialization.EQCLockShapeTypable;
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
public class Lock implements EQCLockShapeTypable {
	/*
	 * LockShape enum which expressed three types of Lock: Readable, ID and AI.
	 * Readable Lock used for signature the Transaction and RPC for example sign the
	 * Transaction then send the Transaction's bytes to EQC Transaction network. AI
	 * Lock used for store the Lock in LockList in the blockchain. ID Lock used for
	 * EQC Passport for example save the Passport and relevant Lock into EQC
	 * blockchain.
	 */
	public enum LockShape {
		READABLE, AI, ID,
	}

	private ID id = null;
	private String readableLock = null;
	private ID passportID = null;
	private byte[] publickey = null;
	private byte[] code = null;

	/**
	 * @param id
	 * @param key
	 * @param code
	 */
	public Lock(ID id, String readableLock, byte[] code) {
		super();
		this.id = id;
		this.readableLock = readableLock;
		this.code = code;
	}

	public Lock() {
	}
	
	public Lock(String readableLock) {
		this.readableLock = readableLock;
	}

	/**
	 * Create Address according to the bytes from EQC blockchain's Transactions' newAccountList.
	 * 
	 * @param bytes
	 * @throws IOException
	 * @throws NoSuchFieldException
	 */
	public Lock(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseAddress(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public Lock(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parseAddress(is);
	}

	private void parseAddress(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		// Parse ID
		id = new ID(EQCType.parseEQCBits(is));
		
		// Parse Address
		readableLock = Util.AddressTool.AIToAddress(EQCType.parseBIN(is));

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
			os.write(EQCType.bytesToBIN(AddressTool.addressToAI(readableLock)));
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
		size += EQCType.bytesToBIN(Util.AddressTool.addressToAI(readableLock)).length;
		if (code != null) {
			size += EQCType.bytesToBIN(code).length;
		}
		size += EQCType.getEQCTypeOverhead(size);
		return size;
	}

	public AddressType getAddressType() {
		return Util.AddressTool.getAddressType(readableLock);
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
	 * @return the readableLock
	 */
	public String getReadableLock() {
		return readableLock;
	}

	/**
	 * @param readableLock the readableLock to set
	 */
	public void setReadableLock(String readableLock) {
		this.readableLock = readableLock;
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
		result = prime * result + ((readableLock == null) ? 0 : readableLock.hashCode());
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
		Lock other = (Lock) obj;
		if (readableLock == null) {
			if (other.readableLock != null)
				return false;
		} else if (!readableLock.equals(other.readableLock))
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
				+ "\"readableLock\":" + ((readableLock == null)?null:"\"" + readableLock + "\"") + ",\n" + "\"Code\":" + ((code == null)?null:"\"" + Util.getHexString(code) + "\"")
				+ "\n" + "}";
	}

	public boolean isGood() {
		return isGood(null);
	}

	public boolean isGood(CompressedPublickey publickey) {
		if (readableLock == null) {
			return false;
		}
		
		AddressTool.AddressType addressType = Util.AddressTool.getAddressType(readableLock);

		// Check Address length is valid
		if (readableLock.length() > Util.MAX_ADDRESS_LEN || readableLock.length() < Util.MIN_ADDRESS_LEN) {
			return false;
		}

		// Check Address type, CRC32 checksum and generated from Publickey is valid
		if (addressType == AddressType.T1 || addressType == AddressType.T2) {
			if (!AddressTool.verifyAddressCRC32C(readableLock)) {
				return false;
			}
			if (publickey != null) {
				if (!AddressTool.verifyAddressPublickey(readableLock, publickey.getCompressedPublickey())) {
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
	public boolean isSanity(LockShape addressShape) {
		// Here exists one bug need check if code is null due to in mvp phase the code should be null
		if (getAddressType() != AddressType.T1 && getAddressType() != AddressType.T2) {
			return false;
		}
		if (addressShape == null) {
			if (readableLock == null) {
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
			if (addressShape == LockShape.AI || addressShape == LockShape.READABLE) {
				if (readableLock == null) {
					return false;
				}
			} else if(addressShape == LockShape.ID) {
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
		return AddressTool.addressToAI(readableLock);
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
	public byte[] getBytes(LockShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (addressShape == Lock.LockShape.ID) {
				os.write(id.getEQCBits());
			} else if (addressShape == Lock.LockShape.READABLE) {
//				Log.info(Util.dumpBytes(EQCType.stringToASCIIBytes(readableLock), 16));
				os.write(EQCType.stringToASCIIBytes(readableLock));
			} else if (addressShape == Lock.LockShape.AI) {
				os.write(AddressTool.addressToAI(readableLock));
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
	public byte[] getBin(LockShape addressShape) {
		byte[] bin = null;
		// Due to EQCBits bytes is BIN type also so here just get it
		if(addressShape == LockShape.ID) {
			bin = getBytes(addressShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(addressShape));
		}
		return bin;
	}
	
	@Override
	public boolean isValid(PassportsMerkleTree accountsMerkleTree, LockShape addressShape) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean compare(Lock lock) {
		if(!id.equals(lock.getId())) {
			return false;
		}
		if(!readableLock.equals(lock.getReadableLock())) {
			return false;
		}
		if(code != null) {
			return false;
		}
		if(lock.getCode() != null) {
			return false;
		}
		return true;
	}

	/**
	 * @return the passportID
	 */
	public ID getPassportID() {
		return passportID;
	}

	/**
	 * @param passportID the passportID to set
	 */
	public void setPassport_id(ID passportID) {
		this.passportID = passportID;
	}

	/**
	 * @return the publickey
	 */
	public byte[] getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(byte[] publickey) {
		this.publickey = publickey;
	}
	
}
