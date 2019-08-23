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
package com.eqchains.blockchain.subchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.velocity.runtime.directive.Parse;

import com.eqchains.blockchain.account.Passport;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.serialization.EQCInheritable;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCSubchain implements EQCTypable, EQCInheritable {
	protected EQCSubchainHeader subchainHeader;
	protected Vector<Transaction> newTransactionList;
	protected long newTransactionListLength;
	// The following is Transactions' Segregated Witness members it's hash will be
	// recorded in the Root's accountsMerkelTreeRoot together with Transaction.
	protected Vector<byte[]> newSignatureList;
	// private txReceipt;
	protected boolean isSegwit;
	
	public void init() {
		newTransactionList = new Vector<>();
		newSignatureList = new Vector<>();
	}
	
	public EQCSubchain() {
		init();
	}
	
	public EQCSubchain(byte[] bytes, boolean isSegwit) throws Exception {
		EQCType.assertNotNull(bytes);
		init();
		this.isSegwit = isSegwit;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is);
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBytes()
	 */
	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getHeaderBytes());
		os.write(getBodyBytes());
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(subchainHeader == null || newSignatureList == null || newSignatureList == null) {
			return false;
		}
		if(!(newSignatureList.isEmpty() && newSignatureList.isEmpty() && newTransactionListLength == 0)) {
			return false;
		}
		if(newTransactionList.size() != newSignatureList.size()) {
			return false;
		}
		long newTransactionListLength = 0;
		for(Transaction transaction:newTransactionList) {
			newTransactionListLength += transaction.getBin(AddressShape.ID).length;
		}
		if(this.newTransactionListLength != newTransactionListLength) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		subchainHeader = new EQCSubchainHeader(is);
	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse NewTransactionList
		ARRAY transactions = null;
		Transaction transaction = null;
		transactions = EQCType.parseARRAY(is);
		if (!transactions.isNULL()) {
			newTransactionListLength = transactions.size;
			ByteArrayInputStream is1 = new ByteArrayInputStream(transactions.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				transaction = Transaction.parseTransaction(EQCType.parseBIN(is1), AddressShape.ID);
				newTransactionList.add(transaction);
			}
			EQCType.assertEqual(transactions.size, newTransactionList.size());
		}
		if (!isSegwit) {
			// Parse NewSignatureList
			ARRAY signatures = null;
			signatures = EQCType.parseARRAY(is);
			byte[] signature = null;
			if (!signatures.isNULL()) {
				ByteArrayInputStream is1 = new ByteArrayInputStream(signatures.elements);
				while (!EQCType.isInputStreamEnd(is1)) {
					signature = EQCType.parseBIN(is1);
					newSignatureList.add(signature);
				}
				EQCType.assertEqual(signatures.size, newSignatureList.size());
			}
		}
		else {
			// Just skip the data stream to keep data stream's format is valid
			EQCType.parseARRAY(is);
		}
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(subchainHeader.getBytes());
		return os.toByteArray();
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getNewTransactionListARRAY());
		if(!isSegwit) {
			os.write(getNewSignatureListARRAY());
		}
		return os.toByteArray();
	}

	/**
	 * @return the EQCSubchainHeader
	 */
	public EQCSubchainHeader getSubchainHeader() {
		return subchainHeader;
	}

	/**
	 * @param EQCSubchainHeader the EQCSubchainHeader to set
	 */
	public void setSubchainHeader(EQCSubchainHeader subchainHeader) {
		this.subchainHeader = subchainHeader;
	}

	/**
	 * @return the newTransactionListLength
	 */
	public long getNewTransactionListLength() {
		return newTransactionListLength;
	}

	/**
	 * @return the newTransactionList
	 */
	public Vector<Transaction> getNewTransactionList() {
		return newTransactionList;
	}

	/**
	 * @return the newSignatureList
	 */
	public Vector<byte[]> getNewSignatureList() {
		return newSignatureList;
	}
	
	public void addTransaction(Transaction transaction) {
		if(transaction.getAssetID().equals(subchainHeader.getID()) && !isTransactionExists(transaction)) {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBin(AddressShape.ID).length;
			// Add Signature
			newSignatureList.add(transaction.getSignature());
		}
	}
	
	public boolean isTransactionExists(Transaction transaction) {
		return newTransactionList.contains(transaction);
	}
	
	public byte[] getRoot() throws Exception {
		Vector<byte[]> vector = new Vector<>();
		vector.add(subchainHeader.getBytes());
		vector.add(getNewTransactionListMerkelTreeRoot());
		vector.add(Util.getMerkleTreeRoot(newSignatureList));
		return Util.getMerkleTreeRoot(vector);
	}
	
	public byte[] getNewTransactionListMerkelTreeRoot() {
		Vector<byte[]> transactions = new Vector<byte[]>();
		for (Transaction transaction : newTransactionList) {
			transactions.add(transaction.getBytes(AddressShape.ID));
		}
		return Util.getMerkleTreeRoot(transactions);
	}
	
	public static EQCSubchain parse(byte[] bytes, boolean isSegwit) throws Exception {
		EQCSubchain eqcSubchain = null;
		ID id = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		id = EQCType.parseID(is);
		if(id.equals(ID.ONE)) {
			eqcSubchain = new EQcoinSubchain(bytes, isSegwit);
		}
		else if(id.equals(ID.SIX)) {
			eqcSubchain = new EthereumSubchain(bytes, isSegwit);
		}
		return eqcSubchain;
	}
	
