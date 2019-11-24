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
import java.security.acl.Owner;
import java.util.Collections;

import org.apache.avro.io.parsing.Symbol;

import com.eqchains.blockchain.account.Account.AccountType;
import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date May 13, 2019
 * @email 10509759@qq.com
 */
public abstract class SmartContractAccount extends Account {
	/**
	 * Body field include LanguageType
	 */
	private ID founderID;
	private LanguageType languageType;
	private long totalStateSize;
	private ID totalStateSizeUpdateHeight;
	
	public enum LanguageType {
		JAVA, INTELLIGENT, MOVE, INVALID;
		public static LanguageType get(int ordinal) {
			LanguageType languageType = null;
			switch (ordinal) {
			case 0:
				languageType = LanguageType.JAVA;
				break;
			case 1:
				languageType = LanguageType.INTELLIGENT;
				break;
			case 2:
				languageType = LanguageType.MOVE;
				break;
			default:
				languageType = LanguageType.INVALID;
				break;
			}
			return languageType;
		}
		public boolean isSanity() {
			if((this.ordinal() < INTELLIGENT.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	
	/**
	 * @author Xun Wang
	 * @date Jun 18, 2019
	 * @email 10509759@qq.com
	 * 
	 * When SmartContract inactive we can achieve it's state DB to GitHub which is free. 
	 * After this all the full node doesn't need store it's state DB in local just keep the relevant Account's state.
	 * When it become active again all the full node can recovery it's state DB from GitHub. 
	 */
	@Deprecated
	public enum State {
		ACTIVE, OVERDUE, INACTIVE, INVALID;
		public static State get(int ordinal) {
			State state = null;
			switch (ordinal) {
			case 0:
				state = State.ACTIVE;
				break;
			case 1:
				state = State.OVERDUE;
				break;
			case 2:
				state = State.INACTIVE;
				break;
			default:
				state = State.INVALID;
				break;
			}
			return state;
		}
		public boolean isSanity() {
			if((this.ordinal() < ACTIVE.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
//	public enum SmartContractType {
//		SUBCHAIN, MISC, INVALID;
//		public static SmartContractType get(int ordinal) {
//			SmartContractType smartContractType = null;
//			switch (ordinal) {
//			case 0:
//				smartContractType = SmartContractType.SUBCHAIN;
//				break;
//			case 1:
//				smartContractType = SmartContractType.MISC;
//				break;
//			default:
//				smartContractType = SmartContractType.INVALID;
//				break;
//			}
//			return smartContractType;
//		}
//		public boolean isSanity() {
//			if((this.ordinal() < SUBCHAIN.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
//				return false;
//			}
//			return true;
//		}
//		public byte[] getEQCBits() {
//			return EQCType.intToEQCBits(this.ordinal());
//		}
//	}
	
	protected SmartContractAccount(AccountType accountType) {
		super(accountType);
	}
	
//	protected SmartContractAccount(byte[] bytes) throws NoSuchFieldException, IOException {
//		super(AccountType.SMARTCONTRACT);
//	}
	
	
//	public static SmartContractType parseSubchainType(ByteArrayInputStream is) {
//		SmartContractType subchainType = SmartContractType.INVALID;
//		byte[] data = null;
//		try {
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				subchainType = SmartContractType.get(EQCType.eqcBitsToInt(data));
//			}
//		} catch (NoSuchFieldException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return subchainType;
//	}

	public SmartContractAccount(byte[] bytes) throws NoSuchFieldException, IOException {
		super(bytes);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse Super Body
		super.parseBody(is);
		// Parse Sub Body
		// Parse FounderID
		founderID = new ID(EQCType.parseEQCBits(is));
		// Parse LanguageType
		languageType = LanguageType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		// Parse TotalStateSize
		totalStateSize = Util.bytesToLong(EQCType.parseBIN(is));
		totalStateSizeUpdateHeight = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(super.getBodyBytes());
			os.write(founderID.getEQCBits());
			os.write(languageType.getEQCBits());
			os.write(EQCType.bytesToBIN(Util.longToBytes(totalStateSize)));
			os.write(totalStateSizeUpdateHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the languageType
	 */
	public LanguageType getLanguageType() {
		return languageType;
	}

	/**
	 * @param languageType the languageType to set
	 */
	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}
	
	public String toInnerJson() {
		return 
					super.toInnerJson() + ",\n" +
					"\"FounderID\":" + "\"" + founderID + "\"" + ",\n" +
					"\"LanguageType\":" + "\"" + languageType + "\"" + ",\n" +
					"\"TotalStateSize\":" + "\"" + totalStateSize + "\"" + ",\n" +
					"\"TotalStateSizeUpdateHeight\":" + "\"" + totalStateSizeUpdateHeight + "\"" + ",\n";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(!super.isSanity()) {
			return false;
		}
		if(founderID == null || languageType == null || totalStateSize <= 0) {
			return false;
		}
		// Need do more job about language type
		if(!founderID.isSanity()) {
			return false;
		}
		return true;
	}

	/**
	 * @return the totalStateSize
	 */
	public long getTotalStateSize() {
		return totalStateSize;
	}

	/**
	 * @param totalStateSize the totalStateSize to set
	 */
	public void setTotalStateSize(long totalStateSize) {
		this.totalStateSize = totalStateSize;
		Log.info(""+totalStateSize);
	}

	/**
	 * @return the totalStateSizeUpdateHeight
	 */
	public ID getTotalStateSizeUpdateHeight() {
		return totalStateSizeUpdateHeight;
	}

	/**
	 * @param totalStateSizeUpdateHeight the totalStateSizeUpdateHeight to set
	 */
	public void setTotalStateSizeUpdateHeight(ID totalStateSizeUpdateHeight) {
		this.totalStateSizeUpdateHeight = totalStateSizeUpdateHeight;
	}

	/**
	 * @return the founderID
	 */
	public ID getFounderID() {
		return founderID;
	}

	/**
	 * @param founderID the founderID to set
	 */
	public void setFounderID(ID founderID) {
		this.founderID = founderID;
	}
	
}
