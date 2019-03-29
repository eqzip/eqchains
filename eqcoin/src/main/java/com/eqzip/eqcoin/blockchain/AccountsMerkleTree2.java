///**
// * EQCoin core - EQCOIN Foundation's EQCoin core library
// * @copyright 2018-2019 EQCOIN Foundation Inc.  All rights reserved...
// * Copyright of all works released by EQCOIN Foundation or jointly released by EQCOIN Foundation 
// * with cooperative partners are owned by EQCOIN Foundation and entitled to protection
// * available from copyright law by country as well as international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, EQCOIN Foundation reserves all rights to take any legal
// * action and pursue any right or remedy available under applicable law.
// * https://www.EQCOIN Foundation.com
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package com.eqzip.eqcoin.blockchain;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.Vector;
//
//import org.jboss.netty.channel.socket.nio.NioDatagramWorker;
//
//import com.eqzip.eqcoin.blockchain.Account.Publickey;
//import com.eqzip.eqcoin.crypto.MerkleTree;
//import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
//import com.eqzip.eqcoin.serialization.EQCTypable;
//import com.eqzip.eqcoin.serialization.EQCType;
//import com.eqzip.eqcoin.util.Log;
//import com.eqzip.eqcoin.util.SerialNumber;
//import com.eqzip.eqcoin.util.Util;
//
///**
// * AccountsMerkleTree represents 3 states(FIXED, MINING, OTHERS) simultaneously
// * in a tree. Unless you are adding a new account(When adding a new Account you
// * just add it in your current state you can merge it to FIXED state later),
// * otherwise modify the FIXED Account, you must first copy the original FIXED
// * Account to the corresponding MINING or OTHERS state.
// * 
// * @author Xun Wang
// * @date Nov 29, 2018
// * @email 10509759@qq.com
// */
//public class AccountsMerkleTree2 implements EQCTypable {
//	public final static int MAX_ACCOUNTS_PER_TREE = 16777216;
//	private Vector<Vector<AccountStatus>> accountsList;
//	private Vector<MerkleTree> accountsMerkleTree;
//	private HeightStatus heightStatus;
//	private byte[] accountsMerkleTreeRoot;
//	private TotalAccountNumberStatus totalAccountNumberStatus;
//	public enum ACCOUNT_STATUS{
//		FIXED, MINING, OTHERS
//	}
//	
//	public AccountsMerkleTree2() {
//		super();
//		init();
//	}
//	
//	private void init() {
//		accountsList = new Vector<Vector<AccountStatus>>();
//		heightStatus = new HeightStatus();
//		totalAccountNumberStatus = new TotalAccountNumberStatus();
//	}
//	
//	/**
//	 * Create Fixed AccountsMerkleTree from H2
//	 * @param height Current Fixed AccountsMerkleTree's height
//	 */
//	public void createFromH2(SerialNumber height) {
//		init();
//		heightStatus.setHeight(height);
//		Vector<byte[]> bytes = null;
//		SerialNumber block_tail = EQCBlockChainH2.getInstance().getBlockTailHeight();
//		totalAccountNumberStatus.setTotalAccountNumber(EQCBlockChainH2.getInstance().getAccountsNumber(height));
//		SerialNumber begin, end;
//		if(height.getSerialNumber().compareTo(block_tail.getSerialNumber()) > 0) {
//			Log.Error("Height " + height.getSerialNumber() + " exceed the blocktail " + block_tail.getSerialNumber());
//			throw new IndexOutOfBoundsException("Height " + height.getSerialNumber() + " exceed the blocktail " + block_tail.getSerialNumber());
//		}
//		for(int i=0; i<=totalAccountNumberStatus.getTotalAccountNumber(ACCOUNT_STATUS.FIXED).longValue()/MAX_ACCOUNTS_PER_TREE; ++i) {
//			begin = new SerialNumber(BigInteger.valueOf(i*MAX_ACCOUNTS_PER_TREE).add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
//			end = new SerialNumber(BigInteger.valueOf((i+1)*MAX_ACCOUNTS_PER_TREE).add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
//			Vector<AccountStatus> accounts = EQCBlockChainH2.getInstance().getAccounts(begin, end, height);
//			// If current height is less than Blocktail's height just update the relevant Account's balance according to the Transaction table
//			if(height.getSerialNumber().compareTo(block_tail.getSerialNumber()) < 0) {
//				for(AccountStatus account : accounts) {
//					account.fixedAccount.setBalance(EQCBlockChainH2.getInstance().getBalance(account.fixedAccount.getAddress(), height));
//				}
//			}
//			accountsList.add(accounts);
//		}
//	}
//	
//	private void createPublickey(Vector<PublicKey> publicKeyList, SerialNumber height, ACCOUNT_STATUS status) {
//		Publickey accountPublickey = null;
//		AccountStatus accountStatus = null;
//		for(PublicKey publicKey : publicKeyList) {
//			accountPublickey = new Publickey();
//			accountPublickey.setPublickey(publicKey.getPublicKey());
//			accountPublickey.setPublickeyCreateHeight(height);
//			accountStatus = getAccountStatus(publicKey.getSerialNumber());
//			accountStatus.addPublickey(accountPublickey, status);
//		}
//	}
//	
//	private void addNewAccount(Vector<Address> addresses, SerialNumber height, ACCOUNT_STATUS status) {
//		AccountStatus accountStatus;
//		for(Address address : addresses) {
//			if((totalAccountNumberStatus.getTotalAccountNumber(status).compareTo(BigInteger.ZERO) == 0) ||
//				(totalAccountNumberStatus.getTotalAccountNumber(status).add(BigInteger.ONE).longValue()%MAX_ACCOUNTS_PER_TREE ) == 0) {
//				accountsList.add(new Vector<AccountStatus>());
//			}
//			accountStatus = new AccountStatus();
//			accountStatus.addAccount(address, height, status);
//			accountsList.get(accountsList.size()-1).add(accountStatus);
//			// Increase current status' Account's Number
//			totalAccountNumberStatus.increaseTotalAccountNumber(status);
//		}
//	}
//	
//	public void updateBalance(Transactions transactions, SerialNumber height, ACCOUNT_STATUS status) {
//		for(Transaction transaction : transactions.getTransactionList()) {
//			if(!transaction.isCoinBase()) {
//				getAccountStatus(transaction.getTxIn().getAddress().getSerialNumber()).updateBalance(-transaction.getTxIn().getValue(), height, status);
//			}
//			for(TxOut txOut : transaction.getTxOutList()) {
//				getAccountStatus(txOut.getAddress().getSerialNumber()).updateBalance(txOut.getValue(), height, status);
//			}
//		}
//	}
//	
//	public void update(Transactions transactions, SerialNumber height, ACCOUNT_STATUS status){
//		addNewAccount(transactions.getAddressList(), height, status);
//		createPublickey(transactions.getPublicKeyList(), height, status);
//		updateBalance(transactions, height, status);
//		heightStatus.setHeight(height, status);
//	}
//
//	public void merge(ACCOUNT_STATUS status) {
//		totalAccountNumberStatus.merge(status);
//		for (Vector<AccountStatus> vecAccount : accountsList) {
//			for (AccountStatus account : vecAccount) {
//				account.merge(status);
//			}
//		}
//	}
//	
//	public void clear() {
//		for (Vector<AccountStatus> vecAccount : accountsList) {
//			for (AccountStatus account : vecAccount) {
//				account.clear();
//			}
//		}
//	}
//	
//	// Before sync need merge first.
//	public void sync(){
//		for (Vector<AccountStatus> vecAccount : accountsList) {
//			for (AccountStatus account : vecAccount) {
//				if(!account.fixedAccount.sync()) {
//					throw new IllegalStateException("During Account sync error occur please check the H2");
//				}
//				account.clear();
//			}
//		}
//	}
//	
//	public AccountStatus getAccountStatus(SerialNumber serialNumber) {
//		int index = 0;
//		int postion = 0;
//		int sn = (int) (serialNumber.longValue() - Util.INIT_ADDRESS_SERIAL_NUMBER);
//		index = sn / MAX_ACCOUNTS_PER_TREE;
//		postion = sn - index * MAX_ACCOUNTS_PER_TREE;
//		return accountsList.get(index).get(postion);
//	}
//	
//	public void buildAccountsMerkleTree(ACCOUNT_STATUS status) {
//		accountsMerkleTree = new Vector<MerkleTree>();
//		Vector<byte[]> bytes = null;
//		for(Vector<AccountStatus> vecAccountStatus : accountsList) {
//			bytes = new Vector<byte[]>();
//			for(AccountStatus accountStatus : vecAccountStatus) {
//				// If Account == null which means the end of AccountList reached
//				if(accountStatus.getAccount(status) == null) {
//					break;
//				}
//				bytes.add(accountStatus.getAccount(status).getBytes());
//			}
//			accountsMerkleTree.add(new MerkleTree(bytes));
//		}
//	}
//	
//	public void generateRoot(ACCOUNT_STATUS status) {
//		Vector<byte[]> bytes = new Vector<byte[]>();
//		for(MerkleTree merkleTree: accountsMerkleTree) {
//			merkleTree.generateRoot();
//			bytes.add(merkleTree.getRoot());
//		}
//		MerkleTree merkleTree = new MerkleTree(bytes);
//		merkleTree.generateRoot();
//		accountsMerkleTreeRoot = merkleTree.getRoot();
//	}
//	
//	public byte[] getRoot() {
//		return accountsMerkleTreeRoot;
//	}
//	
//	public Vector<byte[]> getAccountsMerkleTreeRootList(){
//		Vector<byte[]> bytes = new Vector<byte[]>();
//		for(MerkleTree merkleTree : accountsMerkleTree) {
//			bytes.add(merkleTree.getRoot());
//		}
//		return bytes;
//	}
//	
//	@Override
//	public byte[] getBytes() {
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		try {
//			os.write(EQCType.bytesArrayToARRAY(getAccountsMerkleTreeRootList()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return os.toByteArray();
//	}
//
//	@Override
//	public byte[] getBin() {
//		return EQCType.bytesToBIN(getBytes());
//	}
//	
//	public final static class AccountStatus{
//		private Account fixedAccount;
//		private Account miningAccount;
//		private Account othersAccount;
//		
//		public AccountStatus() {
////			fixedAccount = new Account();
//		}
//		
//		public void merge(ACCOUNT_STATUS status) {
//			if((status == ACCOUNT_STATUS.MINING) && (miningAccount != null)) {
//				fixedAccount = miningAccount;
//			}
//			else if((status == ACCOUNT_STATUS.OTHERS) && (othersAccount != null)) {
//				fixedAccount = othersAccount;
//			}
//		}
//		
//		public Account getAccount(ACCOUNT_STATUS status) {
//			Account account = fixedAccount;
//			if((status == ACCOUNT_STATUS.MINING) && (miningAccount != null)) {
//				account = miningAccount;
//			}
//			else if((status == ACCOUNT_STATUS.OTHERS) && (othersAccount != null)) {
//				account = othersAccount;
//			}
//			else if(status == ACCOUNT_STATUS.FIXED) {
//				account = fixedAccount;
//			}
//			return account;
//		}
//		
//		/**
//		 * @return the fixedAccount
//		 */
//		public Account getFixedAccount() {
//			return fixedAccount;
//		}
//
//		/**
//		 * @param fixedAccount the fixedAccount to set
//		 */
//		public void setFixedAccount(Account fixedAccount) {
//			this.fixedAccount = fixedAccount;
//		}
//
//		/**
//		 * @return the miningAccount
//		 */
//		public Account getMiningAccount() {
//			return miningAccount;
//		}
//
//		/**
//		 * @param miningAccount the miningAccount to set
//		 */
//		public void setMiningAccount(Account miningAccount) {
//			this.miningAccount = miningAccount;
//		}
//
//		/**
//		 * @return the othersAccount
//		 */
//		public Account getOthersAccount() {
//			return othersAccount;
//		}
//
//		/**
//		 * @param othersAccount the othersAccount to set
//		 */
//		public void setOthersAccount(Account othersAccount) {
//			this.othersAccount = othersAccount;
//		}
//
//		// After merge clear fixedAccount's modified mark and set miningAccount&othersAccount to the default status - null
//		public void clear() {
//			clear(ACCOUNT_STATUS.FIXED);
//		}
//		
//		public void clear(ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.MINING) {
//				miningAccount = null;
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				othersAccount = null;
//			}
//			else if(status == ACCOUNT_STATUS.FIXED) {
//				miningAccount = null;
//				othersAccount = null;
//			}
//			fixedAccount.setAddressChanged(false);
//			fixedAccount.setPublickeyChanged(false);
//			fixedAccount.setBalanceChanged(false);
//			fixedAccount.setNonceChanged(false);
//		}
//		
//		public void addAccount(Address address, SerialNumber height, ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.MINING) {
//				this.miningAccount = new Account();
//				this.miningAccount.setAddress(address);
//				this.miningAccount.setAddressCreateHeight(height);
//				this.miningAccount.setAddressChanged(true);
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				this.othersAccount = new Account();
//				this.othersAccount.setAddress(address);
//				this.othersAccount.setAddressCreateHeight(height);
//				this.othersAccount.setAddressChanged(true);
//			}
//			else if(status == ACCOUNT_STATUS.FIXED) {
//				this.fixedAccount = new Account();
//				this.fixedAccount.setAddress(address);
//				this.fixedAccount.setAddressCreateHeight(height);
//				this.fixedAccount.setAddressChanged(true);;
//			}
//		}
//		
//		// When ACCOUNT_STATUS is MINING or OTHERS before addPublickey need copy original Account from fixedAccount
//		public void addPublickey(Publickey publicKey, ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.MINING) {
//				this.miningAccount.setPublickey(publicKey);
//				this.miningAccount.setPublickeyChanged(true);
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				this.othersAccount.setPublickey(publicKey);
//				this.othersAccount.setPublickeyChanged(true);
//			}
//			else if(status == ACCOUNT_STATUS.FIXED) {
//				this.fixedAccount.setPublickey(publicKey);
//				this.fixedAccount.setPublickeyChanged(true);
//			}
//		}
//		
//		public void updateBalance(long value, SerialNumber height, ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.MINING) {
//				if(this.miningAccount == null) {
//					this.miningAccount = this.fixedAccount;
//				}
//				this.miningAccount.updateBalance(value);
//				this.miningAccount.setBalanceChanged(true);
//				this.miningAccount.setBalanceUpdateHeight(height);
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				if(this.othersAccount == null) {
//					this.othersAccount = this.fixedAccount;
//				}
//				this.othersAccount.updateBalance(value);
//				this.othersAccount.setBalanceChanged(true);
//				this.othersAccount.setBalanceUpdateHeight(height);
//			}
//			else if(status == ACCOUNT_STATUS.FIXED) {
//				this.fixedAccount.updateBalance(value);
//				this.fixedAccount.setBalanceChanged(true);
//				this.fixedAccount.setBalanceUpdateHeight(height);
//			}
//		}
//		
//	}
//
//	/**
//	 * @return the height
//	 */
//	public SerialNumber getHeight(ACCOUNT_STATUS status) {
//		return heightStatus.getHeight(status);
//	}
//
//	/**
//	 * @param height the height to set
//	 */
//	public void setHeight(SerialNumber height, ACCOUNT_STATUS status) {
//		heightStatus.setHeight(height, status);
//	}
//
//	public class HeightStatus{
//		public SerialNumber fixedHeight;
//		public SerialNumber miningHeight;
//		public SerialNumber othersHeight;
//		
//		public HeightStatus() {
//			fixedHeight = miningHeight = othersHeight = SerialNumber.ZERO;
//		}
//		
//		public void setHeight(SerialNumber height) {
//			fixedHeight = miningHeight = othersHeight = height;
//		}
//		
//		public void setHeight(SerialNumber height, ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.FIXED) {
//				fixedHeight = height;
//			}
//			else if(status == ACCOUNT_STATUS.MINING) {
//				miningHeight = height;
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				othersHeight = height;
//			}
//		}
//		
//		public SerialNumber getHeight(ACCOUNT_STATUS status) {
//			SerialNumber height = null;
//			if(status == ACCOUNT_STATUS.FIXED) {
//				return fixedHeight;
//			}
//			else if(status == ACCOUNT_STATUS.MINING) {
//				return miningHeight;
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				return othersHeight;
//			}
//			return height;
//		}
//		
//	}
//	
//	public class TotalAccountNumberStatus{
//		private BigInteger fixedTotalAccountNumber;
//		private BigInteger miningTotalAccountNumber;
//		private BigInteger othersTotalAccountNumber;
//		
//		public TotalAccountNumberStatus() {
//			fixedTotalAccountNumber = miningTotalAccountNumber = othersTotalAccountNumber = BigInteger.ZERO;
//		}
//		
//		public void setTotalAccountNumber(BigInteger totalAccountNumber) {
//			fixedTotalAccountNumber = miningTotalAccountNumber = othersTotalAccountNumber = totalAccountNumber;
//		}
//		
//		public void increaseTotalAccountNumber(ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.FIXED) {
//				fixedTotalAccountNumber = fixedTotalAccountNumber.add(BigInteger.ONE);
//			}
//			else if(status == ACCOUNT_STATUS.MINING) {
//				miningTotalAccountNumber = miningTotalAccountNumber.add(BigInteger.ONE);
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				othersTotalAccountNumber = othersTotalAccountNumber.add(BigInteger.ONE);
//			}
//		}
//		
//		public void merge(ACCOUNT_STATUS status) {
//			if(status == ACCOUNT_STATUS.MINING) {
//				fixedTotalAccountNumber = miningTotalAccountNumber;
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				fixedTotalAccountNumber = othersTotalAccountNumber;
//			}
//		}
//		
//		public BigInteger getTotalAccountNumber(ACCOUNT_STATUS status) {
//			BigInteger totalAccountNumber = null;
//			if(status == ACCOUNT_STATUS.FIXED) {
//				totalAccountNumber = fixedTotalAccountNumber;
//			}
//			else if(status == ACCOUNT_STATUS.MINING) {
//				totalAccountNumber = miningTotalAccountNumber;
//			}
//			else if(status == ACCOUNT_STATUS.OTHERS) {
//				totalAccountNumber = othersTotalAccountNumber;
//			}
//			return totalAccountNumber;
//		}
//	}
//	
//	public long getTotalSupply(ACCOUNT_STATUS status) {
//		long totalSupply = 0;
//		for(Vector<AccountStatus> vecAccountStatus: accountsList) {
//			for(AccountStatus accountStatus : vecAccountStatus) {
//				totalSupply += accountStatus.getAccount(status).getBalance();
//			}
//		}
//		return totalSupply;
//	}
//	
//	public BigInteger getTotalTransactionNumbers(ACCOUNT_STATUS status) {
//		BigInteger totalTxNumbers = BigInteger.ZERO;
//		for(Vector<AccountStatus> vecAccountStatus: accountsList) {
//			for(AccountStatus accountStatus : vecAccountStatus) {
//				totalTxNumbers.add(accountStatus.getAccount(status).getNonce());
//			}
//		}
//		return totalTxNumbers;
//	}
//	
//	public BigInteger getTotalAccountNumber(ACCOUNT_STATUS status) {
//		return totalAccountNumberStatus.getTotalAccountNumber(status);
//	}
//	
//	public int getAccountsMerkleTreeSize(ACCOUNT_STATUS status) {
//		int size = 0;
//		if(getTotalAccountNumber(status).mod(BigInteger.valueOf(MAX_ACCOUNTS_PER_TREE)).equals(BigInteger.ZERO)) {
//			size = getTotalAccountNumber(status).divide(BigInteger.valueOf(MAX_ACCOUNTS_PER_TREE)).intValue();
//		}
//		else {
//			size = getTotalAccountNumber(status).divide(BigInteger.valueOf(MAX_ACCOUNTS_PER_TREE)).intValue() + 1;
//		}
//		return size;
//	}
//	
//	public boolean isAddressExists(Address address, ACCOUNT_STATUS status) {
//		boolean isAddressExists = false;
//		for(Vector<AccountStatus> vecAccountStatus : accountsList) {
//			for(AccountStatus accountStatus : vecAccountStatus) {
//				if(accountStatus.getAccount(status).getAddress().equals(address)) {
//					isAddressExists = true;
//					break;
//				}
//			}
//		}
//		return isAddressExists;
//	}
//	
//	public SerialNumber getAddressSerialNumber(Address address, ACCOUNT_STATUS status) {
//		return getAccount(address, status).getSerialNumber();
//	}
//	
//	public boolean isPublicKeyExists(SerialNumber serialNumber, PublicKey publicKey, ACCOUNT_STATUS status) {
//		boolean isPublickeyExists = false;
//		if(getAccountStatus(serialNumber).getAccount(status).getPublickey().equals(publicKey)) {
//			isPublickeyExists = true;
//		}
//		return isPublickeyExists;
//	}
//	
//	public Account getAccount(Address address, ACCOUNT_STATUS status) {
//		Account account = null;
//		for(Vector<AccountStatus> vecAccountStatus : accountsList) {
//			for(AccountStatus accountStatus : vecAccountStatus) {
//				if(accountStatus.getAccount(status).getAddress().equals(address)) {
//					account = accountStatus.getAccount(status);
//					break;
//				}
//			}
//		}
//		return account;
//	}
//	
//	public void addNewAccount(Address address, ACCOUNT_STATUS status) {
//		AccountStatus accountStatus;
//			if((totalAccountNumberStatus.getTotalAccountNumber(status).compareTo(BigInteger.ZERO) == 0) ||
//				(totalAccountNumberStatus.getTotalAccountNumber(status).add(BigInteger.ONE).longValue()%MAX_ACCOUNTS_PER_TREE ) == 0) {
//				accountsList.add(new Vector<AccountStatus>());
//			}
//			accountStatus = new AccountStatus();
//			accountStatus.addAccount(address, heightStatus.getHeight(status), status);
//			accountsList.get(accountsList.size()-1).add(accountStatus);
//			// Increase current status' Account's Number
//			totalAccountNumberStatus.increaseTotalAccountNumber(status);
//	}
//	
////	public class INFO{
////		public BigInteger totalSupply;
////		public BigInteger totalTransactionNumbers;
////		public BigInteger totalAccountNumbers;
////		public INFO() {
////			totalSupply = BigInteger.ZERO;
////			totalTransactionNumbers = BigInteger.ZERO;
////			totalAccountNumbers = BigInteger.ZERO;
////		}
////	}
////	
////	public INFO getTotalSupply() {
////		INFO info = new INFO();
////		for(Vector<AccountStatus> vecAccountStatus: accountsList) {
////			for(AccountStatus accountStatus : vecAccountStatus) {
////				info.totalSupply.add(acc)
////			}
////		}
////		return info;
////	}
//	
//}
