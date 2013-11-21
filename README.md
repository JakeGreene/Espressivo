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

Configuration
-------------
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
