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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.velocity.runtime.directive.Break;
import org.h2.util.IntIntHashMap;

import com.eqchains.blockchain.EQCHive;
import com.eqchains.blockchain.account.EQcoinSubchainAccount;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.accountsmerkletree.Filter;
import com.eqchains.blockchain.accountsmerkletree.Filter.Mode;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.TailInfo;
import com.eqchains.rpc.client.MinerNetworkClient;
import com.eqchains.rpc.client.SyncblockNetworkClient;
import com.eqchains.rpc.service.MinerNetworkService;
import com.eqchains.rpc.service.SyncblockNetworkService;
import com.eqchains.rpc.service.TransactionNetworkService;
import com.eqchains.service.state.EQCServiceState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.service.state.SleepState;
import com.eqchains.service.state.SyncBlockState;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.MODE;

/**
 * @author Xun Wang
 * @date Jul 6, 2019
 * @email 10509759@qq.com
 */
public class SyncBlockService extends EQCService {
	private static SyncBlockService instance;
	private MODE mode;
	
	public static SyncBlockService getInstance() {
		if (instance == null) {
			synchronized (SyncBlockService.class) {
				if (instance == null) {
					instance = new SyncBlockService();
				}
			}
		}
		return instance;
	}
	
    private SyncBlockService() {
    	super();
    	mode = MODE.MINER;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		PossibleNodeService.getInstance().stop();
		MinerNetworkService.getInstance().stop();
		TransactionNetworkService.getInstance().stop();
		SyncblockNetworkService.getInstance().stop();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		switch (state.getState()) {
		case SERVER:
			this.state.set(State.SERVER);
			onServer(state);
			break;
		case FIND:
			this.state.set(State.FIND);
			onFind(state);
			break;
		case SYNC:
			this.state.set(State.SYNC);
			onSync(state);
			break;
		case MINER:
			this.state.set(State.MINER);
			onMiner(state);
			break;
		default:
			break;
		}
	}
	
