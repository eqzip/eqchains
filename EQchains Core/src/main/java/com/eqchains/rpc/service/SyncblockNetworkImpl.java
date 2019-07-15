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
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTON) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqchains.rpc.service;

import java.nio.ByteBuffer;

import org.apache.avro.AvroRemoteException;

import com.eqchains.avro.O;
import com.eqchains.avro.SyncblockNetwork;
import com.eqchains.blockchain.EQCHive;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.Cookie;
import com.eqchains.rpc.TailInfo;
import com.eqchains.service.PossibleNodeService;
import com.eqchains.service.state.PossibleNodeState;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class SyncblockNetworkImpl implements SyncblockNetwork {

	@Override
	public O ping(O cookie) {
		O info = null;
		try {
			Cookie cookie1 = new Cookie(cookie);
			PossibleNodeState possibleNode = new PossibleNodeState();
			possibleNode.setIp(cookie1.getIp());
			possibleNode.setNodeType(NODETYPE.FULL);
			possibleNode.setTime(System.currentTimeMillis());
			PossibleNodeService.getInstance().offerNode(possibleNode);
			info = Util.getDefaultInfo().getO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	@Override
	public O getMinerList() {
		O minerList = null;
		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList().getO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
	}

	@Override
	public O getFullNodeList() {
		O fullNodeList = null;
		try {
			fullNodeList = EQCBlockChainH2.getInstance().getFullNodeList().getO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return fullNodeList;
	}

	@Override
	public O getBlockTail() {
		O io = null;
		TailInfo europa = null;
		Account account = null;
		try {
			europa = new TailInfo();
			europa.setHeight(Util.DB().getEQCBlockTailHeight());
			account = Util.DB().getAccount(ID.THREE);
			europa.setEuropaNonce(account.getAsset(Asset.EQCOIN).getNonce());
			europa.setBlockTailProof(Util.DB().getEQCBlock(europa.getHeight(), true).getEqcHeader().getProof());
			io = europa.getO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	@Override
	public O getBlock(O height) {
		O block = null;
		EQCHive eqcHive = null;
		try {
			eqcHive = Util.DB().getEQCBlock(new ID(height), false);
			if(eqcHive != null) {
				block = eqcHive.getO();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return block;
	}
	
}
