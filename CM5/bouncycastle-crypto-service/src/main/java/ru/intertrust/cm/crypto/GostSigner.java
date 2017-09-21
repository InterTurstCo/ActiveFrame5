package ru.intertrust.cm.crypto;

import java.math.BigInteger;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.GOST3411Digest;
import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.ECGOST3410Signer;

import ru.intertrust.cm.core.model.FatalException;

public class GostSigner implements Signer {
    private final Digest digest;
    private final DSA dsaSigner;
    private boolean forSigning;

    public GostSigner(AlgorithmIdentifier digAlg)
    {
        try{
            this.digest = BcUtil.createDigest(digAlg);
            this.dsaSigner = new ECGOST3410Signer();
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

            byte[] r = new byte[32];
            byte[] s = new byte[32];

            System.arraycopy(signature, 0, s, 0, 32);

            System.arraycopy(signature, 32, r, 0, 32);

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
