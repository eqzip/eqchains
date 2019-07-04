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
package com.eqchains.rpc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import com.eqchains.avro.IO;
import com.eqchains.avro.MinerNetwork;
import com.eqchains.avro.SyncblockNetwork;
import com.eqchains.avro.TransactionNetwork;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class TransactionNetworkProxy extends EQCProxy implements TransactionNetwork {
	private TransactionNetwork proxy = null;
	
	public TransactionNetworkProxy(String ip) throws IOException {
		client = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
		proxy = (TransactionNetwork) SpecificRequestor.getClient(TransactionNetwork.class, client);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public IO ping(IO cookie) throws AvroRemoteException {
		return proxy.ping(cookie);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMinerList()
	 */
	@Override
	public IO getMinerList() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getMinerList();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#sendTransaction(com.eqchains.avro.IO)
	 */
	@Override
	public IO sendTransaction(IO transactionRPC) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.sendTransaction(transactionRPC);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getID(com.eqchains.avro.IO)
	 */
	@Override
	public IO getID(IO readableAddress) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getID(readableAddress);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getAccount(com.eqchains.avro.IO)
	 */
	@Override
	public IO getAccount(IO id) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getAccount(id);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMaxNonce(com.eqchains.avro.IO)
	 */
	@Override
	public IO getMaxNonce(IO id) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getMaxNonce(id);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getBalance(com.eqchains.avro.IO)
	 */
	@Override
	public IO getBalance(IO id) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getBalance(id);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getSignHash(com.eqchains.avro.IO)
	 */
	@Override
	public IO getSignHash(IO id) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getSignHash(id);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public IO getPendingTransactionList(IO id) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return proxy.getPendingTransactionList(id);
	}

	public static long ping(String remoteIP) {
    	NettyTransceiver client = null;
    	TransactionNetwork proxy = null;
    	long time = System.currentTimeMillis();
    	try {
    		client = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(remoteIP), Util.TRANSACTION_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
    		proxy = (TransactionNetwork) SpecificRequestor.getClient(TransactionNetwork.class, client);
    		proxy.ping(cookie.getIO());
    		time = System.currentTimeMillis() - time;
    	}
    	catch (Exception e) {
    		Log.Error(e.getMessage());
    		time = -1;
		}
    	finally {
			if(client != null) {
				client.close();
			}
		}
    	return time;
	}
	
}
