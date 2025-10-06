package com.enemaru.mqtt;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Utility class for creating SSL contexts with mutual TLS authentication
 * using PEM certificate files. Supports both PKCS#1 and PKCS#8 private key formats.
 */
public class SSLContextBuilder {

    private static final JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter();
    private static final JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();

    /**
     * Creates an SSL context with mutual TLS authentication using PEM files.
     *
     * @param caPath   Path to the CA certificate file (PEM format)
     * @param certPath Path to the client certificate file (PEM format)
     * @param keyPath  Path to the private key file (PEM format, PKCS#1 or PKCS#8)
     * @return Configured SSLContext ready for use
     * @throws Exception if any error occurs during SSL context creation
     */
    public static SSLContext buildMutualTLSContext(String caPath, String certPath, String keyPath) throws Exception {
        // Load CA certificate
        X509Certificate caCert = loadCertificate(caPath);

        // Load client certificate
        X509Certificate clientCert = loadCertificate(certPath);

        // Load private key (supports both PKCS#1 and PKCS#8)
        PrivateKey privateKey = loadPrivateKey(keyPath);

        // Create KeyStore for client authentication
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setKeyEntry("client", privateKey, new char[0], new java.security.cert.Certificate[]{clientCert});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, new char[0]);

        // Create TrustStore for CA validation
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry("ca", caCert);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Initialize SSL context
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    /**
     * Loads an X509 certificate from a PEM file.
     *
     * @param certPath Path to the certificate file
     * @return X509Certificate object
     * @throws Exception if certificate loading fails
     */
    private static X509Certificate loadCertificate(String certPath) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(certPath))) {
            Object obj = parser.readObject();
            if (obj instanceof X509CertificateHolder) {
                return certConverter.getCertificate((X509CertificateHolder) obj);
            } else {
                throw new IllegalArgumentException("Unexpected certificate PEM content: " + obj.getClass());
            }
        }
    }

    /**
     * Loads a private key from a PEM file. Supports both PKCS#1 and PKCS#8 formats.
     *
     * @param keyPath Path to the private key file
     * @return PrivateKey object
     * @throws Exception if key loading fails
     */
    private static PrivateKey loadPrivateKey(String keyPath) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(keyPath))) {
            Object obj = parser.readObject();
            if (obj instanceof PEMKeyPair) {
                // PKCS#1 format
                return keyConverter.getKeyPair((PEMKeyPair) obj).getPrivate();
            } else if (obj instanceof PrivateKeyInfo) {
                // PKCS#8 format
                return keyConverter.getPrivateKey((PrivateKeyInfo) obj);
            } else {
                throw new IllegalArgumentException("Unexpected key PEM content: " + (obj == null ? "null" : obj.getClass()));
            }
        }
    }
}