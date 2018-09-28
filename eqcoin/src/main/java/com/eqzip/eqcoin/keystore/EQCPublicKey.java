/**
 * EQCoin core - EQZIP's EQCoin core library
 * @copyright 2018 EQZIP Inc.  All rights reserved...
 * https://www.eqzip.com
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
package com.eqzip.eqcoin.keystore;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECPoint;

import com.eqzip.eqcoin.util.Log;
import com.eqzip.eqcoin.util.Util;

/**
 * EQPublicKey is an EQC tool you can use it as PublicKey to verify the signature or get the PublicKey's compressed encode
 * 
 * @author Xun Wang
 * @date Sep 26, 2018
 * @email 10509759@qq.com
 */
public class EQCPublicKey implements PublicKey {
	private static final long serialVersionUID = 1303765568188200263L;
	/**
	 * The parameters of the secp256r1 or secp521r1 curve that EQCOIN uses.
	 */
	private ECDomainParameters CURVE;
	// All clients must agree on the curve to use by agreement. EQCOIN uses
	// secp256r1 or secp521r1.
	private X9ECParameters params;
	private ECPoint ecPoint;
	
	// For java standard EC
	ECNamedCurveParameterSpec spec;
	ECNamedCurveSpec ecParams;
	ECPublicKey pk;
	
	public EQCPublicKey(int type) {
		if (type == Keystore.P256) {
			params = SECNamedCurves.getByName(Keystore.SECP256R1);
			CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP256R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP256R1, spec.getCurve(), spec.getG(), spec.getN());
		} else if (type == Keystore.P521) {
			params = SECNamedCurves.getByName(Keystore.SECP521R1);
			CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP521R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP521R1, spec.getCurve(), spec.getG(), spec.getN());
		}
	}

	/**
	 * Construct EQPublicKey using the official ECPublicKey of java then you can use EQPublicKey get the compressed public key
	 * 
	 * @param ecPublicKey The interface to an elliptic curve (EC) public key
	 */
	public void setECPoint(final ECPublicKey ecPublicKey) {
		final java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
		final BigInteger xCoord = publicPointW.getAffineX();
		final BigInteger yCoord = publicPointW.getAffineY();
		ecPoint = CURVE.getCurve().createPoint(xCoord, yCoord);
	}
	
	/**
	 * Construct an EQPublicKey with a compressed public key then you can use it to verify the signature
	 * 
	 * @param bytes Compressed public key
	 */
	public void setECPoint(final byte[] compressedPublicKey) {
		ecPoint = CURVE.getCurve().decodePoint(compressedPublicKey);
		ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true));
		KeyFactory kf = null;
		try {
			kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true)), ecParams);
		try {
			pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public byte[] getCompressedPublicKeyEncoded() {
		return ecPoint.getEncoded(true);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return pk.getAlgorithm();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		// TODO Auto-generated method stub
		return pk.getFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return pk.getEncoded();
	}
	
}
