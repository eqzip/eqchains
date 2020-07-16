/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package com.eqchains.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import com.eqchains.avro.O;
import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.blockchain.hive.EQCHeader;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.blockchain.passport.Asset;
import com.eqchains.blockchain.passport.AssetSubchainPassport;
import com.eqchains.blockchain.passport.EQcoinSubchainPassport;
import com.eqchains.blockchain.passport.Lock;
import com.eqchains.blockchain.passport.Passport;
import com.eqchains.blockchain.passport.Lock.LockShape;
import com.eqchains.blockchain.subchain.EQCSubchain;
import com.eqchains.blockchain.subchain.EQcoinSubchain;
import com.eqchains.blockchain.transaction.CoinbaseTransaction;
import com.eqchains.blockchain.transaction.CompressedPublickey;
import com.eqchains.blockchain.transaction.OperationTransaction;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.blockchain.transaction.Transaction.TransactionType;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.blockchain.transaction.operation.Operation;
import com.eqchains.blockchain.transaction.operation.UpdateAddressOperation;
import com.eqchains.blockchain.transaction.operation.UpdateCheckPointOperation;
import com.eqchains.blockchain.transaction.operation.UpdateTxFeeRateOperation;
import com.eqchains.rpc.Balance;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.MaxNonce;
import com.eqchains.rpc.Nest;
import com.eqchains.rpc.SignHash;
import com.eqchains.rpc.TransactionIndex;
import com.eqchains.rpc.TransactionIndexList;
import com.eqchains.rpc.TransactionList;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class EQCBlockChainH2 implements EQCBlockChain<O> {
	private static final String JDBC_URL = "jdbc:h2:" + Util.H2_DATABASE_NAME;
	private static final String USER = "Believer";
	private static final String PASSWORD = "God bless us...";
	private static final String DRIVER_CLASS = "org.h2.Driver";
	private static Connection connection;
//	private static Statement statement;
	private static EQCBlockChainH2 instance;
	private static final int ONE_ROW = 1;
	private static final String TRANSACTION_GLOBAL = "TRANSACTION_GLOBAL";
	private static final String TRANSACTION_MINING = "TRANSACTION_MINING";
	private static final String TRANSACTION_VALID = "TRANSACTION_VALID";
	private static final String TRANSACTION_INDEX_GLOBAL = "TRANSACTION_INDEX_GLOBAL";
	private static final String TRANSACTION_INDEX_MINING = "TRANSACTION_INDEX_MINING";
	private static final String TRANSACTION_INDEX_VALID = "TRANSACTION_INDEX_VALID";
	private static final String LOCK_GLOBAL = "LOCK_GLOBAL";
	private static final String LOCK_MINING = "LOCK_MINING";
	private static final String LOCK_VALID = "LOCK_VALID";
	private static final String PASSPORT_GLOBAL = "PASSPORT_GLOBAL";
	private static final String PASSPORT_MINING = "PASSPORT_MINING";
	private static final String PASSPORT_VALID = "PASSPORT_VALID";
	
	public enum NODETYPE {
		NONE, FULL, MINER
	}
	
	public enum STATUS {
		BEGIN, END
	}
	
	public enum TRANSACTION_OP {
		TXIN, TXOUT, PASSPORT, PUBLICKEY, ADDRESS, TXFEERATE, CHECKPOINT, INVALID;
		public static TRANSACTION_OP get(int ordinal) {
			TRANSACTION_OP op = null;
			switch (ordinal) {
			case 0:
				op = TRANSACTION_OP.TXIN;
				break;
			case 1:
				op = TRANSACTION_OP.TXOUT;
				break;
			case 2:
				op = TRANSACTION_OP.PASSPORT;
				break;
			case 3:
				op = TRANSACTION_OP.PUBLICKEY;
				break;
			case 4:
				op = TRANSACTION_OP.ADDRESS;
				break;
			case 5:
				op = TRANSACTION_OP.TXFEERATE;
				break;
			case 6:
				op = TRANSACTION_OP.CHECKPOINT;
				break;
			default:
				op = TRANSACTION_OP.INVALID;
				break;
			}
			return op;
		}
		public boolean isSanity() {
			if((this.ordinal() < TXIN.ordinal()) || (this.ordinal() >= INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	private String createLockTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id > 0,"
				+ "passport_id BIGINT NOT NULL  CHECK passport_id > 0,"
				+ "readable_lock VARCHAR(255) NOT NULL UNIQUE,"
//				+ "create_height BIGINT NOT NULL CHECK create_height >= 0,"
				+ "publickey BINARY(64) UNIQUE,"
				// For in case exists fork chain need keep this
				+ "publickey_create_height BIGINT NOT NULL CHECK publickey_create_height > 0"
				+ ")";
	}
	
	private String createPassportTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id > 0,"
				+ "lock_id BIGINT NOT NULL UNIQUE CHECK lock_id > 0,"
//				+ "create_height BIGINT NOT NULL CHECK create_height >= 0,"
				+ "hash BINARY(64) NOT NULL,"
				// For in case exists fork chain need keep this
				+ "update_height BIGINT NOT NULL CHECK update_height >= 0,"
				+ "bytes BINARY NOT NULL UNIQUE"
				+ ")";
	}
	
	private String createTransactionIndexTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "sn BIGINT NOT NULL UNIQUE CHECK sn > 0,"
				+ "type TINYINT NOT NULL CHECK type >= 0,"
				+ "height BIGINT NOT NULL CHECK height >= 0,"
				+ "index INT CHECK index >= 0,"
				+ "asset_id BIGINT NOT NULL CHECK asset_id > 0,"
				+ "nonce BIGINT NOT NULL CHECK nonce > 0"
//				+ "FOREIGN KEY (sn) REFERENCES " + tableName.substring(0, tableName.lastIndexOf("_INDEX")) + " (sn) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ")";
	}
	
	private String createTransactionTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "sn BIGINT NOT NULL CHECK sn > 0,"
				+ "op TINYINT NOT NULL CHECK op >= 0,"
				+ "id BIGINT NOT NULL CHECK id > 0,"
				+ "value BIGINT CHECK value > 0,"
				+ "object BINARY,"
				+ ")";
	}
	
	private EQCBlockChainH2() throws ClassNotFoundException, SQLException {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
//			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			connection.createStatement().execute("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SNAPSHOT");
//			connection.setAutoCommit(false);
			connection.commit();
//			createTable();
	}
	
	@Override
	public synchronized boolean dropTable() throws SQLException {
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE ACCOUNT");
			statement.execute("DROP TABLE PUBLICKEY");
			statement.execute("DROP TABLE TRANSACTION");
			statement.execute("DROP TABLE TRANSACTIONS_HASH");
			statement.execute("DROP TABLE SIGNATURE_HASH");
			statement.execute("DROP TABLE SYNCHRONIZATION");
			statement.execute("DROP TABLE TRANSACTION_POOL");
			statement.execute("DROP TABLE TXIN_HEADER_HASH");
			statement.execute("DROP TABLE ACCOUNT_SNAPSHOT");
			statement.close();
//			statement.execute("DROP TABLE ");
			return true; // Here need do more job
	}
	
	public synchronized void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		   	// Create Lock table.
		boolean result = statement.execute(createLockTable(getLockTableName(Mode.GLOBAL)));
		
		 result = statement.execute(createLockTable(getLockTableName(Mode.MINING)));
		 
		 result = statement.execute(createLockTable(getLockTableName(Mode.VALID)));
		 
			// Create Account table. Each Account should be unique and it's Passport's ID should be one by one
			result = statement.execute(createPassportTable(getPassportTableName(Mode.GLOBAL)));
			
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.MINING)));
			 
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.VALID)));
			 
				// Create Global state relevant table's update status table
				result = statement.execute("CREATE TABLE IF NOT EXISTS ACCOUNT_UPDATE_STATUS("
						+ "height BIGINT NOT NULL CHECK height >= 0,"
						+ "snapshot TINYINT NOT NULL,"
						+ "account_merge TINYINT NOT NULL,"
						+ "transaction_merge TINYINT NOT NULL,"
						+ "clear TINYINT NOT NULL"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS EQCHIVE("
						+ "height BIGINT  NOT NULL CHECK height >= 0,"
						+ "bytes BINARY NOT NULL UNIQUE,"
						+ "eqcheader_hash BINARY(64) NOT NULL"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS TEST("
						+ "height BINARY NOT NULL CHECK height >= 0"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS TEST1("
						+ "height BINARY NOT NULL CHECK height >= 0"
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
			
			// Create Transaction relevant table
			statement.execute(createTransactionTable(TRANSACTION_GLOBAL));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_GLOBAL));
			
			// Create Transaction mining relevant table
			statement.execute(createTransactionTable(TRANSACTION_MINING));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_MINING));
			
			// Create Transaction valid relevant table
			statement.execute(createTransactionTable(TRANSACTION_VALID));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_VALID));
			
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
					+ "trans_id BIGINT,"
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
					+ "asset_id BIGINT,"
					+ "txin_id BIGINT,"
					+ "txin_value BIGINT,"
					+ "nonce BIGINT,"
					+ "rawdata BINARY,"
					+ "signature BINARY,"
					+ "proof BINARY(5),"
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
			
			// Create Passport snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS PASSPORT_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
//					+ "address_ai BINARY(33),"
					+ "passport BINARY,"
				/*	+ "account_hash BIGINT(64),"*/
					+ "snapshot_height BIGINT"
					+ ")");
			
			// Create Lock snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS LOCK_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
