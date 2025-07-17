/* Specify your package name here */
package com.module_name.package_name;

import com.samsung.openscp.Apdu;
import com.samsung.openscp.ApduException;
import com.samsung.openscp.BadResponseException;
import com.samsung.openscp.KeyRef;
import com.samsung.openscp.Scp03KeyParams;
import com.samsung.openscp.ScpMode;
import com.samsung.openscp.SecurityDomainSession;
import com.samsung.openscp.SmartCardConnection;
import com.samsung.openscp.StaticKeys;

import java.io.IOException;


public class Scp03ReaderTemplate {

    private final SecurityDomainSession session;
    private final byte[] enc;
    private final byte[] mac;
    private final byte[] dek;

    public Scp03ReaderTemplate(byte[] enc, byte[] mac, byte[] dek) {
        this.enc = enc;
        this.mac = mac;
        this.dek = dek;
        final SmartCardConnection connection = new Connection();
        session = new SecurityDomainSession(connection);
    }


    public void open(final byte[] aid) throws Exception {
        /*
        Add here code to open eSE Reader class session with specified 'aid'        
         */

        final byte keyId = (byte) 0x01;
        final byte keyVersionNumber = (byte) 0x30;
        final KeyRef keyRef = new KeyRef(keyId, keyVersionNumber);
        final StaticKeys staticKeys = new StaticKeys(enc, mac, dek);
        final Scp03KeyParams keyParams = new Scp03KeyParams(keyRef, staticKeys);
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
            Scp03ReaderTemplate.this.close();
        }
    }
}