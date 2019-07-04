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
import java.util.concurrent.PriorityBlockingQueue;

import com.eqchains.blockchain.transaction.operation.Operation.OP;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.MinerNetworkProxy;
import com.eqchains.rpc.SyncblockNetworkProxy;
import com.eqchains.util.Log;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class PossibleNodeService extends EQCService {
	private PriorityBlockingQueue<PossibleNode> pendingMessage;
	
	public static PossibleNodeService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new PossibleNodeService();
				}
			}
		}
		return (PossibleNodeService) instance;
	}
	
    public PossibleNodeService() {
    	pendingMessage = new PriorityBlockingQueue<>();
    	start();
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#runner()
	 */
	@Override
	protected void runner() {
		PossibleNode possibleNode = null;
		try {
			possibleNode = pendingMessage.take();
			if(possibleNode.getNodeType() == NODETYPE.NONE) {
				return;
			}
			if(EQCBlockChainH2.getInstance().isIPExists(possibleNode.ip, possibleNode.nodeType)) {
				return;
			}
			if(possibleNode.getNodeType() == NODETYPE.MINER) {
				if(MinerNetworkProxy.ping(possibleNode.getIp()) > 0) {
					EQCBlockChainH2.getInstance().saveMiner(possibleNode.getIp());
				}      
			}
			else {
				if(SyncblockNetworkProxy.ping(possibleNode.getIp()) > 0) {
					EQCBlockChainH2.getInstance().saveFullNode(possibleNode.getIp());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	public static class PossibleNode implements Comparable<PossibleNode> {
		private String ip;
		private NODETYPE nodeType;
		private long time;
		@Override
		public int compareTo(PossibleNode o) {
			if(nodeType == o.nodeType) {
				return (int) (o.time - time);
			}
			return nodeType.compareTo(o.nodeType);
		}
		/**
		 * @return the ip
		 */
		public String getIp() {
			return ip;
		}
		/**
		 * @param ip the ip to set
		 */
		public void setIp(String ip) {
			this.ip = ip;
		}
		/**
		 * @return the nodeType
		 */
		public NODETYPE getNodeType() {
			return nodeType;
		}
		/**
		 * @param nodeType the nodeType to set
		 */
		public void setNodeType(NODETYPE nodeType) {
			this.nodeType = nodeType;
		}
		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}
		/**
		 * @param time the time to set
		 */
		public void setTime(long time) {
			this.time = time;
		}
	}
	
	public void offerNode(PossibleNode possibleNode) {
		pendingMessage.offer(possibleNode);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
		PossibleNode possibleNode = new PossibleNode();
		possibleNode.setNodeType(NODETYPE.NONE);
		offerNode(possibleNode);
	}
	
}