//	public void buildTransactionsForVerify() throws ClassNotFoundException, SQLException {
//		// Only have CoinBase Transaction just return
//		if (transactions.getNewTransactionList().size() == 1) {
//			Transaction transaction = transactions.getNewTransactionList().get(0);
//			// Set Address for every Transaction
//			// Set TxOut Address
//			for (TxOut txOut : transaction.getTxOutList()) {
//				txOut.getPassport().setReadableAddress(Util.getAddress(txOut.getPassport().getID(), this));
//			}
//			return;
//		}
//
//		// Set Signature for every Transaction
//		// Bug fix change to verify if every Transaction's signature is equal to
//		// Signatures
//		for (int i = 1; i < signatures.getSignatureList().size(); ++i) {
//			transactions.getNewTransactionList().get(i).setSignature(signatures.getSignatureList().get(i));
//		}
//
//		for (int i = 1; i < transactions.getNewTransactionList().size(); ++i) {
//			Transaction transaction = transactions.getNewTransactionList().get(i);
//			// Set PublicKey for every Transaction
//			// Bug fix before add in Transactions every transaction should have signature &
//			// PublicKey.
//			transaction.setCompressedPublickey(Util.getPublicKey(transaction.getTxIn().getPassport().getID(), this));
//			// Set Address for every Transaction
//			// Set TxIn Address
//			transaction.getTxIn().getPassport()
//					.setReadableAddress(Util.getAddress(transaction.getTxIn().getPassport().getID(), this));
//			// Set TxOut Address
//			for (TxOut txOut : transaction.getTxOutList()) {
//				txOut.getPassport().setReadableAddress(Util.getAddress(txOut.getPassport().getID(), this));
//			}
//		}
//	}
	
	private byte[] getNewTransactionListARRAY() {
		if (newTransactionList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> transactions = new Vector<byte[]>();
			for (Transaction transaction : newTransactionList) {
				transactions.add(transaction.getBin(AddressShape.ID));
			}
			return EQCType.bytesArrayToARRAY(transactions);
		}
	}
	
	private byte[] getNewSignatureListARRAY() {
		if (newSignatureList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> signatures = new Vector<byte[]>();
			for (byte[] signature : newSignatureList) {
				signatures.add(EQCType.bytesToBIN(signature));
			}
			return EQCType.bytesArrayToARRAY(signatures);
		}
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
				"\"EQCSubchain\":{\n" + subchainHeader.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n},\n" +
						"\"NewSignatureList\":" + 
						"\n{\n" +
						"\"Size\":\"" + newSignatureList.size() + "\",\n" +
						"\"List\":" + 
							_getNewSignatureList() + "\n}\n" +
				 "\n}\n}";
	}
	
	protected String _getNewTransactionList() {
		String tx = null;
		if (newTransactionList != null && newTransactionList.size() > 0) {
			tx = "\n[\n";
			if (newTransactionList.size() > 1) {
				for (int i = 0; i < newTransactionList.size() - 1; ++i) {
					tx += newTransactionList.get(i) + ",\n";
				}
			}
			tx += newTransactionList.get(newTransactionList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	protected String _getNewSignatureList() {
		String tx = null;
		if (newSignatureList != null && newSignatureList.size() > 0) {
			tx = "\n[\n";
			if (newSignatureList.size() > 1) {
				for (int i = 0; i < newSignatureList.size() - 1; ++i) {
					tx += getSignatureJson(newSignatureList.get(i)) + ",\n";
				}
			}
			tx += getSignatureJson(newSignatureList.get(newSignatureList.size() - 1));
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	protected String getSignatureJson(byte[] signature) {
		return "{\n\"Signature\":\"" + Util.dumpBytes(signature, 16) + "\"\n}";
	}
	
}
