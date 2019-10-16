package ru.intertrust.cm.crypto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CRLException;
import java.security.cert.CertPathBuilderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;

import ru.intertrust.cm.core.model.FatalException;

/**
 * Class for building a certification chain for given certificate and verifying
 * it. Relies on a set of root CA certificates and intermediate certificates
 * that will be used for building the certification chain. The verification
 * process assumes that all self-signed certificates in the set are trusted root
 * CA certificates and all other certificates in the set are intermediate
 * certificates.
 * 
 */
public class CertificateVerifier {
    
    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates and intermediate
     * certificates that will be used for building the certification chain. The
     * verification process assumes that all self-signed certificates in the set
     * are trusted root CA certificates and all other certificates in the set
     * are intermediate certificates.
     * 
     * @param cert
     *            - certificate for validation
     * @param additionalCerts
     *            - set of trusted root CA certificates that will be used as
     *            "trust anchors" and intermediate CA certificates that will be
     *            used as part of the certification chain. All self-signed
     *            certificates are considered to be trusted root CA
     *            certificates. All the rest are considered to be intermediate
     *            CA certificates.
     * @return the certification chain (if verification is successful)
     * @throws CertificateVerificationException
     *             - if the certification is not successful (e.g. certification
     *             path cannot be built or some certificate in the chain is
     *             expired or CRL checks are failed)
     */
    public void verifyCertificate(X509Certificate cert, Set<X509Certificate> additionalCerts) {
        try {
            // Check for self-signed certificate
            if (isSelfSigned(cert)) {
                throw new FatalException("The certificate is self-signed.");
            }

            // Prepare a set of trusted root CA certificates
            // and a set of intermediate certificates
            Set<X509Certificate> trustedRootCerts = getTrustedCertificates();
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
            if (additionalCerts != null) {
                for (X509Certificate additionalCert : additionalCerts) {
                    if (!isSelfSigned(additionalCert)) {
                        intermediateCerts.add(additionalCert);
                    }
                }
            }

            // Attempt to build the certification chain and verify it
            verifyCertificate(cert, trustedRootCerts, intermediateCerts);

            // Check whether the certificate is revoked by the CRL
            // given in its CRL distribution point extension
            verifyCertificateCRLs(cert);

        } catch (CertPathBuilderException certPathEx) {
            throw new FatalException(
                    "Error building certification path: " +
                            cert.getSubjectX500Principal(), certPathEx);
        } catch (Exception ex) {
            throw new FatalException(
                    "Error verifying the certificate: " +
                            cert.getSubjectX500Principal(), ex);
        }
    }

    /**
     * Checks whether given X.509 certificate is self-signed.
     */
    public boolean isSelfSigned(X509Certificate cert)
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        /*try {
            // Try to verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {
            // Invalid signature --> not self-signed
            return false;
        } catch (InvalidKeyException keyEx) {
            // Invalid key --> not self-signed
            return false;
        }*/
        return cert.getIssuerDN().equals(cert.getSubjectDN());
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates (trust anchors) and a
     * set of intermediate certificates (to be used as part of the chain).
     * @param cert
     *            - certificate for validation
     * @param trustedRootCerts
     *            - set of trusted root CA certificates
     * @param intermediateCerts
     *            - set of intermediate certificates
     * @return the certification chain (if verification is successful)
     * @throws GeneralSecurityException
     *             - if the verification is not successful (e.g. certification
     *             path cannot be built or some certificate in the chain is
     *             expired)
     * @throws IOException 
     * @throws OperatorCreationException 
     * @throws CertException 
     */
    private void verifyCertificate(X509Certificate cert, Set<X509Certificate> trustedRootCerts,
            Set<X509Certificate> intermediateCerts) throws GeneralSecurityException, IOException, OperatorCreationException, CertException {

        // Create the selector that specifies the starting certificate
        /*X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        // Create the trust anchors (set of root CA certificates)
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for (X509Certificate trustedRootCert : trustedRootCerts) {
            trustAnchors.add(new TrustAnchor(trustedRootCert, null));
        }

        // Configure the PKIX certificate builder algorithm parameters
        PKIXBuilderParameters pkixParams =
                new PKIXBuilderParameters(trustAnchors, selector);

        // Disable CRL checks (this is done manually as additional step)
        pkixParams.setRevocationEnabled(false);

        // Specify a list of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        pkixParams.addCertStore(intermediateCertStore);

        // Build and verify the certification chain
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        PKIXCertPathBuilderResult result =
                (PKIXCertPathBuilderResult) builder.build(pkixParams);*/
        
        //Ищем вышестоящий сертификат
        X509Certificate parentCer = null; 
        for (X509Certificate x509Certificate : intermediateCerts) {
            if (x509Certificate.getSubjectX500Principal().equals(cert.getIssuerX500Principal())){
                parentCer = x509Certificate;
                break;
            }
        }
        if (parentCer == null){
            for (X509Certificate x509Certificate : trustedRootCerts) {
                if (x509Certificate.getSubjectX500Principal().equals(cert.getIssuerX500Principal())){
                    parentCer = x509Certificate;
                    break;
                }
            }
        }
        
        if (parentCer == null){
            throw new CertPathBuilderException("Not found parent certificate for " + cert);
        }
        
        ContentVerifierProvider contentVerifierProvider =
                new GostContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(parentCer
                        .getEncoded()));

