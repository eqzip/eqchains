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
package com.eqchains.blockchain.account;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;

import com.eqchains.blockchain.account.Account.AccountType;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date May 19, 2019
 * @email 10509759@qq.com
 */
public abstract class Asset implements EQCTypable, EQCInheritable, Comparator<Asset>, Comparable<Asset> {

	public static final ID EQCOIN = ID.ONE;
	/*
	 * Header
	 */
	protected AssetType assetType;
	protected ID version;
	protected ID versionUpdateHeight;
	/*
	 * Body
	 */
	protected ID assetID;
	protected ID createHeight;
	protected ID balance;
	protected ID balanceUpdateHeight;
	protected ID nonce;
	protected ID nonceUpdateHeight;
	
	public enum AssetType {
		COIN, MISC, INVALID;
		public static AssetType get(int ordinal) {
			AssetType assetType = null;
			switch (ordinal) {
			case 0:
				assetType = AssetType.COIN; // Include EQcoin and all AssetSubchain for example ERC20 Token
				break;
			case 1:
				assetType = AssetType.MISC;	// Need do more job to determine the type of MISC for example the unique artwork.
				break;
			default:
				assetType = AssetType.INVALID;
				break;
			}
			return assetType;
		}
		public boolean isSanity() {
			if((this.ordinal() < COIN.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public static AssetType parseAssetType(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		is.mark(0);
		AssetType assetType = AssetType.INVALID;
		assetType = AssetType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		is.reset();
		return assetType;
	}
	
	public static Asset parseAsset(ByteArrayInputStream is)
			throws NoSuchFieldException, IllegalStateException, IOException {
		Asset asset = null;
		AssetType assetType = parseAssetType(is);

		if (assetType == AssetType.COIN) {
			asset = new CoinAsset(is);
		} else if (assetType == AssetType.MISC) {
			asset = new MiscAsset(is);
		}
		return asset;
	}
	
	public Asset(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is);
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public Asset(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parseHeader(is);
		parseBody(is);
	}
	
	public Asset(AssetType assetType) {
		this.assetType = assetType;
		version = ID.ZERO;
		assetID = Asset.EQCOIN;
		balance = ID.ZERO;
		nonce = ID.ZERO;
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
	@Override
	public boolean isSanity() {
		if(assetType == null || version == null || versionUpdateHeight == null || assetID == null || createHeight == null || balance == null || balanceUpdateHeight == null || nonce == null || nonceUpdateHeight == null) {
			return false;
		}
		if(!version.isSanity() || !versionUpdateHeight.isSanity() || !assetID.isSanity() || !createHeight.isSanity() || !balance.isSanity() || !balanceUpdateHeight.isSanity() || !nonce.isSanity() || !nonceUpdateHeight.isSanity()) {
			return false;
		}
		if(assetID.equals(Asset.EQCOIN)) {
			if(balance.compareTo(new ID(Util.MIN_EQC)) < 0) {
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
	public ID getBalance() {
		return balance;
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
					"\"AssetType\":" + "\"" + assetType + "\"" + ",\n" +
					"\"Version\":" + "\"" + version + "\"" + ",\n" +
					"\"VersionUpdateHeight\":" + "\"" + versionUpdateHeight + "\"" + ",\n" +
					"\"AssetID\":" + "\"" + assetID + "\"" + ",\n" +
					"\"CreateHeight\":" + "\"" + createHeight + "\"" + ",\n" +
					"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
					"\"BalanceUpdateHeight\":" + "\"" + balanceUpdateHeight + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
					"\"NonceUpdateHeight\":" + "\"" + nonceUpdateHeight + "\"" + "\n" +
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
	
	@Override
	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse AssetType
		assetType = AssetType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		
		// Parse Version
		version = new ID(EQCType.parseEQCBits(is));
		
		// Parse VersionUpdateHeight
		versionUpdateHeight = new ID(EQCType.parseEQCBits(is));
	}
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse AssetID
		assetID = new ID(EQCType.parseEQCBits(is));
		
		// Parse AssetCreateHeight
		createHeight = new ID(EQCType.parseEQCBits(is));
		
		// Parse Balance
		balance = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
		
		// Parse BalanceUpdateHeight
		balanceUpdateHeight = new ID(EQCType.parseEQCBits(is)); 
		
		// Parse Nonce
		nonce = new ID(EQCType.parseEQCBits(is));
		
		// Parse NonceUpdateHeight
		nonceUpdateHeight = new ID(EQCType.parseEQCBits(is)); 
	}
	@Override
	public byte[] getHeaderBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(assetType.getEQCBits());
			os.write(version.getEQCBits());
			os.write(versionUpdateHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(assetID.getEQCBits());
			os.write(createHeight.getEQCBits());
			os.write(balance.getEQCBits());
			os.write(balanceUpdateHeight.getEQCBits());
			os.write(nonce.getEQCBits());
			os.write(nonceUpdateHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the assetType
	 */
	public AssetType getAssetType() {
		return assetType;
	}

	/**
	 * @param assetType the assetType to set
	 */
	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
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
	 * For system's security here need check if balance is enough for example SmartContract maybe provide wrong input amount
	 * @param amount
	 */
	public void withdraw(ID amount) {
		EQCType.assertNotBigger(amount, balance);
		balance = balance.subtract(amount);
	}
	
	public void deposit(ID amount) {
		if(assetID.equals(EQCOIN)) {
			EQCType.assertNotBigger(amount, ID.valueOf(Util.MAX_EQC));
		}
		balance = balance.add(amount);
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
	 * @return the nonceUpdateHeight
	 */
	public ID getNonceUpdateHeight() {
		return nonceUpdateHeight;
	}

	/**
	 * @param nonceUpdateHeight the nonceUpdateHeight to set
	 */
	public void setNonceUpdateHeight(ID nonceUpdateHeight) {
		this.nonceUpdateHeight = nonceUpdateHeight;
	}
	
}
