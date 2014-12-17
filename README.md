fleetwood-bounder
=================

![Fleetwood Bounder](http://www.motorward.com/wp-content/images/2014/01/breakingbad5.jpg)

## Getting started

Run and read these tests

* src/test/java/com/heisenberg/examples/TaskTest.java
* src/test/java/com/heisenberg/examples/ScriptTest.java
* src/test/java/com/heisenberg/examples/RegisterActivityTypeSimpleExample.java

## Load setup

Create directory $HOME/.heisenberg and place a mongodb 2.6.5 installation in there
with the name  $HOME/.heisenberg/mongodb-2.6.5

Then run the ./mongodb.sharding.start.sh

Build the Heisenberg server:

./build.executable.jar.sh

Start the Heisenberg server:

java -jar target/heisenberg.jar

Run src/test/java/com/heisenberg/test/load/LoadTest.java
