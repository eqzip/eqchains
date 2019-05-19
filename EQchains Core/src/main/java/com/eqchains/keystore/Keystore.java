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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Vector;

import com.eqchains.crypto.EQCPublicKey;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool.AddressType;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Keystore {
	public static final int P256 = 1;
	public static final int P521 = 2;
	public final static String SECP256R1 = "secp256r1";
	public final static String SECP521R1 = "secp521r1";
	private Vector<UserAccount> accounts;
	private static Keystore instance;

	public enum ECCTYPE{
		P256, P521
	}
	
	private Keystore() {
		accounts = loadUserAccounts(Util.KEYSTORE_PATH);
	}

	public static Keystore getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new Keystore();
				}
			}
		}
		return instance;
	}

	public synchronized UserAccount createUserAccount(String userName, String password, ECCTYPE type) {
		UserAccount account = new UserAccount();
		KeyPairGenerator kpg;
		AddressType addressType = AddressType.T1;
		
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if(type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
				addressType = AddressType.T1;
			}
			else if(type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
				addressType = AddressType.T2;
			}
			kpg.initialize(ecsp, SecureRandom.getInstanceStrong());
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			EQCPublicKey eqcPublicKey = new EQCPublicKey(type);
			eqcPublicKey.setECPoint((ECPublicKey) pubKey);
			
			account.setUserName(userName);
			account.setPwdHash(Util.EQCCHA_MULTIPLE_DUAL(password.getBytes(), Util.HUNDREDPULS, true, false));
			account.setPrivateKey(Util.AESEncrypt(((ECPrivateKey)privKey).getS().toByteArray(), password));
			account.setPublicKey(Util.AESEncrypt(eqcPublicKey.getCompressedPublicKeyEncoded(), password));
			account.setReadableAddress(Util.AddressTool.generateAddress(eqcPublicKey.getCompressedPublicKeyEncoded(), addressType));
			account.setBalance(0);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}

		if (account != null && !isUserAccountExist(account)) {
			accounts.add(account);
			saveUserAccounts(accounts);
		}
		return account;
	}

	public Vector<UserAccount> loadUserAccounts(String path) {
		Vector<UserAccount> accounts = new Vector<UserAccount>();
		File file = new File(path);
		if (file.exists()) {
			if (file.length() == 0) {
				Log.info("EQCoin.keystore exists but haven't any account just return.");
				return accounts;
			}
			Log.info("EQCoin.keystore exists and not empty just load it.");
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				while((bytes = EQCType.parseBIN(bis)) != null) {
					if (!UserAccount.isValid(bytes)) {
						Log.Error("not valid account.");
					} else {
						accounts.add(new UserAccount(bytes));
					}
					bytes = null;
				}
//				int value = 0;
//				while ((value = is.read()) != -1) {
//					// Load accounts from EQCoin.keystore
//					if (EQCType.isBin(value)) {
//						byte[] len = new byte[EQCType.getBinLen(value)];
//						is.read(len);
//						int il = EQCType.getBinDataLen(value, len);
//						Log.info("data len：" + il);
//						byte[] acc = new byte[(int) il];
//						is.read(acc);
//						if (!UserAccount.isValid(acc)) {
//							Log.info("Error not valid account.");
//						} else {
//							accounts.add(new UserAccount(acc));
//						}
//					}
//				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("EQCoin.keystore not found: " + e.getMessage());
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("Load accounts failed: " + e.getMessage());
			}  finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.info(e.getMessage());
					}
				}
			}
		}
		return accounts;
	}

	@Deprecated
	public boolean createKeystore(String path) {
		boolean bool = false;
		File file = new File(path);
		if (!file.exists()) {
			Log.info("EQCoin.keystore doesn't exists hasn't any account.");
			try {
				if (file.createNewFile()) {
					bool = true;
					Log.info("EQCoin.keystore create successful.");
				} else {
					bool = false;
					Log.info("EQCoin.keystore create failed.");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("During create EQCoin.keystore error occur" + e.getMessage());
			}
		} else {
			bool = true;
		}
		return bool;
	}

	public boolean saveUserAccounts(final Vector<UserAccount> accounts) {
		boolean bool = true;
		try {
//			if (!createKeystore(KEYSTORE_PATH)) {
//				Log.info("Error, keystore create failed.");
//			}
//			Log.info(Util.KEYSTORE_PATH);
			File file = new File(Util.KEYSTORE_PATH);
			File fileBak = new File(Util.KEYSTORE_PATH_BAK);
			// Backup old key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}

			// Get all accounts
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (UserAccount acc : accounts) {
				bos.write(acc.getBin());
			}

			// Save all accounts to EQCoin.keystore
			OutputStream os = new FileOutputStream(file);
			os.write(bos.toByteArray());
			os.flush();
			os.close();

			// Backup new key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bool = false;
		}
		return bool;
	}

	public boolean isUserAccountExist(UserAccount account) {
		boolean bool = false;
		for (UserAccount acc : accounts) {
			if (acc.equals(account)) {
				bool = true;
				Log.info(account.toString() + " exist.");
				break;
			}
		}
		return bool;
	}

	/**
	 * @return the accounts
	 */
	public Vector<UserAccount> getUserAccounts() {
		return accounts;
	}

}
