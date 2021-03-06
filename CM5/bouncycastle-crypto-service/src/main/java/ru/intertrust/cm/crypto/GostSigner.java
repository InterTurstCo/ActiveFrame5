package ru.intertrust.cm.crypto;

import java.math.BigInteger;

import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.ECGOST3410Signer;
import org.bouncycastle.crypto.signers.ECGOST3410_2012Signer;

import ru.intertrust.cm.core.model.FatalException;

public class GostSigner implements Signer {
    private final Digest digest;
    private final DSA dsaSigner;
    private boolean forSigning;
    private int hashSize;

    public GostSigner(AlgorithmIdentifier digAlg)
    {
        try{
            this.digest = BcUtil.createDigest(digAlg);
            
            if (digAlg.getAlgorithm().equals(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94)
                    || digAlg.getAlgorithm().equals(CryptoProObjectIdentifiers.gostR3411)
                    || digAlg.getAlgorithm().equals(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001)
                    || digAlg.getAlgorithm().equals(CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet)) {
                this.dsaSigner = new ECGOST3410Signer();
            } else if (digAlg.getAlgorithm().equals(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256)
                    || digAlg.getAlgorithm().equals(RosstandartObjectIdentifiers.id_tc26_gost_3411_12_512)){
                this.dsaSigner = new ECGOST3410_2012Signer();
            }else {
                this.dsaSigner = new DSASigner();
            }
            hashSize = digest.getDigestSize();
            
        }catch(Exception ex){
            throw new FatalException("Error create GostSigner", ex);
        }
    }

    public void init(
            boolean forSigning,
            CipherParameters parameters)
    {
        this.forSigning = forSigning;

        AsymmetricKeyParameter k;

        if (parameters instanceof ParametersWithRandom)
        {
            k = (AsymmetricKeyParameter) ((ParametersWithRandom) parameters).getParameters();
        }
        else
        {
            k = (AsymmetricKeyParameter) parameters;
        }

        if (forSigning && !k.isPrivate())
        {
            throw new IllegalArgumentException("Signing Requires Private Key.");
        }

        if (!forSigning && k.isPrivate())
        {
            throw new IllegalArgumentException("Verification Requires Public Key.");
        }

        reset();

        dsaSigner.init(forSigning, parameters);
    }

    /**
     * update the internal digest with the byte b
     */
    public void update(
            byte input)
    {
        digest.update(input);
    }

    /**
     * update the internal digest with the byte array in
     */
    public void update(
            byte[] input,
            int inOff,
            int length)
    {
        digest.update(input, inOff, length);
    }

    /**
     * Generate a signature for the message we've been loaded with using the key
     * we were initialised with.
     */
    public byte[] generateSignature()
    {
        if (!forSigning)
        {
            throw new IllegalStateException("DSADigestSigner not initialised for signature generation.");
        }

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        BigInteger[] sig = dsaSigner.generateSignature(hash);

        byte[] sigBytes = new byte[64];
        byte[] r = sig[0].toByteArray();
        byte[] s = sig[1].toByteArray();

        if (s[0] != 0)
        {
            System.arraycopy(s, 0, sigBytes, 32 - s.length, s.length);
        }
        else
        {
            System.arraycopy(s, 1, sigBytes, 32 - (s.length - 1), s.length - 1);
        }

        if (r[0] != 0)
        {
            System.arraycopy(r, 0, sigBytes, 64 - r.length, r.length);
        }
        else
        {
            System.arraycopy(r, 1, sigBytes, 64 - (r.length - 1), r.length - 1);
        }

        return sigBytes;
    }

    public boolean verifySignature(
            byte[] signature)
    {
        if (forSigning)
        {
            throw new IllegalStateException("DSADigestSigner not initialised for verification");
        }

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        try
        {
            BigInteger[] sig = new BigInteger[2];

            byte[] r = new byte[hashSize];
            byte[] s = new byte[hashSize];

            System.arraycopy(signature, 0, s, 0, hashSize);

            System.arraycopy(signature, hashSize, r, 0, hashSize);

            sig = new BigInteger[2];
            sig[0] = new BigInteger(1, r);
            sig[1] = new BigInteger(1, s);

            return dsaSigner.verifySignature(hash, sig[0], sig[1]);
        } catch (Exception e)
        {
            return false;
        }
    }

    public void reset()
    {
        digest.reset();
    }
}
