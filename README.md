# Encrypted Chat
This is a very simple chat system that uses a server that multiple clients can connect to.
All data transferred between the server and clients is encrypted using the DES algorithm.

There is no way to authenticate users currently and clients are assigned a UUID which is
their initial name when they connect to the server. The client can then use `/nick <name>`
to change their name to a name that isn't currently in use by another client.

## Structure
The program is divided into three different packages within the _chat_ package.

* *main.java.chat.client* - Contains the client code
* *main.java.chat.server* - Contains all the server code
* *main.java.chat.encryption* - Contains code used both by the client and the server to handle the encryption and decryption

#### Multi-threadding
Both the server and the client use threads to ensure concurrent execution.
The server uses the main thread to accept connections and then handles all data from
the accepted client on a child thread. The client uses the main thread for listening 
from the socket and writing the data to the console and a child thread for accepting
data from the commandline and sending it to the console.

#### Encryption
This chat system uses the Java Cryptography Extension which makes it very simple to switch
to another encryption algorithm if that is desired. Currently it uses the DES algorithm
which is generally regarded as not very secure.

There is a separate main class for generating keys under the _encryption_ package.

#### Networking
This chat system uses the built-in Socket library provided by Java for connecting
the clients to the server. Currently it uses a hardcoded port of `9000` defined as a
class constant in `main.java.chat.server.Server`

## Building