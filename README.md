# SCP03 and SCP11 protocols implementation for off-card entity

## General information

### Description

The library provides support for secure communication between off-card entity (OCE) and Security Domain (SD)
using GlobalPlatform's (GP) Secure Channel Protocols that are based on static symmetric keys (SCP03) and certificates (SCP11)

### Specifications

The feature set is according to the specifications listed below except for what is mentioned in known issues:

- [SCP03](https://globalplatform.org/specs-library/secure-channel-protocol-03-amendment-d-v1-2/) up to v1.2
- [SCP11](https://globalplatform.org/specs-library/secure-channel-protocol-11-amendment-f/) up to v1.4

### API documentation

Please refer to the API documentation in the "docs/javadoc" directory for details

### Known issues

#### SCP03 not implemented features

- Lower security levels support - only maximum security level is supported now (C-DECRYPTION, R-ENCRYPTION, C-MAC, and R-MAC)
- Pseudo-random card challenge verification - verification is optional according to the specification
- BEGIN R-MAC session & END R-MAC session commands - implementation is optional according to the specification

#### SCP11 not implemented features

- SD certificate validation
- Usage of CA-KLCC Identifier in GET_DATA (Certificate Store), MUTUAL AUTHENTICATE.
  - For now, only "KID/KVN" is used
  - "CA-KLCC Identifier" and "KID/KVN" usage is mutually exclusive and shall be chosen by OCE
- Usage of Host and Card ID in Key Derivation process
  - For now, it is not used
  - HostID usage is chosen by OCE during MUTUAL AUTHENTICATE / INTERNAL AUTHENTICATE
- Lower security levels support - only maximum security level is supported now (C-DECRYPTION, R-ENCRYPTION, C-MAC, and R-MAC)
  - Key usage is chosen by OCE during MUTUAL AUTHENTICATE / INTERNAL AUTHENTICATE
- Some library exceptions might be not descriptive enough

***

## Environment information

### Requirements

- Java 8
- JCA cryptographic service [provider](https://docs.oracle.com/javase/8/docs/api/java/security/Provider.html) implementation that supports the following algorithms:
  - RSA
  - EC
  - ECDH
  - SHA256
  - AES-CMAC
  - AES-ECB
  - AES-CBC
  - X.509
  
If a default crypto provider from your JDK doesn't support needed algorithms, an external provider implementation can be used.
Here is an example of the Bouncy Castle provider usage in the Gradle project:
```java
dependencies {
  ...
  implementation 'org.bouncycastle:bcpkix-jdk15to18:1.78.1'
}
```
```java
import org.bouncycastle.jce.provider.BouncyCastleProvider;
...
final SecurityDomainSession session = new SecurityDomainSession(connection, new BouncyCastleProvider());
```

### Dependencies

- Simple Logging Facade for Java ([SLF4J](https://www.slf4j.org/))

Note that logging implementation should be also added as a dependency to avoid the "logger not set" warnings.

SLF4J include example using 'slf4j-simple' (other slf4j loggers are also acceptable):

```gradle
dependencies {
    ...
    implementation group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.29'
    implementation group: 'org.slf4j', name: 'slf4j-simple', version: '2.0.16'
}
```

***

## Example of usage

### SCP03 implementation guide
Below is step-by-step explanation of how to use SCP03 protocol to communicate with a smart card.
The similar approach can be used for SCP11.
Also please refer to SCP Reader class example at **examples/Scp03ReaderTemplate.java** file

#### Define SCP03 keys

Declare static SCP03 keys:

``` java
byte[] encKey = {...};
byte[] macKey = {...};
byte[] dekKey = {...};
StaticKeys staticKeys = new StaticKeys(encKey, macKey, dekKey);
```

Declare reference to these keys on the smart card:

``` java
byte keyId = (byte) 0x01;
byte keyVersionNumber = (byte) 0x30;
KeyRef keyRef = new KeyRef(keyId, keyVersionNumber);
```

Declare SCP03 key parameters from these values:

``` java
Scp03KeyParams keyParams = new Scp03KeyParams(keyRef, staticKeys);
```

#### Define connection class to smart card

Implement SmartCardConnection interface using your connection to the smart card:

``` java
class MySmartCardConnection implements SmartCardConnection {
    @Override
    public byte[] sendAndReceive(byte[] apdu) {
        // Use your physical channel to the smart card
        return rapdu;
    }

    @Override
    public boolean isExtendedLengthApduSupported() {
        // Return your smart card property
        return true;
    }

    @Override
    public void close() {
        // Close your physical chanel to the smart card
    }
}
```

See the SmartCardConnection JavaDocs for additional information.

#### Create and use SCP03 session

Set SCP03 mode (S8 or S16) and initialize SCP03 protocol using variables declared above:

``` java
SecurityDomainSession session = new SecurityDomainSession(new MySmartCardConnection());
session.authenticate(keyParams, ScpMode.S8);
```

Transmit APDUs:

``` java
// GlobalPlatform Card Specification, "11.4 GET STATUS Command"
Apdu getStatusCapdu = new Apdu(
    (byte) 0x80,  // CLA
    (byte) 0xF2,  // INS
    (byte) 0x40,  // P1 - list applets or security domains
    (byte) 0x00,  // P2
    new byte[] {(byte) 0x4F, (byte) 0x00)});  // data - search qualifier: all IDs
byte[] rapduData = session.sendAndReceive(getStatusCapdu);
```

### SCP11 implementation guide

Below is step-by-step explanation of how to use SCP11 protocol to communicate with a smart card.
Also please refer to SCP Reader class example at **examples/Scp11ReaderTemplate.java** file

#### Define SCP11 credentials

Declare OCE SCP11 encoded certificates chain and private key

``` java
List<byte[]> certChainOceEcka = ...;
PrivateKey skOceEcka = ...;
```

Declare AES algorithm for session keys that will be generated:
``` java
AesAlg sessionKeysAlg = AesAlg.AES_256;
```

Declare reference to SD and OCE keys on the smart card:

``` java
byte sessionKeyId = (byte) 0x11;
byte sessionKeyVersionNumber = (byte) 0x03;
KeyRef sessionKeyRef = new KeyRef(keyId, sessionKeyVersionNumber);

byte oceKeyId = (byte) 0x10;
KeyRef oceKeyRef = new KeyRef(oceKeyId, sessionKeyRef.getKvn());
```

#### Define connection class with SCP11 to smart card

Please see the corresponding section in the SCP03 guide above

#### Create and use SCP11 session

Get SD certificate's public key from the smart card:

``` java
SecurityDomainSession session = new SecurityDomainSession(new MySmartCardConnection());
List<ScpCertificate> sdCertChain = session.getCertificateBundle(sessionKeyRef);
int eckaCertPosition = sdCertChain.size() - 1;
PublicKey pkSdEcka = sdCertChain.get(eckaCertPosition).getPublicKey();
```

Declare SCP11 key parameters from these values:

``` java
Scp11KeyParams keyParams = 
  new Scp11KeyParams(sessionKeyRef, pkSdEcka, oceKeyRef, skOceEcka, certChainOceEcka, sessionKeysAlg);
```

Set SCP03 mode (S8 or S16) for session keys and initialize SCP11 protocol using variables declared above:

``` java
session.authenticate(keyParams, ScpMode.S8);
```

See APDU transmission example in the corresponding section of the SCP03 guide above.
