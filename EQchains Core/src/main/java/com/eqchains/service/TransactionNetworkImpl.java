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
import java.util.Vector;

import org.apache.avro.AvroRemoteException;

import com.eqchains.avro.IO;
import com.eqchains.avro.TransactionNetwork;
import com.eqchains.blockchain.account.Account;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.Balance;
import com.eqchains.rpc.Code;
import com.eqchains.rpc.Cookie;
import com.eqchains.rpc.MaxNonce;
import com.eqchains.rpc.Nest;
import com.eqchains.rpc.TransactionList;
import com.eqchains.serialization.EQCType;
import com.eqchains.service.PendingTransactionService.PendingTransaction;
import com.eqchains.service.PossibleNodeService.PossibleNode;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;
import com.eqchains.util.Util.AddressTool;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class TransactionNetworkImpl implements TransactionNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public IO ping(IO cookie) throws AvroRemoteException {
		IO info = null;
		Cookie cookie1 = null;
		try {
			cookie1 = new Cookie(cookie);
			if(cookie1.isSanity()) {
				info = Util.getDefaultInfo().getIO();
			}
			else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null).getIO();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMinerList()
	 */
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

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#sendTransaction(com.eqchains.avro.IO)
	 */
	@Override
	public IO sendTransaction(IO transactionRPC) throws AvroRemoteException {
		IO info = null;
		PendingTransaction pendingTransaction = null;
		try {
			pendingTransaction = new PendingTransaction(transactionRPC);
			PendingTransactionService.getInstance().offerPendingTransaction(pendingTransaction);
			info = Util.getDefaultInfo().getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getID(com.eqchains.avro.IO)
	 */
	@Override
	public IO getID(IO readableAddress) throws AvroRemoteException {
		IO id = null;
		Account account = null;
		try {
			account = Util.DB().getAccount(AddressTool.addressToAI(EQCType.bytesToASCIISting(readableAddress.getObject().array())));
			if(account != null) {
				id = account.getID().getIO();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return id;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getAccount(com.eqchains.avro.IO)
	 */
	@Override
	public IO getAccount(IO id) throws AvroRemoteException {
		IO io = null;
		Account account = null;
		try {
			account = Util.DB().getAccount(new ID(id));
			if(account != null) {
				io = account.getIO();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMaxNonce(com.eqchains.avro.IO)
	 */
	@Override
	public IO getMaxNonce(IO nest) throws AvroRemoteException {
		IO io = null;
		MaxNonce maxNonce = null;
		try {
			maxNonce = EQCBlockChainH2.getInstance().getTransactionMaxNonce(new Nest(nest));
			io = maxNonce.getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getBalance(com.eqchains.avro.IO)
	 */
	@Override
	public IO getBalance(IO nest) throws AvroRemoteException {
		IO io = null;
		Balance balance = null;
		try {
			balance = Util.DB().getBalance(new Nest(nest));
			io = balance.getIO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getSignHash(com.eqchains.avro.IO)
	 */
	@Override
	public IO getSignHash(IO id) throws AvroRemoteException {
		IO io = null;
		byte[] signHash = null;
		try {
			signHash = Util.DB().getAccount(new ID(id)).getSignatureHash();
			if(signHash != null) {
				io = new IO(ByteBuffer.wrap(signHash));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public IO getPendingTransactionList(IO id) throws AvroRemoteException {
		IO io = null;
		TransactionList transactionList = new TransactionList();
		Vector<byte[]> vector = null;
		try {
			vector = EQCBlockChainH2.getInstance().getPendingTransactionListInPool(new ID(io));
			if(vector != null) {
				transactionList.addAll(vector);
				io = transactionList.getIO();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

}
