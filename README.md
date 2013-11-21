Espressivo (Alpha)
==========
Espressivo is an open-source music player which can be controlled by any device
with access to your home network and the ability to issue HTTP commands.

Espressivo users control the music being played by adding their choice of
songs to the stream: a continuously running playlist. The administrator 
is the only user with the ability to start and stop the stream. This way,
everyone at a party, eating at a restaurant, or relaxing at home can
control the music being played without interfering with one-another and without
even standing up.

Using Espressivo
-----------------
Espressivo is a server with an HTTP/RESTful interface; clients use the GET and PUT 
HTTP methods in order to read and change the state of the music player.

The following command show how to control an Espressivo server from a shell. This is
obviously not the greatest way for a typical user but it does highlight the server's
functionality.

To view the list of songs available
```
curl -X GET [HOST]:[PORT]/songs

[{
  "id": 0,
  "name": "Song Name",
  "artist": "Artist",
  "album": "Album"
}, {
  "id": 1,
  "name": "Other Song Name",
  "artist": "Artist",
  "album": "Album"
}]
```
To see the meta-data for a specific song
```
curl -X GET [HOST]:[PORT]/songs/[ID]

{
  "id": [ID],
  "name": "Song Name",
  "artist": "Artist",
  "album": "Album"
}   
```
The stream is the playlist that everyone has access to. It has a list of songs 
that need to be played, a currently playing song, the current last song on the list,
and the current state of the stream {Ready, Active, Waiting, Suspended}
```
curl -X GET [HOST]:[PORT]/stream

{
  "songs": [3, 2],
  "current": 1,
  "last": 2,
  "state": "Active"
}
```
In order to add a song to the stream (which appends it to the end)
```
curl -X PUT -H "Content-Type: application/json" -d '{ "id": [ID] }' [HOST]:[PORT]/stream/last
```
To change the state of the stream so that it is playing or paused
```
curl -X PUT -H "Content-Type: application/json" -d '{ "state": "Active" }' [HOST]:[PORT]/stream/state
curl -X PUT -H "Content-Type: application/json" -d '{ "state": "Suspended" }' [HOST]:[PORT]/stream/state
```
and that is it!

Installation and Configuration
-------------------------------
Espressivo is currently in alpha; many of the promised features have yet to
be implemented and the configuration process is devoid of polish. That said,
feel free to try out what is available.

To install and configure Espressivo, you must first clone the repository
```
cd $WORKSPACE
git clone git@github.com:JakeGreene/Espressivo.git
cd Espressivo
```
Then use [sbt-assembly](https://github.com/sbt/sbt-assembly) to build
the espressivo.jar
```
sbt assembly
cd target/scala-2.10
ls # should reveal the espressivo JAR
```
Move this jar to its new home and create its configuration file
```
cp espressivo.jar $ESPRESSIVO_HOME
cd $ESPRESSIVO_HOME
touch app.conf
```
Espressivo uses a [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) configuration file for easy configuration.
Using your editor of choice, fill in app.conf with the following (replacing values where required)
```
espressivo {
  music {
    root = "music/root/directory"
  }
  server {
    host = "192.168.your.ip"
    port = "8080"
  }
}
```
All set. Now just run espressivo and it can be used by anyone!
```
java -jar espressivo.jar app.conf
```
