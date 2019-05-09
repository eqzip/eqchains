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
import java.util.Arrays;

import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * Account table's schema does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public class Account implements EQCTypable {
	private ID version;
	private Address address;
	private ID addressCreateHeight;
	private Publickey publickey;
	private long balance;
	private ID balanceUpdateHeight;
	private ID nonce;
//	private String email;
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	private final static byte VERIFICATION_COUNT = 7;
	
	public Account() {
		super();
		version = ID.ZERO;
//		nonce = ID.ZERO;
	}

	public Account(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		
		// Parse Version
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			version = new ID(data);
		}
		
		// Parse Address
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			address = new Address(data);
		}
		
		// Parse addressCreateHeight
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			addressCreateHeight = new ID(data);
		}
		
		// Parse Publickey
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			if(!EQCType.isNULL(data)) {
				publickey = new Publickey(data);
			}
		}
		
		// Parse balance
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			balance = EQCType.eqcBitsToLong(data);
		}
		
		// Parse balanceUpdateHeight
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			balanceUpdateHeight = new ID(data);
		}

		// Parse nonce
		if ((data = EQCType.parseEQCBits(is)) != null) {
			nonce = new ID(data);
		}
		
//		// Parse email
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			email = EQCType.bytesToASCIISting(data);
//		}

	}
	
	public boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;
		
		// Parse Version
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			version = new ID(data);
		}

		// Parse ID
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			++validCount;
		}

		// Parse Address
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			++validCount;
		}
		
		// Parse addressCreateHeight
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}
		
		// Parse Publickey
		data = null;
		if ((data = EQCType.parseBIN(is)) != null) {
			++validCount;
		}
		
		// Parse balance
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}
		
		// Parse balanceUpdateHeight
		data = null;
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

//		// Parse transactionAccumulateHash
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			++validCount;
//		}
		
		// Parse nonce
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}

