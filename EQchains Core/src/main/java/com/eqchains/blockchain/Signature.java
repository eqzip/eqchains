package com.eqchains.blockchain;
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
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import com.eqzip.eqcoin.serialization.EQCTypable;
//import com.eqzip.eqcoin.serialization.EQCType;
//import com.eqzip.eqcoin.util.Log;
//import com.eqzip.eqcoin.util.SerialNumber;
//
///**
// * The Signature contains the Transaction's Signature every Transaction's Signature is unique.
// * 
// * @author Xun Wang
// * @date Oct 23, 2018
// * @email 10509759@qq.com
// */
//public class Signature implements EQCTypable {
//	/** 
//	 * Current Signature's relevant address' serial number.
//	 */
//	private SerialNumber signatureSerialNumber = null;
//	/**
////	 * The Transaction's relevant Signature which is Bin type.
//	 */
//	private byte[] signature = null;
//	/**
//	 * VERIFICATION_COUNT equal to the number of member variables of the class to be
//	 * verified.
//	 */
//	private final static byte VERIFICATION_COUNT = 2;
//
//	public Signature() {
//		super();
//	}
//
//	public Signature(byte[] bytes) {
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		byte[] data;
//
//		// Parse PublicKey's serial number
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			publickeySerialNumber = new SerialNumber(data);
//		}
//
//		// Parse publicKey
//		data = null;
//		if ((data = EQCType.parseBin(is)) != null) {
//			publicKey = data;
//		}
//	}
//
//	public static boolean isValid(byte[] bytes) {
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		byte[] data;
//		byte validCount = 0;
//
//		// Parse PublicKey's serial number
//		if ((data = EQCType.parseEQCBits(is)) != null) {
//			++validCount;
//		}
//
//		// Parse publicKey
//		data = null;
//		if ((data = EQCType.parseBin(is)) != null) {
//			++validCount;
//		}
//		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
//	}
//
//	/**
//	 * Get the PublicKey's bytes for storage it in the EQC block chain.
//	 * 
//	 * @return byte[]
//	 */
//	@Override
//	public byte[] getBytes() {
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		try {
//			os.write(publickeySerialNumber.getEQCBits());
//			os.write(EQCType.bytesToBin(publicKey));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return os.toByteArray();
//	}
//
//	/**
//	 * @return the publickeySerialNumber
//	 */
//	public SerialNumber getSignatureSerialNumber() {
//		return publickeySerialNumber;
//	}
//
//	/**
//	 * @param publickeySerialNumber the publickeySerialNumber to set
//	 */
//	public void setSignatureSerialNumber(SerialNumber publickeySerialNumber) {
//		this.publickeySerialNumber = publickeySerialNumber;
//	}
//
//	/**
//	 * @return the publicKey
//	 */
//	public byte[] getPublicKey() {
//		return publicKey;
//	}
//
//	/**
//	 * @param publicKey the publicKey to set
//	 */
//	public void setPublicKey(byte[] publicKey) {
//		this.publicKey = publicKey;
//	}
//
//	/**
//	 * Get the PublicKey's BIN for storage it in the EQC block chain.
//	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
//	 * 
//	 * @return byte[]
//	 */
//	@Override
//	public byte[] getBin() {
//		// TODO Auto-generated method stub
//		return EQCType.bytesToBin(getBytes());
//	}
//	
//}
