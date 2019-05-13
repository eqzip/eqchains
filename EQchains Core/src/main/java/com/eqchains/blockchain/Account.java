

package com.eqchains.blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.print.attribute.standard.RequestingUserName;

import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * Account table's schema does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public abstract class Account implements EQCTypable {
	protected AccountType accountType;
	protected ID version;
	protected Key key;
	protected Vector<Asset> assetList;
	protected ID assetListSize;
//	private String email; // KYC
	/*
	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
	 */
	protected final static byte VERIFICATION_COUNT = 4;
	
	public enum AccountType {
		ASSET, SUBCHAIN, SMARTCONTRACT, INVALID;
		public static AccountType get(int ordinal) {
			AccountType accountType = null;
			switch (ordinal) {
			case 0:
				accountType = AccountType.ASSET;
				break;
			case 1:
				accountType = AccountType.SUBCHAIN;
				break;
			case 2:
				accountType = AccountType.SMARTCONTRACT;
				break;
			default:
				accountType = AccountType.INVALID;
				break;
			}
			return accountType;
		}
		public boolean isSanity() {
			if((this.ordinal() < ASSET.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public Account(AccountType accountType) {
		super();
		this.accountType = accountType;
		version = ID.ZERO;
		assetList = new Vector<>();
		key = new Key();
	}
	
	public static AccountType parseAccountType(byte[] bytes) {
		AccountType accountType = AccountType.INVALID;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		try {
			if ((data = EQCType.parseEQCBits(is)) != null) {
				accountType = AccountType.get(EQCType.eqcBitsToInt(data));
				
			}
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return accountType;
	}

	public static Account parseAccount(byte[] bytes) {
		Account account = null;
		AccountType accountType = parseAccountType(bytes);

		try {
			if (accountType == AccountType.ASSET) {
				account = new AssetAccount(bytes);
			} else if (accountType == accountType.SUBCHAIN) {
				account = null;
			} else if (accountType == accountType.SMARTCONTRACT) {
				account = null;
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}
	
	public static AccountType parseAccountType(Address address) {
		AccountType accountType = AccountType.INVALID;
		if(address.getType() == AddressType.T1 || address.getType() == AddressType.T2) {
			accountType = AccountType.ASSET;
		}
		return accountType;
	}
	
	public static Account createAccount(Address address) {
		Account account = null;
		AccountType accountType = parseAccountType(address);

		try {
			if (accountType == AccountType.ASSET) {
				account = new AssetAccount();
				account.getKey().setAddress(address);
			} else if (accountType == accountType.SUBCHAIN) {
				account = null;
			} else if (accountType == accountType.SMARTCONTRACT) {
				account = null;
			} 
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}

	public Account(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		
		// Parse AccountType
		if ((data = EQCType.parseEQCBits(is)) != null) {
			accountType = AccountType.get(new ID(data).intValue());
		}
		
		// Parse Version
		data = null;
		if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
			version = new ID(data);
		}
		
		// Parse Key
		data = null;
		if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
			key = new Key(data);
		}
		
		// Parse Asset
		ARRAY array = null;
		if ((array = EQCType.parseARRAY(is)) != null) {
			for(byte[] asset : array.elements) {
				assetList.add(new Asset(asset));
			}
			assetListSize = new ID(array.length);
		}
		
//		// Parse email
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			email = EQCType.bytesToASCIISting(data);
//		}

	}
	
	protected boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		byte validCount = 0;
		
		// Parse AccountType
		if ((data = EQCType.parseEQCBits(is)) != null) {
			++validCount;
		}
		
		// Parse Version
		data = null;
		if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
			++validCount;
		}
		
		// Parse Key
		data = null;
		if (((data = EQCType.parseBIN(is)) != null) && !EQCType.isNULL(data)) {
			++validCount;
		}
		
		// Parse Asset
		ARRAY array = null;
		if ((array = EQCType.parseARRAY(is)) != null) {
			++validCount;
		}
		
//		// Parse email
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			email = EQCType.bytesToASCIISting(data);
//		}
		
		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
	}
	
//	/**
//	 * @return the balance
//	 */
//	public long getBalance() {
//		return balance;
//	}
//	
//	/**
//	 * @param balance the balance to set
//	 */
//	public void setBalance(long balance) {
//		this.balance = balance;
//	}
	
//	/**
//	 * @return the nonce
//	 */
//	public ID getNonce() {
//		return nonce;
//	}
//	
//	/**
//	 * @param nonce the nonce to set
//	 */
//	public void setNonce(ID nonce) {
//		this.nonce = nonce;
//	}
	
//	public void increaseNonce() {
//		this.nonce = nonce.getNextID();
//	}
	
	/**
	 * @return the ID's EQCBits
	 */
	public byte[] getIDEQCBits() {
		return key.getAddress().getIDEQCBits();
	}
	
	/**
	 * @return the ID
	 */
	public ID getID() {
		return key.getAddress().getID();
	}

//	/**
//	 * @return the addressCreateHeight
//	 */
//	public ID getAddressCreateHeight() {
//		return key.getAddressCreateHeight();
//	}
//
//	/**
//	 * @param addressCreateHeight the addressCreateHeight to set
//	 */
//	public void setAddressCreateHeight(ID addressCreateHeight) {
//		this.key.addressCreateHeight = addressCreateHeight;
//	}
//
//	/**
//	 * @return the publickey
//	 */
//	public Publickey getPublickey() {
//		return key.getPublickey();
//	}
//
//	/**
//	 * @param publickey the publickey to set
//	 */
//	public void setPublickey(Publickey publickey) {
//		this.publickey = publickey;
//	}
//
//	/**
//	 * @return the balanceUpdateHeight
//	 */
//	public ID getBalanceUpdateHeight() {
//		return balanceUpdateHeight;
//	}
//
//	/**
//	 * @param balanceUpdateHeight the balanceUpdateHeight to set
//	 */
//	public void setBalanceUpdateHeight(ID balanceUpdateHeight) {
//		this.balanceUpdateHeight = balanceUpdateHeight;
//	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(accountType.getEQCBits());
			os.write(version.getEQCBits());
			os.write(key.getBin());
			Collections.sort(assetList);
			os.write(getAssetArray());
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
	public static class Key implements EQCTypable {
		private Address address;
		private ID addressCreateHeight;
		private Publickey publickey;
		/*
		 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
		 */
		private final static byte VERIFICATION_COUNT = 4;
		
		public Key() {
			// TODO Auto-generated constructor stub
		}
		
		public Key(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;
			
			// Parse Address
			if ((data = EQCType.parseBIN(is)) != null) {
				address = new Address(data);
			}
			
			// Parse addressCreateHeight
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				addressCreateHeight = new ID(data);
			}
			
			// Parse publickey
			data = null;
			if ((data = EQCType.parseBIN(is)) != null) {
				publickey = new Publickey(data);
			}

		}
		
		public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;
			byte validCount = 0;
			
			// Parse Address
			if ((data = EQCType.parseBIN(is)) != null) {
				++validCount;
			}
			
			// Parse addressCreateHeight
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
			
			// Parse publickey
			data = null;
			if ((data = EQCType.parseBIN(is)) != null) {
				if(Publickey.isValid(data)) {
					++validCount;
				}
			}

			return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
		}
		
		public boolean isPublickeyExists() {
			return publickey != null;
		}
		
		/**
		 * @return the Publickey
		 */
		public Publickey getPublickey() {
			return publickey;
		}
		/**
		 * @param Publickey the Publickey to set
		 */
		public void setPublickey(Publickey publickey) {
			this.publickey = publickey;
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

		@Override
		public byte[] getBytes() {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				os.write(address.getBin());
				os.write(addressCreateHeight.getEQCBits());
				os.write(publickey.getBin());
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
			return "\"Key\":" + "\n{\n" + 
					address.toInnerJson() + ",\n" +
					"\"AddressCreateHeight\":" + "\"" + addressCreateHeight + "\"" + ",\n" +
					((publickey==null)?Publickey.NULL():publickey.toInnerJson()) + "\n" +
					"\n" + "}";
		}
		@Override
		public boolean isSanity(AddressShape... addressShape) {
			if(addressShape.length != 0) {
				return false;
			}
			if(!(publickey != null && publickey.isSanity())) {
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

//		/* (non-Javadoc)
//		 * @see java.lang.Object#hashCode()
//		 */
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + Arrays.hashCode(publickey);
//			result = prime * result + ((publickeyCreateHeight == null) ? 0 : publickeyCreateHeight.hashCode());
//			return result;
//		}
//
//		/* (non-Javadoc)
//		 * @see java.lang.Object#equals(java.lang.Object)
//		 */
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			Publickey other = (Publickey) obj;
//			if (!Arrays.equals(publickey, other.publickey))
//				return false;
//			if (publickeyCreateHeight == null) {
//				if (other.publickeyCreateHeight != null)
//					return false;
//			} else if (!publickeyCreateHeight.equals(other.publickeyCreateHeight))
//				return false;
//			return true;
//		}
		
	}
	
//	public void updateBalance(long value, ID assetID) {
//		if(!idAssetExists(assetID)) {
//			Log.info("Asset " + assetID + " doesn't exists in AssetList just create it.");
//			Asset asset = new Asset();
//			asset.setAssetID(assetID);
//			asset.setBalance(0);
//		}
////		balance += value;
//	}
	
	public boolean idAssetExists(ID assetID) {
		for(Asset asset : assetList) {
			if(asset.assetID.equals(assetID)) {
				return true;
			}
		}
		return false;
	}
	
//	@Deprecated
//	public boolean sync() {
//		return EQCBlockChainH2.getInstance().updateAccount(this);
//	}
	
	public byte[] getHash() {
		return Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(getBytes(), Util.HUNDREDPULS, false);
	}
	
	public boolean isPublickeyExists() {
		return key.isPublickeyExists();
	}
	/**
	 * @return the accountType
	 */
	public AccountType getAccountType() {
		return accountType;
	}

	/**
	 * @return the assetList
	 */
	public Vector<Asset> getAssetList() {
		return assetList;
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
					"\"AccountType\":" + "\"" + accountType + "\"" + ",\n" +
					"\"Version\":" + "\"" + version + "\"" + ",\n" +
					key.toInnerJson() + ",\n" +
					"\"AssetList\":" + "\n{\n" + "\"Size\":" + "\"" + assetList.size() + "\"" + ",\n" + 
					"\"List\":" + "\n" + getAssetListString() + "\n}\n" +
				"}";
	}
	protected String getAssetListString() {
		String asset = "[\n";
		if (assetList.size() > 0) {
			for (int i = 0; i < assetList.size() - 1; ++i) {
				asset += assetList.get(i) + ",\n";
			}
			asset += assetList.get(assetList.size() - 1);
		} else {
			asset += null;
		}
		asset += "\n]";
		return asset;
	}
	@Override
	public boolean isSanity(AddressShape... addressShape) {
		if(addressShape.length != 0) {
			return false;
		}
		if(accountType == null || version == null || key == null || assetList == null || assetListSize == null) {
			return false;
		}
		if(!accountType.isSanity() || !version.isSanity() || !key.isSanity() || !assetListSize.isSanity()) {
			return false;
		}
		if(assetListSize.compareTo(new ID(assetList.size())) != 0) {
			return false;
		}
		for(Asset asset : assetList) {
			if(!asset.isSanity()) {
				return false;
			}
		}
		for(int i=0; i<=assetList.size()-1; ++i) {
			if(assetList.get(i).getAssetID().compareTo(assetList.get(i+1).getAssetID()) >= 0) {
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

//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((address == null) ? 0 : address.hashCode());
//		result = prime * result + ((addressCreateHeight == null) ? 0 : addressCreateHeight.hashCode());
//		result = prime * result + (int) (balance ^ (balance >>> 32));
//		result = prime * result + ((balanceUpdateHeight == null) ? 0 : balanceUpdateHeight.hashCode());
//		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
//		result = prime * result + ((publickey == null) ? 0 : publickey.hashCode());
//		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
//		Account other = (Account) obj;
//		if (address == null) {
//			if (other.address != null)
//				return false;
//		} else if (!address.equals(other.address))
//			return false;
//		if (addressCreateHeight == null) {
//			if (other.addressCreateHeight != null)
//				return false;
//		} else if (!addressCreateHeight.equals(other.addressCreateHeight))
//			return false;
//		if (balance != other.balance)
//			return false;
//		if (balanceUpdateHeight == null) {
//			if (other.balanceUpdateHeight != null)
//				return false;
//		} else if (!balanceUpdateHeight.equals(other.balanceUpdateHeight))
//			return false;
//		if (nonce == null) {
//			if (other.nonce != null)
//				return false;
//		} else if (!nonce.equals(other.nonce))
//			return false;
//		if (publickey == null) {
//			if (other.publickey != null)
//				return false;
//		} else if (!publickey.equals(other.publickey))
//			return false;
//		if (version == null) {
//			if (other.version != null)
//				return false;
//		} else if (!version.equals(other.version))
//			return false;
//		return true;
//	}
//	
	public static class Asset implements EQCTypable, Comparator<Asset>, Comparable<Asset> {
		public static final ID EQCOIN = ID.ONE;
		private ID assetID;
		private ID assetCreateHeight;
		private long balance;
		private ID balanceUpdateHeight;
		private ID nonce;
		/*
		 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
		 */
		private final static byte VERIFICATION_COUNT = 4;
		
		public Asset(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;

			// Parse AssetID
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				assetID = new ID(data);
			}
			
			// Parse AssetCreateHeight
			if (((data = EQCType.parseEQCBits(is)) != null)) {
				assetCreateHeight = new ID(data);
			}

			// Parse Balance
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				balance = EQCType.eqcBitsToLong(data);
			}

			// Parse BalanceUpdateHeight
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				balanceUpdateHeight = new ID(data);
			}

			// Parse Nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				nonce = new ID(data);
			}
		}
		
		public Asset() {
			assetID = EQCOIN;
			nonce = ID.ZERO;
		}

		public static boolean isValid(byte[] bytes) throws NoSuchFieldException, IOException {
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			byte[] data = null;
			byte validCount = 0;

			// Parse AssetID
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}
			
			// Parse AssetCreateHeight
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}

			// Parse Balance
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}

			// Parse BalanceUpdateHeight
			data = null;
			if (((data = EQCType.parseEQCBits(is)) != null) && !EQCType.isNULL(data)) {
				++validCount;
			}

			// Parse Nonce
			data = null;
			if ((data = EQCType.parseEQCBits(is)) != null) {
				++validCount;
			}
			
			return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
		}
		
		@Override
		public byte[] getBytes(AddressShape addressShape) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public byte[] getBytes() {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				os.write(assetID.getEQCBits());
				os.write(assetCreateHeight.getEQCBits());
				os.write(EQCType.longToEQCBits(balance));
				os.write(balanceUpdateHeight.getEQCBits());
				os.write(nonce.getEQCBits());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return os.toByteArray();
		}
		@Override
		public byte[] getBin(AddressShape addressShape) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public byte[] getBin() {
			return EQCType.bytesToBIN(getBytes());
		}
		@Override
		public boolean isSanity(AddressShape... addressShape) {
			if(addressShape.length != 0) {
				return false;
			}
			if(assetID == null || balanceUpdateHeight == null || nonce == null) {
				return false;
			}
			if(!assetID.isSanity() || !balanceUpdateHeight.isSanity() || !nonce.isSanity()) {
				return false;
			}
			if(assetID.equals(ID.ONE)) {
				if(balance < Util.MIN_EQC) {
					return false;
				}
			}
			else {
				if(balance < 0) {
					return false;
				}
			}
			return true;
		}
		/**
		 * @return the assetID
		 */
		public ID getAssetID() {
			return assetID;
		}
		/**
		 * @param assetID the assetID to set
		 */
		public void setAssetID(ID assetID) {
			this.assetID = assetID;
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
		public void updateBalance(long balance) {
			this.balance += balance;
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
			nonce = nonce.getNextID();
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
					"\"Asset\":" + 
					"\n{\n" +
						"\"AssetID\":" + "\"" + assetID + "\"" + ",\n" +
						"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
						"\"BalanceUpdateHeight\":" + "\"" + balanceUpdateHeight + "\"" + ",\n" +
						"\"Nonce\":" + "\"" + nonce + "\"" + "\n" +
					"}";
		}
		@Override
		public int compareTo(Asset o) {
			return assetID.compareTo(o.getAssetID());
		}

		@Override
		public int compare(Asset o1, Asset o2) {
			return o1.getAssetID().compareTo(o2.getAssetID());
		}
		
	}
	
	
	
	/**
	 * @return the version
	 */
	public ID getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(ID version) {
		this.version = version;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(Key key) {
		this.key = key;
	}

	/**
	 * @return the Asset
	 */
	public Asset getAsset(ID assetID) {
		Asset asset = null;
		for(Asset asset2 : assetList) {
			if(asset2.getAssetID().equals(assetID)) {
				asset = asset2;
				break;
			}
		}
		return asset;
	}

	public boolean isAssetExists(ID assetID) {
		for(Asset asset2 : assetList) {
			if(asset2.getAssetID().equals(assetID)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param Asset the Asset to set
	 */
	public void setAsset(Asset asset) {
		if(isAssetExists(asset.getAssetID())) {
			Asset asset2 = getAsset(asset.getAssetID());
			asset2 = asset;
		}
		else {
			assetList.add(asset);
		}
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
			"\"Publickey\":null"; 
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
	
	private byte[] getAssetArray() {
		if (assetList.size() == 0) {
			return EQCType.bytesToBIN(null);
		}
		else {
			Vector<byte[]> assets = new Vector<byte[]>();
			for (Asset asset : assetList) {
				assets.add(asset.getBytes());
			}
			return EQCType.bytesArrayToARRAY(assets);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			for (Address address : AccountList) {
//				try {
//					os.write(address.getAIBin());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.Error(e.getMessage());
//				}
//			}
//			return EQCType.bytesToBIN(os.toByteArray());
		}
	}

	/**
	 * Just place a stub at here should create a new SubchainAccount
	 * for Subchains which contains SubchainHead
	 * One more thing...
	 * @author Xun Wang
	 * @date May 10, 2019
	 * @email 10509759@qq.com
	 */
	public class SubchainHead {
		
	}
	
}
