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

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import com.eqchains.avro.IO;
import com.eqchains.avro.SyncblockNetwork;
import com.eqchains.test.Test;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 28, 2019
 * @email 10509759@qq.com
 */
public class SyncblockNetworkProxy implements SyncblockNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.SyncblockNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public IO ping(IO cookie) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.SyncblockNetwork#getMinerList()
	 */
	@Override
	public IO getMinerList() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.SyncblockNetwork#getFullNodeList()
	 */
	@Override
	public IO getFullNodeList() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.SyncblockNetwork#getBlockTail()
	 */
	@Override
	public IO getBlockTail() throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.SyncblockNetwork#getBlock(com.eqchains.avro.IO)
	 */
	@Override
	public IO getBlock(IO height) throws AvroRemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public static long ping(String remoteIP) {
//		Test.ping("14.221.176.138", "129.28.206.27");
		Cookie cookie = new Cookie();
		cookie.setIp(Util.IP);
		cookie.setVersion(Util.PROTOCOL_VERSION);
		long time = System.currentTimeMillis();
    	NettyTransceiver client = null;
    	try {
    		client = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName(remoteIP), 7997), 3000l);
    		SyncblockNetwork proxy = (SyncblockNetwork) SpecificRequestor.getClient(SyncblockNetwork.class, client);
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
