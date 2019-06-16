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
import java.nio.ByteBuffer;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.avro.util.Utf8;

import com.eqchains.keystore.Keystore;
import com.eqchains.rpc.avro.Block;
import com.eqchains.rpc.avro.Cookie;
import com.eqchains.rpc.avro.Europa;
import com.eqchains.rpc.avro.FullNodeList;
import com.eqchains.rpc.avro.Height;
import com.eqchains.rpc.avro.MinerList;
import com.eqchains.rpc.avro.Status;
import com.eqchains.rpc.avro.SyncblockNetwork;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jan 24, 2019
 * @email 10509759@qq.com
 */
public class SyncblockNetworkService extends Thread {
	private static SyncblockNetworkService instance;

	public static class SyncblockNetworkImpl implements SyncblockNetwork {

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
		public Europa getBlockTail(Cookie cookie) throws AvroRemoteException {
			Europa europa = new Europa();
			Height height = new Height();
			height.setCookie(Util.getCookie());
			try {
				height.setHeight(Util.DB().getEQCBlockTailHeight().longValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			europa.setHeight(height);
			europa.setNonce(0l);
			return europa;
		}

		@Override
		public Block getBlock(Height height) throws AvroRemoteException {
			Block block = new Block();
			ByteBuffer byteBuffer = null;
			try {
				byteBuffer = ByteBuffer.wrap(Util.DB().getEQCBlock(new ID(height.getHeight()), false).getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			block.setBlock(byteBuffer);
			block.setCookie(Util.getCookie());
			return block;
		}

	}

	private static Server server;

	private static void startServer() throws IOException {
		if(server != null) {
			server.close();
		}
		server = new NettyServer(new SpecificResponder(SyncblockNetwork.class, new SyncblockNetworkImpl()),
				new InetSocketAddress(7997));
	}

	public static SyncblockNetworkService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new SyncblockNetworkService();
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
		if(!this.isAlive()) {
			super.start();
		}
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
			Log.info("Starting SyncblockNetworkService...");
			startServer();
			Log.info("SyncblockNetworkService started...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During Starting SyncblockNetworkService error occur: " + e.getMessage());
		}

	}
	
	public void close() {
		if(server != null) {
			Log.info("Begin close SyncblockNetworkService...");
			server.close();
			server = null;
			Log.info("SyncblockNetworkService closed...");
			this.interrupt();
		}
	}
	
}
