# Doodlr
## Introduction
Imagine if you can chat and draw with others in Mxcrxsxft Pxxnt. Yes, that's basically what I am doing.

## Running this locally
1. Clone this repository / download code as zip
2. Modify `akka.actor.remote.artery.canonical.hostname`'s value in `src/main/resources/com/doodlr/view/configs/client-config.conf` 
according to the local IP of your device
3. Modify `akka.actor.remote.artery.canonical.hostname`'s value in `src/main/resources/com/doodlr/view/configs/server-config.conf`
   according to the local IP of your device
4. In `src/main/scala/com/doodlr/ClientMain` line `54`,
    ````scala
   val address = akka.actor.Address("akka",
       "DoodlrSystem",
       "192.168.100.5", // replace this line only with the IP address of your server
       2222
   )
   ````
5. Run `ServerMain`
6. Run `ClientMain`

You might have to run at least 2 instances of `ClientMain` to the interactions of clients in action.

Make sure all devices running your clients and server are under the same network.

## About running multiple instances of clients (ClientMain)
1. If you wish to have 1 client instance each on multiple devices
   1. You have to download this code on multiple devices
   2. You have to change `akka.actor.remote.artery.canonical.hostname` of `client-config.conf` on all your client devices, according to their local IP
2. If you wish to have multiple client instances on 1 device
   1. You have to change `akka.actor.remote.artery.canonical.hostname` of `client-config.conf` on your client device, according to its local IP

## About running multiple instances of servers (ServerMain)
Same process. But I don't think there is need for that, as the server is only used for peer discovery of clients, and clients interact with each other in a P2P manner. Imagine it it being torrent, `ServerMain` is basically only acting as the tracker.

## About bundling
into `.jar`s, `.exe`s, `.app`, and all kinds of stuff.

Since you are required to configure IP addresses with code in this dev setting, we do not *JARify* it. \
If you wish to deploy it, feel free to pre-configure the configurations according to your needs, bundle it into `server.jar` and `client.jar` or any equivalent, and run each accordingly.