//					+ "address_ai BINARY(33),"
					+ "lock BINARY,"
				/*	+ "account_hash BIGINT(64),"*/
					+ "snapshot_height BIGINT"
					+ ")");
			
			// Create Transaction max continues Nonce table
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_MAX_NONCE("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "subchain_id BIGINT,"
					+ "max_nonce BIGINT"
					+ ")");
			
			// Create EQchains Network table
			statement.execute("CREATE TABLE IF NOT EXISTS NETWORK ("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "ip  VARCHAR,"
					+ "type INT,"
					+ "counter INT,"
					+ "sync_time BIGINT"
					+ ")");
			
			statement.close();
			
			if(result) {
				Log.info("Create table");
			}
	}
	
	public synchronized static EQCBlockChainH2 getInstance() throws ClassNotFoundException, SQLException {
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
	@Deprecated
	public synchronized ID getAddressID(Lock address) {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
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
	@Deprecated
	public synchronized Lock getAddress(ID serialNumber) {
		Lock address = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				address = new Lock();
				address.setReadableLock(resultSet.getString("address"));
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
	
	@Deprecated
	public synchronized Vector<Passport> getAllAccounts(ID height) {
		Vector<Passport> accounts = new Vector<Passport>();
//		Account account = null;
//		Address address = null;
//		try {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM ACCOUNT WHERE address_update_height>=?");
//			preparedStatement.setLong(1, height.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			while (resultSet.next()) {
//				account = new AssetAccount();
//				address = new Address();
//				address.setReadableAddress(resultSet.getString("address"));
//				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
//				// Here need do more job to retrieve the code of address. Need consider
//				// sometimes the code is null but otherwise the code isn't null
//				account.setAddress(address);
//				account.setBalance(resultSet.getLong("balance"));
//				accounts.add(account);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return accounts;
	}
	
	@Deprecated
	public synchronized Vector<Passport> getAccounts(ID begin, ID end, ID height) {
		Vector<Passport> accounts = new Vector<>();
//		Account account = null;
//		Address address = null;
//		Publickey publickey = null;
//		try {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM ACCOUNT WHERE id>=? AND id<? AND address_update_height<=?");
//			preparedStatement.setLong(1, begin.longValue());
//			preparedStatement.setLong(2, end.longValue());
//			preparedStatement.setLong(3, height.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			while (resultSet.next()) {
//				account = new Account();
//				address = new Address();
//				address.setReadableAddress(AddressTool.AIToAddress(resultSet.getBytes("address")));
//				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
//				account.setAddress(address);
//				account.setAddressCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("address_update_height"))));
//				if(resultSet.getBytes("publickey") != null) {
//					publickey = new Publickey();
//					publickey.setPublickey(resultSet.getBytes("publickey"));
//					publickey.setPublickeyCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("publickey_update_height"))));
//					account.setPublickey(publickey);
//				}
//				account.setBalance(resultSet.getLong("balance"));
//				account.setBalanceUpdateHeight(new ID(BigInteger.valueOf(resultSet.getLong("balance_update_height"))));
//				account.setNonce(new ID(BigInteger.valueOf(resultSet.getLong("nonce"))));
//				accounts.add(account);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return accounts;
	}

	@Deprecated
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
	@Deprecated
	public synchronized boolean isAddressExists(Lock address) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			 preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
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
	
	@Deprecated
	public synchronized boolean appendAddress(Lock address, ID address_update_height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("INSERT INTO ACCOUNT (sn, address, code, height) VALUES('" 
//					+ address.getSerialNumber().longValue() + "','"
//					+ address.getAddress() + "','"
//					+ ((null == address.getCode())?"":address.getCode()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
//					+ height.longValue() + "')");
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNT (id, address, code, address_update_height, balance, nonce) VALUES (?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, address.getId().longValue());
			preparedStatement.setBytes(2, address.getBytes(LockShape.AI));
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

	@Deprecated
	public synchronized boolean deleteAddress(Lock address) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Deprecated
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
	@Deprecated
	public synchronized ID getLastAddressSerialNumber() {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id = SELECT MAX(id) FROM ACCOUNT");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE key = SELECT MAX(key) FROM ACCOUNT");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressTotalNumbers()
	 */
	@Deprecated
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeySerialNumber(com.eqzip.
	 * eqcoin.blockchain.Address)
	 */
	@Deprecated
	public synchronized ID getPublicKeySerialNumber(Lock address) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKey(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	@Deprecated
	public synchronized CompressedPublickey getPublicKey(ID serialNumber) {
		CompressedPublickey publicKey = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM PUBLICKEY WHERE address_sn='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM PUBLICKEY WHERE address_id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				publicKey = new CompressedPublickey();
				publicKey.setCompressedPublickey(resultSet.getBytes("publickey"));
//				 publicKey.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return publicKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isPublicKeyExists(com.eqzip.eqcoin.
	 * blockchain.PublicKey)
	 */
	@Deprecated
	public synchronized boolean isPublicKeyExists(CompressedPublickey publicKey) {
		try {
			ResultSet resultSet;
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE publickey = ?");
			preparedStatement.setBytes(1, publicKey.getCompressedPublickey());
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

	@Deprecated
	public synchronized boolean savePublicKey(CompressedPublickey publicKey, ID height) {
		int result = 0;
//		try {
////			result = statement.executeUpdate("INSERT INTO PUBLICKEY (address_sn, publickey, height) VALUES('" 
////					+ publicKey.getSerialNumber().longValue() + "','"
////					+ ((null == publicKey.getPublicKey())?"":publicKey.getPublicKey()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
////					+ height.longValue() + "')");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("INSERT INTO PUBLICKEY (address_id, publickey, height) VALUES (?, ?, ?)");
////			preparedStatement.setLong(1, publicKey.getId().longValue());
//			preparedStatement.setBytes(2, publicKey.getCompressedPublickey());
//			preparedStatement.setLong(3, height.longValue());
//			result = preparedStatement.executeUpdate();
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean deletePublicKey(CompressedPublickey publicKey) {
		int result = 0;
//		try {
//			result = statement.executeUpdate(
//					"DELETE FROM PUBLICKEY WHERE publickey='" + publicKey.getCompressedPublickey() + "'");
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result >= 1;
	}

	@Deprecated
	public synchronized boolean deletePublicKeyFromHeight(ID height) {
		int result = 0;
//		try {
//			result = statement.executeUpdate("DELETE FROM PUBLICKEY WHERE height>='" + height.longValue() + "'");
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getLastPublicKeySerialNumber()
	 */
	@Deprecated
	public synchronized ID getLastPublicKeySerialNumber() {
		ID serialNumber = null;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM PUBLICKEY WHERE key = SELECT MAX(key) FROM PUBLICKEY");
//			if (resultSet.next()) {
//				serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return serialNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeyTotalNumbers()
	 */
	@Deprecated
	public synchronized long getPublicKeyTotalNumbers() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCBlock(com.eqzip.eqcoin.util.
	 * SerialNumber, boolean)
	 */
	@Override
	public synchronized EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			eqcHive = new EQCHive(resultSet.getBytes("bytes"), isSegwit);
		}
		preparedStatement.close();
		return eqcHive;
	}

	public synchronized boolean isEQCHiveExists(ID height) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		preparedStatement.close();
		return isExists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isEQCBlockExists(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
	@Deprecated
	public synchronized boolean isEQCBlockExists(ID height) {
		boolean isEQCBlockExists = false;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#saveEQCBlock(com.eqzip.eqcoin.
	 * blockchain.EQCBlock)
	 */
	@Override
	public synchronized boolean saveEQCHive(EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		EQCHive eqcHive2 = null;
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isEQCHiveExists(eqcHive.getHeight())) {
			preparedStatement = connection
					.prepareStatement("UPDATE EQCHIVE SET height = ?, bytes = ?,  eqcheader_hash = ? WHERE height = ?");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getBytes());
			preparedStatement.setBytes(3, eqcHive.getEqcHeader().getHash());
			preparedStatement.setLong(4, eqcHive.getHeight().longValue());
			rowCounter = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + rowCounter);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO EQCHIVE (height, bytes, eqcheader_hash) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getBytes());
			preparedStatement.setBytes(3, eqcHive.getEqcHeader().getHash());
			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		}
		connection.commit();
		EQCType.assertEqual(rowCounter, ONE_ROW);
		preparedStatement.close();
//		eqcHive2 = getEQCHive(eqcHive.getHeight(), false);
//		EQCType.assertEqual(eqcHive.getBytes(), eqcHive2.getBytes());
//		byte[] eqcHeaderHash = getEQCHeaderHash(eqcHive.getHeight());
//		EQCType.assertEqual(eqcHive2.getEqcHeader().getHash(), eqcHeaderHash);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#deleteEQCBlock(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
	@Override
	public synchronized boolean deleteEQCHive(ID height) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM EQCHIVE WHERE height =?");
		preparedStatement.setLong(1, height.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		preparedStatement.close();
//		if (isEQCHiveExists(height)) {
//			throw new IllegalStateException("deleteEQCHive No." + height + " failed EQCHive still exists");
//		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCHeader(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	@Deprecated
	public synchronized EQCHeader getEQCHeader(ID height) {
		EQCHeader eqcHeader = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
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
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getBalance(com.eqzip.eqcoin.
	 * blockchain.Address)
	 */
	@Deprecated
	public synchronized long getBalance(Lock address) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
//		try {
//			// Get all TxIn's value
//			resultSet = statement.executeQuery(
//					"SELECT * FROM TRANSACTION WHERE address_id = '" + address.getId().longValue() + "' AND io = true");
//			while (resultSet.next()) {
//				txInValue += resultSet.getLong("value");
//			}
//
//			// Get all TxOut's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = false");
//			while (resultSet.next()) {
////				Log.info("balance: " + txOutValue);
//				txOutValue += resultSet.getLong("value");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return txOutValue - txInValue;
	}

	@Deprecated
	public synchronized long getBalance(Lock address, ID height) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
//		try {
//			// Get all TxIn's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
//			while (resultSet.next()) {
//				txInValue += resultSet.getLong("value");
//			}
//
//			// Get all TxOut's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
//			while (resultSet.next()) {
//				txOutValue += resultSet.getLong("value");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return txInValue - txOutValue;
	}

	@Deprecated
	public synchronized ID getTxInHeight(Lock txInAddress) {
		ID height = null;
		try {
			ResultSet resultSet;
			if (txInAddress.getReadableLock() != null) {
				PreparedStatement preparedStatement = connection
						.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
				preparedStatement.setBytes(1, txInAddress.getBytes(LockShape.AI));
				resultSet = preparedStatement.executeQuery();
			} else if (txInAddress.getId() != null) {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
				preparedStatement.setLong(1, txInAddress.getId().longValue());
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
	public synchronized boolean saveEQCBlockTailHeight(ID height) throws SQLException {
		int rowCounter = 0;
		Statement statement = null;
			statement = connection.createStatement();
			if (getEQCBlockTailHeight() != null) {
				rowCounter = statement.executeUpdate(
						"UPDATE SYNCHRONIZATION SET block_tail_height='" + height.longValue() + "' WHERE key='1'");

			} else {
				rowCounter = statement.executeUpdate(
						"INSERT INTO SYNCHRONIZATION (block_tail_height) VALUES('" + height.longValue() + "')");
			}
			EQCType.assertEqual(rowCounter, ONE_ROW);
			ID savedHeight = getEQCBlockTailHeight();
			Objects.requireNonNull(savedHeight);
			EQCType.assertEqual(height.longValue(), savedHeight.longValue());
			Log.info("saveEQCBlockTailHeight " + height + " successful");
		return true;
	}

	@Override
	public synchronized ID getEQCBlockTailHeight() throws SQLException {
		ID id = null;
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNCHRONIZATION");
		if (resultSet.next()) {
			id = new ID(BigInteger.valueOf(resultSet.getLong("block_tail_height")));
		}
		statement.close();
		return id;
	}

	@Override
	public synchronized ID getTotalAccountNumbers(ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCBlockTailHeight());
		AssetSubchainPassport assetSubchainPassport = null;

		if (height.compareTo(getEQCBlockTailHeight()) < 0) {
			assetSubchainPassport = (AssetSubchainPassport) getPassportSnapshot(ID.ONE, height);
		} else {
			assetSubchainPassport = (AssetSubchainPassport) getPassport(ID.ONE);
		}
		return assetSubchainPassport.getAssetSubchainHeader().getTotalPassportNumbers();
	}

	@Deprecated
	public synchronized boolean setTotalAccountNumbers(ID numbers) {
		int result = 0;
//		try {
//			if (getEQCBlockTailHeight() != null) {
//				result = statement.executeUpdate(
//						"UPDATE SYNCHRONIZATION SET total_account_numbers='" + numbers.longValue() + "' WHERE key='1'");
//
//			} else {
//				result = statement.executeUpdate(
//						"INSERT INTO SYNCHRONIZATION (total_account_numbers) VALUES('" + numbers.longValue() + "')");
//			}
////			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized byte[] getEQCHeaderHash(ID height) throws Exception {
//		EQCType.assertNotBigger(height, getEQCBlockTailHeight());
		byte[] hash = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			hash = resultSet.getBytes("eqcheader_hash");
		}
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getTransactionList(com.eqzip.eqcoin
	 * .blockchain.Address)
	 */
	@Override
	public synchronized Vector<Transaction> getTransactionListInPool()
			throws SQLException, NoSuchFieldException, IllegalStateException, IOException {
		Vector<Transaction> transactions = new Vector<Transaction>();
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE qos>='9' OR " + "(qos='8') OR "
						+ "(qos='4' AND receieved_timestamp<=?) OR " + "(qos='2' AND receieved_timestamp<=?) OR "
						+ "(qos='1' AND receieved_timestamp<=?) AND "
						+ "(record_status = FALSE) ORDER BY qos DESC, receieved_timestamp ASC");
		preparedStatement.setLong(1, (System.currentTimeMillis() - 200000));
		preparedStatement.setLong(2, (System.currentTimeMillis() - 400000));
		preparedStatement.setLong(3, (System.currentTimeMillis() - 600000));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			byte[] bytes = resultSet.getBytes("rawdata");
//			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//
//			// Parse Transaction
//			Transaction transaction = Transaction.parseTransaction(EQCType.parseBIN(is), Passport.AddressShape.READABLE);
//			// Parse PublicKey
//			PublicKey publickey = new PublicKey();
//			publickey.setPublicKey(EQCType.parseBIN(is));
//			transaction.setPublickey(publickey);
//
//			// Parse Signature
//			transaction.setSignature(EQCType.parseBIN(is));
			// Parse Transaction
			transaction = Transaction.parseRPC(bytes);
			transactions.add(transaction);
		}
//		Collections.sort(transactions);
		return transactions;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws SQLException {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND qos<=?");
		preparedStatement.setLong(1, transaction.getTxIn().getKey().getId().longValue());
		preparedStatement.setLong(2, transaction.getNonce().longValue());
		preparedStatement.setLong(3, transaction.getQosRate().longValue());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws SQLException {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND proof=?");
		preparedStatement.setLong(1, transactionIndex.getId().longValue());
		preparedStatement.setLong(2, transactionIndex.getNonce().longValue());
		preparedStatement.setBytes(3, transactionIndex.getProof());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	/**
	 * @param transaction EQC Transaction include PublicKey and Signature so for
	 *                    every Transaction it's raw is unique
	 * @return boolean If add Transaction successful return true else return false
	 * @throws SQLException
	 */
	@Override
	public synchronized boolean saveTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
		int nResult = 0;
		PreparedStatement preparedStatement = null;
		if (!isTransactionExistsInPool(transaction)) {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO TRANSACTION_POOL (asset_id, txin_id, nonce, rawdata, signature, proof, qos, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, transaction.getAssetID().longValue());
			preparedStatement.setLong(2, transaction.getTxIn().getKey().getId().longValue());
			preparedStatement.setLong(3, transaction.getNonce().longValue());
			preparedStatement.setBytes(4, transaction.getRPCBytes());
			preparedStatement.setBytes(5, transaction.getSignature());
			preparedStatement.setBytes(6, transaction.getProof());
			preparedStatement.setLong(7, transaction.getQosRate().longValue());
			preparedStatement.setLong(8, System.currentTimeMillis());
			preparedStatement.setBoolean(9, false);
			preparedStatement.setBoolean(10, false);
			result = preparedStatement.executeUpdate();
		} else {
			preparedStatement = connection.prepareStatement(
					"UPDATE TRANSACTION_POOL SET rawdata=?, signature=?, proof=?, qos=?, receieved_timestamp=?, record_status=?, record_height=? WHERE txin_id=? AND nonce=?");
			preparedStatement.setBytes(1, transaction.getRPCBytes());
			preparedStatement.setBytes(2, transaction.getSignature());
			preparedStatement.setBytes(3, transaction.getProof());
			preparedStatement.setLong(4, transaction.getQosRate().longValue());
			preparedStatement.setLong(5, System.currentTimeMillis());
			preparedStatement.setBoolean(6, false);
			preparedStatement.setBoolean(7, false);
			preparedStatement.setLong(8, transaction.getTxIn().getKey().getId().longValue());
			preparedStatement.setLong(9, transaction.getNonce().longValue());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
//			result = statement.executeUpdate("DELETE FROM TRANSACTION_POOL WHERE rawdata='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
		PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE signature= ?");
		preparedStatement.setBytes(1, transaction.getSignature());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

//	@Override
//	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws SQLException {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM TRANSACTION_POOL WHERE signature='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
//			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE signature= ?");
//			preparedStatement.setBytes(1, transaction.getSignature());
//			ResultSet resultSet  = preparedStatement.executeQuery(); 
//			if(resultSet.next()) {
//				 return true;
//			 }
//		return false;
//	}

	@Deprecated
	public synchronized Vector<Transaction> getTransactionList(Lock address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public synchronized Vector<Transaction> getTransactionList(Lock address, ID height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public synchronized boolean addTransaction(Transaction transaction, ID height, int trans_id) throws SQLException {
		int result = 0;
		int validCount = 0;// transaction.isCoinBase()?(transaction.getTxOutNumber()):(1+transaction.getTxOutNumber());

//		if (!transaction.isCoinBase()) {
//			result += statement
//					.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('"
//							+ height.longValue() + "','" + trans_id + "','" + true + "','"
//							+ transaction.getTxIn().getKey().getId().longValue() + "','"
//							+ transaction.getTxIn().getValue() + "')");
//		}
//		for (TxOut txOut : transaction.getTxOutList()) {
//			result += statement
//					.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('"
//							+ height.longValue() + "','" + trans_id + "','" + false + "','"
//							+ txOut.getKey().getId().longValue() + "','" + txOut.getValue() + "')");
//		}
//			Log.info("result: " + result);
		return result == validCount;
	}

	@Deprecated
	public synchronized boolean deleteTransactionFromHeight(ID height) throws SQLException {
		int result = 0;
//		result = statement.executeUpdate("DELETE FROM TRANSACTION WHERE height>='" + height.longValue() + "'");
//		Log.info("result: " + result);
		return result >= ONE_ROW;
	}


	@Deprecated
	public synchronized int getTransactionNumbersIn24hours(Lock address, ID currentHeight) {
		int numbers = 0;
		long heightOffset = ((currentHeight.longValue() - 8640) > 0) ? (currentHeight.longValue() - 8640) : 0;
		try {
			ResultSet resultSet;
			if (address.getId() == null) {
				address.setID(getAddressID(address));
			}

//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getSerialNumber().longValue() + "' AND height >='" + heightOffset + "'");

			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM TRANSACTION WHERE address_id = ? AND height >= ? AND io = TRUE");
			preparedStatement.setLong(1, address.getId().longValue());
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

//	@Deprecated
//	public synchronized boolean addAllTransactions(EQCHive eqcBlock) throws SQLException {
//		int isSuccessful = 0;
//		Vector<Transaction> transactions = eqcBlock.getTransactions().getNewTransactionList();
//		for(int i=0; i<transactions.size(); ++i) {
//			if(addTransaction(transactions.get(i), eqcBlock.getHeight(), i)) {
//				++isSuccessful;
//			}
//		}
//		return isSuccessful == transactions.size();
//	}

	@Deprecated
	public synchronized boolean isSignatureExists(byte[] signature) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM SIGNATURE_HASH WHERE signature='" + signature + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM SIGNATURE_HASH WHERE signature = ?");
			preparedStatement.setBytes(1, Util.EQCCHA_MULTIPLE_DUAL(signature, Util.ONE, false, true));
			ResultSet resultSet = preparedStatement.executeQuery();
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

	@Deprecated
	public synchronized boolean addAllAddress(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(Passport passport : eqcBlock.getTransactions().getNewPassportList()) {
//			isSuccess = appendAddress(passport, eqcBlock.getHeight());
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized boolean addAllPublicKeys(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(CompressedPublickey publicKey : eqcBlock.getTransactions().getNewCompressedPublickeyList()) {
//			isSuccess = savePublicKey(publicKey, eqcBlock.getHeight());
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized boolean appendSignature(ID height, int trans_id, ID txin_id, byte[] signature) {
		int result = 0;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"INSERT INTO SIGNATURE_HASH (height, trans_id, txin_id, signature) VALUES(?, ?, ?, ?)");
			preparedStatement.setLong(1, height.longValue());
			preparedStatement.setInt(2, trans_id);
			preparedStatement.setLong(3, txin_id.longValue());
			preparedStatement.setBytes(4, Util.EQCCHA_MULTIPLE_DUAL(signature, Util.ONE, false, true));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean addAllSignatures(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(int i=0; i<eqcBlock.getSignatures().getSignatureList().size(); ++i) {
//			isSuccess = appendSignature(eqcBlock.getHeight(), i, eqcBlock.getTransactions().getNewTransactionList().get(i+1).getTxIn().getPassport().getId(), eqcBlock.getSignatures().getSignatureList().get(i));
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized long getBalanceFromAccount(Lock address) {
		long balance = 0;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableLock() + "'");
//			if (resultSet.next()) {
//				balance = resultSet.getLong("balance");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return balance;
	}

	@Deprecated
	public synchronized boolean updateBalanceInAccount(Lock address, long balance) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET balance = ? where address = ?");
			preparedStatement.setLong(1, balance);
			preparedStatement.setString(2, address.getReadableLock());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized int getNonce(Lock address) {
		int nonce = 0;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableLock() + "'");
//			if (resultSet.next()) {
//				nonce = resultSet.getInt("nonce");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return nonce;
	}

	@Deprecated
	public synchronized boolean updateNonce(Lock address, int nonce) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET nonce = ? where address = ?");
			preparedStatement.setInt(1, nonce);
			preparedStatement.setString(2, address.getReadableLock());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionsInPool(EQCHive eqcBlock)
			throws ClassNotFoundException, Exception {
		int isSuccessful = 0;
		for (Transaction transaction : eqcBlock.getEQcoinSubchain().getNewTransactionList()) {
			if (deleteTransactionInPool(transaction)) {
				++isSuccessful;
			}
		}
		return isSuccessful == eqcBlock.getEQcoinSubchain().getNewTransactionList().size();
	}

	@Override
	public synchronized boolean close() throws SQLException {
		boolean boolResult = true;
		if (connection != null) {
			connection.close();
			connection = null;
		}
		return boolResult;
	}

	public boolean savePassport(Passport account) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getPassport(account.getId()) != null) {
			preparedStatement = connection.prepareStatement(
					"UPDATE ACCOUNT SET id = ?, address_ai = ?,  create_height = ?, hash = ?, update_height = ?, bytes = ? WHERE id = ?");
			preparedStatement.setLong(1, account.getId().longValue());
			preparedStatement.setBytes(2, account.getKey().getAddressAI());
			preparedStatement.setLong(3, account.getCreateHeight().longValue());
			preparedStatement.setBytes(4,
					account.isSaveHash() ? Objects.requireNonNull(account.getHash()) : Util.NULL_HASH);
			preparedStatement.setLong(5, account.getUpdateHeight().longValue());
			preparedStatement.setBytes(6, account.getBytes());
			preparedStatement.setLong(7, account.getId().longValue());
			rowCounter = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + rowCounter);
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO ACCOUNT (id, address_ai, create_height, hash, update_height, bytes) VALUES (?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, account.getId().longValue());
			preparedStatement.setBytes(2, account.getKey().getAddressAI());
			preparedStatement.setLong(3, account.getCreateHeight().longValue());
			preparedStatement.setBytes(4,
					account.isSaveHash() ? Objects.requireNonNull(account.getHash()) : Util.NULL_HASH);
			preparedStatement.setLong(5, account.getUpdateHeight().longValue());
			preparedStatement.setBytes(6, account.getBytes());
			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		Passport savedAccount = getPassport(account.getId());
		EQCType.assertEqual(account.getBytes(), savedAccount.getBytes());
		return true;
	}

	public Passport getPassport(ID id) throws SQLException, NoSuchFieldException, IllegalStateException, IOException {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
		}
		return account;
	}
	
	public boolean deletePassport(ID id, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM " + getPassportTableName(mode) + " WHERE id =?");
		preparedStatement.setLong(1, id.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		if (getPassport(id, mode) != null) {
			throw new IllegalStateException("deleteAccount No." + id + " failed Account still exists");
		}
		return true;
	}

	public Passport getPassport(byte[] addressAI)
			throws SQLException, NoSuchFieldException, IllegalStateException, IOException {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address_ai=?");
		preparedStatement.setBytes(1, addressAI);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
		}
		return account;
	}

	@Override
	public synchronized Passport getPassportSnapshot(ID accountID, ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCBlockTailHeight());
		Passport account = null;
//		Account tailAccount = Util.DB().getAccount(accountID);
//		if(tailAccount.getUpdateHeight().compareTo(height) <= 0) {
//			account = tailAccount;
//		}
//		else {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height <=? ORDER BY snapshot_height DESC LIMIT 1");
		preparedStatement.setLong(1, accountID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("account"));
		}
//		}

//		if(account == null) {
//			throw new IllegalStateException("getAccountSnapshot No. " + accountID + " relevant Account is NULL");
//		}

		return account;
	}

	@Override
	public synchronized Passport getPassportSnapshot(byte[] addressAI, ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCBlockTailHeight());
		Passport account = null;
		Passport tailAccount = getPassport(addressAI, Mode.GLOBAL);
		if (tailAccount.getUpdateHeight().compareTo(height) <= 0) {
			account = tailAccount;
		} else {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT * FROM ACCOUNT_SNAPSHOT WHERE address_ai=? AND snapshot_height <=? ORDER BY snapshot_height DESC LIMIT 1");
			preparedStatement.setBytes(1, addressAI);
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				account = Passport.parseAccount(resultSet.getBytes("account"));
			}
		}

//		if(account == null) {
//			throw new NullPointerException(AddressTool.AIToAddress(addressAI) + " relevant Account is NULL");
//		}

		return account;
	}

	public synchronized boolean isPassportSnapshotExists(ID accountID, ID height) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height=?");
		preparedStatement.setLong(1, accountID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public synchronized boolean savePassportSnapshot(Passport account, ID height) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isPassportSnapshotExists(account.getId(), height)) {
			preparedStatement = connection.prepareStatement(
					"UPDATE ACCOUNT_SNAPSHOT SET address_ai = ?, account = ?, snapshot_height = ? where id = ?");
			preparedStatement.setBytes(1, account.getKey().getAddressAI());
			preparedStatement.setBytes(2, account.getBytes());
			preparedStatement.setLong(3, height.longValue());
			preparedStatement.setLong(4, account.getId().longValue());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE Account: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO ACCOUNT_SNAPSHOT (id, address_ai, account, snapshot_height) VALUES (?, ?, ?, ?)");
			preparedStatement.setLong(1, account.getId().longValue());
			preparedStatement.setBytes(2, account.getKey().getAddressAI());
			preparedStatement.setBytes(3, account.getBytes());
			preparedStatement.setLong(4, height.longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT Account ID: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM ACCOUNT_SNAPSHOT WHERE snapshot_height " + (isForward ? ">=?" : "<=?"));
		preparedStatement.setLong(1, height.longValue());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	@Deprecated
	public ID getTransactionMaxNonce(Transaction transaction) throws SQLException {
		ID nonce = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=?");
		preparedStatement.setLong(1, transaction.getTxIn().getKey().getId().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			nonce = new ID(resultSet.getLong("max_nonce"));
		}
		return nonce;
	}

	@Deprecated
	public boolean saveTransactionMaxNonce(Transaction transaction) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO TRANSACTION_MAX_NONCE (id, max_nonce) VALUES (?, ?)");
		preparedStatement.setLong(1, transaction.getTxIn().getKey().getId().longValue());
		preparedStatement.setLong(2, transaction.getNonce().longValue());
		result = preparedStatement.executeUpdate();
		return result == ONE_ROW;
	}

	@Override
	public byte[] getEQCHeaderBuddyHash(ID height, ID currentTailHeight) throws Exception {
		byte[] hash = null;
		// Due to the latest Account is got from current node so it's xxxUpdateHeight
		// doesn't higher than currentTailHeight
//		EQCType.assertNotBigger(height, tail);
		// Here need pay attention to shouldn't include tail height because
		if (height.compareTo(currentTailHeight) < 0) {
			hash = getEQCHeaderHash(height);
		} else if (height.equals(currentTailHeight)) {
			hash = getEQCHeaderHash(height.getPreviousID());
		}
//		else if(height.equals(tail.getNextID())){
//			hash = getEQCHeaderHash(tail);
//		}
		else {
			throw new IllegalArgumentException(
					"Height " + height + " shouldn't bigger than current tail height " + currentTailHeight);
		}
		return hash;
	}

	@Override
	public synchronized MaxNonce getTransactionMaxNonce(Nest nest)
			throws ClassNotFoundException, Exception {
		MaxNonce maxNonce = null;
//		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=? AND subchain_id=?");
//		preparedStatement.setLong(1, nest.getId().longValue());
//		preparedStatement.setLong(2, nest.getAssetID().longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if(resultSet.next()) {
//			maxNonce = new MaxNonce();
//			maxNonce.setNonce(ID.valueOf(resultSet.getLong("max_nonce")));
//		}
//		if(maxNonce == null) {
//			maxNonce = new MaxNonce();
//			maxNonce.setNonce(Util.DB().getAccount(nest.getId()).getAsset(nest.getAssetID()).getNonce());
//		}
		ID currentNonce = getPassport(nest.getId(), Mode.GLOBAL).getAsset(nest.getAssetID()).getNonce();
		maxNonce = new MaxNonce(currentNonce);
		Vector<Transaction> transactions = getPendingTransactionListInPool(nest);
		if (!transactions.isEmpty()) {
			Comparator<Transaction> reverseComparator = Collections.reverseOrder();
			Collections.sort(transactions, reverseComparator);
			Vector<ID> unique = new Vector<>();
			for (Transaction transaction : transactions) {
				if (!unique.contains(transaction.getNonce())) {
					unique.add(transaction.getNonce());
				} else {
					Log.info("Current transaction's nonce is duplicate just discard it");
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				}
				if (transaction.getNonce().compareTo(currentNonce) <= 0) {
					Log.info("Current transaction's nonce is invalid just discard it");
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				}
			}
			transactions = getPendingTransactionListInPool(nest);
			if (!transactions.isEmpty()) {
				Collections.sort(transactions);
				if (transactions.firstElement().getNonce().equals(currentNonce.getNextID())) {
					int i = 0;
					for (; i < (transactions.size() - 1); ++i) {
//						if (i < (transactions.size() - 2)) {
						if (!transactions.get(i).getNonce().getNextID().equals(transactions.get(i + 1).getNonce())) {
							break;
						}
//						}
					}
					maxNonce = new MaxNonce(transactions.get(i).getNonce());
				}
			}
		}
		return maxNonce;
	}

	@Override
	public synchronized boolean isTransactionMaxNonceExists(Nest nest) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=? AND subchain_id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setLong(2, nest.getAssetID().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Deprecated
	@Override
	public boolean saveTransactionMaxNonce(Nest nest, MaxNonce maxNonce) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isTransactionMaxNonceExists(nest)) {
			preparedStatement = connection
					.prepareStatement("UPDATE TRANSACTION_MAX_NONCE SET max_nonce = ?, subchain_id = ? WHERE id = ?");
			preparedStatement.setLong(1, maxNonce.getNonce().longValue());
			preparedStatement.setLong(2, nest.getAssetID().longValue());
			preparedStatement.setLong(3, nest.getId().longValue());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO TRANSACTION_MAX_NONCE (id, max_nonce, subchain_id) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, nest.getId().longValue());
			preparedStatement.setLong(2, maxNonce.getNonce().longValue());
			preparedStatement.setLong(3, nest.getAssetID().longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteTransactionMaxNonce(Nest nest) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM TRANSACTION_MAX_NONCE WHERE id=? AND subchain_id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setLong(2, nest.getAssetID().longValue());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public Balance getBalance(Nest nest) throws SQLException, Exception {
		Balance balance = new Balance();
		balance.deposit(getPassport(nest.getId(), Mode.GLOBAL).getAsset(nest.getAssetID()).getBalance());
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND record_status=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setBoolean(2, false);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			balance.withdraw(ID.valueOf(resultSet.getLong("txin_value")));
		}
		return balance;
	}

	@Override
	public boolean isIPExists(String ip, NODETYPE nodeType) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = null;
		if (nodeType == NODETYPE.MINER || nodeType == NODETYPE.FULL) {
			preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=? AND type=?");
			preparedStatement.setString(1, ip);
			preparedStatement.setInt(2, nodeType.ordinal());
		} else {
			preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
			preparedStatement.setString(1, ip);
		}
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public boolean isMinerExists(String ip) throws SQLException, Exception {
		return isIPExists(ip, NODETYPE.MINER);
	}

	@Override
	public boolean saveMiner(String ip) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, counter = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(2, 0);
			preparedStatement.setString(3, ip);
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO NETWORK (ip, type, counter, sync_time) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, ip);
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(3, 0);
			preparedStatement.setLong(4, 0);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteMiner(String ip) throws SQLException, Exception {
		return deleteNode(ip);
	}

	public boolean deleteNode(String ip) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip);
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public IPList<O> getMinerList() throws SQLException, Exception {
		IPList<O> ipList = new IPList();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
		preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			ipList.addIP(resultSet.getString("ip"));
		}
		return ipList;
	}

	@Override
	public boolean isFullNodeExists(String ip) throws SQLException, Exception {
		return isIPExists(ip, NODETYPE.FULL);
	}

	@Override
	public boolean saveFullNode(String ip) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (!isIPExists(ip, NODETYPE.MINER)) {
			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type) VALUES (?, ?)");
			preparedStatement.setString(1, ip);
			preparedStatement.setInt(2, NODETYPE.FULL.ordinal());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteFullNode(String ip) throws SQLException, Exception {
		return deleteNode(ip);
	}

	@Override
	public IPList<O> getFullNodeList() throws SQLException, Exception {
		IPList<O> ipList = new IPList();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
		preparedStatement.setInt(1, NODETYPE.FULL.ordinal());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			ipList.addIP(resultSet.getString("ip"));
		}
		return ipList;
	}

	@Override
	public Vector<Transaction> getPendingTransactionListInPool(Nest nest) throws SQLException, Exception {
		Vector<Transaction> transactionList = new Vector<>();
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=? AND asset_id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setLong(2, nest.getAssetID().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transaction = Transaction.parseRPC(resultSet.getBytes("rawdata"));
			transaction.getTxIn().getKey().setID(nest.getId());
			transactionList.add(transaction);
		}
		return transactionList;
	}

	@Override
	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws SQLException, Exception {
		TransactionIndexList transactionIndexList = new TransactionIndexList();
		TransactionIndex transactionIndex = null;
		transactionIndexList.setSyncTime(currentSyncTime);
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT txin_id, nonce, proof FROM TRANSACTION_POOL WHERE receieved_timestamp>=? AND receieved_timestamp<?");
		preparedStatement.setLong(1, previousSyncTime);
		preparedStatement.setLong(2, currentSyncTime);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transactionIndex = new TransactionIndex();
			transactionIndex.setID(ID.valueOf(resultSet.getLong("txin_id")));
			transactionIndex.setNonce(ID.valueOf(resultSet.getLong("nonce")));
			transactionIndex.setProof(resultSet.getBytes("proof"));
			transactionIndexList.addTransactionIndex(transactionIndex);
		}
		return transactionIndexList;
	}

	private Transaction getTransactionInPool(TransactionIndex transactionIndex) throws SQLException, Exception {
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND proof=?");
		preparedStatement.setLong(1, transactionIndex.getId().longValue());
		preparedStatement.setLong(2, transactionIndex.getNonce().longValue());
		preparedStatement.setBytes(3, transactionIndex.getProof());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			transaction = Transaction.parseRPC(resultSet.getBytes("rawdata"));
		}
		return transaction;
	}

	@Override
	public boolean saveMinerSyncTime(String ip, long syncTime) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, sync_time = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setLong(2, syncTime);
			preparedStatement.setString(3, ip);
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO NETWORK (ip, type, sync_time) VALUES (?, ?, ?)");
			preparedStatement.setString(1, ip);
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setLong(3, syncTime);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public long getMinerSyncTime(String ip) throws SQLException, Exception {
		long sync_time = 0;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			sync_time = resultSet.getLong("sync_time");
		}
		return sync_time;
	}

	@Override
	public SignHash getSignHash(ID id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveIPCounter(String ip, int counter) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, counter = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(2, counter);
			preparedStatement.setString(3, ip);
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type, counter) VALUES (?, ?, ?)");
			preparedStatement.setString(1, ip);
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(3, counter);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public int getIPCounter(String ip) throws SQLException, Exception {
		int counter = 0;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			counter = resultSet.getInt("counter");
		}
		return counter;
	}

	public void deleteAccountFromTo(ID fromID, ID toID) throws Exception {
		// TODO Auto-generated method stub

	}

	public void deleteEQCHiveFromTo(ID fromHeight, ID toHeight) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean savePassport(Passport account, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getPassport(account.getId(), mode) != null) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getPassportTableName(mode)
					+ " SET id = ?, address_ai = ?,  create_height = ?, hash = ?, update_height = ?, bytes = ? WHERE id = ?");
			preparedStatement.setLong(1, account.getId().longValue());
			preparedStatement.setBytes(2, account.getKey().getAddressAI());
			preparedStatement.setLong(3, account.getCreateHeight().longValue());
			preparedStatement.setBytes(4,
					account.isSaveHash() ? Objects.requireNonNull(account.getHash()) : Util.NULL_HASH);
			preparedStatement.setLong(5, account.getUpdateHeight().longValue());
			preparedStatement.setBytes(6, account.getBytes());
			preparedStatement.setLong(7, account.getId().longValue());
			rowCounter = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + rowCounter);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getPassportTableName(mode)
							+ " (id, address_ai, create_height, hash, update_height, bytes) VALUES (?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, account.getId().longValue());
			preparedStatement.setBytes(2, account.getKey().getAddressAI());
			preparedStatement.setLong(3, account.getCreateHeight().longValue());
			preparedStatement.setBytes(4,
					account.isSaveHash() ? Objects.requireNonNull(account.getHash()) : Util.NULL_HASH);
			preparedStatement.setLong(5, account.getUpdateHeight().longValue());
			preparedStatement.setBytes(6, account.getBytes());
			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		Passport savedPassport = getPassport(account.getId(), mode);
		EQCType.assertEqual(account.getBytes(), savedPassport.getBytes());
		return true;
	}

	@Override
	public Passport getPassport(ID id, Mode mode) throws Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
		}
		return account;
	}
	
	@Override
	public Passport getPassport(ID id, ID height) throws Exception {
		Passport account = null;
		if (height.equals(getEQCBlockTailHeight())) {
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM " + PASSPORT_GLOBAL + " WHERE id=?");
			preparedStatement.setLong(1, id.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				account = Passport.parseAccount(resultSet.getBytes("bytes"));
			}
		} else {
			account = getPassportSnapshot(id, height);
		}
		return account;
	}

	/* (non-Javadoc)
	 * Here only need clear ACCOUNT_MINING or ACCOUNT_VALID
	 * @see com.eqchains.persistence.EQCBlockChain#clear(com.eqchains.blockchain.accountsmerkletree.Filter.Mode)
	 */
	@Override
	public boolean clearPassport(Mode mode) throws Exception {
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM " + getPassportTableName(mode));
		preparedStatement.executeUpdate();
		preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getPassportTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			throw new IllegalStateException("Clear " + getPassportTableName(mode)
					+ " failed still have items in it");
		}
		return true;
	}

	@Override
	public Passport getPassport(byte[] addressAI, Mode mode) throws Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "
				+ getPassportTableName(mode) + " WHERE address_ai = ?");
		preparedStatement.setBytes(1, addressAI);
		;
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
		}
		return account;
	}

	/* (non-Javadoc)
	 * Here only need merge from ACCOUNT_MINING or ACCOUNT_VALID to ACCOUNT
	 * @see com.eqchains.persistence.EQCBlockChain#merge(com.eqchains.blockchain.accountsmerkletree.Filter.Mode)
	 */
	@Override
	public boolean mergePassport(Mode mode) throws SQLException, Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getPassportTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
			account.setHash(resultSet.getBytes("hash"));
			account.setSaveHash(true);
			savePassport(account, Mode.GLOBAL);
		}
		return true;
	}

	@Override
	public boolean takePassportSnapshot(Mode mode, ID height) throws SQLException, Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + ((mode == Mode.MINING) ? "ACCOUNT_MINING" : "ACCOUNT_VALID"));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			account = Passport.parseAccount(resultSet.getBytes("bytes"));
			account.setHash(resultSet.getBytes("hash"));
			account.setSaveHash(true);
			savePassportSnapshot(account, height);
		}
		return true;
	}

