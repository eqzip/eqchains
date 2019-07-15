package com.eqchains.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqchains.avro.O;
import com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree;
import com.eqchains.serialization.EQCType;
import com.eqchains.util.ID;

public class Nest extends AvroO {
	private ID id;
	private ID assetID;
	
	public Nest(O io) throws Exception {
		parse(io);
	}
	
	public Nest() {
	}

	@Override
	public boolean isSanity() {
		if(id == null || assetID == null) {
			return false;
		}
		if(!id.isSanity() || !assetID.isSanity()) {
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
		id = EQCType.parseID(is);
		assetID = EQCType.parseID(is);
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(id.getEQCBits());
		os.write(assetID.getEQCBits());
		return os.toByteArray();
	}

	/**
	 * @return the id
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setID(ID id) {
		this.id = id;
	}

	/**
	 * @return the assetID
	 */
	public ID getAssetID() {
		return assetID;
	}

	/**
	 * @param assetID the assetID to set
	 */
	public void setAssetID(ID assetID) {
		this.assetID = assetID;
	}
	
}
