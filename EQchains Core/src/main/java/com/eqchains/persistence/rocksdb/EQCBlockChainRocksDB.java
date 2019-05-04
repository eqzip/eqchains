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
package com.eqchains.persistence.rocksdb;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.h2.command.ddl.CreateTable;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.MutableColumnFamilyOptions;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.TransactionDB;
import org.rocksdb.TransactionDBOptions;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import com.eqchains.blockchain.Account;
import com.eqchains.blockchain.EQCBlock;
import com.eqchains.blockchain.EQCBlockChain;
import com.eqchains.blockchain.EQCHeader;
import com.eqchains.blockchain.PublicKey;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;


/**
 * @author Xun Wang
 * @date Mar 11, 2019
 * @email 10509759@qq.com
 */
public class EQCBlockChainRocksDB implements EQCBlockChain {
	private static EQCBlockChainRocksDB instance;
	private static RocksDB rocksDB;
	private static List<ColumnFamilyHandle> columnFamilyHandles;
	private static List<byte[]> defaultColumnFamilyNames;
	private static List<ColumnFamilyDescriptor> columnFamilyDescriptors;
	private static WriteOptions writeOptions;
	public static final byte[] SUFFIX_AI = "AI".getBytes();
	public static final byte[] SUFFIX_HASH = "Hash".getBytes();
	public static final byte[] EQCBLOCK_TABLE = "EQCBlock".getBytes();
	public static final byte[] EQCBLOCK_HASH_TABLE = addSuffix(EQCBLOCK_TABLE, SUFFIX_HASH);
	public static final byte[] ACCOUNT_TABLE = "Account".getBytes();
	public static final byte[] ACCOUNT_AI_TABLE = addSuffix(ACCOUNT_TABLE, SUFFIX_AI);
	public static final byte[] ACCOUNT_HASH_TABLE = addSuffix(ACCOUNT_TABLE, SUFFIX_HASH);
	public static final byte[] ACCOUNT_MINERING_TABLE = "AccountMinering".getBytes();
	public static final byte[] ACCOUNT_MINERING_AI_TABLE = addSuffix(ACCOUNT_MINERING_TABLE, SUFFIX_AI);
	public static final byte[] ACCOUNT_MINERING_HASH_TABLE = addSuffix(ACCOUNT_MINERING_TABLE, SUFFIX_HASH);
	public static final byte[] MISC_TABLE = "Misc".getBytes();
	public static final byte[] PREFIX_H = "H".getBytes();
	public static final byte[] EQCBLOCK_TAIL_HEIGHT = "EQCBlock_Tail_Height".getBytes();
	
	public enum TABLE {
		DEFAULT, EQCBLOCK, EQCBLOCK_HASH, ACCOUNT, ACCOUNT_AI, ACCOUNT_HASH, MISC
	}

	static {
		RocksDB.loadLibrary();
		defaultColumnFamilyNames = Arrays.asList(RocksDB.DEFAULT_COLUMN_FAMILY, EQCBLOCK_TABLE, EQCBLOCK_HASH_TABLE,
				ACCOUNT_TABLE, ACCOUNT_AI_TABLE, ACCOUNT_HASH_TABLE, MISC_TABLE);
		getInstance();
	}

