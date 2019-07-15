package com.eqchains.rpc;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import com.eqchains.avro.O;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.blockchain.transaction.Transaction;
import com.eqchains.serialization.EQCType;
import com.eqchains.serialization.EQCType.ARRAY;

public class TransactionList extends AvroO {
	private Vector<byte[]> transactionList;
	private long transactonListSize;
	
	public TransactionList() {
		transactionList = new Vector<>();
	}
	
	public TransactionList(O io) throws Exception {
		transactionList = new Vector<>();
		parse(io);
	}
	
	@Override
	public boolean isSanity() {
		if(transactionList == null) {
			return false;
		}
		if(transactonListSize != transactionList.size()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(AccountsMerkleTree accountsMerkleTree) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		ARRAY array = EQCType.parseARRAY(is);
		ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
		for(int i=0; i<array.length; ++i) {
			transactionList.add(EQCType.parseBIN(iStream));
		}
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		if(transactionList.size() == 0) {
			return EQCType.bytesArrayToARRAY(null);
		}
		else {
			return EQCType.bytesArrayToARRAY(transactionList);
		}
	}
	
	public void addTransaction(Transaction transaction) {
		if(transaction != null) {
			transactionList.add(EQCType.bytesToBIN(transaction.getRPCBytes()));
		}
	}

	public void addAll(Vector<byte[]> transactionList) {
		this.transactionList.addAll(transactionList);
	}
	
}