//	public boolean savePossibleNode(String ip, NODETYPE nodeType) throws SQLException, Exception {
//		int result = 0;
//		PreparedStatement preparedStatement = null;
//		if (!isIPExists(ip, NODETYPE.FULL)) {
//			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type) VALUES (?, ?)");
//			preparedStatement.setString(1, ip);
//			if(nodeType == NODETYPE.MINER) {
//				preparedStatement.setInt(2, NODETYPE.POSSIBLEMINER.ordinal());
//			}
//			else {
//				preparedStatement.setInt(2, NODETYPE.POSSIBLEFULL.ordinal());
//			}
//			result = preparedStatement.executeUpdate();
////				Log.info("INSERT: " + result);
//		}
//		return result == ONE_ROW;
//	}
//	
//	public IPList getPossibleNode(NODETYPE nodeType) throws SQLException, Exception {
//		IPList ipList = new IPList();
//		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
//		preparedStatement.setInt(1, nodeType.ordinal());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			ipList.addIP(resultSet.getString("ip"));
//		}
//		return ipList;
//	}
//	
//	public boolean updatePossibleNode(String ip, NODETYPE nodeType) throws SQLException {
//		int result = 0;
//		PreparedStatement preparedStatement = null;
//		preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ? where ip = ?");
//		preparedStatement.setInt(1, nodeType.ordinal());
//		preparedStatement.setString(2, ip);
//		result = preparedStatement.executeUpdate();
////					Log.info("UPDATE: " + result);
//		return result == ONE_ROW;
//	}

	public synchronized EQCHive getEQCHiveFile(ID height, boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				eqcHive = new EQCHive(is.readAllBytes(), isSegwit);
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
		}
		return eqcHive;
	}

	public synchronized boolean isEQCBlockExistsFile(ID height) {
		boolean isEQCBlockExists = false;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	public synchronized boolean saveEQCHiveFile(EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		EQCHive eqcHive2 = null;
		File file = new File(Util.HIVE_PATH + eqcHive.getHeight().longValue() + Util.EQC_SUFFIX);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(eqcHive.getBytes());
		// Save EQCBlock
		OutputStream os = new FileOutputStream(file);
		os.write(bos.toByteArray());
		os.flush();
		os.close();
		eqcHive2 = getEQCHive(eqcHive.getHeight(), false);
		EQCType.assertEqual(eqcHive.getBytes(), Objects.requireNonNull(eqcHive2).getBytes());
		return true;
	}

	public synchronized boolean deleteEQCHiveFile(ID height) throws Exception {
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists()) {
			if (file.delete()) {
				Log.info("EQCHive No." + height + " delete successful");
			} else {
				Log.info("EQCHive No." + height + " delete failed");
			}
		} else {
			Log.info("EQCHive No." + height + " doesn't exists");
		}
		if (getEQCHive(height, true) != null) {
			throw new IllegalStateException("EQCHive No." + height + " delete failed");
		}
		return true;
	}

	public synchronized EQCHeader getEQCHeaderFile(ID height) {
		EQCHeader eqcHeader = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
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

	public ID isTransactionIndexExists(Transaction transaction, ID height, ID index) throws Exception {
		ID sn = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION WHERE type=? AND height=? AND index=? AND asset_id=? AND nonce=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		preparedStatement.setByte(1, (byte) transaction.getTransactionType().ordinal());
		preparedStatement.setLong(2, height.longValue());
		preparedStatement.setLong(3, index.longValue());
		preparedStatement.setLong(4, transaction.getAssetID().longValue());
		preparedStatement.setLong(5, transaction.getNonce().longValue());
		resultSet = preparedStatement.executeQuery();
		resultSet.last();
		if(resultSet.getRow() > 1) {
			throw new IllegalStateException("Only can exists one result set for " + transaction + " at height: " + height + " index: " + index);
		}
		if (resultSet.first()) {
			sn = ID.valueOf(resultSet.getLong("sn"));
		}
		preparedStatement.close();
		return sn;
	}

	public boolean addTransactionIndex(Transaction transaction, ID height, ID index, ID sn) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isTransactionIndexExists(transaction, height, index) != null) {
			throw new IllegalStateException(
					"Result set " + transaction + " at height: " + height + " index: " + index + " shouldn't exists");
		}
		preparedStatement = connection.prepareStatement("INSERT INTO " + TRANSACTION_INDEX_GLOBAL
				+ " (sn, type, height, index, asset_id, nonce) VALUES (?, ?, ?, ?, ?, ?)");
		preparedStatement.setLong(1, sn.longValue());
		preparedStatement.setByte(2, (byte) transaction.getTransactionType().ordinal());
		preparedStatement.setLong(3, height.longValue());
		preparedStatement.setLong(4, index.longValue());
		preparedStatement.setLong(5, transaction.getAssetID().longValue());
		preparedStatement.setLong(6, transaction.getNonce().longValue());
		rowCounter = preparedStatement.executeUpdate();
		EQCType.assertEqual(rowCounter, ONE_ROW);