	private EQCBlockChainRocksDB() {
		List<byte[]> columnFamilyNames = new ArrayList<>();
		columnFamilyNames.addAll(defaultColumnFamilyNames);
		columnFamilyHandles = new ArrayList<>();
		writeOptions = new WriteOptions();
		final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
		try {
			for(byte[] bytes: RocksDB.listColumnFamilies(new Options(), Util.ROCKSDB_PATH)) {
				if(!isDefaultColumnFamily(bytes)) {
					columnFamilyNames.add(bytes);
				}
			}
			final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new Vector();
			for(byte[] bytes : columnFamilyNames) {
				columnFamilyDescriptors.add(new ColumnFamilyDescriptor(bytes));
			}
			rocksDB = RocksDB.open(dbOptions, Util.ROCKSDB_PATH, columnFamilyDescriptors,
					columnFamilyHandles);
			List<ColumnFamilyHandle> tempColumnFamilyHandles = new ArrayList<>();
			for(int i=0; i<columnFamilyNames.size(); ++i) {
				if(!isDefaultColumnFamily(columnFamilyNames.get(i))) {
					clearTable(columnFamilyHandles.get(i));
					dropTable(columnFamilyHandles.get(i));
					columnFamilyHandles.get(i).close();
					tempColumnFamilyHandles.add(columnFamilyHandles.get(i));
				}
			}
			columnFamilyHandles.removeAll(tempColumnFamilyHandles);
			MutableColumnFamilyOptions mutableColumnFamilyOptions = MutableColumnFamilyOptions.builder().setCompressionType(CompressionType.NO_COMPRESSION).build();
			rocksDB.setOptions(getTableHandle(TABLE.EQCBLOCK), mutableColumnFamilyOptions);
			rocksDB.setOptions(getTableHandle(TABLE.EQCBLOCK_HASH), mutableColumnFamilyOptions);
			rocksDB.setOptions(getTableHandle(TABLE.ACCOUNT), mutableColumnFamilyOptions);
			rocksDB.setOptions(getTableHandle(TABLE.ACCOUNT_AI), mutableColumnFamilyOptions);
			rocksDB.setOptions(getTableHandle(TABLE.ACCOUNT_HASH), mutableColumnFamilyOptions);
			rocksDB.setOptions(getTableHandle(TABLE.MISC), mutableColumnFamilyOptions);
			Thread.sleep(10000);
		} catch (RocksDBException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	private boolean isContains(List<byte[]> columnFamilyNames, byte[] columnFamilyName) {
		boolean isSucc = false;
		for(byte[] bytes : columnFamilyNames) {
			if(Arrays.equals(bytes, columnFamilyName)) {
				isSucc = true;
				break;
			}
		}
		return isSucc;
	}

	public synchronized static EQCBlockChainRocksDB getInstance() {
		if (instance == null) {
			synchronized (EQCBlockChainH2.class) {
				if (instance == null) {
					instance = new EQCBlockChainRocksDB();
				}
			}
		}
		return instance;
	}
	
	/**
	 * @return the rocksDB
	 */
	public static RocksDB getRocksDB() {
		return rocksDB;
	}

	public boolean isDefaultColumnFamily(byte[] columnFamilyName) {
		boolean isExists = false;
		for(byte[] bytes : defaultColumnFamilyNames) {
			if(Arrays.equals(bytes, columnFamilyName)) {
				return true;
			}
		}
		return isExists;
	}
	
	public static ColumnFamilyHandle getTableHandle(TABLE table) {
		return columnFamilyHandles.get(table.ordinal());
	}
	
	public static void put(TABLE table, byte[] key, byte[] value) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.put(getTableHandle(table), key, value);
		rocksDB.write(writeOptions, writeBatch);
	}

	public static byte[] get(TABLE table, byte[] key) throws RocksDBException {
		return rocksDB.get(getTableHandle(table), key);
	}
	
	public static void delete(TABLE table, byte[] key) throws RocksDBException {
		WriteBatch writeBatch = new WriteBatch();
		writeBatch.delete(getTableHandle(table), key);
		rocksDB.write(writeOptions, writeBatch);
	}
	
	public static ColumnFamilyHandle  createTable(byte[] columnFamilyName) {
		ColumnFamilyHandle columnFamilyHandle = null;
		try {
			columnFamilyHandle = rocksDB.createColumnFamily(new ColumnFamilyDescriptor(columnFamilyName));
		} catch (RocksDBException e) {
			e.printStackTrace();
			Log.Error("During CreateTable " + columnFamilyName + " error occur: " + e.getMessage());
		}
		return columnFamilyHandle;
	}
	
	public static boolean dropTable(ColumnFamilyHandle columnFamilyHandle) {
		boolean isSucc = true;
		try {
			rocksDB.dropColumnFamily(columnFamilyHandle);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During deleteTable " + columnFamilyHandle + " error occur: " + e.getMessage());
		}
		return isSucc;
	}
	
	public static boolean clearTable(ColumnFamilyHandle columnFamilyHandle) {
		boolean isSucc = true;
		WriteBatch writeBatch = null;
		RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle);
		rocksIterator.seekToFirst();
		while(rocksIterator.isValid()) {
			try {
				writeBatch = new WriteBatch();
				writeBatch.delete(columnFamilyHandle, rocksIterator.key());
				rocksDB.write(writeOptions, writeBatch);
				rocksIterator.next();
			} catch (RocksDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During clearTable " + columnFamilyHandle + " error occur: " + e.getMessage());
			}
		}
		return isSucc;
	}
	
