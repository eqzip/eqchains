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
package com.eqchains.keystore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.ECPrivateKey;

import com.eqchains.blockchain.account.Passport.AddressShape;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.crypto.EQCPublicKey;
import com.eqchains.keystore.Keystore.ECCTYPE;
import com.eqchains.serialization.EQCTypable;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class UserAccount implements EQCTypable {

	private String userName;
	private byte[] pwdHash;
	private byte[] privateKey;
	private byte[] publicKey;
	private String readableAddress;
	private long balance;

	public UserAccount() {
	}

	/**
	 * @param userName
	 * @param pwdHash
	 * @param privateKey
	 * @param publicKey
	 * @param readableAddress
	 * @param balance
	 */
	public UserAccount(String userName, byte[] pwdHash, byte[] privateKey, byte[] publicKey, String readableAddress, long balance) {
		super();
		this.userName = userName;
		this.pwdHash = pwdHash;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.readableAddress = readableAddress;
		this.balance = balance;
	}

	public UserAccount(byte[] bytes) throws NoSuchFieldException, IOException {
		super();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);

		// Parse userName
		userName = EQCType.bytesToASCIISting(EQCType.parseBIN(is));

		// Parse pwdHash
		pwdHash = EQCType.parseBIN(is);

		// Parse privateKey
		privateKey = EQCType.parseBIN(is);

		// Parse publicKey
		publicKey = EQCType.parseBIN(is);

		// Parse address
		readableAddress = EQCType.bytesToASCIISting(EQCType.parseBIN(is));

		// Parse balance
		balance = EQCType.eqcBitsToLong(EQCType.parseEQCBits(is));

	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// userName
			os.write(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(userName)));
			// pwdHash
			os.write(EQCType.bytesToBIN(pwdHash));
			// privateKey
			os.write(EQCType.bytesToBIN(privateKey));
			// publicKey
			os.write(EQCType.bytesToBIN(publicKey));
			// address
			os.write(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(readableAddress)));
			// balance
			os.write(EQCType.longToEQCBits(balance));
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
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the pwdHash
	 */
	public byte[] getPwdHash() {
		return pwdHash;
	}

	/**
	 * @param pwdHash the pwdHash to set
	 */
	public void setPwdHash(byte[] pwdHash) {
		this.pwdHash = pwdHash;
	}

	/**
	 * @return the privateKey
	 */
	public byte[] getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}
	
	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return the readableAddress
	 */
	public String getReadableAddress() {
		return readableAddress;
	}
	
	public byte[] getAddressAI() {
		return AddressTool.addressToAI(readableAddress);
	}

	/**
	 * @param readableAddress the readableAddress to set
	 */
	public void setReadableAddress(String readableAddress) {
		this.readableAddress = readableAddress;
	}

	/**
	 * @return the balance
	 */
	public long getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(long balance) {
		this.balance = balance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readableAddress == null) ? 0 : readableAddress.hashCode());
		result = prime * result + Arrays.hashCode(privateKey);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserAccount other = (UserAccount) obj;
		if (readableAddress == null) {
			if (other.readableAddress != null)
				return false;
		} else if (!readableAddress.equals(other.readableAddress))
			return false;
		if (!Arrays.equals(privateKey, other.privateKey))
			return false;
		return true;
	}
	
	public boolean isPasswordCorrect(String password) {
		return Arrays.equals(pwdHash, Util.EQCCHA_MULTIPLE_DUAL(password.getBytes(), Util.HUNDREDPULS, true, false));
	}
	
	public byte[] signTransaction(String password, TransferTransaction transaction) {
		byte[] signature = null;
		try {
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			PrivateKey privateKey = Util.getPrivateKey(Util.AESDecrypt(this.privateKey, password), transaction.getTxIn().getPassport().getAddressType());
			ecdsa.initSign(privateKey);
			ecdsa.update(transaction.getBytes(AddressShape.READABLE));
			signature = ecdsa.sign();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return signature;
	}
	
	public boolean verifyTransaction(String password, TransferTransaction transaction) {
		boolean boolVerifyResult = false;
		EQCPublicKey eqcPublicKey = null;
		if(Util.AddressTool.getAddressType(transaction.getTxIn().getPassport().getReadableAddress()) == AddressType.T1) {
			eqcPublicKey = new EQCPublicKey(ECCTYPE.P256);
		}
		else if(transaction.getTxIn().getPassport().getAddressType() == AddressType.T2) {
			eqcPublicKey = new EQCPublicKey(ECCTYPE.P521);
		}
		eqcPublicKey.setECPoint(Util.AESDecrypt(publicKey, password));
		Signature sign;
		try {
			sign = Signature.getInstance("SHA1withECDSA", "SunEC");
			sign.initVerify(eqcPublicKey);
			sign.update(transaction.getBytes(AddressShape.READABLE));
			boolVerifyResult = sign.verify(transaction.getSignature());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		} 
		return boolVerifyResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return
				"{\n" +
				"\"Account\":" + 
				"{\n" +
					"\"userName\":" + "\"" + userName + "\"" + ",\n" +
					"\"pwdHash\":" + "\"" + Util.dumpBytes(pwdHash, 16) + "\"" + ",\n" +
					"\"privateKey\":" + "\"" + Util.dumpBytes(privateKey, 16)  + "\"" + ",\n" +
					"\"publicKey\":" + "\"" + Util.dumpBytes(publicKey, 16)  + "\"" + ",\n" +
					"\"address\":" + "\"" + readableAddress + "\"" + ",\n" +
					"\"balance\":" + "\"" + Long.toHexString(balance) + "\"" + "\n" +
				"}\n" +
			"}";
	}

	public AddressType getAddressType() {
		return Util.AddressTool.getAddressType(readableAddress);
	}

	@Override
	public boolean isSanity() {
		if(userName == null || pwdHash == null || privateKey == null || publicKey == null || readableAddress == null) {
			return false;
		}
		if(pwdHash.length != Util.HASH_LEN) {
			return false;
		}
		if(readableAddress.length() < Util.MIN_ADDRESS_LEN || readableAddress.length() > Util.MAX_ADDRESS_LEN) {
			return false;
		}
		if(balance < 0 || balance >= Util.MAX_EQC) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
