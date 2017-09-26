package ru.intertrust.cm.crypto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentVerifierProviderBuilder;

public class GostContentVerifierProviderBuilder
        extends BcContentVerifierProviderBuilder {
    private DigestAlgorithmIdentifierFinder digestAlgorithmFinder;

    public GostContentVerifierProviderBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmFinder) {
        this.digestAlgorithmFinder = digestAlgorithmFinder;
    }

    @Override
    protected Signer createSigner(AlgorithmIdentifier sigAlgId)
            throws OperatorCreationException {
        AlgorithmIdentifier digAlg = digestAlgorithmFinder.find(sigAlgId);

        return new GostSigner(digAlg);
    }

    @Override
    protected AsymmetricKeyParameter extractKeyParameters(SubjectPublicKeyInfo publicKeyInfo) throws IOException {
        ASN1ObjectIdentifier algorithmIdentifier = publicKeyInfo.getAlgorithm().getAlgorithm();

        AsymmetricKeyInfoConverter keyFactory = null;

        if (algorithmIdentifier.equals(CryptoProObjectIdentifiers.gostR3410_2001)
                || algorithmIdentifier.equals(CryptoProObjectIdentifiers.gostR3410_2001DH)
                || algorithmIdentifier.equals(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_ESDH)) {
            keyFactory = new org.bouncycastle.jcajce.provider.asymmetric.ecgost.KeyFactorySpi();
        } else if (algorithmIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256)
                || algorithmIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512)
                || algorithmIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_256)
                || algorithmIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_512)) {
            keyFactory = new org.bouncycastle.jcajce.provider.asymmetric.ecgost12.KeyFactorySpi();
        }

        if (keyFactory != null) {
            PublicKey key = keyFactory.generatePublic(publicKeyInfo);
            try {
                return ECUtil.generatePublicKeyParameter(key);
            } catch (InvalidKeyException e) {
                throw new IOException(e);
            }
        } else {
            return PublicKeyFactory.createKey(publicKeyInfo);
        }
    }
}
