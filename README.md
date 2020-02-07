# Encrypted Chat

### Table of Contents
* [About the Project](#About-the-Project)
* [Getting Started](#Getting-Started)
    * [Prerequisites](#Prerequisites)
    * [Configuration](#Configuration)
    * [Installing](#Installing)
* [Structure](#Structure)
    * [Multi-threading](#Multi-threading)
    * [Encryption](#Encryption)
    * [Networking](#Networking)
        * [Messages](#Messages)
    * [Graphical User Interface](#Graphical-User-Interface)
    * [Database](#Database)
* [Authors](#Authors)
* [License](#License)

## About the Project
This is a very simple chat system that uses a server that multiple clients can connect to.
All data transferred between the server and clients is encrypted using the DES algorithm.

There is no way to authenticate users currently and clients are assigned a UUID which is
their initial name when they connect to the server. The client can then use `/nick <name>`
to change their name to a name that isn't currently in use by another client. Though there
is a skeleton available for sending authentication messages.

The program also is currently commandline-only though it's set up for creating a GUI aspect,
see [Graphical User Interface](#Graphical-User-Interface).

## Getting Started

### Prerequisites
* [Java JDK 13](https://jdk.java.net/13/)
* [Gradle 6.1.1](https://gradle.org/install/)
* JavaFX 13 (Will be downloaded by Gradle)

I recommend using [SDKMAN!](https://sdkman.io/) for installing and managing the JDK and Gradle versions.
It greatly simplifies the installation process and handling different versions.

```
sdk install java 13.0.2.hs-adpt
sdk install gradle 6.1.1
```

### Configuration
Both the client and the server require a _SECRET_KEY_ and _config.yml_ files to be able to run.
The _config.yml_ file will be provided in the distribution zip but the _SECRET_KEY_ needs to be
given to you by the server owner or generated using the _util_ sub-project which has a main
method to generate a key file.

On Windows run `gradlew util:run`
<br>
On Linux run `./gradlew util:run`

This will generate a key file in the project directory. This file will need to be placed in the
working directory when you run the client/server.

To find out the current working directory:
* On Windows run `cd`
* On Linux run `pwd`

### Installing
You can build individual distribution zips for each of the sub-projects. A distribution zip
contains all of the required libraries, default configuration files, and a script to run the
program.

The scripts are contained in the `bin` directory.
<br>
The config files are in the root directory.
<br>
The libraries are contained in the `lib` directory.

To compile a distribution zip:
* On Windows `gradlew <subproject>:distZip`
* Onm Linux `./gradlew <subproject>:distZip`

You can find the zip file in the sub-project's build directory `<subproject>/build/distributions`
<br>
You can then extract it and run `bin/<subproject>` to start the project.

Example:
```
~/EncryptedChat> ./gradlew client:distZip
...

BUILD SUCCESSFUL in 10s
8 actionable tasks: 8 executes
~/EncryptedChat> cd client/build/distributions
~/EncryptedChat/client/build/distributions> unzip client-0.1.2.zip
...
~/EncryptedChat/client/build/distributions>bin/client
```

__If desired you can also run the sub-projects without compiling into a distribution zip__
__but I recommend not using the built in gradle run tasks as it can be glitchy due to the__
__infinite loops that the project uses. Use your IDE's built in main method run functionality instead.__

## Structure
The project is divided into three separate sub-project

* _util_ - contains all of the utility classes used by both the client and server
* _server_ - The server code that facilitates communication between clients
* _client_ - The client code that connects to the server

### Multi threading
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
classes found in the util sub project: `chat.encryption.CryptoManager` and `chat.encryption.KeyGeneratorMain` 
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

## Authors

* Evan Fiordeliso - _Initial Work_ - [FiFiTiDo](https://github.com/FiFiTiDo)

See also the list of [contributors](https://github.com/FiFiTiDo/EncryptedChat/contributors) who participated in this project.


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details