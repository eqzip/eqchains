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

import java.util.concurrent.PriorityBlockingQueue;

import com.eqchains.avro.IO;
import com.eqchains.blockchain.account.Account;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.keystore.Keystore;
import com.eqchains.persistence.h2.EQCBlockChainH2;
import com.eqchains.persistence.h2.EQCBlockChainH2.NODETYPE;
import com.eqchains.rpc.MaxNonce;
import com.eqchains.rpc.MinerNetworkProxy;
import com.eqchains.rpc.SyncblockNetworkProxy;
import com.eqchains.service.PossibleNodeService.PossibleNode;
import com.eqchains.util.ID;
import com.eqchains.util.Log;
import com.eqchains.util.Util;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class PendingTransactionService extends EQCService {
	private PriorityBlockingQueue<PendingTransaction> pendingMessage;
	
	public static PendingTransactionService getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new PendingTransactionService();
				}
			}
		}
		return (PendingTransactionService) instance;
	}
	
    public PendingTransactionService() {
    	pendingMessage = new PriorityBlockingQueue<>();
    	start();
	}

    /* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#runner()
	 */
	@Override
	protected void runner() {
		PendingTransaction pendingTransaction = null;
		Transaction transaction = null;
		try {
			pendingTransaction = pendingMessage.take();
			if(pendingTransaction.getTime() == 0) {
				return;
			}
			transaction = Transaction.parseRPC(pendingTransaction.getTransaction());
			transaction.update();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	public static class PendingTransaction implements Comparable<PendingTransaction> {
		private byte[] transaction;
		private long time;
		
		public PendingTransaction() {
		}
		
		public PendingTransaction(IO transactionRPC) {
			transaction = transactionRPC.getObject().array();
			time = System.currentTimeMillis();
		}
		
		@Override
		public int compareTo(PendingTransaction o) {
			return (int) (o.time - time);
		}
		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}
		/**
		 * @param time the time to set
		 */
		public void setTime(long time) {
			this.time = time;
		}
		/**
		 * @return the transaction
		 */
		public byte[] getTransaction() {
			return transaction;
		}
		/**
		 * @param transaction the transaction to set
		 */
		public void setTransaction(byte[] transaction) {
			this.transaction = transaction;
		}
	}
	
	public void offerPendingTransaction(PendingTransaction pendingTransaction) {
		pendingMessage.offer(pendingTransaction);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
		PendingTransaction pendingTransaction = new PendingTransaction();
		pendingTransaction.setTime(0);
		offerPendingTransaction(pendingTransaction);
	}
    
}
