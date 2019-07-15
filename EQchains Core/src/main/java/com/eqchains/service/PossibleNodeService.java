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

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import com.eqchains.blockchain.transaction.operation.Operation.OP;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.client.MinerNetworkClient;
import com.eqchains.rpc.client.SyncblockNetworkClient;
import com.eqchains.service.state.EQCServiceState;
import com.eqchains.service.state.PossibleNodeState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public final class PossibleNodeService extends EQCService {
	private static PossibleNodeService instance;
	private IPList blackList;
	
	private PossibleNodeService() {
    	super();
    	blackList = new IPList();
	}
	
	public static PossibleNodeService getInstance() {
		if (instance == null) {
			synchronized (PossibleNodeService.class) {
				if (instance == null) {
					instance = new PossibleNodeService();
				}
			}
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		PossibleNodeState possibleNode = null;
		try {
			this.state.set(State.POSSIBLENODE);
			possibleNode = (PossibleNodeState) state;
			if(possibleNode.getNodeType() == NODETYPE.NONE) {
				return;
			}
			if(blackList.contains(possibleNode.getIp())) {
				return;
			}
			if(Util.SINGULARITY_IP.equals(possibleNode.getIp())) {
				return;
			}
			if(EQCBlockChainH2.getInstance().isIPExists(possibleNode.getIp(), possibleNode.getNodeType())) {
				return;
			}
			if(possibleNode.getNodeType() == NODETYPE.MINER) {
				if(MinerNetworkClient.ping(possibleNode.getIp()) > 0) {
					EQCBlockChainH2.getInstance().saveMiner(possibleNode.getIp());
				}
				else {
					if(!blackList.contains(possibleNode.getIp())) {
						blackList.addIP(possibleNode.getIp());
					}
				}
			}
			else {
				if(SyncblockNetworkClient.ping(possibleNode.getIp()) > 0) {
					EQCBlockChainH2.getInstance().saveFullNode(possibleNode.getIp());
				}
				else {
					if(!blackList.contains(possibleNode.getIp())) {
						blackList.addIP(possibleNode.getIp());
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public void offerNode(PossibleNodeState possibleNode) {
		offerState(possibleNode);
	}
	
}
