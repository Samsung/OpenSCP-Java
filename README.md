# SCP03 and SCP11 protocols implementation for off-card entity

## Description

The library provides support for secure communication between off-card entity (OCE) and Security Domain (SD)
using GlobalPlatform's (GP) Secure Channel Protocols that are based on static symmetric keys (SCP03) and certificates (SCP11)

## Specifications

The feature set is according to the specifications listed below except for what is mentioned in known issues:

- [SCP03](https://globalplatform.org/specs-library/secure-channel-protocol-03-amendment-d-v1-2/) up to v1.2
- [SCP11](https://globalplatform.org/specs-library/secure-channel-protocol-11-amendment-f/) up to v1.4

## Documentation

- [API](docs/javadoc)
- [Environment information](/Samsung/OpenSCP-Java/wiki/Environment)
- [Usage example](/Samsung/OpenSCP-Java/wiki/Usage-example)

## Known issues

### SCP03 not implemented features

- Lower security levels support - only maximum security level is supported now (C-DECRYPTION, R-ENCRYPTION, C-MAC, and R-MAC)
- Pseudo-random card challenge verification - verification is optional according to the specification
- BEGIN R-MAC session & END R-MAC session commands - implementation is optional according to the specification

### SCP11 not implemented features

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
