///**
// * EQchains core - EQchains Foundation's EQchains core library
// * @copyright 2018-present EQchains Foundation All rights reserved...
// * Copyright of all works released by EQchains Foundation or jointly released by
// * EQchains Foundation with cooperative partners are owned by EQchains Foundation
// * and entitled to protection available from copyright law by country as well as
// * international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, EQchains Foundation reserves all rights to
// * take any legal action and pursue any right or remedy available under applicable
// * law.
// * https://www.eqchains.com
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
//package com.eqchains.blockchain.account;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//
//import com.eqchains.blockchain.account.SmartContractAccount.SmartContractType;
//import com.eqchains.serialization.EQCType;
//import com.eqchains.util.ID;
//
///**
// * @author Xun Wang
// * @date May 14, 2019
// * @email 10509759@qq.com
// */
//abstract class SubchainAccount extends SmartContractAccount {
//	private SubchainType subchainType;
//	private ID version;
//	
//	/*
//	 * VERIFICATION_COUNT equal to the number of member variables of the class to be verified.
//	 */
//	protected final static byte HEADER_VERIFICATION_COUNT = 2; // Super Header + Sub Header verify count
//	protected final static byte BODY_VERIFICATION_COUNT = 2; // Super Body + Sub Body verify count
//	public final static byte MAX_VERSION = 0;
//	
//	public enum SubchainType {
//		ASSET, INVALID;
//		public static SubchainType get(int ordinal) {
//			SubchainType subchainType = null;
//			switch (ordinal) {
//			case 0:
//				subchainType = SubchainType.ASSET;
//				break;
//			default:
//				subchainType = SubchainType.INVALID;
//				break;
//			}
//			return subchainType;
//		}
//		public boolean isSanity() {
//			if((this.ordinal() < ASSET.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
//				return false;
//			}
//			return true;
//		}
//		public byte[] getEQCBits() {
//			return EQCType.intToEQCBits(this.ordinal());
//		}
//	}
//
//	protected SubchainAccount(byte[] bytes) throws NoSuchFieldException, IOException {
//		super(SmartContractType.SUBCHAIN);
//		EQCType.assertNotNull(bytes);
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		parseHeader(is);
//		parseBody(is);
//		EQCType.assertNoRedundantData(is);
//	}
//	
//	protected SubchainAccount(SubchainType subchainType) {
//		super(SmartContractType.SUBCHAIN);
//		this.subchainType = subchainType;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.account.SmartContractAccount#parseHeader(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
//		// TODO Auto-generated method stub
//		super.parseHeader(is);
//		// Parse Sub Header
//		// Parse SmartContractType
//		byte[] data = null;
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			subchainType = SubchainType.get(new ID(data).intValue());
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.account.SmartContractAccount#parseBody(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
//		// TODO Auto-generated method stub
//		super.parseBody(is);
//		byte[] data = null;
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			version = EQCType.eqcBitsToID(data);
//		}
//	}
//	
//	
//
//}
