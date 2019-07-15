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
package com.eqchains.rpc.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import com.eqchains.avro.O;
import com.eqchains.avro.MinerNetwork;
import com.eqchains.blockchain.EQCHive;
import com.eqchains.rpc.Cookie;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.Info;
import com.eqchains.rpc.TransactionIndexList;
import com.eqchains.rpc.TransactionList;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class MinerNetworkClient extends EQCClient {
	
	public static Info ping(Cookie cookie, String ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			info = new Info(client.ping(cookie.getO()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return info;
	}

	public static IPList getMinerList(String ip) throws Exception {
		IPList ipList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			ipList = new IPList(client.getMinerList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return ipList;
	}

	public static IPList getFullNodeList(String ip) throws Exception {
		IPList ipList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			ipList = new IPList(client.getFullNodeList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return ipList;
	}

	public static Info broadcastNewBlock(EQCHive block, String ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			info = new Info(client.broadcastNewBlock(block.getO()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return info;
	}

	public static TransactionIndexList getTransactionIndexList(String ip) throws Exception {
		TransactionIndexList transactionIndexList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			transactionIndexList = new TransactionIndexList(client.getTransactionIndexList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return transactionIndexList;
	}

	public static TransactionList getTransactionList(O transactionList, String ip) throws Exception {
		TransactionList transactionList2 = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			transactionList2 = new TransactionList(client.getTransactionList(transactionList));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
			throw e;
		}
		return transactionList2;
	}

	public static long ping(String remoteIP) {
    	NettyTransceiver nettyTransceiver = null;
    	MinerNetwork client = null;
    	long time = System.currentTimeMillis();
    	try {
    		nettyTransceiver = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(remoteIP), Util.MINER_NETWORK_PORT), Util.DEFAULT_TIMEOUT);
    		client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
    		client.ping(cookie.getO());
    		time = System.currentTimeMillis() - time;
    	}
    	catch (Exception e) {
    		Log.Error(e.getMessage());
    		time = -1;
		}
    	finally {
			if(nettyTransceiver != null) {
				nettyTransceiver.close();
			}
		}
    	return time;
	}
	
	public static String getFastestServer(IPList ipList) {
		String fastestServer = null;
		long time = 0;
		long maxTime = 0;
		for(String ip:ipList.getIpList()) {
			time = ping(ip);
			if(time > maxTime) {
				fastestServer = ip;
				maxTime = time;
			}
		}
		return fastestServer;
	}
	
}
