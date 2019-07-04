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

import java.nio.ByteBuffer;

import org.apache.avro.AvroRemoteException;

import com.eqchains.avro.IO;
import com.eqchains.avro.SyncblockNetwork;
import com.eqchains.blockchain.EQCHive;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.account.Asset;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.Cookie;
import com.eqchains.rpc.Europa;
import com.eqchains.service.PossibleNodeService.PossibleNode;
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
	public IO ping(IO cookie) throws AvroRemoteException {
		IO info = null;
		try {
			Cookie cookie1 = new Cookie(cookie);
			PossibleNode possibleNode = new PossibleNode();
			possibleNode.setIp(cookie1.getIp());
			possibleNode.setNodeType(NODETYPE.FULL);
			possibleNode.setTime(System.currentTimeMillis());
			PossibleNodeService.getInstance().offerNode(possibleNode);
			info = Util.getDefaultInfo().getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	@Override
	public IO getMinerList() throws AvroRemoteException {
		IO minerList = null;
		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList().getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
	}

	@Override
	public IO getFullNodeList() throws AvroRemoteException {
		IO fullNodeList = null;
		try {
			fullNodeList = EQCBlockChainH2.getInstance().getFullNodeList().getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return fullNodeList;
	}

	@Override
	public IO getBlockTail() throws AvroRemoteException {
		IO io = null;
		Europa europa = null;
		Account account = null;
		try {
			europa = new Europa();
			europa.setHeight(Util.DB().getEQCBlockTailHeight());
			account = Util.DB().getAccount(ID.THREE);
			europa.setNonce(account.getAsset(Asset.EQCOIN).getNonce());
			europa.setBlockTailProof(Util.DB().getEQCBlock(europa.getHeight(), true).getEqcHeader().getProof());
			io = europa.getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	@Override
	public IO getBlock(IO height) throws AvroRemoteException {
		IO block = null;
		EQCHive eqcHive = null;
		try {
			eqcHive = Util.DB().getEQCBlock(new ID(height), false);
			if(eqcHive != null) {
				block = eqcHive.getIO();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return block;
	}
	
}
