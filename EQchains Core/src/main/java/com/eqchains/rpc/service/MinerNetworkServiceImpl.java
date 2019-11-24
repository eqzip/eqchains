/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
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
package com.eqchains.rpc.service;

import org.apache.avro.AvroRemoteException;

import com.eqchains.avro.O;
import com.eqchains.blockchain.hive.EQCHive;
import com.eqchains.persistence.EQCBlockChainH2;
import com.eqchains.persistence.EQCBlockChainH2.NODETYPE;
import com.eqchains.avro.MinerNetwork;
import com.eqchains.rpc.Code;
import com.eqchains.rpc.Cookie;
import com.eqchains.rpc.Info;
import com.eqchains.rpc.NewHive;
import com.eqchains.rpc.TransactionIndexList;
import com.eqchains.rpc.TransactionList;
import com.eqchains.service.PendingNewHiveService;
import com.eqchains.service.PossibleNodeService;
import com.eqchains.service.state.NewHiveState;
import com.eqchains.service.state.PossibleNodeState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class MinerNetworkServiceImpl implements MinerNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public O ping(O cookie) {
		Info<O> info = null;
		O o = null;
		Cookie cookie1 = null;
		try {
			cookie1 = new Cookie(cookie);
			Log.info("Received ping from: " + cookie1.getIp());
			if (cookie1.isSanity()) {
				PossibleNodeState possibleNode = new PossibleNodeState();
				possibleNode.setIp(cookie1.getIp());
				possibleNode.setNodeType(NODETYPE.MINER);
				possibleNode.setTime(System.currentTimeMillis());
				PossibleNodeService.getInstance().offerNode(possibleNode);
				info = Util.getDefaultInfo();
			} else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null);
			}
			o = info.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		Log.info("Give ping response to " + cookie1.getIp() + " info: " + info);
		return o;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#getMinerList()
	 */
	@Override
	public O getMinerList() {
		O minerList = null;
		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#getFullNodeList()
	 */
	@Override
	public O getFullNodeList() {
		O fullNodeList = null;
		try {
			fullNodeList = EQCBlockChainH2.getInstance().getFullNodeList().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return fullNodeList;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#sendNewBlock(com.eqchains.avro.IO)
	 */
	@Override
	public O broadcastNewBlock(O block) {
		O info = null;
		NewHive newBlock = null;
		NewHiveState newBlockState = null;
		try {
			newBlock = new NewHive(block);
			Log.info("MinerNetworkServiceImpl received new block");
			if(newBlock.getCookie().isSanity()) {
				info = Util.getDefaultInfo().getProtocol();
				newBlockState = new NewHiveState(State.PENDINGNEWBLOCK);
				newBlockState.setNewBlock(newBlock);
				PendingNewHiveService.getInstance().offerNewBlockState(newBlockState);
				Log.info("Call PendingNewBlockService handle the new block");
			}
			else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null).getProtocol();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#getTransactionIndexList()
	 */
	@Override
	public O getTransactionIndexList(O synctime) {
		O io = null;
		long syncTime = Util.bytesToLong(synctime.getO().array());
		TransactionIndexList<O> transactionIndexList = null;
		try {
			transactionIndexList = EQCBlockChainH2.getInstance().getTransactionIndexListInPool(syncTime, System.currentTimeMillis());
			io = transactionIndexList.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public O getTransactionList(O transactionIndexList) {
		O io = null;
		TransactionList<O> transactionList = null;
		try {
			transactionList = EQCBlockChainH2.getInstance().getTransactionListInPool(new TransactionIndexList(transactionIndexList));
			io = transactionList.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

}