	private void onFind(EQCServiceState state) {
		IPList minerList = null;
		String maxTail = null;
		TailInfo minerTailInfo = null;
		TailInfo maxTailInfo = null;
		boolean isMaxTail = true;

		try {
			// Before already do syncMinerList in Util.init during every time EQchains core
			// startup
			minerList = EQCBlockChainH2.getInstance().getMinerList();
			Log.info("MinerList's size: " + minerList.getIpList().size());
			if (minerList.isEmpty()) {
				if (Util.IP.equals(Util.SINGULARITY_IP)) {
					// This is Singularity node and miner list is empty just start Minering
					offerState(new EQCServiceState(State.MINER));
					return;
				} 
			}
			minerList.addIP(Util.SINGULARITY_IP);
			Vector<TailInfo> minerTailList = new Vector<>();
			for (String ip : minerList.getIpList()) {
				try {
					if(!ip.equals(Util.IP)) {
						minerTailInfo = SyncblockNetworkClient.getBlockTail(ip);
					}
				} catch (Exception e) {
					minerTailInfo = null;
					Log.Error(name + e.getMessage());
				}
				if (minerTailInfo != null) {
					minerTailInfo.setIp(ip);
					minerTailList.add(minerTailInfo);
				}
			}
			if(minerTailList.isEmpty()) {
				if (Util.IP.equals(Util.SINGULARITY_IP)) {
					// This is Singularity node and miner list is empty just start Minering
					offerState(new EQCServiceState(State.MINER));
					return;
				} 
				else {
					// Network error all miner node and singularity node can't connect just sleep then try again 
					Log.Error("Network error all miner node and singularity node can't connect just sleep then try again");
					offerState(new SleepState(Util.BLOCK_INTERVAL/4));
					return;
				}
			}
			Comparator<TailInfo> reverseComparator = Collections.reverseOrder();
			Collections.sort(minerTailList, reverseComparator);
			// Retrieve the max Hive TailInfo
			Log.info("minerTailList: " + minerTailList.size());
			maxTailInfo = minerTailList.get(0);
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			Log.info("LocalTail: " + Util.DB().getEQCBlockTailHeight());
			EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE);
			if (maxTailInfo.getCheckPointHeight().compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& maxTailInfo.getHeight().compareTo(Util.DB().getEQCBlockTailHeight()) > 0) {
				isMaxTail = false;
				IPList minerIpList = new IPList();
				for (TailInfo tailInfo2 : minerTailList) {
					if (tailInfo2.equals(minerTailList.get(0))) {
						minerIpList.addIP(tailInfo2.getIp());
					}
				}
				maxTail = MinerNetworkClient.getFastestServer(minerIpList);
				if (maxTail == null) {
					offerState(new SleepState(Util.BLOCK_INTERVAL/4));
				}
			}
			if (!isMaxTail) {
				// Begin sync to MaxTail
				SyncBlockState syncBlockState = new SyncBlockState();
				syncBlockState.setIp(maxTail);
				offerState(syncBlockState);
			} else {
				if (mode == MODE.MINER) {
					offerState(new EQCServiceState(State.MINER));
				} else {
					offerState(new SleepState(Util.BLOCK_INTERVAL/4));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch bloc
			e.printStackTrace();
			Log.Error(name + e.getMessage());
			offerState(new SleepState(Util.BLOCK_INTERVAL/4));
		}
	}
	
	private void onSync(EQCServiceState state) {
		SyncBlockState syncBlockState = (SyncBlockState) state;
		AccountsMerkleTree accountsMerkleTree = null;
		boolean isValidChain = false;
		
		try {
			EQCHive localTailHive = Util.DB().getEQCBlock(Util.DB().getEQCBlockTailHeight(), true);
			if(syncBlockState.getEqcHive() != null) {
				// Received new block from the Miner network
				Log.info("Received new block from the Miner network");
				if(syncBlockState.getEqcHive().getHeight().isNextID(localTailHive.getHeight())) {
					if(Arrays.equals(syncBlockState.getEqcHive().getEqcHeader().getPreHash(), localTailHive.getHash())) {
						 accountsMerkleTree = new AccountsMerkleTree(localTailHive.getHeight(), new Filter(Mode.VALID));
						if(syncBlockState.getEqcHive().isValid(accountsMerkleTree)) {
							// Maybe here need do more job
							accountsMerkleTree.takeSnapshot();
							accountsMerkleTree.merge();
							accountsMerkleTree.clear();
							Util.DB().saveEQCBlock(syncBlockState.getEqcHive());
							Util.DB().saveEQCBlockTailHeight(syncBlockState.getEqcHive().getHeight());
							Log.info("New block valid passed");
						}
						offerState(new EQCServiceState(State.FIND));
						return;
					}
				}
			}
			// Need sync so just begin sync to tail
			TailInfo maxTailInfo = SyncblockNetworkClient.getBlockTail(syncBlockState.getIp());
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			ID localTail = Util.DB().getEQCBlockTailHeight();
			Log.info("LocalTail: " + localTail);
			EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE);
			long base = localTail.longValue();
			if(maxTailInfo.getHeight().compareTo(localTail) > 0 && maxTailInfo.getCheckPointHeight().compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0) {
				if(localTail.compareTo(ID.ZERO) > 0) {
					for(; base>=eQcoinSubchainAccount.getCheckPointHeight().longValue(); --base) {
						// Here need add getEQCHeader in SyncblockNetwork
						if(Arrays.equals(Util.DB().getEQCHeaderHash(ID.valueOf(base)), SyncblockNetworkClient.getBlock(ID.valueOf(base+1), syncBlockState.getIp()).getEqcHeader().getPreHash())) {
							isValidChain = true;
							break;
						}
					}
					if (isValidChain) {
						// Here need check if from i to the fork chain's tail's difficulty is valid.
						// Remove fork block
						for (long i = base + 1; i <= localTail.longValue(); ++i) {
							Util.DB().deleteEQCBlock(ID.valueOf(i));
						}
						// Remove Snapshot
						EQCBlockChainH2.getInstance().deleteAccountSnapshotFrom(ID.valueOf(base + 1), true);
						// Remove unused Account
						EQcoinSubchainAccount eQcoinSubchainAccount2 = (EQcoinSubchainAccount) EQCBlockChainH2
								.getInstance().getAccountSnapshot(ID.ONE, ID.valueOf(base));
						// Here need remove accounts after base
						ID originalAccountNumbers = eQcoinSubchainAccount.getAssetSubchainHeader().getTotalAccountNumbers();
						ID baseAccountNumbers = eQcoinSubchainAccount2.getAssetSubchainHeader().getTotalAccountNumbers();
						for (long i = baseAccountNumbers.longValue() + 1; i <= originalAccountNumbers.longValue(); ++i) {
							Util.DB().deleteAccount(ID.valueOf(i));
						}
						Util.DB().saveEQCBlockTailHeight(ID.valueOf(base));
					}
				}
				else {
					isValidChain = true;
				}

				// Sync block
				if (isValidChain) {
					for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
						localTailHive = SyncblockNetworkClient.getBlock(ID.valueOf(i), syncBlockState.getIp());
						if (localTailHive == null) {
							Log.Error("During sync block error occur just  goto find again");
							break;
						}
						accountsMerkleTree = new AccountsMerkleTree(ID.valueOf(i-1), new Filter(Mode.VALID));
 						if (localTailHive.isValid(accountsMerkleTree)) {
							Log.info("Verify No. " + i + " hive passed");
							accountsMerkleTree.takeSnapshot();
							accountsMerkleTree.merge();
							accountsMerkleTree.clear();
							Util.DB().saveEQCBlock(localTailHive);
							Util.saveEQCBlockTailHeight(ID.valueOf(i));
							Log.info("Current tail: " + Util.DB().getEQCBlockTailHeight());
						} else {
							Log.Error("Valid blockchain failed just goto find");
							accountsMerkleTree.clear();
							offerState(new SleepState(Util.BLOCK_INTERVAL/4));
							return;
						}
					}
				}
			}
			// Successful sync to current tail just goto find to check if reach the tail
			offerState(new EQCServiceState(State.FIND));
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(name + e.getMessage());
			offerState(new SleepState(Util.BLOCK_INTERVAL/4));
		}
	}
	
	private void onMiner(EQCServiceState state) {
		Log.info("onMiner");
		synchronized (EQCService.class) {
			if (!MinerService.getInstance().isRunning()) {
				MinerService.getInstance().start();
			} else if (MinerService.getInstance().isPausing.get()) {
				try {
					if(MinerService.getInstance().getNewBlockHeight().compareTo(Util.DB().getEQCBlockTailHeight()) <= 0) {
						Log.info("Changed to new minering base");
						MinerService.getInstance().stop();
						MinerService.getInstance().start();
					}
					else {
						Log.info("Still in the tail just resume minering");
						MinerService.getInstance().resumePause();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
		// If is Miner or Full node Ping the others to register in miner list
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onSleep(com.eqchains.service.state.SleepState)
	 */
	@Override
	protected void onSleep(SleepState state) {
		offerState(new EQCServiceState(State.FIND));
	}
	
	private void onServer(EQCServiceState state) {
		// During start service if any exception occur will interrupt the process then here will do nothing
		if(!PossibleNodeService.getInstance().isRunning()) {
			PossibleNodeService.getInstance().start();
		}
		if(!MinerNetworkService.getInstance().isRunning()) {
			MinerNetworkService.getInstance().start();
		}
 		if(!TransactionNetworkService.getInstance().isRunning()) {
			TransactionNetworkService.getInstance().start();
		}
		if(!SyncblockNetworkService.getInstance().isRunning()) {
			SyncblockNetworkService.getInstance().start();
		}
		if(!PendingNewBlockService.getInstance().isRunning()) {
			PendingNewBlockService.getInstance().start();
		}
		if(!BroadcastNewBlockService.getInstance().isRunning()) {
			BroadcastNewBlockService.getInstance().start();
		}
		try {
			if (!Util.IP.equals(Util.SINGULARITY_IP)) {
				long time = 0;
				for(int i=0; i<3; ++i) {
					time = MinerNetworkClient.ping(Util.SINGULARITY_IP);
					if(time != -1) {
						break;
					}
				}
				Util.syncMinerList();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		offerState(new EQCServiceState(State.FIND));
	}
	
}