//		// Parse email
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			++validCount;
//		}
		
		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
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
	 * @return the balance
	 */
	public long getBalance() {
		return balance;
	}
	
	/**
	 * @param balance the balance to set
	 */
	public void setBalance(long balance) {
		this.balance = balance;
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
	
	public void increaseNonce() {
		this.nonce = nonce.getNextID();
	}
	
	/**
	 * @return the ID's EQCBits
	 */
	public byte[] getIDEQCBits() {
		return address.getIDEQCBits();
	}
	
	/**
	 * @return the ID
	 */
	public ID getID() {
		return address.getID();
	}

	/**
	 * @return the addressCreateHeight
	 */
	public ID getAddressCreateHeight() {
		return addressCreateHeight;
	}

	/**
	 * @param addressCreateHeight the addressCreateHeight to set
	 */
	public void setAddressCreateHeight(ID addressCreateHeight) {
		this.addressCreateHeight = addressCreateHeight;
	}

	/**
	 * @return the publickey
	 */
	public Publickey getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(Publickey publickey) {
		this.publickey = publickey;
	}

	/**
	 * @return the balanceUpdateHeight
	 */
	public ID getBalanceUpdateHeight() {
		return balanceUpdateHeight;
	}

	/**
	 * @param balanceUpdateHeight the balanceUpdateHeight to set
	 */
	public void setBalanceUpdateHeight(ID balanceUpdateHeight) {
		this.balanceUpdateHeight = balanceUpdateHeight;
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(version.getEQCBits());
			os.write(address.getBin());
			os.write(addressCreateHeight.getEQCBits());
			if(publickey != null) {
				os.write(publickey.getBin());
			}
			else {
				os.write(EQCType.NULL);
			}
			os.write(EQCType.longToEQCBits(balance));
			os.write(balanceUpdateHeight.getEQCBits());
			os.write(nonce.getEQCBits());
//			// In MVP Phase email always be null so here just append EQCType.NULL
//			os.write(EQCType.NULL);
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
	 * @author Xun Wang
	 * @date Dec 14, 2018
	 * @email 10509759@qq.com
	 */
	public static class Publickey implements EQCTypable {
		private byte[] publickey;
		private ID publickeyCreateHeight;
		/*
		 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
		 */
		private final static byte VERIFICATION_COUNT = 2;
		
		public Publickey() {
			// TODO Auto-generated constructor stub
		}
		
		public Publickey(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;
			
			// Parse publickey
			if ((data = EQCType.parseBIN(is)) != null) {
				publickey =data;
			}

			// Parse publickeyCreateHeight
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				publickeyCreateHeight = new ID(data);
			}

		}
		
		public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;
			byte validCount = 0;
			
			// Parse publickey
			if ((data = EQCType.parseBIN(is)) != null) {
				++validCount;
			}

			// Parse publickeyCreateHeight
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
			
			return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
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
		/**
		 * @return the publickeyCreateHeight
		 */
		public ID getPublickeyCreateHeight() {
			return publickeyCreateHeight;
		}
		/**
		 * @param publickeyCreateHeight the publickeyCreateHeight to set
		 */
		public void setPublickeyCreateHeight(ID publickeyCreateHeight) {
			this.publickeyCreateHeight = publickeyCreateHeight;
		}

		@Override
		public byte[] getBytes() {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				os.write(EQCType.bytesToBIN(publickey));
				os.write(publickeyCreateHeight.getEQCBits());
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
			return "\"Publickey\":" + "{\n" + "\"Publickey\":" + ((publickey == null)?null:"\"" + Util.dumpBytes(publickey, 16) + "\"") + ",\n"
					+ "\"PublickeyCreateHeight\":" + "\"" + publickeyCreateHeight + "\""
					+ "\n" + "}";
		}
		
		public static String NULL() {
			return 
			"\"Publickey\":\"null\""; 
		}

		@Override
		public boolean isSanity(AddressShape... addressShape) {
			if(addressShape.length != 0) {
				return false;
			}
			if(!(publickey != null && publickeyCreateHeight != null)) {
				return false;
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
			result = prime * result + Arrays.hashCode(publickey);
			result = prime * result + ((publickeyCreateHeight == null) ? 0 : publickeyCreateHeight.hashCode());
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
			Publickey other = (Publickey) obj;
			if (!Arrays.equals(publickey, other.publickey))
				return false;
			if (publickeyCreateHeight == null) {
				if (other.publickeyCreateHeight != null)
					return false;
			} else if (!publickeyCreateHeight.equals(other.publickeyCreateHeight))
				return false;
			return true;
		}
		
	}
	
	public void updateBalance(long value) {
		balance += value;
	}
	
//	@Deprecated
//	public boolean sync() {
//		return EQCBlockChainH2.getInstance().updateAccount(this);
//	}
	
	public byte[] getHash() {
		return Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(getBytes(), Util.HUNDREDPULS, false);
	}
	
	public boolean isPublickeyExists() {
		return publickey != null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}
	
	public String toInnerJson() {
		return 
				"\"Account\":" + 
				"\n{\n" +
					address.toInnerJson() + ",\n" +
					"\"AddressCreateHeight\":" + "\"" + addressCreateHeight + "\"" + ",\n" +
					((publickey==null)?Publickey.NULL():publickey.toInnerJson()) + ",\n" +
					"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
					"\"BalanceUpdateHeight\":" + "\"" + balanceUpdateHeight + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce.toString(16) + "\"" + "\n" +
				"}";
	}

	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length != 0) {
			return false;
		}
		if(balance < Util.MIN_EQC) {
			return false;
		}
		if((address == null) || (addressCreateHeight == null) || (balanceUpdateHeight == null) || (nonce == null)) {
			return false;
		}
		if(!address.isSanity(addressShape)) {
			return false;
		}
		if(publickey != null) {
			if(!publickey.isSanity()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public byte[] getBytes(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return getBytes();
	}

	@Override
	public byte[] getBin(AddressShape addressShape) {
		// TODO Auto-generated method stub
		return getBin();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((addressCreateHeight == null) ? 0 : addressCreateHeight.hashCode());
		result = prime * result + (int) (balance ^ (balance >>> 32));
		result = prime * result + ((balanceUpdateHeight == null) ? 0 : balanceUpdateHeight.hashCode());
		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
		result = prime * result + ((publickey == null) ? 0 : publickey.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		Account other = (Account) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (addressCreateHeight == null) {
			if (other.addressCreateHeight != null)
				return false;
		} else if (!addressCreateHeight.equals(other.addressCreateHeight))
			return false;
		if (balance != other.balance)
			return false;
		if (balanceUpdateHeight == null) {
			if (other.balanceUpdateHeight != null)
				return false;
		} else if (!balanceUpdateHeight.equals(other.balanceUpdateHeight))
			return false;
		if (nonce == null) {
			if (other.nonce != null)
				return false;
		} else if (!nonce.equals(other.nonce))
			return false;
		if (publickey == null) {
			if (other.publickey != null)
				return false;
		} else if (!publickey.equals(other.publickey))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
}
