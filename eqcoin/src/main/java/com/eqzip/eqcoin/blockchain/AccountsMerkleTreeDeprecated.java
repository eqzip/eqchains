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
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Vector;
//
//import com.eqzip.eqcoin.crypto.MerkleTree;
//import com.eqzip.eqcoin.persistence.h2.EQCBlockChainH2;
//import com.eqzip.eqcoin.serialization.EQCTypable;
//import com.eqzip.eqcoin.serialization.EQCType;
//import com.eqzip.eqcoin.util.Log;
//import com.eqzip.eqcoin.util.SerialNumber;
//import com.eqzip.eqcoin.util.Util;
//
///**
// * @author Xun Wang
// * @date Feb 22, 2019
// * @email 10509759@qq.com
// */
//public class AccountsMerkleTreeDeprecated implements EQCTypable {
//	public final static int MAX_ACCOUNTS_PER_TREE = 16777216;
//	private Vector<Vector<Account>> accountsList;
//	private Vector<MerkleTree> accountsMerkleTree;
//	private SerialNumber height;
//	private byte[] accountsMerkleTreeRoot;
//	private BigInteger totalAccountNumber;
//
//	public AccountsMerkleTree() {
//		super();
//		init();
//	}
//	
//	private void init() {
//		accountsList = new Vector<>();
//	}
//	
//	/**
//	 * Create Fixed AccountsMerkleTree from H2
//	 * @param height Current Fixed AccountsMerkleTree's height
//	 */
//	public void createFromH2(SerialNumber height) {
//		init();
//		this.height = height;
//		Vector<byte[]> bytes = null;
//		SerialNumber block_tail = EQCBlockChainH2.getInstance().getBlockTailHeight();
//		totalAccountNumber = EQCBlockChainH2.getInstance().getAccountsNumber(height);
//		SerialNumber begin, end;
//		if(height.getSerialNumber().compareTo(block_tail.getSerialNumber()) > 0) {
//			Log.Error("Height " + height.getSerialNumber() + " exceed the blocktail " + block_tail.getSerialNumber());
//			throw new IndexOutOfBoundsException("Height " + height.getSerialNumber() + " exceed the blocktail " + block_tail.getSerialNumber());
//		}
//		for(int i=0; i<=totalAccountNumber.longValue()/MAX_ACCOUNTS_PER_TREE; ++i) {
//			begin = new SerialNumber(BigInteger.valueOf(i*MAX_ACCOUNTS_PER_TREE).add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
//			end = new SerialNumber(BigInteger.valueOf((i+1)*MAX_ACCOUNTS_PER_TREE).add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
//			Vector<Account> accounts = EQCBlockChainH2.getInstance().getAccounts(begin, end, height);
//			// If current height is less than Blocktail's height just update the relevant Account's balance according to the Transaction table
//			if(height.getSerialNumber().compareTo(block_tail.getSerialNumber()) < 0) {
//				for(Account account : accounts) {
//					account.setBalance(EQCBlockChainH2.getInstance().getBalance(account.getAddress(), height));
//				}
//			}
//			accountsList.add(accounts);
//		}
//	}
//	
//	public void merge(AccountsChangeLog accountsChangeLog) {
//		
//	}
//	
//	public void buildAccountsMerkleTree() {
//		accountsMerkleTree = new Vector<MerkleTree>();
//		Vector<byte[]> bytes = null;
//		for(Vector<Account> vecAccount : accountsList) {
//			bytes = new Vector<byte[]>();
//			for(Account account : vecAccount) {
//				// If Account == null which means error occur
//				if(account == null) {
//					throw new IllegalStateException("Account shouldn't be null");
//				}
//				bytes.add(account.getBytes());
//			}
//			accountsMerkleTree.add(new MerkleTree(bytes));
//		}
//	}
//	
//	public void generateRoot() {
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
//	public Vector<Vector<Account>> getAccountList(){
//		return accountsList;
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
//	public class AccountsChangeLog {
//		AccountsMerkleTree baseAccountsMerkleTree;
//		Map<SerialNumber, Account> modifiedAccounts;
//		Vector<Account> newAccountList;
//		private Vector<MerkleTree> accountsMerkleTree;
//		private SerialNumber height;
//		private byte[] accountsMerkleTreeRoot;
//		private BigInteger totalAccountNumber;
//		
//		public AccountsChangeLog(AccountsMerkleTree accountsMerkleTree) {
//			baseAccountsMerkleTree = accountsMerkleTree;
//			modifiedAccounts = new HashMap<SerialNumber, Account>();
//			newAccountList = new Vector<>();
//			totalAccountNumber = baseAccountsMerkleTree.totalAccountNumber;
//		}
//		
//		public void addModifiedAccount(Account account) {
//			modifiedAccounts.put(account.getSerialNumber(), account);
//		}
//		
//		public Account getModifiedAccount(SerialNumber serialNumber) {
//			return modifiedAccounts.get(serialNumber);
//		}
//		
//		public boolean isAccountExists(Address address) {
//			return modifiedAccounts.containsKey(address.getSerialNumber());
//		}
//		
//		public void addNewAccount(Account account) {
//			newAccountList.add(account);
//			totalAccountNumber = totalAccountNumber.add(BigInteger.ONE);
//		}
//		
//		public Vector<Account> getNewAccountList(){
//			return newAccountList;
//		}
//		
//		public void buildAccountsMerkleTree() {
//			accountsMerkleTree = new Vector<MerkleTree>();
//			Vector<byte[]> bytes = null;
//			Vector<Vector<Account>>  accountList = baseAccountsMerkleTree.getAccountList();
//			
//			// Handle the AccountList except the last one
//			for(int i=0; i<accountList.size()-1; ++i) {
//				bytes = new Vector<byte[]>();
//				for(Account account : accountList.get(i)) {
//					// If Account == null which means error occur
//					if(account == null) {
//						throw new IllegalStateException("Account shouldn't be null");
//					}
//					if(isAccountExists(account.getAddress())) {
//						bytes.add(getModifiedAccount(account.getSerialNumber()).getBytes());
//					}
//					else {
//						bytes.add(account.getBytes());
//					}
//				}
//				accountsMerkleTree.add(new MerkleTree(bytes));
//			}
//			
//			// Handle the last AccountsMerkleTree
//			bytes = new Vector<byte[]>();
//			for(Account account : accountList.lastElement()) {
//				// If Account == null which means error occur
//				if(account == null) {
//					throw new IllegalStateException("Account shouldn't be null");
//				}
//				if(isAccountExists(account.getAddress())) {
//					bytes.add(getModifiedAccount(account.getSerialNumber()).getBytes());
//				}
//				else {
//					bytes.add(account.getBytes());
//				}
//			}
//			if(accountList.lastElement().size() + newAccountList.size() <= MAX_ACCOUNTS_PER_TREE) {
//				for(Account account : newAccountList) {
//					bytes.add(account.getBytes());
//				}
//				accountsMerkleTree.add(new MerkleTree(bytes));
//			}
//			else {
//				for(int i=0; i<(MAX_ACCOUNTS_PER_TREE - accountList.lastElement().size()); ++i) {
//					bytes.add(accountList.lastElement().get(i).getBytes());
//				}
//				accountsMerkleTree.add(new MerkleTree(bytes));
//				bytes = new Vector<byte[]>();
//				for(int i=(MAX_ACCOUNTS_PER_TREE - accountList.lastElement().size()); i<accountList.lastElement().size(); ++i) {
//					bytes.add(accountList.lastElement().get(i).getBytes());
//				}
//				accountsMerkleTree.add(new MerkleTree(bytes));
//			}
//		}
//		
//		public void generateRoot() {
//			Vector<byte[]> bytes = new Vector<byte[]>();
//			for(MerkleTree merkleTree: accountsMerkleTree) {
//				merkleTree.generateRoot();
//				bytes.add(merkleTree.getRoot());
//			}
//			MerkleTree merkleTree = new MerkleTree(bytes);
//			merkleTree.generateRoot();
//			accountsMerkleTreeRoot = merkleTree.getRoot();
//		}
//		
//		public byte[] getRoot() {
//			return accountsMerkleTreeRoot;
//		}
//		
//	}
//	
//}
