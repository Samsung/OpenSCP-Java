/* Specify your package name here */
package com.module_name.package_name;

import com.samsung.openscp.AesAlg;
import com.samsung.openscp.Apdu;
import com.samsung.openscp.ApduException;
import com.samsung.openscp.BadResponseException;
import com.samsung.openscp.KeyRef;
import com.samsung.openscp.Scp11KeyParams;
import com.samsung.openscp.ScpCertificate;
import com.samsung.openscp.ScpMode;
import com.samsung.openscp.SecurityDomainSession;
import com.samsung.openscp.SmartCardConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.List;


public class Scp11ReaderTemplate {

    private final SecurityDomainSession session;
    private final byte[] skOceEcka;
    private final byte[] certOceEcka;
    private final byte[] sessionKeysAlg;

    public Scp11ReaderTemplate(
            final byte[] skOceEcka,
            final byte[] certOceEcka,
            final AesAlg sessionKeysAlg) {
        this.skOceEcka = skOceEcka;
        this.certOceEcka = certOceEcka;
        this.sessionKeysAlg = sessionKeysAlg;
        final SmartCardConnection connection = new Connection();
        session = new SecurityDomainSession(connection);
    }


    public void open(final byte[] aid) throws Exception {
        /*
        Add here code to open eSE Reader class session with specified 'aid'
         */

        final byte keyId = (byte) 0x11;
        final byte keyVersionNumber = (byte) 0x03;
        final KeyRef keyRef = new KeyRef(keyId, keyVersionNumber);
        final List<ScpCertificate> sdCertChain = session.getCertificateBundle(keyRef);
        final int eckaCertPosition = sdCertChain.size() - 1;
        final PublicKey pkSdEcka = sdCertChain.get(eckaCertPosition).getPublicKey();
        final Scp11KeyParams keyParams = createKeyParams(keyRef, pkSdEcka, skOceEcka, certOceEcka, sessionKeysAlg);

        final ScpMode mode = ScpMode.S8;
        try {
            session.authenticate(keyParams, mode);
        } catch (IOException | ApduException | BadResponseException e) {
            // Throw your exception here
            throw new Exception(e);
        }
    }


    public byte[] transmit(final Apdu apdu) throws Exception {
        try {
            return session.sendAndReceiveApdu(apdu);
        } catch (IOException | ApduException e) {
            // Throw your exception here
            throw new Exception(e);
        }
    }


    public void close() {
        /*
        Add here code to close eSE Reader session
         */
    }


    private class Connection implements SmartCardConnection {
        @Override
        public byte[] sendAndReceive(byte[] apdu) throws IOException {
            try {
                /*
                Call here 'transmitPlainChannel()' method of your eSE Reader class to send & receive raw APDU
                 */
                return transmitPlainChannel(apdu);
            } catch (IOException e) { // Use here exception specific to your eSE Reader class implementation
                this.close();
                throw new IOException(e);
            }
        }


        @Override
        public boolean isExtendedLengthApduSupported() {
            return true;
        }


        @Override
        public void close() throws IOException {
            Scp11ReaderTemplate.this.close();
        }
    }


    private static Scp11KeyParams createKeyParams(final KeyRef sessionRef,
                                                  final PublicKey pkSdEcka,
                                                  final byte[] skOceEckaBytes,
                                                  final byte[] certOceEckaBytes,
                                                  final AesAlg sessionKeysAlg)
            throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final byte oceKid = 0x10;
        final KeyRef oceKeyRef = new KeyRef(oceKid, sessionRef.getKvn());
        final PrivateKey skOceEcka = createEcPrivateKey(skOceEckaBytes);
        /* This is an example in case you have OCE certificate only.
         * Alternatively, you will have a chain of certificates for OCE. 
         * In that case use the following lines should be replaced with 
         * passing cert chain directly code from 'certOceEckaBytesChain'
         */
        final List<byte[]> certChain = Collections.singletonList(certOceEckaBytes);
        return new Scp11KeyParams(
                sessionRef,
                pkSdEcka,
                oceKeyRef,
                skOceEcka,
                certChain,
                sessionKeysAlg
        );
    }


    private static PrivateKey createEcPrivateKey(final byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory keyFactory = KeyFactory.getInstance("EC");
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}