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
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.security.acl.Owner;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.print.attribute.standard.RequestingUserName;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.serialization.EQCHashInheritable;
import com.eqchains.serialization.EQCHashTypable;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.serialization.SoleUpdate;
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
public abstract class Account implements EQCHashTypable, EQCHashInheritable {
	/**
	 * Header field include AccountType
	 */
	protected AccountType accountType;
	protected ID createHeight;
	protected ID version;
	protected ID versionUpdateHeight;
	/**
	 * Body field include Version, Passport, Publickey, AssetList
	 */
	protected Passport passport;
	protected ID passportCreateHeight;
	protected Publickey publickey;
	protected Vector<Asset> assetList;
	protected ID assetListSize;
	protected ID assetUpdateHeight;
	protected SoleUpdate soleUpdate;
//	private String email; // KYC
	protected final static ID MAX_VERSION = ID.ZERO;
	
	/**
	 * AccountType include ASSET Account and SmartContract Account.
	 * SmartContract Account include AssetSubchainAccount and MiscAccount.
	 * @author Xun Wang
	 * @date May 19, 2019
	 * @email 10509759@qq.com
	 */
	public enum AccountType {
		ASSET, SMARTCONTRACT, ASSETSUBCHAIN, EQCOINSUBCHAIN, MISC, INVALID;
		public static AccountType get(int ordinal) {
			AccountType accountType = null;
			switch (ordinal) {
			case 0:
				accountType = AccountType.ASSET;
				break;
			case 2:
				accountType = AccountType.ASSETSUBCHAIN;
				break;
			case 3:
				accountType = AccountType.EQCOINSUBCHAIN;
				break;
			case 4:
				accountType = AccountType.MISC;
				break;
			default:
				accountType = AccountType.INVALID;
				break;
			}
			return accountType;
		}
		public boolean isSanity() {
			if((this.ordinal() < ASSET.ordinal()) || (this.ordinal() > INVALID.ordinal()) || (this.ordinal() == INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public static AccountType parseAccountType(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		AccountType accountType = AccountType.INVALID;
		accountType = AccountType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		return accountType;
	}

	public static Account parseAccount(byte[] bytes) throws NoSuchFieldException, IllegalStateException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Account account = null;
		AccountType accountType = parseAccountType(is);

		try {
			if (accountType == AccountType.ASSET) {
				account = new AssetAccount(bytes);
			} 
			else if (accountType == accountType.EQCOINSUBCHAIN) {
				account = new EQcoinSubchainAccount(bytes);
			} 
			else if (accountType == accountType.ASSETSUBCHAIN) {
				account = new AssetSubchainAccount(bytes);
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}
	
	public static AccountType parseAccountType(Passport passport) {
		AccountType accountType = AccountType.INVALID;
		if(passport.getAddressType() == AddressType.T1 || passport.getAddressType() == AddressType.T2) {
			accountType = AccountType.ASSET;
		}
		else if(passport.getAddressType() == AddressType.T3) {
			// If code.SmartContractType == Subchain 
		}
		else if(passport.getAddressType() == AddressType.T4) {
			// If code.SmartContractType == Misc
		}
		return accountType;
	}
	
	public static Account createAccount(Passport passport) {
		Account account = null;
		AccountType accountType = parseAccountType(passport);

		try {
			if (accountType == AccountType.ASSET) {
				account = new AssetAccount();
				account.setPassport(passport);
			} else if (accountType == accountType.ASSETSUBCHAIN) {
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
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// Parse Header
		parseHeader(is);
		// Parse Body
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse AccountType
 		accountType = AccountType.get(new ID(EQCType.parseEQCBits(is)).intValue());
 		// Parse CreateHeight
 		createHeight = new ID(EQCType.parseEQCBits(is));
		// Parse Version
		version = new ID(EQCType.parseEQCBits(is));
		// Parse VersionCreateHeight
		versionUpdateHeight = new ID(EQCType.parseEQCBits(is));
	}
	
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse Passport
		passport = new Passport(is);
		// Parse AddressCreateHeight
		passportCreateHeight = new ID(EQCType.parseEQCBits(is));
		// Parse Publickey
		publickey = new Publickey(is);
		// Parse Asset
		ARRAY array = null;
		if (!(array = EQCType.parseARRAY(is)).isNULL()) {
			assetListSize = new ID(array.length);
			ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
			for(int i = 0; i<assetListSize.intValue(); ++i) {
				assetList.add(Asset.parseAsset(iStream));
			}
			EQCType.assertNoRedundantData(iStream);
		} else {
			throw EQCType.NULL_OBJECT_EXCEPTION;
		}
		// Parse AssetUpdateHeight
		assetUpdateHeight = new ID(EQCType.parseEQCBits(is));
//		// Parse email
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			email = EQCType.bytesToASCIISting(data);
//		}
	}
	
	private void init() {
		version = ID.ZERO;
		versionUpdateHeight = ID.ZERO;
		assetList = new Vector<>();
		publickey = new Publickey();
		soleUpdate = new SoleUpdate();
	}
	
	public Account(AccountType accountType) {
		super();
		this.accountType = accountType;
		init();
	}
	
	/**
	 * @return the ID's EQCBits
	 */
	public byte[] getIDEQCBits() {
		return passport.getIDEQCBits();
	}
	
	/**
	 * @return the ID
	 */
	public ID getID() {
		return passport.getID();
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getHeaderBytes());
			os.write(getBodyBytes());
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
	public byte[] getHeaderBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(accountType.getEQCBits());
			os.write(createHeight.getEQCBits());
			os.write(version.getEQCBits());
			os.write(versionUpdateHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(passport.getBytes());
			os.write(passportCreateHeight.getEQCBits());
			if(publickey.isNULL()) {
				os.write(EQCType.NULL);
			}
			else {
				os.write(publickey.getBytes());
			}
			Collections.sort(assetList);
			os.write(getAssetArray());
			os.write(assetUpdateHeight.getEQCBits());
//			// In MVP Phase email always be null so here just append EQCType.NULL
//			os.write(EQCType.NULL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
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
	
//	@Deprecated
//	public boolean sync() {
//		return EQCBlockChainH2.getInstance().updateAccount(this);
//	}
	
	public byte[] getHash() {
		return Util.EQCCHA_MULTIPLE_DUAL(getHashBytes(), Util.HUNDREDPULS, true, false);
	}
	
	public boolean isPublickeyExists() {
		return !publickey.isNULL();
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
					passport.toInnerJson() + ",\n" +
					"\"AddressCreateHeight\":" + "\"" + passportCreateHeight + "\"" + ",\n" +
					((publickey.isNULL())?Publickey.NULL():publickey.toInnerJson()) + ",\n" +
					"\"AssetList\":" + "\n{\n" + "\"Size\":" + "\"" + assetList.size() + "\"" + ",\n" + 
					"\"List\":" + "\n" + getAssetListString() + "\n}\n" +
				"}";
	}
	public String getAssetListString() {
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
	public boolean isSanity() {
		if(accountType == null || version == null || passport == null || passportCreateHeight == null || publickey == null || assetList == null || assetListSize == null) {
			return false;
		}
		if(!accountType.isSanity() || !version.isSanity() || !passport.isSanity(null) || !passportCreateHeight.isSanity() || !publickey.isSanity() || !assetListSize.isSanity()) {
			return false;
		}
		if(version.compareTo(MAX_VERSION) > 0) {
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
		for(Asset asset : assetList) {
			if(asset.getAssetID().equals(assetID)) {
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
		}
	}

	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}

	public byte[] getHashBytes() {
		return getHashBytes(soleUpdate);
	}
	
	@Override
	public byte[] getHashBytes(SoleUpdate soleUpdate) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getHeaderHashBytes(soleUpdate));
			os.write(getBodyHashBytes(soleUpdate));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getHeaderHashBytes(SoleUpdate soleUpdate) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(accountType.getEQCBits());
			os.write(createHeight.getEQCBits());
			soleUpdate.update(os, createHeight);
			os.write(version.getEQCBits());
			os.write(versionUpdateHeight.getEQCBits());
			soleUpdate.update(os, versionUpdateHeight);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBodyHashBytes(SoleUpdate soleUpdate) throws ClassNotFoundException, RocksDBException, SQLException, Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(passport.getBytes());
			os.write(passportCreateHeight.getEQCBits());
			soleUpdate.update(os, passportCreateHeight);
			if(publickey.isNULL()) {
				os.write(EQCType.NULL);
			}
			else {
				os.write(publickey.getHashBytes(soleUpdate));
			}
			Collections.sort(assetList);
			os.write(getAssetArray());
			os.write(assetUpdateHeight.getEQCBits());
			soleUpdate.update(os, assetUpdateHeight);
//			// In MVP Phase email always be null so here just append EQCType.NULL
//			os.write(EQCType.NULL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the createHeight
	 */
	public ID getCreateHeight() {
		return createHeight;
	}

	/**
	 * @param createHeight the createHeight to set
	 */
	public void setCreateHeight(ID createHeight) {
		this.createHeight = createHeight;
	}

	/**
	 * @return the versionUpdateHeight
	 */
	public ID getVersionUpdateHeight() {
		return versionUpdateHeight;
	}

	/**
	 * @param versionUpdateHeight the versionUpdateHeight to set
	 */
	public void setVersionUpdateHeight(ID versionUpdateHeight) {
		this.versionUpdateHeight = versionUpdateHeight;
	}

	/**
	 * @return the assetUpdateHeight
	 */
	public ID getAssetUpdateHeight() {
		return assetUpdateHeight;
	}

	/**
	 * @param assetUpdateHeight the assetUpdateHeight to set
	 */
	public void setAssetUpdateHeight(ID assetUpdateHeight) {
		this.assetUpdateHeight = assetUpdateHeight;
	}

	public byte[] getSignatureHash() throws Exception {
		SoleUpdate soleUpdate = new SoleUpdate();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(passport.getIDEQCBits());
		soleUpdate.update(os, createHeight);
		soleUpdate.update(os, versionUpdateHeight);
		soleUpdate.update(os, passportCreateHeight);
		return os.toByteArray();
	}

	/**
	 * @return the passport
	 */
	public Passport getPassport() {
		return passport;
	}

	/**
	 * @param passport the passport to set
	 */
	public void setPassport(Passport passport) {
		this.passport = passport;
	}

	/**
	 * @return the passportCreateHeight
	 */
	public ID getPassportCreateHeight() {
		return passportCreateHeight;
	}

	/**
	 * @param passportCreateHeight the passportCreateHeight to set
	 */
	public void setPassportCreateHeight(ID passportCreateHeight) {
		this.passportCreateHeight = passportCreateHeight;
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
	
}
