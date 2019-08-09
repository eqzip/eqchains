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
import org.rocksdb.RocksDBException;

import com.eqchains.blockchain.account.EQcoinSubchainAccount;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.accountsmerkletree.Filter;
import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainRocksDB;
import com.eqchains.rpc.NewBlock;
import com.eqchains.service.state.EQCServiceState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.service.state.NewBlockState;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Oct 12, 2018
 * @email 10509759@qq.com
 */
public final class MinerService extends EQCService {
	private static MinerService instance;
	private static AccountsMerkleTree accountsMerkleTree;
	private ID newBlockHeight;
	
	private MinerService() {
	}

	public static MinerService getInstance() {
		if (instance == null) {
			synchronized (MinerService.class) {
				if (instance == null) {
					instance = new MinerService();
				}
			}
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		super.start();
		worker.setPriority(Thread.MIN_PRIORITY);
		offerState(EQCServiceState.getDefaultState());
		Log.info(name + "started");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.
	 * EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		Log.info("Begin minering...");
		this.state.set(State.MINER);
		while (isRunning.get()) {
 			onPause("prepare minering");
			if(!isRunning.get()) {
				Log.info("Exit from prepare minering");
				break;
			}
			// Get current EQCBlock's tail
			ID blockTailHeight;
			try {
				blockTailHeight = EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight();
				/////////////////////////////////////////////////
//				if(blockTailHeight.compareTo(ID.valueOf(6)) == 0) {
//					break;
//				}
			} catch (RocksDBException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.Error(e1.getMessage());
				break;
			}
			EQCHive blockTail;
			try {
				Log.info("blockTailHeight: " + blockTailHeight);
				blockTail = EQCBlockChainRocksDB.getInstance().getEQCHive(blockTailHeight, false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.Error(e.getMessage());
				break;
			}

			// Begin making new EQCBlock
			newBlockHeight = blockTailHeight.getNextID();
			// If create AccountsMerkleTree just create it
			try {
				accountsMerkleTree = new AccountsMerkleTree(newBlockHeight, new Filter(Mode.MINERING));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.Error(e1.getMessage());
				break;
			}
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
				newEQCBlock.accountingEQCHive(accountsMerkleTree);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("Warning during Build Transactions error occur have to exit need check to find the reason: "
						+ e.getMessage());
				break;
			}
			
			Log.info("Begin mining new block height: " + newBlockHeight);
//			Log.info(newEQCBlock.toString());
			Log.info("size: " + newEQCBlock.getBytes().length);
			try {
				EQCHive eqcHive = new EQCHive(newEQCBlock.getBytes(), false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			
			// Send locked transactions to EQC network haven't implement, doesn't implement in MVP phase...
			
			// Beginning calculate new EQCBlock's hash
			BigInteger hash;
			ID nonce = ID.ZERO;
			BigInteger difficulty = Util.targetBytesToBigInteger(newEQCBlock.getEqcHeader().getTarget());
			while (true) {
				onPause("minering");
				if(!isRunning.get()) {
					Log.info("Exit from minering");
					break;
				}
				newEQCBlock.getEqcHeader().setNonce(nonce);
				hash = new BigInteger(1, newEQCBlock.getHash());
				if (hash.compareTo(difficulty) <= 0) { 
					try {
						synchronized (EQCService.class) {
							// Here add synchronized to avoid conflict with Sync block service handle new received block
							Log.info("Begin synchronized (EQCService.class)");
							onPause("verify new block");
							if(!isRunning.get()) {
								// Here need check if it has been stopped
								Log.info("Exit from verify new block");
								break;
							}
							
							Log.info(Util.getHexString(newEQCBlock.getHash()));
							Log.info("EQC Block No." + newEQCBlock.getHeight().longValue() + " Find use: "
									+ (System.currentTimeMillis() - newEQCBlock.getEqcHeader().getTimestamp().longValue())
									+ " ms, details:");

							Log.info(newEQCBlock.getEqcHeader().toString());
//							Log.info(newEQCBlock.getRoot().toString());
							
//							try {
//								PendingNewBlockService.getInstance().pause();
//							}
//							catch (Exception e) {
//								Log.Error(e.getMessage());
//							}
							// Check if current local tail is the mining base in case which has been changed by SyncBlockService
							if (newBlockHeight.isNextID(Util.DB().getEQCBlockTailHeight())) {
								Log.info("Still on the tail just save it");
//								EQCBlockChainRocksDB.getInstance().saveEQCBlock(newEQCBlock);
								// Save the snapshot of current tail height's changed Accounts from Account
								// Table to Snapshot Table
								Log.info("Begin take snapshot at height: " + accountsMerkleTree.getHeight());
								accountsMerkleTree.takeSnapshot();
								Log.info("End take snapshot at height: " + accountsMerkleTree.getHeight());
								// Merge the new block tail height's all modified Accounts to Account Table
								accountsMerkleTree.merge(newEQCBlock); // Here maybe throws Rocksdb Exception
								accountsMerkleTree.clear();
								EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(newEQCBlock.getHeight());
								try {
									// Send new block to EQC Miner network
									NewBlockState newBlockState = new NewBlockState(State.BROADCASTNEWBLOCK);
									EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE);
									NewBlock newBlock = new NewBlock();
									newBlock.setEqcHive(newEQCBlock);
									newBlock.setCheckPointHeight(eQcoinSubchainAccount.getCheckPointHeight());
									newBlockState.setNewBlock(newBlock);
									BroadcastNewBlockService.getInstance().offerNewBlockState(newBlockState);
								}
								catch (Exception e) {
									Log.Error(e.getMessage());
								}
								EQCBlockChainH2.getInstance().deleteTransactionsInPool(newEQCBlock);
//								// Here exists one bug before delete the old history snapshot need recovery the checkpoint's height's status first
//								EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB()
//										.getAccount(ID.ONE);
//								EQCBlockChainH2.getInstance()
//										.deleteAccountSnapshotFrom(eQcoinSubchainAccount.getCheckPointHeight(), false);
//								try {
//									PendingNewBlockService.getInstance().resumePause();
//								}
//								catch (Exception e) {
//									Log.Error(e.getMessage());
//								}
							}
							else {
								Log.Error("Current mining height is: " + newBlockHeight + " but local tail height changed to: " + Util.DB().getEQCBlockTailHeight() + 
										"so have to discard this block");
							}
							Log.info("End synchronized (EQCService.class)");
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
					break;
				}
				nonce = nonce.getNextID();
			}
		}
		Log.info("End of minering");
	}

	/**
	 * @return the newBlockHeight
	 */
	public ID getNewBlockHeight() {
		return newBlockHeight;
	}
	
}
