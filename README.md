# TokenD Android authenticator
TokenD Authenticator increases level of your account security. 
It stores your private keys on the device and allows you to grant and revoke an access to your 
accounts for other applications.

## Motivation
Despite TokenD uses a public-key cryptography, security of the system can be compromised 
while keys are being derived from a password. Users are comfortable having one password for many 
services so the risk of password comprometation is high. Attacker with a database of stolen 
email-password pairs can go over it making requests to the keyserver and get access to some accounts.
To get rid of passwords key storage should be moved to the client side. 
Keypair of the account can be stored on a user’s mobile device using special app. 
Then to access user’s account client application must generate a keypair and ask 
user to authorize it using stored keypair.

## How it works
This app securely stores keypairs of user's accounts. In case if some app would like to get access
to the account it generates it's own keypair and asks user to grant access for it's public key.
Authenticator then creates a transaction which grants access to the account for given public key 
by adding it as an [new signer](https://tokend.gitbook.io/knowledge-base/technical-details/key-entities/signer) 
and signs the transaction with stored keypair.

![Auth flow sequence chart](https://docs.google.com/uc?export=download&id=1lOqTB3IQ19ULT0fiJ78qC0prYGW0baRQ "Auth flow")

## Security overview
### Key storage
TokenD Authenticator uses modern encryption and key derivation algorithms in order to provide a 
maximum level of keys storage security.

* Account keypair is defined by ACCOUNT PRIVATE KEY on Ed25519 curve;
* ACCOUNT PRIVATE KEY is a 256-bit key encrypted with ACCOUNT KEY;
* ACCOUNT KEY is a 256-bit key derived from the MASTER KEY using account-specific KDF attributes;
* MASTER KEY is a randomly generated 256-bit key encrypted with MASTER KEY ENCRYPTION KEY;
* MASTER KEY ENCRYPTION KEY is a 256-bit key derived from USER KEY using master KDF attributes;
* USER KEY is a PIN code or a password entered by user.

```
(USER KEY, MASTER KDF) --scrypt-> MASTER ENCRYPTION KEY
(MASTER ENCRYPTION KEY, ENCRYPTED MASTER KEY) --AES-256-> MASTER KEY
(MASTER KEY, ACCOUNT KEY KDF) --scrypt-> ACCOUNT KEY
(ACCOUNT KEY, ENCRYPTED ACCOUNT PRIVATE KEY) --AES-256-> ACCOUNT PRIVATE KEY
(ACCOUNT PRIVATE KEY) --Ed25519-> ACCOUNT KEYPAIR
```

For scrypt KDF we use the following params: N=16384, R=8, P=1.

### Application
By default Android provides enough level of security for applications. 
We can safely store PIN code in the Android KeyStore for Fingerprint auth 
and store decrypted master key in memory to improve performance and usability. 
To avoid PIN bruteforcing we use timeouts.

On rooted devices we don't store any keys in memory or in the Android KeyStore.
We also use password as a user key instead of PIN code to make off-device bruteforcing inefficient. 

## Credits
⛏ <a href="https://distributedlab.com/" target="_blank">Distributed Lab</a>, 2018