# CoolWallet Android app

CoolWallet Android app connects with the CoolWallet (a wireless Bitcoin cold storage hardware device) and makes commands via Bluetooth Low Energy. This app uses [blockr.io](http://blockr.io/documentation/api) and [blockchain.info](https://blockchain.info/api) APIs to get account balances, transaction histories and broadcast transactions to the Bitcoin network.

# Features

- BIP 32 HD wallet
- Used addresses coloured grey, unused addresses coloured white
- Set security policies for CoolWallet
- Sync balance with the blockchain to set card display
- HD wallet recovery
- Send recipient's address and amount from app to CoolWallet for signing
- Receive signed transaction from CoolWallet to broadcast to the Bitcoin network
- Transaction history lists
- Enter OTP shown on CoolWallet display and send it back for verification
- Generate address QR code and request amount
- Notifications for receiving bitcoins and device connection

# Prerequisite

***Get the right tools:***

- Install Android studio

- Install a Java Development Kit (JDK) Version 6 or later.

- Install Android SDK.

- Select and install a set of packages and platforms from the Android SDK. 

  ***Note:*** The SDK platform must be Android API level 18 or above.

- Add the Android Development Tools (ADT) plug-in to android studio.



#Bluetooth API

Please see [this document](https://github.com/CoolBitX-Technology/coolwallet-ios/blob/master/docs/CW-SPEC-0002-se_spi_apdu_spec_v0110.pdf) with specifications for commands and responses.

***Note:*** 
As Bluetooth is required to communicate with the CoolWallet, testing can only be done on a real device (Android 4.3 or later).
