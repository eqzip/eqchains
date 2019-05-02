/**
 * EQCoin core - EQCOIN Foundation's EQCoin core library
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
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
package com.eqchains.persistence.h2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.naming.spi.DirStateFactory.Result;
import javax.security.sasl.RealmCallback;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.EQCBlock;
import com.eqchains.blockchain.EQCBlockChain;
import com.eqchains.blockchain.EQCHeader;
import com.eqchains.blockchain.Index;
import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.Root;
import com.eqchains.blockchain.Signatures;
import com.eqchains.blockchain.Transactions;
import com.eqchains.blockchain.Account.Publickey;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.Address.AddressShape;
import com.eqchains.configuration.Configuration;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class EQCBlockChainH2 implements EQCBlockChain {

	private static final String JDBC_URL = "jdbc:h2:" + Util.H2_DATABASE_NAME;
	private static final String USER = "W3C SGML";
	/**
	 * @see https://github.com/bitcoin/bitcoin/blob/f5a623eb66c81d9d7b11206d574430af0127546d/src/chainparams.cpp
	 */
	private static final String PASSWORD = "abc";//"The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
	private static final String DRIVER_CLASS = "org.h2.Driver";
	private static Connection connection;
	private static Statement statement;
	private static EQCBlockChainH2 instance;
	private static final int ONE_ROW = 1;
	
	static {
		getInstance();
	}
	
	private EQCBlockChainH2() {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			statement = connection.createStatement();
//			if(!Configuration.getInstance().isInitH2()) {
//				Configuration.getInstance().updateIsInitH2(true);
//				createTable();
//			}
			createTable();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
//		finally {
//			try {
//				statement.close();
//				connection.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			
//		}
	}
	
	public synchronized void dropTable() {
		try {
			statement.execute("DROP TABLE ACCOUNT");
			statement.execute("DROP TABLE PUBLICKEY");
			statement.execute("DROP TABLE TRANSACTION");
			statement.execute("DROP TABLE TRANSACTIONS_HASH");
			statement.execute("DROP TABLE SIGNATURE_HASH");
			statement.execute("DROP TABLE SYNCHRONIZATION");
			statement.execute("DROP TABLE TRANSACTION_POOL");
			statement.execute("DROP TABLE TXIN_HEADER_HASH");
			statement.execute("DROP TABLE ACCOUNT_SNAPSHOT");
//			statement.execute("DROP TABLE ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	private synchronized void createTable() {
		try {
			// Create Account table. Each address should be unique and it's serial number should be one by one
			boolean result = statement.execute("CREATE TABLE IF NOT EXISTS ACCOUNT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "address BINARY(33),"
					+ "email VARCHAR(20),"
					+ "code BINARY,"
					+ "address_update_height BIGINT,"
					+ "publickey BINARY,"
					+ "publickey_update_height BIGINT,"
					+ "balance BIGINT,"
					+ "balance_update_height BIGINT,"
					+ "nonce BIGINT"
					+ ")");
			
			// Create Balance table which contain every Account's history balance
			statement.execute("CREATE TABLE IF NOT EXISTS BALANCE("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "height BIGINT,"
					+ "balance BIGINT"
					+ ")");
			
			// Create PublicKey table
			statement.execute("CREATE TABLE IF NOT EXISTS PUBLICKEY("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "address_id BIGINT,"
					+ "publickey BINARY,"
					+ "height BIGINT"
					+ ")");
			
			// Create Transaction table
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "height BIGINT,"
					+ "trans_id INT,"
					+ "io BOOLEAN,"
					+ "address_id BIGINT,"
					+ "value BIGINT"
					+ ")");
			
			// Create EQC block transactions hash table for fast verify the transaction saved in the TRANSACTION table.
			// Calculate the HASH according to the transactions saved in the TRANSACTION table.
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTIONS_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "height BIGINT,"
					+ "hash BINARY(16)"
					+ ")");
						
			// Create EQC block signatures hash table each transaction's signature hash should be unique
			statement.execute("CREATE TABLE IF NOT EXISTS SIGNATURE_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "height BIGINT,"
					+ "trans_id INT,"
					+ "txin_id BIGINT,"
					+ "signature BINARY(16)"
					+ ")");
			
			// EQC block tail and Account tail height table for synchronize EQC block and Account
			statement.execute("CREATE TABLE IF NOT EXISTS SYNCHRONIZATION("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "block_tail_height BIGINT,"
					+ "total_account_numbers BIGINT"
					+ ")");
			
			// EQC Transaction Pool table
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_POOL("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "txin_address VARCHAR(51),"
					+ "txin_value BIGINT,"
					+ "tx_fee BIGINT,"
					+ "rawdata BINARY,"
					+ "signature_hash BINARY(32),"
					+ "tx_size INT,"
					+ "qos BIGINT,"
					+ "receieved_timestamp BIGINT,"
					+ "record_status BOOLEAN,"
					+ "record_height BIGINT"
					+ ")");
			
			// Create TxIn's EQC Block header's hash table
			statement.execute("CREATE TABLE IF NOT EXISTS TXIN_HEADER_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "header_hash BINARY,"
					+ "address_id BIGINT,"
					+ "height BIGINT"
					+ ")");
			
			// Create Account snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS ACCOUNT_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "readable_address VARCHAR(51),"
					+ "account BINARY,"
				/*	+ "account_hash BIGINT(64),"*/
					+ "snapshot_height BIGINT"
					+ ")");
			
			if(result) {
				Log.info("Create table");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	public synchronized static EQCBlockChainH2 getInstance() {
		if(instance == null) {
			synchronized (EQCBlockChainH2.class) {
				if(instance == null) {
					instance = new EQCBlockChainH2();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressSerialNumber(com.eqzip.eqcoin.blockchain.Address)
	 */
	@Override
	public synchronized ID getAddressID(Address address) {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getAddressShapeBytes(AddressShape.AI));
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddress(com.eqzip.eqcoin.util.SerialNumber)
	 */
	@Override
	public synchronized Address getAddress(ID serialNumber) {
		Address address = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				address = new Address();
				address.setReadableAddress(resultSet.getString("address"));
				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
				// Here need do more job to retrieve the code of address. Need consider
				// sometimes the code is null but otherwise the code isn't null
				address.setCode(resultSet.getBytes("code"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return address;
	}
	
	public synchronized Vector<Account> getAllAccounts(ID height) {
		Vector<Account> accounts = new Vector<Account>();
		Account account = null;
		Address address = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE address_update_height>=?");
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				account = new Account();
				address = new Address();
				address.setReadableAddress(resultSet.getString("address"));
				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
				// Here need do more job to retrieve the code of address. Need consider
				// sometimes the code is null but otherwise the code isn't null
				account.setAddress(address);
				account.setBalance(resultSet.getLong("balance"));
				accounts.add(account);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return accounts;
	}
	
	public synchronized Vector<Account> getAccounts(ID begin, ID end, ID height) {
		Vector<Account> accounts = new Vector<>();
		Account account = null;
		Address address = null;
		Publickey publickey = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE id>=? AND id<? AND address_update_height<=?");
			preparedStatement.setLong(1, begin.longValue());
			preparedStatement.setLong(2, end.longValue());
			preparedStatement.setLong(3, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				account = new Account();
				address = new Address();
				address.setReadableAddress(AddressTool.AIToAddress(resultSet.getBytes("address")));
				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
				account.setAddress(address);
				account.setAddressCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("address_update_height"))));
				if(resultSet.getBytes("publickey") != null) {
					publickey = new Publickey();
					publickey.setPublickey(resultSet.getBytes("publickey"));
					publickey.setPublickeyCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("publickey_update_height"))));
					account.setPublickey(publickey);
				}
				account.setBalance(resultSet.getLong("balance"));
				account.setBalanceUpdateHeight(new ID(BigInteger.valueOf(resultSet.getLong("balance_update_height"))));
				account.setNonce(new ID(BigInteger.valueOf(resultSet.getLong("nonce"))));
				accounts.add(account);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return accounts;
	}

	public synchronized ID getTotalAccountNumber(ID height) {
		long accountsNumber = 0;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT COUNT(*) FROM ACCOUNT AS rowcount WHERE address_update_height<=?");
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				accountsNumber = resultSet.getLong(1);//.getLong("rowcount");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return new ID(accountsNumber);
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#isAddressExists(com.eqzip.eqcoin.blockchain.Address)
	 */
	@Override
	public synchronized boolean isAddressExists(Address address) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			 preparedStatement.setBytes(1, address.getBytes(AddressShape.AI));
			 ResultSet resultSet = preparedStatement.executeQuery();
			 if(resultSet.next()) {
				 return true;
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}
	
	public synchronized boolean appendAddress(Address address, ID address_update_height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("INSERT INTO ACCOUNT (sn, address, code, height) VALUES('" 
//					+ address.getSerialNumber().longValue() + "','"
//					+ address.getAddress() + "','"
//					+ ((null == address.getCode())?"":address.getCode()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
//					+ height.longValue() + "')");
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNT (id, address, code, address_update_height, balance, nonce) VALUES (?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, address.getID().longValue());
			preparedStatement.setBytes(2, address.getBytes(AddressShape.AI));
			preparedStatement.setBytes(3,  address.getCode());
			preparedStatement.setLong(5, address_update_height.longValue() );
			preparedStatement.setLong(6, 0);
			preparedStatement.setLong(7, 0);
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	public synchronized boolean deleteAddress(Address address) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getBytes(AddressShape.AI));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	public synchronized boolean deleteAddressFromHeight(ID height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM ACCOUNT WHERE height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT WHERE height>=?");
			preparedStatement.setLong(1, height.longValue());
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result >= ONE_ROW;
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getLastAddressSerialNumber()
	 */
	public synchronized ID getLastAddressSerialNumber() {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id = SELECT MAX(id) FROM ACCOUNT");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE key = SELECT MAX(key) FROM ACCOUNT");
			ResultSet resultSet = preparedStatement.executeQuery();
			 if(resultSet.next()) {
				 serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressTotalNumbers()
	 */
	public synchronized long getAddressTotalNumbers() {
		// TODO Auto-generated method stub
		return 0;
	}
	
//	public synchronized boolean updateAccount(Account account) {
//		int result = 0;
//		int validCount = 0;
//		try {
//			if (account.isAddressChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"INSERT INTO ACCOUNT (sn, address, code, code_hash, address_update_height, nonce) VALUES (?, ?, ?, ?, ?, ?)");
//				preparedStatement.setLong(1, account.getAddress().getSerialNumber().longValue());
//				preparedStatement.setBytes(2, account.getAddress().getAddressAI());
//				preparedStatement.setBytes(3, account.getAddress().getCode());
//				preparedStatement.setBytes(4, account.getAddress().getCodeHash());
//				preparedStatement.setLong(5, account.getAddressCreateHeight().longValue());
//				preparedStatement.setLong(6, 0);
//				result += preparedStatement.executeUpdate();
//			}
//			
//			if(account.isPublickeyChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET publickey = ?, publickey_update_height = ? where sn = ?");
//				preparedStatement.setBytes(1, account.getPublickey().getPublickey());
//				preparedStatement.setLong(2, account.getPublickey().getPublickeyCreateHeight().longValue());
//				preparedStatement.setLong(3, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//			
//			if(account.isBalanceChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET balance = ?, balance_update_height  = ? where sn = ?");
//				preparedStatement.setLong(1, account.getBalance());
//				preparedStatement.setLong(2, account.getBalanceUpdateHeight().longValue());
//				preparedStatement.setLong(3, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//			
//			if(account.isNonceChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET nonce = ? where sn = ?");
//				preparedStatement.setLong(1, account.getNonce().longValue());
//				preparedStatement.setLong(2, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return result == validCount;
//	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeySerialNumber(com.eqzip.eqcoin.blockchain.Address)
	 */
	public synchronized ID getPublicKeySerialNumber(Address address) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKey(com.eqzip.eqcoin.util.SerialNumber)
	 */
	@Override
	public synchronized PublicKey getPublicKey(ID serialNumber) {
		PublicKey publicKey = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM PUBLICKEY WHERE address_sn='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PUBLICKEY WHERE address_id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			 if(resultSet.next()) {
				 publicKey = new PublicKey();
				 publicKey.setPublicKey(resultSet.getBytes("publickey"));
				 publicKey.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return publicKey;
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#isPublicKeyExists(com.eqzip.eqcoin.blockchain.PublicKey)
	 */
	@Override
	public synchronized boolean isPublicKeyExists(PublicKey publicKey) {
		try {
			ResultSet resultSet;
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE publickey = ?");
			preparedStatement.setBytes(1, publicKey.getPublicKey());
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}

	@Override
	public synchronized boolean savePublicKey(PublicKey publicKey, ID height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("INSERT INTO PUBLICKEY (address_sn, publickey, height) VALUES('" 
//					+ publicKey.getSerialNumber().longValue() + "','"
//					+ ((null == publicKey.getPublicKey())?"":publicKey.getPublicKey()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
//					+ height.longValue() + "')");
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO PUBLICKEY (address_id, publickey, height) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, publicKey.getID().longValue());
			preparedStatement.setBytes(2, publicKey.getPublicKey());
			preparedStatement.setLong(3, height.longValue());
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}
	
	@Override
	public synchronized boolean deletePublicKey(PublicKey publicKey) {
		int result = 0;
		try {
			result = statement.executeUpdate("DELETE FROM PUBLICKEY WHERE publickey='" + publicKey.getPublicKey() + "'");
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result >= 1;
	}

	public synchronized boolean deletePublicKeyFromHeight(ID height) {
		int result = 0;
		try {
			result = statement.executeUpdate("DELETE FROM PUBLICKEY WHERE height>='" + height.longValue() + "'");
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getLastPublicKeySerialNumber()
	 */
	public synchronized ID getLastPublicKeySerialNumber() {
		ID serialNumber = null;
		try {
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM PUBLICKEY WHERE key = SELECT MAX(key) FROM PUBLICKEY");
			 if(resultSet.next()) {
				 serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeyTotalNumbers()
	 */
	public synchronized long getPublicKeyTotalNumbers() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCBlock(com.eqzip.eqcoin.util.SerialNumber, boolean)
	 */
	@Override
	public synchronized EQCBlock getEQCBlock(ID height, boolean isSegwit) {
		EQCBlock eqcBlock = null;
		File file = new File(Util.BLOCK_PATH + height.longValue() + Util.EQC_SUFFIX);
		if(file.exists() && file.isFile() && (file.length() > 0)) {
			eqcBlock = new EQCBlock();
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					if (!EQCHeader.isValid(bytes)) {
						throw new ClassCastException("getEQCBlock during parse EqcHeader error occur wrong format");
					}
					eqcBlock.setEqcHeader(new EQCHeader(bytes));
				}
				// Parse Root
				bytes = null;
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					if (!Root.isValid(bytes)) {
						throw new ClassCastException("getEQCBlock during parse Root error occur wrong format");
					}
					eqcBlock.setRoot(new Root(bytes));
				}
//				// Parse Index
//				bytes = null;
//				if ((bytes = EQCType.parseBIN(bis)) != null) {
//					if (!Index.isValid(bytes)) {
//						throw new ClassCastException("getEQCBlock during parse Index error occur wrong format");
//					}
//					eqcBlock.setIndex(new Index(bytes));
//				}
				// Parse Transactions
				bytes = null;
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					if (!Transactions.isValid(bytes)) {
						throw new ClassCastException("getEQCBlock during parse Transactions error occur wrong format");
					}
					eqcBlock.setTransactions(new Transactions(bytes));
				}
				if (!isSegwit) {
					// Parse Signatures
					bytes = null;
					if ((bytes = EQCType.parseBIN(bis)) != null) {
						if (!Signatures.isValid(bytes)) {
							throw new ClassCastException(
									"getEQCBlock during parse Signatures error occur wrong format");
						}
						eqcBlock.setSignatures(new Signatures(bytes));
					}
				}
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				eqcBlock = null;
				Log.Error(e.getMessage());
			}
		}
		return eqcBlock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isEQCBlockExists(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
	@Override
	public synchronized boolean isEQCBlockExists(ID height) {
		boolean isEQCBlockExists = false;
		File file = new File(Util.BLOCK_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isEQCBlockExists(com.eqzip.eqcoin.
	 * blockchain.EQCBlock)
	 */
	public synchronized boolean isEQCBlockExists(EQCBlock eqcBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#saveEQCBlock(com.eqzip.eqcoin.
	 * blockchain.EQCBlock)
	 */
	@Override
	public synchronized boolean saveEQCBlock(EQCBlock eqcBlock) {
		boolean isSaveSuccessful = true;
		File file = new File(Util.BLOCK_PATH + eqcBlock.getHeight().longValue() + Util.EQC_SUFFIX);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(eqcBlock.getBytes());
			// Save EQCBlock
			OutputStream os = new FileOutputStream(file);
			os.write(bos.toByteArray());
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isSaveSuccessful = false;
			Log.Error(e.getMessage());
		}
		return isSaveSuccessful;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#deleteEQCBlock(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
	@Override
	public synchronized boolean deleteEQCBlock(ID height) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCHeader(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	public synchronized EQCHeader getEQCHeader(ID height) {
		EQCHeader eqcHeader = null;
		File file = new File(Util.BLOCK_PATH + height.longValue() + Util.EQC_SUFFIX);
		if(file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					if (!EQCHeader.isValid(bytes)) {
						throw new ClassCastException("getEQCBlock during parse EqcHeader error occur wrong format");
					}
					eqcHeader = new EQCHeader(bytes);
				}
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				eqcHeader = null;
				Log.Error(e.getMessage());
			}
		}
		return eqcHeader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getTransactionsHash(com.EQCOIN Foundation.
	 * eqcoin.util.SerialNumber)
	 */
	@Override
	public synchronized byte[] getTransactionsHash(ID height) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getBalance(com.eqzip.eqcoin.
	 * blockchain.Address)
	 */
	public synchronized long getBalance(Address address) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
		try {
			// Get all TxIn's value
			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
					+ address.getID().longValue() + "' AND io = true");
			while (resultSet.next()) {
				txInValue += resultSet.getLong("value");
			}
			
			// Get all TxOut's value
			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
					+ address.getID().longValue() + "' AND io = false");
			while (resultSet.next()) {
//				Log.info("balance: " + txOutValue);
				txOutValue += resultSet.getLong("value");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return txOutValue - txInValue;
	}

	public synchronized long getBalance(Address address, ID height) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
		try {
			// Get all TxIn's value
			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
					+ address.getID().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
			while (resultSet.next()) {
				txInValue += resultSet.getLong("value");
			}
			
			// Get all TxOut's value
			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
					+ address.getID().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
			while (resultSet.next()) {
				txOutValue += resultSet.getLong("value");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return txInValue - txOutValue;
	}

//	@Override
//	public synchronized Vector<Transaction> getTransactionList(Address address, SerialNumber height) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public synchronized byte[] getTxInBlockHeaderHash(Address txInAddress) {
//		byte[] bytes = null;
//		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM TXIN_HEADER_HASH WHERE address_id ='" + txInAddress.getSerialNumber().longValue() + "'");
//			 if(resultSet.next()) {
//				 bytes = resultSet.getBytes("header_hash");
//			 }
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return bytes;
//	}
//	
//	@Override
//	public synchronized boolean setTxInBlockHeaderHash(byte[] hash, SerialNumber addressSerialNumber, SerialNumber height) {
//		int result = 0;
//		try {
//			result = statement.executeUpdate("INSERT INTO TXIN_HEADER_HASH (header_hash, address_sn, height) VALUES('" 
//					+ hash + "','"
//					+ addressSerialNumber.longValue() + "','"
//					+ height.longValue() + "')");
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return result == ONE_ROW;
//	}

	public synchronized ID getTxInHeight(Address txInAddress) {
		ID height = null;
		try {
			ResultSet resultSet;
			if (txInAddress.getReadableAddress() != null) {
				PreparedStatement preparedStatement = connection
						.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
				preparedStatement.setBytes(1, txInAddress.getBytes(AddressShape.AI));
				resultSet = preparedStatement.executeQuery();
			} else if (txInAddress.getID() != null) {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
				preparedStatement.setLong(1, txInAddress.getID().longValue());
				resultSet = preparedStatement.executeQuery();
			} else {
				throw new NullPointerException("Address and SerialNumber cannot both be null");
			}
			if (resultSet.next()) {
				height = new ID(BigInteger.valueOf(resultSet.getLong("address_update_height")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return height;
	}

	@Override
	public synchronized boolean saveEQCBlockTailHeight(ID height) {
		int result = 0;
		try {
			if (getEQCBlockTailHeight() != null) {
				result = statement.executeUpdate("UPDATE SYNCHRONIZATION SET block_tail_height='"
						+ height.longValue() + "' WHERE key='1'");

			}
			else {
				result = statement.executeUpdate("INSERT INTO SYNCHRONIZATION (block_tail_height) VALUES('" 
						+ height.longValue() + "')");
			}
//			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized ID getEQCBlockTailHeight() {
		ID serialNumber = null;
		try {
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNCHRONIZATION");
			 if(resultSet.next()) {
				 serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("block_tail_height")));
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	public synchronized ID getTotalAccountNumbers(ID height) {
		ID serialNumber = null;
		try {
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNCHRONIZATION");
			 if(resultSet.next()) {
				 serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("total_account_numbers")));
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	public synchronized boolean setTotalAccountNumbers(ID numbers) {
		int result = 0;
		try {
			if (getEQCBlockTailHeight() != null) {
				result = statement.executeUpdate("UPDATE SYNCHRONIZATION SET total_account_numbers='"
						+ numbers.longValue() + "' WHERE key='1'");

			}
			else {
				result = statement.executeUpdate("INSERT INTO SYNCHRONIZATION (total_account_numbers) VALUES('" 
						+ numbers.longValue() + "')");
			}
//			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}
	
	@Override
	public synchronized byte[] getEQCHeaderHash(ID height) {
		EQCBlock eqcBlock = getEQCBlock(height, true);
		return Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(eqcBlock.getEqcHeader().getBytes(), Util.HUNDRED_THOUSAND, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getTransactionList(com.eqzip.eqcoin
	 * .blockchain.Address)
	 */
	@Override
	public synchronized Vector<Transaction> getTransactionListInPool() {
		Vector<Transaction> transactions = new Vector<Transaction>();
		try {
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE qos>='9' OR "
						+ "(qos='8') OR "
					 	+ "(qos='4' AND receieved_timestamp<=?) OR "
						+ "(qos='2' AND receieved_timestamp<=?) OR "
					 	+ "(qos='1' AND receieved_timestamp<=?) AND "
					 	+ "(record_status = FALSE) ORDER BY qos DESC, receieved_timestamp ASC"
						 );
			 preparedStatement.setLong(1, (System.currentTimeMillis()-200000));
			 preparedStatement.setLong(2, (System.currentTimeMillis()-400000));
			 preparedStatement.setLong(3, (System.currentTimeMillis()-600000));
			 ResultSet resultSet = preparedStatement.executeQuery();
			 while(resultSet.next()) {
				 byte[] bytes = resultSet.getBytes("rawdata");
				 ByteArrayInputStream is = new ByteArrayInputStream(bytes);
				 byte[] data = null;
				 
				 // Parse Transaction
				 if((data = EQCType.parseBIN(is)) != null) {
					 if(Transaction.isValid(data, Address.AddressShape.READABLE)) {
						 Transaction transaction = Transaction.parseTransaction(data, Address.AddressShape.READABLE);
						 transactions.add(transaction);
						// Parse PublicKey
						data = null;
						if ((data = EQCType.parseBIN(is)) != null) {
							PublicKey publickey = new PublicKey();
							publickey.setPublicKey(data);
							transaction.setPublickey(publickey);
						}

						// Parse Signature
						data = null;
						if ((data = EQCType.parseBIN(is)) != null) {
							transaction.setSignature(data);
						}
					 }
				 }
			 }
		} catch (SQLException | NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
//		Collections.sort(transactions);
		return transactions;
	}
	
	/**
	 * @param transaction EQC Transaction include PublicKey and Signature so for every Transaction it's raw is unique
	 * @return boolean If add Transaction successful return true else return false
	 */
	@Override
	public synchronized boolean addTransactionInPool(Transaction transaction) {
		int result = 0;
		try {
//			result = statement.executeUpdate("INSERT INTO TRANSACTION_POOL (txin_address, txin_value, tx_fee, rawdata, tx_size, qos, receieved_timestamp) VALUES('" 
//					+ transaction.getTxIn().getAddress().getAddress() + "','"
//					+ transaction.getTxIn().getValue() + "','"
//					+ transaction.getTxFee() + "','"
//					+ transaction.getBytes(AddressShape.ADDRESS) + "','"
//					+ size + "','"
//					+ qos + "','"
//					+ timestamp + "')");
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO TRANSACTION_POOL (txin_address, txin_value, tx_fee, rawdata, signature_hash, tx_size, qos, receieved_timestamp, record_status) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, transaction.getTxIn().getAddress().getReadableAddress());
			preparedStatement.setLong(2, transaction.getTxIn().getValue());
			preparedStatement.setLong(3, transaction.getTxFeeLimit());
			preparedStatement.setBytes(4, transaction.getRPCBytes());
			preparedStatement.setBytes(5, Util.SHA3_256(transaction.getSignature()));
			preparedStatement.setInt(6, transaction.getMaxBillingSize());
			preparedStatement.setLong(7, transaction.getQosRate());
			preparedStatement.setLong(8, System.currentTimeMillis());
			preparedStatement.setBoolean(9, false);
			result =preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(Transaction transaction) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM TRANSACTION_POOL WHERE rawdata='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE signature_hash= ?");
			preparedStatement.setBytes(1,  Util.SHA3_256(transaction.getSignature()));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(Transaction transaction) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM TRANSACTION_POOL WHERE signature='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE signature_hash= ?");
			preparedStatement.setBytes(1,  Util.SHA3_256(transaction.getSignature()));
			ResultSet resultSet  = preparedStatement.executeQuery(); 
			if(resultSet.next()) {
				 return true;
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}

	public synchronized Vector<Transaction> getTransactionList(Address address) {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized Vector<Transaction> getTransactionList(Address address, ID height) {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized boolean addTransaction(Transaction transaction, ID height, int trans_id) {
		int result = 0;
		int validCount = transaction.isCoinBase()?(transaction.getTxOutNumber()):(1+transaction.getTxOutNumber());
		
		try {
			if(!transaction.isCoinBase()) {
				result += statement.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('" 
					+ height.longValue() + "','"
					+ trans_id + "','"
					+ true + "','"
					+ transaction.getTxIn().getAddress().getID().longValue() + "','"
					+ transaction.getTxIn().getValue() + "')");
			}
			for(TxOut txOut : transaction.getTxOutList()) {
				result += statement.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('" 
					+ height.longValue() + "','"
					+ trans_id + "','"
					+ false + "','"
					+ txOut.getAddress().getID().longValue() + "','"
					+ txOut.getValue() + "')");
			}
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == validCount;
	}

	public synchronized boolean deleteTransactionFromHeight(ID height) {
		int result = 0;
		try {
			result = statement.executeUpdate("DELETE FROM TRANSACTION WHERE height>='" + height.longValue() + "'");
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result >= ONE_ROW;
	}

	public synchronized boolean isTransactionExists(Transaction transaction) {
		return isSignatureExists(transaction.getSignature());
	}

	public synchronized int getTransactionNumbersIn24hours(Address address, ID currentHeight) {
		int numbers = 0;
		long heightOffset = ((currentHeight.longValue() - 8640)>0)?(currentHeight.longValue() - 8640):0;
		try {
			ResultSet resultSet;
			if (address.getID() == null) {
				address.setID(getAddressID(address));
			}

//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getSerialNumber().longValue() + "' AND height >='" + heightOffset + "'");
			
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION WHERE address_id = ? AND height >= ? AND io = TRUE");
			preparedStatement.setLong(1, address.getID().longValue() );
			preparedStatement.setLong(2, heightOffset);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				++numbers;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return numbers;
	}

	public synchronized boolean addAllTransactions(EQCBlock eqcBlock) {
		int isSuccessful = 0;
		Vector<Transaction> transactions = eqcBlock.getTransactions().getNewTransactionList();
		for(int i=0; i<transactions.size(); ++i) {
			if(addTransaction(transactions.get(i), eqcBlock.getHeight(), i)) {
				++isSuccessful;
			}
		}
		return isSuccessful == transactions.size();
	}

	public synchronized boolean isSignatureExists(byte[] signature) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM SIGNATURE_HASH WHERE signature='" + signature + "'");
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SIGNATURE_HASH WHERE signature = ?");
			 preparedStatement.setBytes(1, Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(signature, Util.ONE, true));
			 ResultSet resultSet = preparedStatement.executeQuery();
			 if(resultSet.next()) {
				 return true;
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}

	public synchronized boolean addAllAddress(EQCBlock eqcBlock) {
		boolean isSuccess = false;
		for(Address address : eqcBlock.getTransactions().getNewAccountList()) {
			isSuccess = appendAddress(address, eqcBlock.getHeight());
		}
		return isSuccess;
	}

	public synchronized boolean addAllPublicKeys(EQCBlock eqcBlock) {
		boolean isSuccess = false;
		for(PublicKey publicKey : eqcBlock.getTransactions().getNewPublicKeyList()) {
			isSuccess = savePublicKey(publicKey, eqcBlock.getHeight());
		}
		return isSuccess;
	}

	public synchronized boolean appendSignature(ID height, int trans_id, ID txin_id, byte[] signature) {
		int result = 0;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SIGNATURE_HASH (height, trans_id, txin_id, signature) VALUES(?, ?, ?, ?)");
			preparedStatement.setLong(1, height.longValue());
			preparedStatement.setInt(2, trans_id);
			preparedStatement.setLong(3, txin_id.longValue());
			preparedStatement.setBytes(4, Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(signature, Util.ONE, true));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	public synchronized boolean addAllSignatures(EQCBlock eqcBlock) {
		boolean isSuccess = false;
		for(int i=0; i<eqcBlock.getSignatures().getSignatureList().size(); ++i) {
			isSuccess = appendSignature(eqcBlock.getHeight(), i, eqcBlock.getTransactions().getNewTransactionList().get(i+1).getTxIn().getAddress().getID(), eqcBlock.getSignatures().getSignatureList().get(i));
		}
		return isSuccess;
	}

	public synchronized long getBalanceFromAccount(Address address) {
		long balance = 0;
		try {
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableAddress() + "'");
			 if(resultSet.next()) {
				 balance = resultSet.getLong("balance");
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return balance;
	}

	public synchronized boolean updateBalanceInAccount(Address address, long balance) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET balance = ? where address = ?");
			preparedStatement.setLong(1, balance);
			preparedStatement.setString(2, address.getReadableAddress());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return result == ONE_ROW;
	}

	public synchronized int getNonce(Address address) {
		int nonce = 0;
		try {
			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableAddress() + "'");
			 if(resultSet.next()) {
				 nonce = resultSet.getInt("nonce");
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return nonce;
	}

	public synchronized boolean updateNonce(Address address, int nonce) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET nonce = ? where address = ?");
			preparedStatement.setInt(1, nonce);
			preparedStatement.setString(2, address.getReadableAddress());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionsInPool(EQCBlock eqcBlock) {
		int isSuccessful = 0;
		Vector<Transaction> transactions = eqcBlock.getTransactions().getNewTransactionList();
		for(int i=1; i<transactions.size(); ++i) {
			if(deleteTransactionInPool(transactions.get(i))) {
				++isSuccessful;
			}
		}
		return isSuccessful == transactions.size()-1;
	}

	@Override
	public boolean close() {
		boolean boolResult = true;
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				boolResult = false;
				Log.Error(e.getMessage());
			}
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				boolResult = false;
				Log.Error(e.getMessage());
			}
		}
		return boolResult;
	}

	@Override
	public boolean saveAccount(Account account) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Account getAccount(ID serialNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteAccount(ID serialNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Account getAccount(byte[] addressAI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveTotalAccountNumbers(ID numbers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public synchronized Account getAccountSnapshot(ID accountID, ID height) {
		Account account = null;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height <=? ORDER BY snapshot_height DESC LIMIT 1");
			preparedStatement.setLong(1, accountID.longValue());
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				account = new Account(resultSet.getBytes("account"));
			} else {
				preparedStatement = connection.prepareStatement(
						"SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height>? ORDER BY snapshot_height ASC LIMIT 1");
				preparedStatement.setLong(1, accountID.longValue());
				preparedStatement.setLong(2, height.longValue());
				resultSet = preparedStatement.executeQuery();
				if(resultSet.next()) {
					account = new Account(resultSet.getBytes("account"));
				}
			}
		} catch (SQLException | NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}
	
	public synchronized boolean isAccountSnapshotExists(ID accountID, ID height) {
		boolean isSucc = false;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height=?");
			preparedStatement.setLong(1, accountID.longValue());
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				isSucc = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isSucc;
	}

	@Override
	public synchronized boolean saveAccountSnapshot(Account account, ID height) {
		int result = 0;
		PreparedStatement preparedStatement = null;
		try {
			if (isAccountSnapshotExists(account.getID(), height)) {
					preparedStatement = connection.prepareStatement("UPDATE ACCOUNT_SNAPSHOT SET account = ?, snapshot_height = ? where id = ?");
					preparedStatement.setBytes(1, account.getBytes());
					preparedStatement.setLong(2, height.longValue());
					preparedStatement.setLong(3, account.getID().longValue());
					result = preparedStatement.executeUpdate();
					Log.info("UPDATE: " + result);
			}
			else {
				preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNT_SNAPSHOT (id, readable_address, account, snapshot_height) VALUES (?, ?, ?, ?)");
				preparedStatement.setLong(1, account.getID().longValue());
				preparedStatement.setString(2, account.getAddress().getReadableAddress());
				preparedStatement.setBytes(3, account.getBytes());
				preparedStatement.setLong(4, height.longValue());
				result = preparedStatement.executeUpdate();
				Log.info("INSERT: " + result);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteAccountSnapshot(ID height, boolean isForward) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT_SNAPSHOT WHERE snapshot_height " + (isForward?">=?":"<=?"));
			preparedStatement.setLong(1, height.longValue());
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result >= ONE_ROW;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		close();
	}

}