	public static ID getTableItemNumbers(ColumnFamilyHandle columnFamilyHandle) {
		ID id = ID.ZERO;
		RocksIterator rocksIterator = rocksDB.newIterator(columnFamilyHandle);
		rocksIterator.seekToFirst();
		while(rocksIterator.isValid()) {
			id = id.getNextID();
			rocksIterator.next();
		}
		return id;
	}
	
	public static ID getAccountNumbers() {
		return getTableItemNumbers(getTableHandle(TABLE.ACCOUNT));
//		if(numbers.mod(BigInteger.valueOf(3)).compareTo(BigInteger.valueOf(3)) != 0) {
//			throw new IllegalStateException("Account table is not synchronized.");
//		}
//		return new ID(numbers.divide(BigInteger.valueOf(3)));
	}
	
	public static ID getEQCBlockNumbers() {
		return getTableItemNumbers(getTableHandle(TABLE.EQCBLOCK));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressSerialNumber(com.EQCOIN Foundation.
	 * eqcoin.blockchain.Address)
	 */
	@Override
	public ID getAddressID(Address address) {
		ID serialNumber = null;
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT, address.getAddressAI());
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAddressSerialNumber " + address.toString() + " error occur: " + e.getMessage());
		}
		if(bytes != null) {
			serialNumber = new ID(bytes);
		}
		return serialNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddress(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	@Override
	public Address getAddress(ID serialNumber) {
		Address address = null;
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT, serialNumber.getEQCBits());
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAddress " + address.toString() + " error occur: " + e.getMessage());
		}
		if(bytes != null) {
			Account account;
			try {
				account = new Account(bytes);
				address = account.getAddress();
			} catch (NoSuchFieldException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("During getAddress " + address.toString() + " error occur: " + e.getMessage());
			}
		}
		return address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#appendAddress(com.eqzip.eqcoin.
	 * blockchain.Address, com.eqzip.eqcoin.util.SerialNumber)
	 */
	public boolean appendAddress(Address address, ID address_create_height) {
		boolean isSucc = true;
		Account account = new Account();
		account.setAddress(address);
		account.setAddressCreateHeight(address_create_height);
		WriteBatch writeBatch = new WriteBatch();
		try {
			writeBatch.put(getTableHandle(TABLE.ACCOUNT), account.getIDEQCBits(), account.getBytes());
			writeBatch.put(getTableHandle(TABLE.ACCOUNT), account.getAddress().getAddressAI(), account.getIDEQCBits());
			writeBatch.put(getTableHandle(TABLE.ACCOUNT), addPrefixH(account.getIDEQCBits()), account.getHash());
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During appendAddress " + address.toString() + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isAddressExists(com.eqzip.eqcoin.
	 * blockchain.Address)
	 */
	@Override
	public boolean isAddressExists(Address address) {
		boolean isSucc = false;
		try {
			// For security issue only support search address via AddressAI
			if(rocksDB.get(getTableHandle(TABLE.ACCOUNT_AI), address.getAddressAI()) != null) {
				isSucc = true;
			}
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During isAddressExists " + address.toString() + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#deleteAddress(com.eqzip.eqcoin.
	 * blockchain.Address)
	 */
	public boolean deleteAddress(Address address) {
		boolean isSucc = true;
		WriteBatch writeBatch = new WriteBatch();
		try {
			writeBatch.delete(getTableHandle(TABLE.ACCOUNT), address.getID().getEQCBits());
			writeBatch.delete(getTableHandle(TABLE.ACCOUNT), address.getAddressAI());
			writeBatch.delete(getTableHandle(TABLE.ACCOUNT), addPrefixH(address.getID().getEQCBits()));
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During deleteAddress " + address.toString() + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#deleteAddressFromHeight(com.EQCOIN Foundation.
	 * eqcoin.util.SerialNumber)
	 */
	public boolean deleteAddressFromHeight(ID height) {
		boolean isSucc = true;
		WriteBatch writeBatch = new WriteBatch();
		try {
			ID serialNumber = ID.ZERO;
			byte[] bytes = null;
			for(int i=0; i<height.longValue(); ++i) {
				bytes = rocksDB.get(getTableHandle(TABLE.ACCOUNT), new ReadOptions(), serialNumber.getEQCBits());
				Account account = new Account(bytes);
				writeBatch.delete(account.getAddress().getID().getEQCBits());
				writeBatch.delete(account.getAddress().getAddressAI());
				writeBatch.delete(addPrefixH(account.getAddress().getID().getEQCBits()));
			}
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException | NoSuchFieldException | IOException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During deleteAddressFromHeight " + height.toString() + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	public static byte[] addPrefixH(byte[] bytes) {
		return addPrefix(PREFIX_H, bytes);
	}
	
	public static byte[] addPrefix(byte[] prefix, byte[] bytes) {
		return ByteBuffer.allocate(prefix.length + bytes.length).put(prefix).put(bytes).array();
	}
	
	public static byte[] addSuffix(byte[] bytes, byte[] suffix) {
		return ByteBuffer.allocate(bytes.length + suffix.length).put(bytes).put(suffix).array();
	}

//	@Override
//	public ID getTotalAccountNumbers(ID height) {
//		return getEQCBlock(height, true).getRoot().getTotalAccountNumber();
//	}

	@Override
	public boolean saveAccount(Account account) {
		boolean isSucc = true;
		WriteBatch writeBatch = new WriteBatch();
		try {
			writeBatch.put(getTableHandle(TABLE.ACCOUNT), account.getIDEQCBits(), account.getBytes());
			Log.info(account.toString());
			Log.info(Util.dumpBytes(account.getAddress().getAddressAI(), 16));
			writeBatch.put(getTableHandle(TABLE.ACCOUNT_AI), account.getAddress().getAddressAI(), account.getIDEQCBits());
			writeBatch.put(getTableHandle(TABLE.ACCOUNT_HASH), account.getIDEQCBits(), account.getHash());
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During appendAddress " + account + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	@Override
	public Account getAccount(ID id) {
		Account account = null;
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT, id.getEQCBits());
			if(bytes != null) {
					account = new Account(bytes);
			}
		} catch (RocksDBException | NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAccount " + id + " error occur: " + e.getMessage());
		}
		return account;
	}
	
	public ID getAccountID(byte[] addressAI) {
		ID id = null;
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT_AI, addressAI);
			if(bytes != null) {
					id = new ID(bytes);
			}
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAccount " + addressAI + " error occur: " + e.getMessage());
		}
		return id;
	}
	
	public byte[] getAccountHash(ID id) {
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT_HASH, id.getEQCBits());
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAccount " + id + " error occur: " + e.getMessage());
		}
		return bytes;
	}

	@Override
	public boolean deleteAccount(ID serialNumber) {
		boolean isSucc = true;
		WriteBatch writeBatch = new WriteBatch();
		try {
			byte[] bytes = get(TABLE.ACCOUNT, serialNumber.getEQCBits());
			if (null != bytes) {
				Account account = new Account(bytes);
				writeBatch.delete(getTableHandle(TABLE.ACCOUNT), serialNumber.getEQCBits());
				writeBatch.delete(getTableHandle(TABLE.ACCOUNT), account.getAddress().getAddressAI());
				writeBatch.delete(getTableHandle(TABLE.ACCOUNT), addPrefixH(serialNumber.getEQCBits()));
				rocksDB.write(writeOptions, writeBatch);
			}
		} catch (RocksDBException | NoSuchFieldException | IOException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During deleteAccount " + serialNumber + " error occur: " + e.getMessage());
		}
		return isSucc;
	}
	
	@Override
	public EQCBlock getEQCBlock(ID height, boolean isSegwit) {
		EQCBlock eqcBlock = null;
		byte[] bytes = null;
		
		try {
			if((bytes = get(TABLE.EQCBLOCK, height.getEQCBits())) != null) {
				eqcBlock = new EQCBlock(bytes, isSegwit);
			}
		} catch (RocksDBException | NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getEQCBlock " + height + " error occur: " + e.getMessage());
		}
		return eqcBlock;
	}
	
	@Override
	public boolean isEQCBlockExists(ID height) {
		boolean isSucc = false;
		byte[] bytes = null;
		try {
			if((bytes = get(TABLE.EQCBLOCK, height.getEQCBits())) != null) {
				isSucc = true;
			}
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During isEQCBlockExists " + height + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	@Override
	public boolean saveEQCBlock(EQCBlock eqcBlock) {
		boolean isSucc = true;
		try {
			WriteBatch writeBatch = new WriteBatch();
			writeBatch.put(getTableHandle(TABLE.EQCBLOCK), eqcBlock.getHeight().getEQCBits(), eqcBlock.getBytes());
			writeBatch.put(getTableHandle(TABLE.EQCBLOCK_HASH), eqcBlock.getHeight().getEQCBits(), eqcBlock.getEqcHeader().getHash());
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During saveEQCBlock " + eqcBlock + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	@Override
	public boolean deleteEQCBlock(ID height) {
		boolean isSucc = true;
		try {
			WriteBatch writeBatch = new WriteBatch();
			writeBatch.delete(getTableHandle(TABLE.EQCBLOCK), height.getEQCBits());
			writeBatch.delete(getTableHandle(TABLE.EQCBLOCK), addPrefixH(height.getEQCBits()));
			rocksDB.write(writeOptions, writeBatch);
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During saveEQCBlock " + height + " error occur: " + e.getMessage());
		}
		return isSucc;
	}

	@Override
	public byte[] getTransactionsHash(ID height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<Transaction> getTransactionListInPool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addTransactionInPool(Transaction transaction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteTransactionInPool(Transaction transaction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteTransactionsInPool(EQCBlock eqcBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTransactionExistsInPool(Transaction transaction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getEQCHeaderHash(ID height) {
		byte[] bytes = null;
		try {
			bytes = get(TABLE.EQCBLOCK_HASH, height.getEQCBits());
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return bytes;
	}

	@Override
	public ID getEQCBlockTailHeight() {
		ID serialNumber = null;
		try {
			serialNumber = new ID(get(TABLE.MISC, EQCBLOCK_TAIL_HEIGHT));
		} catch (RocksDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getEQCBlockTailHeight error occur: " + e.getMessage());
		}
		return serialNumber;
	}

	@Override
	public boolean saveEQCBlockTailHeight(ID height) {
		boolean isSucc = true;
		try {
			put(TABLE.MISC, EQCBLOCK_TAIL_HEIGHT, height.getEQCBits());
		} catch (RocksDBException e) {
			isSucc = false;
			e.printStackTrace();
			Log.Error("During saveEQCBlockTailHeight " + height + " error occur: " + e.getMessage());
		}
		return isSucc;
	}
	
	@Override
	public boolean close() {
		for(ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
			columnFamilyHandle.close();
		}
		rocksDB.close();
		return false;
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

	@Override
	public PublicKey getPublicKey(ID serialNumber) {
		PublicKey publicKey = null;
		Account account = null;
		if((account = getAccount(serialNumber)) != null) {
			publicKey = new PublicKey();
			publicKey.setPublicKey(account.getPublickey().getPublickey());
		}
		return publicKey;
	}

	@Override
	public boolean savePublicKey(PublicKey publicKey, ID height) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPublicKeyExists(PublicKey publicKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deletePublicKey(PublicKey publicKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Account getAccount(byte[] addressAI) {
		Account account = null;
		byte[] bytes = null;
		try {
			bytes = get(TABLE.ACCOUNT_AI, addressAI);
			if (bytes != null) {
				bytes = get(TABLE.ACCOUNT, bytes);
				if (bytes != null) {
					account = new Account(bytes);
				}
			}
		} catch (RocksDBException | NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During getAccount " + addressAI + " error occur: " + e.getMessage());
		}
		return account;
	}

	@Override
	public ID getTotalAccountNumbers(ID height) {
		return getEQCBlock(height, true).getRoot().getTotalAccountNumbers();
	}

	@Override
	public boolean saveTotalAccountNumbers(ID numbers) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Account getAccountSnapshot(ID accountID, ID height) {
		return null;
	}

	@Override
	public boolean saveAccountSnapshot(Account account, ID height) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteAccountSnapshot(ID height, boolean isForward) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dropTable() {
		for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
			try {
				clearTable(columnFamilyHandle);
				if(!Arrays.equals(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyHandle.getName())) {
					dropTable(columnFamilyHandle);
				}
			} catch (RocksDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
		}
	}
	
	public void dumpEQCBlock() {
		ID tail = getEQCBlockNumbers();
		Log.info("Current have " + tail + " blocks.");
		for(int i=0; i<tail.longValue(); ++i) {
			Log.info(getEQCBlock(new ID(i), false).toString());
			Log.info("EQCHeader's Hash: " + Util.dumpBytes(getEQCHeaderHash(new ID(i)), 16));
		}
	}
	
	public void dumpAccount() {
		ID tail = getTotalAccountNumbers(getEQCBlockTailHeight());
		Log.info("Current have " + tail + " Accounts.");
		Account account = null;
		for(int i=1; i<=tail.longValue(); ++i) {
			account = getAccount(new ID(i));
			Log.info(account.toString());
			Log.info(Util.dumpBytes(account.getAddress().getAddressAI(), 16));
			Log.info("ID: " + getAccountID(account.getAddress().getAddressAI()).toString());
			Log.info("Account's Hash: " + Util.dumpBytes(getAccountHash(account.getID()), 16));
		}
	}
	
	public void dumpAccountID() {}

	/**
	 * @return the writeOptions
	 */
	public static WriteOptions getWriteOptions() {
		return writeOptions;
	}
	
	public static ID dumpTable(TABLE table) {
		ID tail = getTableItemNumbers(getTableHandle(table));
		Log.info(table + " have " + tail + " elements.");
		
		ID id = ID.ZERO;
		RocksIterator rocksIterator = rocksDB.newIterator(getTableHandle(table));
		rocksIterator.seekToFirst();
		while (rocksIterator.isValid()) {
			if (table == TABLE.ACCOUNT) {
				Log.info("Key: " + new ID(rocksIterator.key()));
				try {
					Account account = new Account(rocksIterator.value());
					Log.info("Value: " + account.toString());
					Log.info("AddressAI: " + Util.dumpBytes(account.getAddress().getAddressAI(), 16));
				} catch (NoSuchFieldException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (table == TABLE.ACCOUNT_AI) {
				Log.info("Key: " + Util.dumpBytes(rocksIterator.key(), 16));
				Log.info("Value: " + new ID(rocksIterator.value()).toString());
			}
			rocksIterator.next();
		}
		return id;
	}
	
}
