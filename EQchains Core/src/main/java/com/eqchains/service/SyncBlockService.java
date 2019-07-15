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
    	mode = MODE.FULL;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		switch (state.getState()) {
		case FIND:
			onFind(state);
			break;
		case SYNC:
			onSync(state);
			break;
		case SLEEP:
			onSleep(state);
			break;
		case MINER:
			onMiner(state);
			break;
		default:
			break;
		}
	}
	
	private void onFind(EQCServiceState state) {
		boolean isSingularityNode = false;
		IPList minerList = null;
		String tail = null;
		TailInfo tailInfo = null;
		TailInfo currentTail = null;

		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList();
			if (minerList.isEmpty()) {
				if (Util.IP.equals(Util.SINGULARITY_IP)) {
					// This is Singularity node and miner list is empty just start Minering
					offerState(new EQCServiceState(State.MINER));
					return;
				} else {
					tail = Util.SINGULARITY_IP;
				}
			} else {
				Vector<TailInfo> tailInfos = new Vector<>();
				IPList ipList = new IPList();
				for (String ip : minerList.getIpList()) {
					try {
						tailInfo = SyncblockNetworkClient.getBlockTail(ip);
					} catch (Exception e) {
						tailInfo = null;
						Log.Error(name + e.getMessage());
					}
					if (tailInfo != null) {
						tailInfo.setIp(ip);
						tailInfos.add(tailInfo);
					}
				}
				Comparator<TailInfo> reverseComparator = Collections.reverseOrder();
				Collections.sort(tailInfos, reverseComparator);
				currentTail = tailInfos.get(0);
				for (TailInfo tailInfo2 : tailInfos) {
					if (tailInfo2.equals(tailInfos.get(0))) {
						ipList.addIP(tailInfo2.getIp());
					}
				}
				tail = MinerNetworkClient.getFastestServer(ipList);
				if (tail == null) {
					tail = Util.SINGULARITY_IP;
				}
			}
			if (Util.DB().getEQCBlockTailHeight().compareTo(currentTail.getHeight()) < 0) {
				// Begin sync to tail
				SyncBlockState syncBlockState = new SyncBlockState();
				syncBlockState.setIp(tail);
				offerState(state);
			} else {
				if (mode == MODE.MINER) {
					offerState(new EQCServiceState(State.MINER));
				} else {
					// Sleep then find again
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(name + e.getMessage());
		}
	}
	
	private void onSync(EQCServiceState state) {
		SyncBlockState syncBlockState = (SyncBlockState) state;
		AccountsMerkleTree accountsMerkleTree = null;
		
		try {
			EQCHive eqcHive = Util.DB().getEQCBlock(Util.DB().getEQCBlockTailHeight(), true);
			if(syncBlockState.getEqcHive() == null) {
				if(syncBlockState.getEqcHive().getHeight().isNextID(eqcHive.getHeight())) {
					if(Arrays.equals(syncBlockState.getEqcHive().getEqcHeader().getPreHash(), eqcHive.getHash())) {
						 accountsMerkleTree = new AccountsMerkleTree(eqcHive.getHeight(), new Filter(Mode.VALID));
						if(syncBlockState.getEqcHive().isValid(accountsMerkleTree)) {
							accountsMerkleTree.takeSnapshot();
							accountsMerkleTree.merge();
							accountsMerkleTree.clear();
						}
						Util.DB().saveEQCBlock(syncBlockState.getEqcHive());
						Util.DB().saveEQCBlockTailHeight(syncBlockState.getEqcHive().getHeight());
						return;
					}
				}
			}
			TailInfo tailInfo = SyncblockNetworkClient.getBlockTail(syncBlockState.getIp());
			ID tail = Util.DB().getEQCBlockTailHeight();
			long i=tail.longValue();
			// Here need change 0 to checkpoint height
			for(; i>0; --i) {
				if(Arrays.equals(Util.DB().getEQCHeaderHash(ID.valueOf(i)), SyncblockNetworkClient.getBlock(ID.valueOf(i+1), syncBlockState.getIp()).getEqcHeader().getPreHash())) {
					break;
				}
			}
			// Remove old block
			for(long j=i+1; j<tail.longValue(); ++j) {
				Util.DB().deleteEQCBlock(ID.valueOf(j));
			}
			// Remove unused Account 
			if(i>tail.subtract(Util.EUROPA).longValue()) {
				// Here need check if overhead the last checkpoint if true this Subchain is invalid
				// Here need remove accounts after i
				ID total = Util.DB().getTotalAccountNumbers(Util.DB().getEQCBlockTailHeight());
				for(long l=i+1; l<=total.longValue(); ++l) {
					Util.DB().deleteAccount(ID.valueOf(l));
				}
				Util.DB().saveEQCBlockTailHeight(ID.valueOf(i));
			}
			else {
				
			}
			// Sync block
			for(long k=i; k<=tailInfo.getHeight().longValue(); ++k) {
				eqcHive = SyncblockNetworkClient.getBlock(ID.valueOf(k), syncBlockState.getIp());
				if(eqcHive == null) {
					Log.Error("During sync block error occur just  goto find again");
					break;
				}
				accountsMerkleTree = new AccountsMerkleTree(ID.valueOf(k), new Filter(Mode.VALID));
				if(eqcHive.isValid(accountsMerkleTree)) {
					Util.DB().saveEQCBlock(eqcHive);
					accountsMerkleTree.takeSnapshot();
					accountsMerkleTree.merge();
					accountsMerkleTree.clear();
					Util.saveEQCBlockTailHeight(ID.valueOf(k));
				}
				else {
					Log.Error("Valid blockchain failed just goto find");
					accountsMerkleTree.clear();
					return;
				}
			}
			// Successful sync to current tail just goto find to check if reach the tail
			offerState(new EQCServiceState(State.FIND));
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(name + e.getMessage());
		}
	}
	
	private void onMiner(EQCServiceState state) {
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
		if(!MinerService.getInstance().isRunning()) {
			MinerService.getInstance().start();
		}
		else if(MinerService.getInstance().getState() == State.PAUSE) {
				MinerService.getInstance().resumePause();
		}
		// If is Miner or Full node Ping the others to register in miner list
	}
	
}
