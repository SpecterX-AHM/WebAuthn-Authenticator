# WebAuthn Authenticator 
**WebAuthn** is a web authentication standard approved in March, 2019 by the **[World Wide Web Consortium (W3C)](https://www.w3.org/Consortium/)** making it easy for websites, services, and applications to offer robust 
authentication without relying on passwords as it allows users to be authenticated using [public-key cryptography](https://en.wikipedia.org/wiki/Public-key_cryptography). 

Most websites, services, and applications have difficulty providing secure and convenient authentication for users. 
Passwords are the problem as they tend to be either so simple that they are easily guessed by hackers or so complex 
that they are hard for users to remember. And afterall, passwords, regardless of their complexity, are vulnerable to 
phishing and data breaches.  

The fact that a password can be a shared secret makes it vulnerable. We don't see this weakness in public-key 
cryptography. The **Web Authentication (WebAuthn) API** is a specification written by W3C and FIDO, with the 
participation of Google, Yubico, Microsoft, Mozilla, and others. This API allows users to be authenticated using 
public-key cryptography. 

WebAuthn is the only web authentication method that is phishing resistant and its also implemented across a variety 
of browsers. See [supported browsers](https://caniuse.com/?search=webauthn). A WebAuthn authenticator allows you to 
authenticate using two types of authenticators: 
- **Platform authenticators**: are attached to a specific device and only work on them. For e.x., Windows Hello, 
  Macbook's TouchBar, iOS Touch/FaceID and Android's fingerprint scanner, etc. 
- **Roaming authenticators**: are cross-platform authenticators and are removable, can be used on multiple devices 
  using USB, NFC or Bluetooth. For e.x., Yubikey.

## modules
- **webauthn-server-core**: Server-side WebAuthn library for Java. See [JavaDoc](https://developers.yubico.com/java-webauthn-server/JavaDoc/webauthn-server-core-minimal/latest/com/yubico/webauthn/package-summary.html).
- **webauthn-server-attestation**: Simplified implementation of [MetadataService](https://developers.yubico.com/java-webauthn-server/JavaDoc/webauthn-server-core-minimal/latest/com/yubico/webauthn/attestation/MetadataService.html).  
- **yubico-util**: contains some utility classes, not part of the Public API.  
- **webauthn-authenticator**: Contains the implementation of WebAuthn authenticator. 

## build and run  
You can use the already included [Gradle Wrapper v7.2](https://docs.gradle.org/current/userguide/gradle_wrapper.html) 
for 
running the WebAuthn Authenticator.  
```  
$ git clone git@github.com:SpecterX-AHM/WebAuthn-Authenticator.git 
$ cd WebAuthn-Authenticator 
```    

Make sure to use [jdk-11.0.13](https://www.oracle.com/java/technologies/downloads/#java11-windows).


To build 
```
$ ./gradlew build  
```    

To run
```
$ ./gradlew run 
```    
This will serve the application at https://localhost:8888.

To generate a war 
```
$ ./gradlew war 
```  





Some references and gratitude to:  
- [Web Authentication:
  An API for accessing Public Key Credentials](https://www.w3.org/TR/webauthn/).
- [java-webauthn-server](https://github.com/Yubico/java-webauthn-server).  


