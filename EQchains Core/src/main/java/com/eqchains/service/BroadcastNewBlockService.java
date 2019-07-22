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

import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.rpc.IPList;
import com.eqchains.rpc.Info;
import com.eqchains.rpc.client.MinerNetworkClient;
import com.eqchains.service.state.EQCServiceState;
import com.eqchains.service.state.NewBlockState;
import com.eqchains.service.state.EQCServiceState.State;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jul 13, 2019
 * @email 10509759@qq.com
 */
public class BroadcastNewBlockService extends EQCService {
	private static BroadcastNewBlockService instance;
	
	public static BroadcastNewBlockService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new BroadcastNewBlockService();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		NewBlockState newBlockState = null;
		try {
			this.state.set(State.BROADCASTNEWBLOCK);
			newBlockState = (NewBlockState) state;
			if(!Util.IP.equals(Util.SINGULARITY_IP)) {
				try {
//					Log.info("BroadcastNewBlock to SINGULARITY_IP: ");
					Info info = MinerNetworkClient.broadcastNewBlock(newBlockState.getNewBlock(), Util.SINGULARITY_IP);
					Log.info("BroadcastNewBlock to SINGULARITY_IP result: " + info.getCode());
				}
				catch (Exception e) {
					Log.Error(e.getMessage());
				}
			}
			IPList minerList = EQCBlockChainH2.getInstance().getMinerList();
			if(!minerList.isEmpty()) {
				for(String ip:minerList.getIpList()) {
					if(!Util.IP.equals(ip)) {
						try {
//							Log.info("BroadcastNewBlock to: " + ip);
							Info info = MinerNetworkClient.broadcastNewBlock(newBlockState.getNewBlock(), ip);
							Log.info("BroadcastNewBlock to: " + ip + " result: " + info.getCode());
						}
						catch (Exception e) {
							Log.Error(e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(name + e.getMessage());
		}
	}

	public void offerNewBlockState(NewBlockState newBlockState) {
//		Log.info("offerNewBlockState: " + newBlockState);
		offerState(newBlockState);
	}
	
}
