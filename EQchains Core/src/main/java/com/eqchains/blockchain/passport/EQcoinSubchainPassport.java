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
package com.eqchains.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqchains.blockchain.passport.Passport.AccountType;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;

/**
 * @author Xun Wang
 * @date Jun 22, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSubchainPassport extends AssetSubchainPassport {
	/**
	 * Body field include TxFeeRate
	 */
	private byte txFeeRate;
	private ID checkPointHeight;
	private byte[] checkPointHash;

	public EQcoinSubchainPassport() {
		super(AccountType.EQCOINSUBCHAIN);
	}
	
	public EQcoinSubchainPassport(byte[] bytes) throws NoSuchFieldException, IOException {
		super(bytes);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		super.parseBody(is);
		// Parse TxFeeRate
		txFeeRate = EQCType.parseBIN(is)[0];
		// Parse CheckPoint Height
		checkPointHeight = EQCType.parseID(is);
		// Parse CheckPoint Hash
		checkPointHash = EQCType.parseBIN(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(super.getBodyBytes());
			os.write(EQCType.bytesToBIN(new byte[]{txFeeRate}));
			os.write(checkPointHeight.getEQCBits());
			os.write(EQCType.bytesToBIN(checkPointHash));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
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
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"EQcoinSubchainAccount\":" + 
				"\n{\n" +
					super.toInnerJson() + ",\n" +
					"\"TxFeeRate\":" + "\"" + txFeeRate + "\"" +
				"\n}";
	}
	
	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(!super.isSanity()) {
			return false;
		}
		if(checkPointHeight == null || checkPointHash == null) {
			return false;
		}
		if(!checkPointHeight.isSanity() || checkPointHash.length != 32) {
			return false;
		}
		if(txFeeRate < 1 || txFeeRate >10) {
			return false;
		}
		return true;
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

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}

	/**
	 * @return the checkPointHash
	 */
	public byte[] getCheckPointHash() {
		return checkPointHash;
	}

	/**
	 * @param checkPointHash the checkPointHash to set
	 */
	public void setCheckPointHash(byte[] checkPointHash) {
		this.checkPointHash = checkPointHash;
	}
	
}
