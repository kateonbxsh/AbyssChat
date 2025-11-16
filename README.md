# AbyssChat : Decentralized Chat System

AbyssChat is a decentralized LAN Chat System running under Java.

## Design

### Goal

The goal of this project is to make a decentralized LAN chat system
where users are able to choose usernames, chat to each other, and
discover each other using a contact list.

![progress bar](progressbar.png)

## How to run

### Pre-requisites

Before proceeding, make sure you have:
- Java 21+
- Maven 3+

installed.

### Compilation

To compile the project, from the project directory, run:
```bash
mvn compile
```

### Running 

To run the app, from the project directory, run:
```bash
mvn exec:java -D exec.mainClass="net.chatsystem.Client"
```

If you require to try out the app locally on the same computer (up to two clients), you must run the app with the following arguments

For the first client
```bash
mvn exec:java -D exec.mainClass="net.chatsystem.Client" -D exec.args="--local1"
```

For the second client
```bash
mvn exec:java -D exec.mainClass="net.chatsystem.Client" -D exec.args="--local2"
```
What this will do is set the sending and receiving ports to different values, and cross them for the two apps (local1 receive port is local2 send port, and vice versa)

> [!IMPORTANT]  
> In both cases, make sure ports 2050, 2051 and 2052 are open and free

### Unit tests 

To run unit tests, from the project directory, run:
```bash
mvn test
```

### App specification

- After running, input your username to login.
- Then use one of these commands

| Command           | Description                       |
|-------------------|-----------------------------------|
| `/help`           | Show list of available commands   |
| `/contacts`       | Show your contact list            |
| `/me`             | Show your profile                 |
| `/changeusername` | Self-explanatory                  |
| `/disconnect`     | Disconnect from the chat          |

- You may also close the chat directly, it will automatically let everyone know you disconnected



