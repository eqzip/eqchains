/**
 * EQCoin core - EQZIP's EQCoin core library
 * @copyright 2018 EQZIP Inc.  All rights reserved...
 * https://www.eqzip.com
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
package com.eqzip.eqcoin.keystore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Vector;
import com.eqzip.eqcoin.util.EQCType;
import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Keystore {
	private Vector<Account> accounts;
	private final String KEYSTORE_PATH = Util.PATH + "/EQCoin.keystore";
	private final String KEYSTORE_PATH_BAK = Util.PATH + "/EQCoin.keystore.bak";
	private static Keystore instance;

	private Keystore() {
		accounts = loadAccounts(KEYSTORE_PATH);
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

	public synchronized void createAccount(Account account) {
		if (!isAccountExist(account)) {
			accounts.add(account);
			saveAccounts(accounts);
		}
	}

	public Vector<Account> loadAccounts(String path) {
		Vector<Account> accounts = new Vector<Account>();
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
				int value = 0;
				while ((value = is.read()) != -1) {
					// Load accounts from EQCoin.keystore
					if (EQCType.isBin(value)) {
						byte[] len = new byte[EQCType.getBinLen(value)];
						is.read(len);
						int il =  EQCType.getBinDataLen(value, len);
						Log.info("data len：" + il);
						byte[] acc = new byte[(int)il];
						is.read(acc);
						if (!Account.isValid(acc)) {
							Log.info("Error not valid account.");
						}
						else {
							accounts.add(new Account(acc));
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("EQCoin.keystore not found: " + e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("Load accounts failed: " + e.getMessage());
			} finally {
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

	public boolean saveAccounts(final Vector<Account> accounts) {
		boolean bool = true;
		try {
//			if (!createKeystore(KEYSTORE_PATH)) {
//				Log.info("Error, keystore create failed.");
//			}
			File file = new File(KEYSTORE_PATH);
			File fileBak = new File(KEYSTORE_PATH_BAK);
			// Backup old key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if(fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}
			
			// Get all accounts
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (Account acc : accounts) {
				bos.write(EQCType.bytesToBin(acc.getBytes()));
			}
			
			// Save all accounts to EQCoin.keystore
			OutputStream os = new FileOutputStream(file);
			os.write(bos.toByteArray());
			os.flush();
			os.close();

			// Backup new key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if(fileBak.exists()) {
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

	public boolean isAccountExist(Account account) {
		boolean bool = false;
		for (Account acc : accounts) {
			if (acc.equals(account)) {
				bool = true;
				Log.info(account.toString() + " doesn't exist.");
			}
		}
		return bool;
	}

}
