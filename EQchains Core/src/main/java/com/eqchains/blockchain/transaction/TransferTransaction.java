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

import java.awt.Window.Type;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.AssetAccount;
import com.eqchains.blockchain.account.Publickey;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Transaction.TXFEE_RATE;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransferTransaction extends Transaction {
	protected Vector<TxOut> txOutList;
	public final static int MIN_TXOUT = 1;
	public final static int MAX_TXOUT = 10;
	
	private void init() {
		txOutList = new Vector<TxOut>();
	}
	
	public TransferTransaction(TransactionType transactionType) {
		super(transactionType);
		init();
	}

	public TransferTransaction(byte[] bytes, Passport.AddressShape addressShape)
			throws NoSuchFieldException, IOException, UnsupportedOperationException, NoSuchFieldException, IllegalStateException {
		super(TransactionType.TRANSFER);
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is, addressShape);
		EQCType.assertNoRedundantData(is);
	}

	public TransferTransaction() {
		super(TransactionType.TRANSFER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getRPCBytes()
	 */
	@Override
	public byte[] getRPCBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(getBytes(Passport.AddressShape.READABLE)));
			os.write(EQCType.bytesToBIN(publickey.getPublicKey()));
			os.write(EQCType.bytesToBIN(signature));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
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
		result = prime * result + Arrays.hashCode(signature);
		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
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
		TransferTransaction other = (TransferTransaction) obj;
		if (!Arrays.equals(signature, other.signature))
			return false;
		if (txIn == null) {
			if (other.txIn != null)
				return false;
		} else if (!txIn.equals(other.txIn))
			return false;
		if (txOutList == null) {
			if (other.txOutList != null)
				return false;
		} else {
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> originalTxOutList = new Vector<TxOut>();
			for (TxOut txOut : txOutList) {
				originalTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(originalTxOutList);
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> targetTxOutList = new Vector<TxOut>();
			for (TxOut txOut : other.txOutList) {
				targetTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(targetTxOutList);
			// Compare temporarily saved TxOut collections sorted alphabetically in
			// alphabetical order.
			if (!originalTxOutList.equals(targetTxOutList))
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}

	public String toInnerJson() {
		return

		"\"TransferTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"Signature\":" + ((signature == null) ? null : "\"" + Util.getHexString(signature) + "\"") + ",\n" + "\"Publickey\":" 
				+ ((publickey.getPublicKey() == null) ? null : "\"" + Util.getHexString(publickey.getPublicKey()) + "\"")+ "\n" + "}";
	}

	/**
	 * 0. 验证Transaction的完整性： 0.1 对于CoinBase transaction至少包括一个TxOut。 0.2 对于非CoinBase
	 * transaction至少包括一个TxOut&一个TxIn。 1. 验证TxIn余额是否足够？
	 * 之前高度的余额减去当前EQCBlock中之前的交易记录中已经花费的余额。 2. 验证TxIn address是否有效&和公钥是否一致？ 3.
	 * 验证TxIn‘s block header‘s hash+bin(TxIn+TxOut）的签名能否通过？ 4.
	 * 验证TxHash是否在之前的区块中不存在，也即此交易是唯一的交易。防止重放攻击。
	 * 验证Signature在之前的区块中&当前的EQCBlock中不存在。防止重放攻击。 5. 验证TxOut的数量是不是大于等于1&小于等于10。 6.
	 * 验证TxOut的地址是不是唯一的存在？也即每个TxOut地址只能出现一次。 7. 验证TxOut是否小于TxIn？ 8.
	 * 验证是否TxFee大于零，验证是否所有的Txin&TxOut的Value大于零。 // 9. 验证TxFee是否足够？
	 * 
	 * @return
	 * @throws RocksDBException 
	 * @throws IllegalStateException 
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws Exception 
	 */
	public boolean isValid(AccountsMerkleTree accountsMerkleTree, AddressShape addressShape) throws NoSuchFieldException, IllegalStateException, RocksDBException, IOException, Exception {
		
		Account txInAccount = accountsMerkleTree.getAccount(txIn.getPassport());
		
		if(!isSanity(addressShape)) {
			return false;
		}

		// Check Nonce is positive
		if (!isNoncePositive()) {
			return false;
		}

		// Check if Publickey's ID is equal to TxIn's ID
		if (!publickey.getID().equals(txIn.getPassport().getID())) {
			return false;
		}

		// Check if Publickey exists in Account and equal to current Publickey
		if (txInAccount.isPublickeyExists()) {
			if (!Arrays.equals(txInAccount.getPublickey().getPublickey(), publickey.getPublicKey())) {
				return false;
			}
		} else {
			// Verify Publickey
			if (AddressTool.verifyAddressPublickey(txIn.getPassport().getReadableAddress(), publickey.getPublicKey())) {
				return false;
			}
		}
		
		// Check balance
		if (txIn.getValue() + Util.MIN_EQC > txInAccount.getAsset(getAssetID()).getBalance().longValue()) {
			return false;
		}

		// Check if the number of TxOut is greater than MIN_TXOUT and less than or equal to MAX_TXOUT
		if (!isTxOutNumberValid()) {
			return false;
		}

		// Check if the TxOut's Address is unique
		if (!isTxOutAddressUnique()) {
			return false;
		}

		// Check if TxOut's Address is valid
		if (!isTxOutAddressValid()) {
			return false;
		}

		// Check if TxOut's Address doesn't include TxIn
		if (isTxOutAddressIncludeTxInAddress()) {
			return false;
		}

		// Check if all TxIn and TxOut's value is valid
		if (!isAllValueValid()) {
			return false;
		}

		// Check if TxFeeLimit is valid
		if (!isTxFeeLimitValid()) {
			return false;
		}

		// Verify if Transaction's signature can pass
		if (!verify(accountsMerkleTree)) {
			return false;
		}

		return true;
	}

	@Override
	public int getMaxBillingSize() {
		int size = 0;

		// Nonce
		size += EQCType.bigIntegerToEQCBits(nonce).length;

		// TxIn Serial Number size
		size += Util.BASIC_SERIAL_NUMBER_LEN;//txIn.getAddress().getID().getEQCBits().length;
		// TxIn value's size
		size += Util.BASIC_VALUE_NUMBER_LEN;

		// TxOut size
		for (TxOut txOut : txOutList) {
			size += Util.BASIC_SERIAL_NUMBER_LEN;
			size += EQCType.longToEQCBits(txOut.getValue()).length;
		}

		// For Binxx's overhead size
		size += EQCType.getEQCTypeOverhead(size);

		// Transaction's AddressList size which storage the Address
		for (TxOut txOut : txOutList) {
			size += txOut.getPassport().getBillingSize();
		}

		// Transaction's PublickeyList size
		size += publickey.getBillingSize();

		// Transaction's Signature size
		if(txIn.getPassport().getAddressType() == AddressType.T1) {
			size += Util.P256_BASIC_SIGNATURE_LEN;
		}
		else if(txIn.getPassport().getAddressType() == AddressType.T2) {
			size += Util.P521_BASIC_SIGNATURE_LEN;
		}
		Log.info("Total size: " + size);
		return size;
	}

//	@Override
//	public int getBillingSize() {
//		int size = 0;
//
//		// Transaction's Serial Number format's size which storage in the EQC Blockchain
//		size += getBin().length;
//		Log.info("ID size: " + size);
//
//		// Transaction's AddressList size which storage the new Address
//		for (TxOut txOut : txOutList) {
//			if (txOut.isNew()) {
//				size += txOut.getAddress().getBin(AddressShape.AI).length;
//				Log.info("New TxOut: " + txOut.getAddress().getBin(AddressShape.AI).length);
//			}
//		}
//
//		// Transaction's PublickeyList size
//		if (publickey.isNew()) {
//			size += publickey.getBin().length;
//			Log.info("New Publickey: " + publickey.getBin().length);
//		}
//
//		// Transaction's Signature size
//		size += EQCType.bytesToBIN(signature).length;
//		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
//
//		Log.info("Total size: " + size);
//		return size;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBytes(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBytes(Passport.AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization body
			os.write(getBodyBytes(addressShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBin(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBin(Passport.AddressShape addressShape) {
		return EQCType.bytesToBIN(getBytes(addressShape));
	}
	
	protected boolean isBasicSanity(AddressShape addressShape) {
		if (transactionType == null || nonce == null || txIn == null || txOutList == null || publickey == null
				|| signature == null) {
			return false;
		}
		
		if(!getAssetID().equals(Asset.EQCOIN)) {
			return false;
		}

		if (!publickey.isSanity()) {
			return false;
		}
		
		if (!isTxOutNumberValid()) {
			return false;
		}

		if (!txIn.isSanity(addressShape)) {
			return false;
		} else {
			if (!txIn.getPassport().isGood()) {
				return false;
			}
			if(txIn.getPassport().getAddressType() != AddressType.T1 || txIn.getPassport().getAddressType() != AddressType.T2) {
				return false;
			}
		}
		for (TxOut txOut : txOutList) {
			if (!txOut.isSanity(addressShape)) {
				return false;
			}
			if(txOut.getPassport().getAddressType() != AddressType.T1 || txOut.getPassport().getAddressType() != AddressType.T2) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isSanity(AddressShape addressShape) {
		if(!isBasicSanity(addressShape)) {
			return false;
		}
		if (transactionType != TransactionType.TRANSFER) {
			return false;
		}
		return true;
	}

	/**
	 * @return the txOutList
	 */
	public Vector<TxOut> getTxOutList() {
		return txOutList;
	}

	/**
	 * @param txOutList the txOutList to set
	 */
	public void setTxOutList(Vector<TxOut> txOutList) {
		this.txOutList = txOutList;
	}
	
	public void addTxOut(TxOut txOut) {
		if (txOutList.size() >= MAX_TXOUT) {
			throw new UnsupportedOperationException("The number of TxOut cannot exceed 10.");
		}
		if (!isTxOutAddressExists(txOut)) {
			if (!isTxOutAddressEqualsTxInAddress(txOut)) {
				txOutList.add(txOut);
			} else {
				Log.Error(txOut + " equal to TxIn Address: " + txIn + " just ignore it.");
			}
		} else {
			Log.Error(txOut + " already exists in txOutList just ignore it.");
		}
	}

	public int getTxOutNumber() {
		return txOutList.size();
	}

	public long getTxOutValues() {
		long totalTxOut = 0;
		for (TxOut txOut : txOutList) {
			totalTxOut += txOut.getValue();
		}
		return totalTxOut;
	}
	
	public boolean isTxOutValueLessThanTxInValue() {
		return getTxOutValues() < txIn.getValue();
	}

	public boolean isTxOutAddressIncludeTxInAddress() {
		for (TxOut txOut : txOutList) {
			if (isTxOutAddressEqualsTxInAddress(txOut)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTxOutAddressEqualsTxInAddress(TxOut txOut) {
		if(txIn == null) {
			return false;
		}
		return txOut.getPassport().getReadableAddress().equals(txIn.getPassport().getReadableAddress());
	}

	public boolean isTxOutNumberValid() {
		return (txOutList.size() >= MIN_TXOUT) && (txOutList.size() <= MAX_TXOUT);
	}

	public boolean isAllValueValid() {
		if ((txIn.getValue() < Util.MIN_EQC) || (txIn.getValue() >= Util.MAX_EQC)) {
			return false;
		}

		for (TxOut txOut : txOutList) {
			if ((txOut.getValue() < Util.MIN_EQC) || (txOut.getValue() >= Util.MAX_EQC)) {
				return false;
			}
		}

		return true;
	}
	
	public long getTxFeeLimit() {
		return txIn.getValue() - getTxOutValues();
	}

	public boolean isTxFeeLimitValid() {
		Log.info("getTxFeeLimit(): " + getTxFeeLimit());
		Log.info("getMaxTxFeeLimit(): " + getMaxTxFeeLimit());
		Log.info("getDefaultTxFeeLimit(): " + getDefaultTxFeeLimit());
		boolean boolIsValid = true;
		if (getTxFeeLimit() < getDefaultTxFeeLimit()) {
			boolIsValid = false;
		} 
//		else if ((getTxFeeLimit() <= getMaxTxFeeLimit()) && (getTxFeeLimit() % getDefaultTxFeeLimit()) != 0) {
//			boolIsValid = false;
//		}
		return boolIsValid;
	}
	
	public long getBillingValue() {
		Log.info("TxFee: " + getTxFee() + " TxOutValues: " + getTxOutValues() + " TxFeeLimit: " + getTxFeeLimit());
		return getTxFee() + getTxOutValues();
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}
	
	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}
	
	public boolean isTxOutAddressExists(TxOut txOut) {
		boolean boolIsExists = false;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getPassport().getReadableAddress().equals(txOut.getPassport().getReadableAddress())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}

	public boolean isTxOutAddressUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getPassport().equals(txOutList.get(j).getPassport())) {
					return false;
				}
			}
		}
		return true;
	}

	public int getBillingSize() {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin(AddressShape.ID).length;
		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getBin(AddressShape.AI).length;
				Log.info("New TxOut: " + txOut.getBin(AddressShape.AI).length);
			}
		}

		// Transaction's PublickeyList size
		if (publickey.isNew()) {
			size += publickey.getBin().length;
			Log.info("New Publickey: " + publickey.getBin().length);
		}

		// Transaction's Signature size
		size += EQCType.bytesToBIN(signature).length;
		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
		Log.info("Total size: " + size);
		return size;
	}

	public boolean isTxOutAddressValid() {
		for (TxOut txOut : txOutList) {
			if (!txOut.getPassport().isGood(null)) {
				return false;
			}
		}
		return true;
	}
	
	protected String getTxOutString() {
		String tx = "[\n";
		if (txOutList.size() > 0) {
			for (int i = 0; i < txOutList.size() - 1; ++i) {
				tx += txOutList.get(i) + ",\n";
			}
			tx += txOutList.get(txOutList.size() - 1);
		} else {
			tx += null;
		}
		tx += "\n]";
		return tx;
	}
	
	public void prepareAccounting(AccountsMerkleTree accountsMerkleTree, ID initID) throws Exception {
		// Fill in TxIn's Address
		txIn.getPassport().setID(accountsMerkleTree.getAddressID(txIn.getPassport()));
		// Fill in Publickey's Serial Number
		publickey.setID(txIn.getPassport().getID());

		// Update Publickey's isNew status if need
			if (!accountsMerkleTree.isPublicKeyExists(getPublickey())) {
				getPublickey().setNew(true);
			}

		// Update TxOut's Address' isNew status if need
		for (TxOut txOut : txOutList) {
			if (!accountsMerkleTree.isAccountExists(txOut.getPassport(), true)) {
				txOut.getPassport().setID(initID);
				txOut.setNew(true);
				initID = initID.getNextID();
			} else {
				// For security issue need retrieve and fill in every Address' ID
				// according to it's AddressAI
				txOut.getPassport().setID(accountsMerkleTree.getAddressID(txOut.getPassport()));
			}
		}
	}
	
	public boolean isAllAddressIDValid(AccountsMerkleTree accountsMerkleTree) {
		if(!super.isAllAddressIDValid(accountsMerkleTree)) {
			return false;
		}
		for(TxOut txOut : txOutList) {
			if(txOut.getPassport().getID().compareTo(accountsMerkleTree.getTotalAccountNumbers()) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public void prepareVerify(AccountsMerkleTree accountsMerkleTree, byte[] signature) throws IllegalStateException, NoSuchFieldException, RocksDBException, IOException, ClassNotFoundException, SQLException {
		super.prepareVerify(accountsMerkleTree, signature);
		// Fill in TxOut's Address' ReadableAddress
		for (TxOut txOut : getTxOutList()) {
			txOut.getPassport().setReadableAddress(accountsMerkleTree.getAddress(txOut.getPassport().getID()).getReadableAddress());
		}
	}
	
	public void update(AccountsMerkleTree accountsMerkleTree)
			throws Exception {
		WriteBatch writeBatch = new WriteBatch();
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		Account account = accountsMerkleTree.getAccount(txIn.getPassport().getID());
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		account.getAsset(getAssetID()).increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		account.getAsset(getAssetID()).withdraw(new ID(getBillingValue()));
		// Update current Transaction's TxIn Publickey if need
		if (publickey.isNew()) {
			Publickey publickey = new Publickey();
			publickey.setPublickey(this.publickey.getPublicKey());
			publickey.setPublickeyCreateHeight(accountsMerkleTree.getHeight().getNextID());
			account.setPublickey(publickey);
		}
		writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(0), account.getIDEQCBits(),
				account.getBytes());
		writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(1),
				account.getPassport().getAddressAI(), account.getIDEQCBits());

		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				account = new AssetAccount();
				account.setPassport(txOut.getPassport());
				account.setLockCreateHeight(accountsMerkleTree.getHeight().getNextID());
				accountsMerkleTree.increaseTotalAccountNumbers();
			} else {
				account = accountsMerkleTree.getAccount(txOut.getPassport().getID());
			}
			account.getAsset(getAssetID()).deposit(new ID(txOut.getValue()));
			writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(0), account.getIDEQCBits(),
					account.getBytes());
			writeBatch.put(accountsMerkleTree.getFilter().getColumnFamilyHandles().get(1),
					account.getPassport().getAddressAI(), account.getIDEQCBits());
		}
		accountsMerkleTree.getFilter().batchUpdate(writeBatch);
	}
	
	public void parseBody(ByteArrayInputStream is, AddressShape addressShape)
			throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {

		super.parseBody(is, addressShape);

		byte txOutCount = 0;
		// Parse TxOut
		while (txOutCount < MAX_TXOUT) {
			TxOut txOut = new TxOut(is, addressShape);
			// Add TxOut
			txOutList.add(txOut);
		}
	}

	@Override
	public byte[] getBodyBytes(AddressShape addressShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Super
			os.write(super.getBodyBytes(addressShape));
			// Serialization TxOut
			for (TxOut txOut : txOutList) {
				os.write(txOut.getBytes(addressShape));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

}
