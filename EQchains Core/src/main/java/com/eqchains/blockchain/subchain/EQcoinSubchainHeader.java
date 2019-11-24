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
package com.eqchains.blockchain.subchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqchains.blockchain.account.Asset;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date July 30, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSubchainHeader extends EQCSubchainHeader {
	/**
	 * Calculate this according to newAddressList ARRAY's length
	 */
	private ID totalAccountNumbers;
	private CoinbaseTransaction coinbaseTransaction;
	
	public EQcoinSubchainHeader(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is);
	}
	
	public EQcoinSubchainHeader(ByteArrayInputStream is) throws Exception {
		parseBody(is);
	}

	public EQcoinSubchainHeader() {
		id = Asset.EQCOIN;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		totalAccountNumbers = EQCType.parseID(is);
		coinbaseTransaction = (CoinbaseTransaction) CoinbaseTransaction.parseTransaction(EQCType.parseBIN(is), AddressShape.ID);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(super.getBodyBytes());
		os.write(totalAccountNumbers.getEQCBits());
		if(coinbaseTransaction != null) {
			os.write(coinbaseTransaction.getBin(AddressShape.ID));
		}
		else {
			os.write(EQCType.NULL);
		}
		return os.toByteArray();
	}

	/**
	 * @return the totalAccountNumbers
	 */
	public ID getTotalAccountNumbers() {
		return totalAccountNumbers;
	}

	/**
	 * @param totalAccountNumbers the totalAccountNumbers to set
	 */
	public void setTotalAccountNumbers(ID totalAccountNumbers) {
		this.totalAccountNumbers = totalAccountNumbers;
	}

	/**
	 * @return the coinbaseTransaction
	 */
	public CoinbaseTransaction getCoinbaseTransaction() {
		return coinbaseTransaction;
	}

	/**
	 * @param coinbaseTransaction the coinbaseTransaction to set
	 */
	public void setCoinbaseTransaction(CoinbaseTransaction coinbaseTransaction) {
		this.coinbaseTransaction = coinbaseTransaction;
	}

	public boolean isSanity(AccountsMerkleTree accountsMerkleTree) {
		if(!isSanity()) {
			return false;
		}
		if(accountsMerkleTree.getHeight().compareTo(Util.getMaxCoinbaseHeight(accountsMerkleTree.getHeight())) < 0) {
			if(totalAccountNumbers == null || coinbaseTransaction == null) {
				return false;
			}
			if(!totalAccountNumbers.isSanity() || !coinbaseTransaction.isSanity(AddressShape.ID)) {
				return false;
			}
		}
		else {
			if(totalAccountNumbers == null || coinbaseTransaction != null) {
				return false;
			}
			if(!totalAccountNumbers.isSanity()) {
				return false;
			}
		}
		return true;
	}
	
	protected String toInnerJson() {
		return "\"EQcoinSubchainHeader\":" + "{\n" + "\"SubchainID\":" + "\"" + id + "\"" + ",\n"
				+ "\"TotalTxFee\":" + "\"" + totalTxFee + "\"" + ",\n" + "\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\"" + ",\n"
				 + "\"TotalAccountNumbers\":" + "\"" + totalAccountNumbers + "\"" + ",\n" + coinbaseTransaction.toInnerJson()
				+ "\n" + "}";
	}
	
}
