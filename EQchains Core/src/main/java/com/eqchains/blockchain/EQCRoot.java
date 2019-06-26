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
import java.util.Arrays;

import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Nov 12, 2018
 * @email 10509759@qq.com
 */
public class EQCRoot implements EQCTypable {
	private ID version;
	private byte txFeeRate;
	/**
	 * Save the root of Accounts Merkel Tree.
	 */
	private byte[] accountsMerkelTreeRoot;
	/**
	 * Save the root of Transactions' Merkel Tree. Including
	 * the root of TransactionList, SignatureList, PublickeyList
	 * and AddressList's Merkel Tree.
	 */
	private byte[] transactionsMerkelTreeRoot;

	public EQCRoot() {
		version = ID.ZERO;
	}

	public EQCRoot(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// Parse Version
		version = new ID(EQCType.parseEQCBits(is));

		// Parse TxFeeRate
		txFeeRate = EQCType.parseEQCBits(is)[0];

		// Parse Accounts hash
		accountsMerkelTreeRoot = EQCType.parseBIN(is);

		// Parse TransactionsMerkelTreeRoot hash
		transactionsMerkelTreeRoot = EQCType.parseBIN(is);
	}

	public byte[] getHash() {
		return Util.EQCCHA_MULTIPLE_DUAL(getBytes(), Util.HUNDREDPULS, true, false);
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(version.getEQCBits());
			os.write(EQCType.intToEQCBits(txFeeRate&0xFF));
			os.write(EQCType.bytesToBIN(accountsMerkelTreeRoot));
			os.write(EQCType.bytesToBIN(transactionsMerkelTreeRoot));
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
	 * @return the accountsMerkelTreeRoot
	 */
	public byte[] getAccountsMerkelTreeRoot() {
		return accountsMerkelTreeRoot;
	}

	/**
	 * @return the transactionsMerkelTreeRoot
	 */
	public byte[] getTransactionsMerkelTreeRoot() {
		return transactionsMerkelTreeRoot;
	}

	/**
	 * @param transactionsMerkelTreeRoot the transactionsMerkelTreeRoot to set
	 */
	public void setTransactionsMerkelTreeRoot(byte[] transactionsMerkelTreeRoot) {
		this.transactionsMerkelTreeRoot = transactionsMerkelTreeRoot;
	}

	/**
	 * @param accountsMerkelTreeRoot the accountsMerkelTreeRoot to set
	 */
	public void setAccountsMerkelTreeRoot(byte[] accountsMerkelTreeRoot) {
		this.accountsMerkelTreeRoot = accountsMerkelTreeRoot;
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
		return "\"Root\":" + "\n{\n" + "\"Version\":" + "\"" + version + "\"" + ",\n" + "\"TxFeeRate\":" + "\"" + txFeeRate + "\"" + ",\n" 
				+ "\"AccountsMerkelTreeRoot\":" + "\""
				+ Util.dumpBytes(accountsMerkelTreeRoot, 16) + "\"" + ",\n" + "\"TransactionsMerkelTreeRoot\":" + "\""
				+ Util.dumpBytes(transactionsMerkelTreeRoot, 16) + "\"" + "\n" + "}";
	}

	@Override
	public boolean isSanity() {
		if (version == null || accountsMerkelTreeRoot == null || transactionsMerkelTreeRoot == null) {
			return false;
		}
//		if (totalSupply.compareTo(Util.MIN_EQC) < 0 || totalSupply.compareTo(Util.MAX_EQC) > 0) {
//			return false;
//		}
//		if(!(txFeeRate > 0 && txFeeRate <= 127)) {
//			return false;
//		}
		if(!(txFeeRate > 0 && txFeeRate <= 10)) {
			return false;
		}
		if (accountsMerkelTreeRoot.length != Util.HASH_LEN || transactionsMerkelTreeRoot.length != Util.HASH_LEN) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the txFeeRate
	 */
	public byte getTxFeeRate() {
		return txFeeRate;
	}

	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(byte txFeeRate) {
		this.txFeeRate = txFeeRate;
	}
	
}
