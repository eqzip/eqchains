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
import java.security.acl.Owner;
import java.util.Collections;

import org.apache.avro.io.parsing.Symbol;

import com.eqchains.blockchain.account.Account.AccountType;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;
import com.eqchains.util.ID;
import com.eqchains.util.Log;

/**
 * @author Xun Wang
 * @date May 13, 2019
 * @email 10509759@qq.com
 */
public abstract class SmartContractAccount extends Account {
	/**
	 * Body field include LanguageType
	 */
	private LanguageType languageType;
	private ID leasePeriod;
	public final static byte MAX_VERSION = 0;
	
	public enum LanguageType {
		JAVA, INTELLIGENT, INVALID;
		public static LanguageType get(int ordinal) {
			LanguageType languageType = null;
			switch (ordinal) {
			case 0:
				languageType = LanguageType.JAVA;
				break;
			case 1:
				languageType = LanguageType.INTELLIGENT;
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

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse Super Body
		super.parseBody(is);
		// Parse Sub Body
		// Parse LanguageType
		languageType = LanguageType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		// Parse leasePeriod
		leasePeriod = EQCType.eqcBitsToID(EQCType.parseEQCBits(is));
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(super.getBodyBytes());
			os.write(languageType.getEQCBits());
			os.write(leasePeriod.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the leasePeriod
	 */
	public ID getLeasePeriod() {
		return leasePeriod;
	}

	/**
	 * @param leasePeriod the leasePeriod to set
	 */
	public void setLeasePeriod(ID leasePeriod) {
		this.leasePeriod = leasePeriod;
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
					"\"LanguageType\":" + "\"" + languageType + "\"" + ",\n" +
					"\"LeasePeriod\":" + "\"" + leasePeriod + "\"" + ",\n";
	}
	
}