//		Account savedAccount = getAccount(account.getId());
//		EQCType.assertEqual(account.getBytes(), savedAccount.getBytes());
		return true;
	}

	public boolean deleteTransactionIndex(Transaction transaction, ID height, ID index) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM " + TRANSACTION_INDEX_GLOBAL + " WHERE asset_id=? AND height=? AND index=?");
		preparedStatement.setLong(1, transaction.getAssetID().longValue());
		preparedStatement.setLong(2, height.longValue());
		preparedStatement.setLong(3, index.longValue());
		rowCounter = preparedStatement.executeUpdate();
		preparedStatement.close();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		if ((isTransactionIndexExists(transaction, height, index)) != null) {
			throw new IllegalStateException("deleteTransactionIndex " + transaction + " failed Transaction index still exists");
		}
		return true;
	}
	
	private String getLockTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = LOCK_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = LOCK_MINING;
		}
		else if(mode == Mode.VALID) {
			table = LOCK_VALID;
		}
		return table;
	}
	
	private String getPassportTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = PASSPORT_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = PASSPORT_MINING;
		}
		else if(mode == Mode.VALID) {
			table = PASSPORT_VALID;
		}
		return table;
	}
	
	private String getTransactionTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = TRANSACTION_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = TRANSACTION_MINING;
		}
		else if(mode == Mode.VALID) {
			table = TRANSACTION_VALID;
		}
		return table;
	}

	private String getTransactionIndexTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = TRANSACTION_INDEX_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = TRANSACTION_INDEX_MINING;
		}
		else if(mode == Mode.VALID) {
			table = TRANSACTION_INDEX_VALID;
		}
		return table;
	}
	
	public boolean isTransactionExists(ID sn, Mode mode) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getTransactionTableName(mode) + " WHERE sn=?");
		preparedStatement.setLong(1, sn.longValue());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	public boolean deleteTransaction(Transaction transaction, ID sn, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM " + getTransactionTableName(mode) + " WHERE sn=?");
		preparedStatement.setLong(1, sn.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		if (isTransactionExists(sn, mode)) {
			throw new IllegalStateException("deleteTransaction from " + getTransactionTableName(mode) + " failed Transaction still exists");
		}
		return true;
	}

	public ID isTransactionIndexExists(Transaction transaction, ID height, ID index, Mode mode) throws Exception {
		ID sn = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION " + ((mode == Mode.MINING)?TRANSACTION_INDEX_MINING:TRANSACTION_INDEX_VALID) + " WHERE type=? AND height=? AND index=? AND asset_id=? AND nonce=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		preparedStatement.setByte(1, (byte) transaction.getTransactionType().ordinal());
		preparedStatement.setLong(2, height.longValue());
		preparedStatement.setLong(3, index.longValue());
		preparedStatement.setLong(4, transaction.getAssetID().longValue());
		preparedStatement.setLong(5, transaction.getNonce().longValue());
		resultSet = preparedStatement.executeQuery();
		resultSet.last();
		if(resultSet.getRow() > 1) {
			throw new IllegalStateException("Only can exists one result set for " + transaction + " at height: " + height + " index: " + index);
		}
		if (resultSet.first()) {
			sn = ID.valueOf(resultSet.getLong("sn"));
		}
		preparedStatement.close();
		return sn;
	}

	@Override
	public synchronized ID isTransactionExists(Transaction transaction, Mode mode) throws SQLException {
		ID sn = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String transactionIndexTable = getTransactionIndexTableName(mode);
		String transactionTable = getTransactionTableName(mode);
		
		preparedStatement = connection
				.prepareStatement("SELECT * FROM " + transactionIndexTable + " INNER JOIN " + transactionTable + 
						" ON " + transactionIndexTable + ".sn=" + transactionTable + ".sn " + " WHERE " + transactionIndexTable + ".type=? AND " + transactionIndexTable + ".asset_id=? AND " + transactionIndexTable + ".nonce=? AND " + transactionTable + ".op=? AND " + transactionTable + ".id=?", ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
		if (transaction instanceof CoinbaseTransaction) {
			preparedStatement.setByte(1, (byte) transaction.getTransactionType().ordinal());
			preparedStatement.setLong(2, transaction.getAssetID().longValue());
			preparedStatement.setLong(3, transaction.getNonce().longValue());
			preparedStatement.setByte(4, (byte) TRANSACTION_OP.TXOUT.ordinal());
			preparedStatement.setLong(5, transaction.getTxOutList().get(2).getKey().getId().longValue());
		} else {
			preparedStatement.setByte(1, (byte) transaction.getTransactionType().ordinal());
			preparedStatement.setLong(2, transaction.getAssetID().longValue());
			preparedStatement.setLong(3, transaction.getNonce().longValue());
			preparedStatement.setByte(4, (byte) TRANSACTION_OP.TXIN.ordinal());
			preparedStatement.setLong(5, transaction.getTxIn().getKey().getId().longValue());
		}
		resultSet = preparedStatement.executeQuery();
		resultSet.last();
		if(resultSet.getRow() > 1) {
			throw new IllegalStateException("Only can exists one result set for " + transaction);
		}
		if (resultSet.first()) {
			// Here need check if exist transaction equal to original transaction
			sn = ID.valueOf(resultSet.getLong("sn"));
		}
		preparedStatement.close();
		return sn;
	}
	
	@Override
	public boolean saveTransaction(Transaction transaction, ID height, ID index, ID sn, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		String transactionIndexTable = getTransactionIndexTableName(mode);
		String transactionTable = getTransactionTableName(mode);
		
		ID sn1 = isTransactionExists(transaction, mode);
		if (sn1 != null) {
			throw new IllegalStateException(
					"Result set " + transaction + " at height: " + height + " index: " + index + " shouldn't exists");
		}
		
		preparedStatement = connection.prepareStatement("INSERT INTO " + transactionIndexTable + " (sn, type, height, index, asset_id, nonce) VALUES(?, ?, ?, ?, ?, ?)");
		// Save Transaction Index
		preparedStatement.setLong(1, sn.longValue());
		preparedStatement.setByte(2, (byte) transaction.getTransactionType().ordinal());
		preparedStatement.setLong(3, height.longValue());
		if(index == null) {
			preparedStatement.setNull(4, Types.INTEGER);
		}
		else {
			preparedStatement.setInt(4, index.intValue());
		}
		preparedStatement.setLong(5, transaction.getAssetID().longValue());
		preparedStatement.setLong(6, transaction.getNonce().longValue());
		preparedStatement.executeUpdate();
		
		preparedStatement = connection.prepareStatement("INSERT INTO " + transactionTable + " (sn, op, id, value, object) VALUES(?, ?, ?, ?, ?)");
		// Save Transaction
		if(transaction.getTransactionType() == TransactionType.TRANSFER) {
			preparedStatement.setLong(1, sn.longValue());
			preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXIN.ordinal());
			preparedStatement.setLong(3, transaction.getTxIn().getKey().getId().longValue());
			preparedStatement.setLong(4,  transaction.getTxIn().getValue() );
			preparedStatement.setNull(5, Types.BINARY);
			preparedStatement.addBatch();
			for(TxOut txOut:transaction.getTxOutList()) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXOUT.ordinal());
				preparedStatement.setLong(3, txOut.getKey().getId().longValue());
				preparedStatement.setLong(4,  txOut.getValue() );
				preparedStatement.setNull(5, Types.BINARY);
				preparedStatement.addBatch();
				if(txOut.isNew()) {
					preparedStatement.setLong(1, sn.longValue());
					preparedStatement.setByte(2, (byte) TRANSACTION_OP.PASSPORT.ordinal());
					preparedStatement.setLong(3, txOut.getKey().getId().longValue());
					preparedStatement.setNull(4,  Types.BIGINT);
					preparedStatement.setBytes(5, txOut.getKey().getAddressAI());
					preparedStatement.addBatch();
				}
			}
		}
		else if(transaction.getTransactionType() == TransactionType.OPERATION) {
			preparedStatement.setLong(1, sn.longValue());
			preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXIN.ordinal());
			preparedStatement.setLong(3, transaction.getTxIn().getKey().getId().longValue());
			preparedStatement.setLong(4,  transaction.getTxIn().getValue() );
			preparedStatement.setNull(5, Types.BINARY);
			preparedStatement.addBatch();
			for(TxOut txOut:transaction.getTxOutList()) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXOUT.ordinal());
				preparedStatement.setLong(3, txOut.getKey().getId().longValue());
				preparedStatement.setLong(4,  txOut.getValue() );
				preparedStatement.setNull(5, Types.BINARY);
				preparedStatement.addBatch();
				if(txOut.isNew()) {
					preparedStatement.setLong(1, sn.longValue());
					preparedStatement.setByte(2, (byte) TRANSACTION_OP.PASSPORT.ordinal());
					preparedStatement.setLong(3, txOut.getKey().getId().longValue());
					preparedStatement.setNull(4,  Types.BIGINT);
					preparedStatement.setBytes(5, txOut.getKey().getAddressAI());
					preparedStatement.addBatch();
				}
			}
			Operation operation = ((OperationTransaction) transaction).getOperation();
			if(operation instanceof UpdateAddressOperation) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.ADDRESS.ordinal());
				preparedStatement.setLong(3, transaction.getTxIn().getKey().getId().longValue());
				preparedStatement.setNull(4,  Types.BIGINT);
				preparedStatement.setBytes(5, ((UpdateAddressOperation)operation).getAddress().getAddressAI());
				preparedStatement.addBatch();
			}
			else if(operation instanceof UpdateTxFeeRateOperation) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXFEERATE.ordinal());
				preparedStatement.setLong(3, transaction.getTxIn().getKey().getId().longValue());
				preparedStatement.setNull(4,  Types.BIGINT);
				preparedStatement.setBytes(5, new byte[] {((UpdateTxFeeRateOperation)operation).getTxFeeRate()});
				preparedStatement.addBatch();
			}
			else if(operation instanceof UpdateCheckPointOperation) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.CHECKPOINT.ordinal());
				preparedStatement.setLong(3, transaction.getTxIn().getKey().getId().longValue());
				preparedStatement.setNull(4,  Types.BIGINT);
				preparedStatement.setBytes(5, ((UpdateCheckPointOperation)operation).getCheckPointHash());
				preparedStatement.addBatch();
			}
		}
		else if(transaction.getTransactionType() == TransactionType.COINBASE) {
			for(TxOut txOut:transaction.getTxOutList()) {
				preparedStatement.setLong(1, sn.longValue());
				preparedStatement.setByte(2, (byte) TRANSACTION_OP.TXOUT.ordinal());
				preparedStatement.setLong(3, txOut.getKey().getId().longValue());
				preparedStatement.setLong(4,  txOut.getValue() );
				preparedStatement.setNull(5, Types.BINARY);
				preparedStatement.addBatch();
				if(txOut.isNew()) {
					preparedStatement.setLong(1, sn.longValue());
					preparedStatement.setByte(2, (byte) TRANSACTION_OP.PASSPORT.ordinal());
					preparedStatement.setLong(3, txOut.getKey().getId().longValue());
					preparedStatement.setNull(4,  Types.BIGINT);
					preparedStatement.setBytes(5, txOut.getKey().getAddressAI());
					preparedStatement.addBatch();
				}
			}
		}
		
		preparedStatement.executeBatch();
		
		// Check if the saved Transaction equal to original Transaction
		// Check if current transaction index have been saved
		preparedStatement = connection
				.prepareStatement("SELECT * FROM " + transactionIndexTable + " WHERE sn=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		preparedStatement.setLong(1, sn.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		resultSet.last();
		if(resultSet.getRow() > 1) {
			throw new IllegalStateException("Only can exists one result set for " + transaction);
		}
		if (!resultSet.first()) {
			throw new IllegalStateException("After saveTransaction with SN:" + sn + " transaction index should exists");
		}
		else {
			if(sn.longValue() != resultSet.getLong("sn") || transaction.getTransactionType().ordinal() != resultSet.getByte("type") || index.longValue() != resultSet.getInt("index") ||  transaction.getAssetID().longValue() != resultSet.getLong("asset_id") || transaction.getNonce().longValue() != resultSet.getLong("nonce")) {
				throw new IllegalStateException("After saveTransaction with SN:" + sn + " transaction index should equal");
			}
		}
		
		ID nonce = ID.valueOf(resultSet.getLong("nonce"));
		// Check if current transaction have been saved
		preparedStatement = connection
				.prepareStatement("SELECT * FROM " + transactionTable + " WHERE sn=?");
		preparedStatement.setLong(1, sn.longValue());
		resultSet = preparedStatement.executeQuery();
		Transaction transaction2 = Transaction.parseTransaction(resultSet, transaction.getTransactionType());
		transaction2.setNonce(nonce);
		if(!transaction2.compare(transaction)) {
			throw new IllegalStateException("After saveTransaction with SN:" + sn + " transaction should exists");
		}
		preparedStatement.close();
		
		return true;
	}

	@Override
	public boolean deleteTransaction(Transaction transaction, Mode mode) throws Exception {
		ID sn = isTransactionExists(transaction, mode);
		Statement statement = null;
		ResultSet resultSet = null;
		String transactionIndexTable = getTransactionIndexTableName(mode);
		String transactionTable = getTransactionTableName(mode);
		
		if(sn != null) {
			statement = connection.createStatement();
			statement.addBatch("DELETE FROM TRANSACTION_INDEX WHERE sn=" + sn.longValue());
			statement.addBatch("DELETE FROM TRANSACTION WHERE sn=" + sn.longValue());
			statement.executeBatch();
			statement.close();
			// Check if all transaction have been deleted
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_INDEX WHERE sn=?");
			preparedStatement.setLong(1, sn.longValue());
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				throw new IllegalStateException("After deleteTransaction with SN:" + sn + " transaction shouldn't exists");
			}
			// Check if all transaction index have been deleted
			preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION WHERE sn==?");
			preparedStatement.setLong(1, sn.longValue());
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				throw new IllegalStateException("After deleteTransactionFrom:" + sn + " transaction index shouldn't exists");
			}
			preparedStatement.close();
		}
		return true;
	}

	@Override
	public boolean deleteTransactionFrom(ID height, Mode mode) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String transactionIndexTable = getTransactionIndexTableName(mode);
		String transactionTable = getTransactionTableName(mode);
		Vector<ID> transactionList = new Vector<>();
		
		preparedStatement = connection.prepareStatement("SELECT * FROM " + transactionIndexTable + " WHERE height >= ?");
		preparedStatement.setLong(1, height.longValue());
		resultSet = preparedStatement.executeQuery();
		
		while(resultSet.next()) {
			preparedStatement = connection.prepareStatement("DELETE FROM " + transactionTable + " WHERE sn=?");
			Log.info("Sn:" + resultSet.getLong("sn"));
			transactionList.add(ID.valueOf(resultSet.getLong("sn")));
			preparedStatement.setLong(1, resultSet.getLong("sn"));
			int nResult = preparedStatement.executeUpdate();
			Log.info("Result: " + nResult);
		}
		
		for(ID id:transactionList) {
			// Check if all transaction have been deleted
			preparedStatement = connection.prepareStatement("SELECT * FROM " + transactionTable + " WHERE sn=?");
			preparedStatement.setLong(1, id.longValue());
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				throw new IllegalStateException("After deleteTransactionFrom:" + height + " at " + transactionTable + " shouldn't exists");
			}
		}
		
		preparedStatement = connection.prepareStatement("DELETE FROM " + transactionIndexTable + "  WHERE height >= ?");
		preparedStatement.setLong(1, height.longValue());
		preparedStatement.executeUpdate();
		// Check if all transaction index have been deleted
		preparedStatement = connection.prepareStatement("SELECT * FROM " + transactionIndexTable + " WHERE height >= ?");
		preparedStatement.setLong(1, height.longValue());
		resultSet = preparedStatement.executeQuery();
		if(resultSet.next()) {
			throw new IllegalStateException("After deleteTransactionIndexFrom:" + height + " transaction index shouldn't exists");
		}
		preparedStatement.close();
		return true;
	}

	@Override
	public boolean saveTransactions(EQCHive eqcHive, Mode mode) throws Exception {
		 getPassport(Asset.EQCOIN, eqcHive.getHeight());
		
		for(EQCSubchain eqcSubchain:eqcHive.getSubchainList()) {
//			saveTransaction(eqcSubchain, eqcHive.getHeight(), index, sn, mode)
		}
		return true;
	}

	@Override
	public boolean saveTransactions(EQCSubchain eqcSubchain, ID height, Mode mode) throws Exception {
		EQcoinSubchainPassport eQcoinSubchainPassport = (EQcoinSubchainPassport) getPassport(Asset.EQCOIN, height.getPreviousID());
		ID sn = eQcoinSubchainPassport.getAssetSubchainHeader().getTotalTransactionNumbers().getNextID();
		if(eqcSubchain instanceof EQcoinSubchain) {
			saveTransaction(((EQcoinSubchain) eqcSubchain).getEQcoinSubchainHeader().getCoinbaseTransaction(), height, null, sn, mode);
		}
		
		for(int i=0; i<eqcSubchain.getNewTransactionList().size(); ++i) {
			saveTransaction(eqcSubchain.getNewTransactionList().get(i), height, ID.valueOf(i), sn, mode);
			sn = sn.getNextID();
		}
		
		return false;
	}

	@Override
	public ID getTotalTransactionNumbers(ID height, ID assetID, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public  TransactionList<byte[]> getTransactionListInPool(TransactionIndexList<byte[]> transactionIndexList)
			throws SQLException, Exception {
		TransactionList<byte[]> transactionList = new TransactionList();
		for (TransactionIndex<byte[]> transactionIndex : transactionIndexList.getTransactionIndexList()) {
			transactionList.addTransaction(getTransactionInPool(transactionIndex));
		}
		return transactionList;
	}
	
	public synchronized boolean test(ID id) throws Exception {
		int rowCounter = 0;
		connection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
			preparedStatement = connection
					.prepareStatement("INSERT INTO TEST (height) VALUES (?)");
			preparedStatement.setBytes(1, id.getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.setBytes(1, id.getNextID().getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.executeBatch();
			
//			int i = 1/0;
			
			preparedStatement = connection
					.prepareStatement("INSERT INTO TEST1 (height) VALUES (?)");
			preparedStatement.setBytes(1, id.getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.setBytes(1, id.getNextID().getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.executeBatch();
			
			connection.commit();
			
//			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		return true;
	}
	
	public synchronized void test1(ID height) throws Exception {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TEST WHERE height=?");
		preparedStatement.setBytes(1, height.getEQCBits());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			Log.info(new ID(resultSet.getBytes(1)).toString());
		}
	}

	@Override
	public boolean saveLock(Lock lock, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getLock(lock.getId(), mode) != null) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getLockTableName(mode)
					+ " SET id = ?, passport_id = ?,  readable_lock = ?, publickey = ? WHERE id = ?");
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getPassportTableName(mode)
							+ " (id, passport_id, readable_lock, publickey) VALUES (?, ?, ?, ?)");
		}
		
		preparedStatement.setLong(1, lock.getId().longValue());
		preparedStatement.setLong(2, lock.getPassportID().longValue());
		preparedStatement.setString(3, lock.getReadableLock());
		if(lock.getPublickey() == null) {
			preparedStatement.setNull(4, Types.BINARY);
		}
		else {
			preparedStatement.setBytes(4, lock.getPublickey());
		}
		rowCounter = preparedStatement.executeUpdate();
//				Log.info("UPDATE: " + rowCounter);
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
//		Passport savedPassport = getPassport(account.getId(), mode);
//		EQCType.assertEqual(account.getBytes(), savedPassport.getBytes());
		return true;
	}

	@Override
	public Lock getLock(ID id, Mode mode) throws Exception {
		Lock lock = null;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + getLockTableName(mode) + " WHERE id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lock = Passport.parseAccount(resultSet.getBytes("bytes"));
//		}
		return lock;
	}

	@Override
	public Lock getLock(ID id, ID height) throws Exception {
		Lock lock = null;
//		if (height.equals(getEQCBlockTailHeight())) {
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM " + LOCK_GLOBAL + " WHERE id=?");
//			preparedStatement.setLong(1, id.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			if (resultSet.next()) {
//				lock = Passport.parseAccount(resultSet.getBytes("bytes"));
//			}
//		} else {
//			account = getAccountSnapshot(id, height);
//		}
		return lock;
	}

	@Override
	public Lock getLock(String readableLock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteLock(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean clearLock(Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Passport getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Passport getLockSnapshot(byte[] addressAI, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLockSnapshot(Lock lock, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteLockSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mergeLock(Mode mode) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean takeLockSnapshot(Mode mode, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
}
