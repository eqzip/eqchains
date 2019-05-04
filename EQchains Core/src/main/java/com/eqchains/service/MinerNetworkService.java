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
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;

import com.eqchains.keystore.Keystore;
import com.eqchains.rpc.avro.Block;
import com.eqchains.rpc.avro.Cookie;
import com.eqchains.rpc.avro.FullNodeList;
import com.eqchains.rpc.avro.MinerList;
import com.eqchains.rpc.avro.MinerNetwork;
import com.eqchains.rpc.avro.MineringBase;
import com.eqchains.rpc.avro.Status;
import com.eqchains.rpc.avro.SyncblockNetwork;
import com.eqchains.rpc.avro.TransactionHashList;
import com.eqchains.rpc.avro.TransactionList;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jan 24, 2019
 * @email 10509759@qq.com
 */
public class MinerNetworkService extends Thread {
	private static MinerNetworkService instance;

	public static class MinerNetworkImpl implements MinerNetwork {

		@Override
		public Status ping(Cookie cookie) throws AvroRemoteException {
			// Here need add function to test ping
			return Util.getStatus();
		}

		@Override
		public MinerList getMinerList(Cookie cookie) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FullNodeList getFullNodeList(Cookie cookie) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Status sendNewBlock(Block newBlock) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Status sendMineringBase(MineringBase mineringBase) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MineringBase getMineringBase(Cookie cookie) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TransactionHashList getTransactionHashList(Cookie cookie) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TransactionList getTransactionList(TransactionHashList transactionHashList) throws AvroRemoteException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static Server server;

	private static void startServer() throws IOException {
		if(server != null) {
			server.close();
		}
		server = new NettyServer(new SpecificResponder(SyncblockNetwork.class, new MinerNetworkImpl()),
				new InetSocketAddress(7799));
	}

	public static MinerNetworkService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new MinerNetworkService();
				}
			}
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		try {
			Log.info("Starting MinerNetworkService...");
			startServer();
			Log.info("MinerNetworkService started...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During Starting MinerNetworkService error occur: " + e.getMessage());
		}

	}
	
	public void close() {
		if(server != null) {
			Log.info("Begin close MinerNetworkService...");
			server.close();
			server = null;
			Log.info("MinerNetworkService closed...");
			this.interrupt();
		}
	}
	
}