        SignerInformationVerifier signerInformationVerifier =
                new SignerInformationVerifier(new DefaultCMSSignatureAlgorithmNameGenerator(), new DefaultSignatureAlgorithmIdentifierFinder(), contentVerifierProvider, new CAdESDigestCalculatorProvider());
        
        X509CertificateHolder cerHolder = new X509CertificateHolder(cert.getEncoded());
        cerHolder.isSignatureValid(contentVerifierProvider);
        
        if (!isSelfSigned(parentCer)){
            verifyCertificate(parentCer, trustedRootCerts, intermediateCerts);
        }
        
    }

    public HashSet<X509Certificate> getTrustedCertificates() throws CertificateException {
        try {
            HashSet<X509Certificate> result = new HashSet<X509Certificate>();

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            Arrays.asList(trustManagerFactory.getTrustManagers()).stream().forEach(t -> {
                result.addAll(Arrays.asList(((X509TrustManager) t).getAcceptedIssuers()));
            });

            return result;
        } catch (Exception ex) {
            throw new CertificateException("Error get trusted Certificates", ex);
        }
    }

    /**
     * Extracts the CRL distribution points from the certificate (if available)
     * and checks the certificate revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
     * 
     * @param cert
     *            the certificate to be checked for revocation
     * @throws CertificateVerificationException
     *             if the certificate is revoked
     */
    public void verifyCertificateCRLs(X509Certificate cert) {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            for (int i=0; i<crlDistPoints.size(); i++) {
                X509CRL crl = null;
                try{
                    //Пытаемся скачать список отзыва
                    crl = downloadCRL(crlDistPoints.get(i));
                }catch(Exception ex){
                    //Не удалось скачать CRL, не ошибка если адресов несколько
                    if (i == crlDistPoints.size() - 1){
                        //Выбрасываем исключение если не удалось скачать хотя бы с одного адреса из сертификата
                        throw new FatalException(
                                "Can not download CRL from " + crlDistPoints, ex);
                    }else{
                        continue;
                    }
                }
                if (crl.isRevoked(cert)) {
                    throw new FatalException(
                            "The certificate is revoked by CRL: " + crlDistPoints.get(i));
                }else{
                    //Достаточно проверить CRL на одном из адресов указанных в сертификате
                    break;
                }
            }
        } catch (Exception ex) {
            throw new FatalException(
                    "Can not verify CRL for certificate: " +
                            cert.getSubjectX500Principal(), ex);
        }
    }

    /**
     * Downloads CRL from given URL. Supports http, https, ftp and ldap based
     * URLs.
     */
    private X509CRL downloadCRL(String crlURL) throws IOException,
            CertificateException, CRLException,
            NamingException {
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
                || crlURL.startsWith("ftp://")) {
            X509CRL crl = downloadCRLFromWeb(crlURL);
            return crl;
        } else if (crlURL.startsWith("ldap://")) {
            X509CRL crl = downloadCRLFromLDAP(crlURL);
            return crl;
        } else {
            throw new FatalException(
                    "Can not download CRL from certificate " +
                            "distribution point: " + crlURL);
        }
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
     */
    private X509CRL downloadCRLFromLDAP(String ldapURL)
            throws CertificateException, NamingException, CRLException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        DirContext ctx = new InitialDirContext(env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        byte[] val = (byte[]) aval.get();
        if ((val == null) || (val.length == 0)) {
            throw new FatalException(
                    "Can not download CRL from: " + ldapURL);
        } else {
            InputStream inStream = new ByteArrayInputStream(val);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL) cf.generateCRL(inStream);
            return crl;
        }
    }

    /**
     * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
     * http://crl.infonotary.com/crl/identity-ca.crl
     */
    private X509CRL downloadCRLFromWeb(String crlURL)
            throws MalformedURLException, IOException, CertificateException,
            CRLException {
        URL url = new URL(crlURL);
        InputStream crlStream = url.openStream();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
            return crl;
        } finally {
            crlStream.close();
        }
    }

    /**
     * Extracts all CRL distribution point URLs from the
     * "CRL Distribution Point" extension in a X.509 certificate. If CRL
     * distribution point extension is unavailable, returns an empty list.
     */
    public List<String> getCrlDistributionPoints(
            X509Certificate cert) throws CertificateParsingException, IOException {
        byte[] crldpExt = cert.getExtensionValue(
                X509Extensions.CRLDistributionPoints.getId());
        if (crldpExt == null) {
            List<String> emptyList = new ArrayList<String>();
            return emptyList;
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(
                new ByteArrayInputStream(crldpExt));
        ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(
                new ByteArrayInputStream(crldpExtOctets));
        ASN1Primitive derObj2 = oAsnInStream2.readObject();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<String>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null) {
                if (dpn.getType() == DistributionPointName.FULL_NAME) {
                    GeneralName[] genNames = GeneralNames.getInstance(
                            dpn.getName()).getNames();
                    // Look for an URI
                    for (int j = 0; j < genNames.length; j++) {
                        if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                            String url = DERIA5String.getInstance(
                                    genNames[j].getName()).getString();
                            crlUrls.add(url);
                        }
                    }
                }
            }
        }
        return crlUrls;
    }

}
