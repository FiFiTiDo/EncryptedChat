# Encrypted Chat
This is a very simple chat system that uses a server that multiple clients can connect to.
All data transferred between the server and clients is encrypted using the DES algorithm.

There is no way to authenticate users currently and clients are assigned a UUID which is
their initial name when they connect to the server. The client can then use `/nick <name>`
to change their name to a name that isn't currently in use by another client. Though there
is a skeleton available for sending authentication messages.

The program also is currently commandline-only though it's set up for creating a GUI aspect,
see [Graphical User Interface](#Graphical-User-Interface).

## Structure
The program is divided into three separate sub-programs

* _util_ - contains all of the utility classes used by both the client and server
* _server_ - The server code that facilitates communication between clients
* _client_ - The client code that connects to the server

### Multi-threading
Within the util subprogram there exists `chat.socket.ThreadedSocket` this is used by
both the client and server to prevent the need to reuse code. This class is what handles
all of the socket communication, including encoding/decoding and converting to/from json.

The client also has a main UI thread for the GUI. Any operations that are are updating the
gui must occur on the main UI thread, to run code on the main UI thread from another thread
you can run `Platform#runLater(Runnable)` that is found within the `javafx.application` package.

### Encryption
Since, in the name, it is an encrypted chat, it uses encryption to more securely send data
between the server and clients. It uses the built-in Java Cryptography Extension (JCE) to 
which is a very simple encryption library. Currently the program uses the DES algorithm
to encrypt communications which is generally regarded as not that secure, so I'd recommend
changing it to a different algorithm. This should be rather simple by going into 
classes found in the util sub project: `chat.encryption.Encryptor` and `chat.encryption.KeyGeneratorMain` 
(Though it probably would require more than just changing the algorithm type)

### Networking
As this is a chat program, it has to use networking to communicate between the server and the
clients. All networking is handled within the __util__ subproject in the packages `util.socket`
and `util.messages`.

`util.socket` contains the classes necessary for handling a socket connection, mainly exceptions
and listeners, but most importantly `chat.socket.ThreadedSocket` which I previously mention is
used by both the server and client to simplify socket handling.

#### Messages
`util.messages` contains classes that are used for the messages that can be sent between the server 
and clients. `chat.socket.ThreadedSocket` uses a library called _gson_ to convert the object to/from
json and sends the encrypted json through the socket.

Messages all have a `command` and `data`. Best practice is to not hardcode the keys
used for the data and command, you can check out the existing messages as to how I recommend
dealing with this.

To handle messages, the `ThreadedSocket` class calls a listener called `chat.socket.OnMessageListener`
you can set this by running `ThreadedSocket#setOnMessageListener(OnMessageListener)` this will pass
the Message object and the ThreadedSocket instance (itself).

### Graphical User Interface
The _Client_ sub-project is set up to start creating a graphical user interface (GUI) for it using
the library [JavaFX](https://openjfx.io/). The layout is configured using FXML files, you can read
more about this at [Mastering FXML](https://docs.oracle.com/javase/8/javafx/fxml-tutorial/index.html).
Though this tutorial was written for Java 8, it can still help introduce you to the basics of FXML.
There is also the general JavaFX tutorial [here](https://docs.oracle.com/javase/8/javafx/get-started-tutorial/index.html)
which again was designed for Java 8 but can still be helpful for learnig more about JavaFX. There are
also various tutorials online about how to create a GUI using JavaFX.

### Database
The _Server_ sub-project is set up to start handling database operations for various future features
like possible rooms or user authentication. It is configured to use a library called [EBean](https://ebean.io/) 
([documentation](https://ebean.io/docs/)). This is a very simple databse ORM, it is not necessary to use
this library and you can remove it from gradle if you wish, I just wanted to make it easier by finding
a simple library and pre-installing it to avoid you having to deal with Gradle.

## Building

### Requirements
* [Java JDK 13](https://jdk.java.net/13/)
* [Gradle 6.1.1](https://gradle.org/install/)
* JavaFX 13 (Will be downloaded by Gradle)

I recommend using [SDKMAN!](https://sdkman.io/) for installing and managing the JDK and Gradle versions.
It greatly simplifies the installation process and handling different versions.

```
sdk install java 13.0.2.hs-adpt
sdk install gradle 6.1.1
```

### About

This project relies on gradle to make it easier to build and manage dependencies.
Since the project is divided into multiple sub-projects, each sub-project has its
own build directory.

You need to build each sub-project individually using `./gradlew <subproject>:distZip`
which will place a zip file with `<subproject>/build/distributions` that contains a
start script (for both windows and linux) in the `bin` folder and the code for the 
sub-project and all of its dependencies in the `lib` folder.

To run a distribution, just extract it and run `bin/<subproject>`

_Note: if `./gradlew` does not work, try using `gradle task`_

### Running the program

1. The Server and Client will both crash if there is no key present, to generate one
use `./gradlew util:run`, this will generate a `SECRET_KEY` file. This file can stay
where it is when running the clients and server in place. When using a jar, make sure
it is in the same directory as the jar file. __The server and clients all need the same__
__key file to be able to communicate.__

2. When running the server in-place I'd recommend using your IDE's built-in run functionality
as it can be a little bit glitchy with `./gradlew server:run`

3. Same for the client though you can try to use `./gradlew client:run`

You can also run the server/client by building a distribtion, see [_About_](#About) for more info.