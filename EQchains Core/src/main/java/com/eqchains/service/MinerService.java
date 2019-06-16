/**
 * EQZIPWallet - EQchains Foundation's EQZIPWallet
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
package com.eqchains.service;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;

import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.EQCHive;
import com.eqchains.blockchain.EQCHeader;
import com.eqchains.blockchain.TransactionsHeader;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.accountsmerkletree.Filter;
import com.eqchains.blockchain.transaction.Address;
import com.eqchains.blockchain.transaction.TransferTransaction;
import com.eqchains.blockchain.transaction.TxOut;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.rocksdb.EQCBlockChainRocksDB;
import com.eqchains.rpc.avro.Height;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Oct 12, 2018
 * @email 10509759@qq.com
 */
public final class MinerService extends Thread {

	private static boolean isBehind;
	private static AccountsMerkleTree accountsMerkleTree;
	private static MinerService instance;

	public static MinerService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new MinerService();
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				}
			}
		}
		return instance;
	}

//	/**
//	 * @return the accountsMerkleTree
//	 */
//	public AccountsMerkleTree getAccountsMerkleTree() {
//		return accountsMerkleTree;
//	}

	public void setBehind() {
		isBehind = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Log.info("Begin minering...");
		while (true) {
//			try {
				isBehind = false;
				// Get current EQCBlock's tail
//			SerialNumber blockTailHeight = EQCBlockChainH2.getInstance().getEQCBlockTailHeight();
//			EQCBlock blockTail = EQCBlockChainH2.getInstance().getEQCBlock(blockTailHeight, false);
				ID blockTailHeight;
				try {
					blockTailHeight = EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight();
				} catch (RocksDBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.Error(e1.getMessage());
					break;
				}
				EQCHive blockTail;
				try {
					blockTail = EQCBlockChainRocksDB.getInstance().getEQCBlock(blockTailHeight, false);
				} catch (NoSuchFieldException | RocksDBException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.Error(e1.getMessage());
					break;
				}

				// If haven't create AccountsMerkleTree just create it
				try {
					accountsMerkleTree = new AccountsMerkleTree(blockTailHeight,
							new Filter(EQCBlockChainRocksDB.ACCOUNT_MINERING_TABLE));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.Error(e1.getMessage());
					break;
				}
//			if(blockTailHeight.getSerialNumber().compareTo(BigInteger.ZERO) == 0 || AccountsMerkleTreeService.getInstance().getAccountsMerkleTree().getHeight(ACCOUNT_STATUS.FIXED).getSerialNumber().compareTo(blockTailHeight.getSerialNumber()) != 0 ) {
//				AccountsMerkleTreeService.getInstance().getAccountsMerkleTree().createFromH2(blockTailHeight);
//			}

				// Create new EQCBlock
				ID newBlockHeight = new ID(blockTailHeight.add(BigInteger.ONE));
				EQCHive newEQCBlock;
				try {
					newEQCBlock = new EQCHive(newBlockHeight, blockTail.getEqcHeader().getHash());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.Error(e1.getMessage());
					break;
				}

				// Initial new EQCBlock
				try {
					// Build Transactions and initial Root
					newEQCBlock.accountingTransactions(newBlockHeight, accountsMerkleTree);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(
							"Warning during Build Transactions error occur have to exit need check to find the reason: "
									+ e.getMessage());
					break;
				}

				// Send locked transactions to EQC network haven't implement...

				// Beginning calculate new EQCBlock's hash
				byte[] bytes;
				BigInteger hash;
				ID nonce = ID.ZERO;
				while (true) {
//				Log.info("" + newEQCBlock.getEqcHeader().getNonce());
					newEQCBlock.getEqcHeader().setNonce(nonce);
					hash = new BigInteger(1, newEQCBlock.getHash());
//						Util.EQCCHA_MULTIPLE((bytes = Util.updateNonce(newEQCBlock.getEqcHeader().getBytes(), ++nonce)),
//						Util.HUNDRED_THOUSAND, true));
//	        	System.out.println("hash: " + Util.bigIntegerTo512String(hash));
//				byte[] abc = new byte[63];
//				for(int i=0; i<63; ++i) {
//					abc[i] = (byte) 0xff;
//				}
					if (hash.compareTo(Util.targetBytesToBigInteger(newEQCBlock.getEqcHeader().getTarget())) <= 0) { // new
																														// BigInteger(1,
																														// abc))
																														// <=
																														// 0){//
//	        		time1 = System.currentTimeMillis();
						Log.info(Util.getHexString(newEQCBlock.getHash()));
						Log.info("EQC Block No." + newEQCBlock.getHeight().longValue() + " Find use: "
								+ (System.currentTimeMillis() - newEQCBlock.getEqcHeader().getTimestamp().longValue())
								+ " ms, details:");

						Log.info(newEQCBlock.getEqcHeader().toString());
//					Log.info(newEQCBlock.getRoot().toString());

						// 暂时先放在这个位置将来要换到异步的Miner Network Service中。
						try {
//							EQCBlockChainH2.getInstance().saveEQCBlock(newEQCBlock);
							EQCBlockChainRocksDB.getInstance().saveEQCBlock(newEQCBlock);
							accountsMerkleTree.takeSnapshot();
							accountsMerkleTree.merge();
							EQCBlockChainH2.getInstance().deleteTransactionsInPool(newEQCBlock);
							EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(newEQCBlock.getHeight());
							if (blockTailHeight.compareTo(Util.EUROPA) > 0) {
								EQCBlockChainH2.getInstance().deleteAccountSnapshot(blockTailHeight.subtract(Util.EUROPA),
										false);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.Error(e.getMessage());
						}
						
						// Send new block to EQC Miner network

						break;
					}
					if (isBehind) {
						break;
					}
					nonce = nonce.getNextID();
				}
//			} catch (Exception e) {
//				Log.Error("During minering error occur: " + e.getMessage() + " have to terminate the minering process...");
//				break;
//			}
		}
	}
}
