# PVM

* Depends on
  * Jackson
  * Joda time

* process engine API
  * builder api for process definitions
  * start a new process instance
  * send message to java activity so it can continue
* process definition impl beans
* process instance impl beans
* activity pluggability
* in memory process engine
  * keeps process definitions and instances in synchornized maps
* task service interface + in memory implementation (*)
* timer service interface + in memory implementation  (*)
* basic control flow activity types
  * Exclusive gateway
  * Parallel gateway
  * Start event
  * End event
  * Script task (automatic activity, based on the jvm scripting engine, no external dependencies)
  * User task (wait state, depends on our own TaskService api interface) 
* java typing system (as all is in memory)
* bindings
  * dynamic access to process variables 
  * activity configuration properties can be bound to nested values of process variable values
  * also conversion can be done
  * based on jvm javascript engine (so no external dependencies)
* json serialization of process definitions and instances


# PERSISTENCE MONGODB

* Depends on
  * PVM
  * Mongo driver
* MongoProcessEngine for storing process definitions and instances
* MongoTimerService for persistent timers
* MongoTaskService for mongo persisted tasks

# PERSISTENCE JDBC

* Depends on
  * PVM
  * http://jdbi.org/
* JDBCProcessEngine for storing process definitions and instances
* JDBCTimerService for persistent timers
* JDBCTaskService for mongo persisted tasks

(if we find contributors for this module :)

# REST

* Depends on
  * PVM
  * A nice web framework (jersey or something more fancy)
* Exposes the process engine API over REST
* Skeletons for building your own activity types, data sources and triggers, run them 
  outside the JVM and plug them into a remote process engine them over REST APIs 

# BPMN

* Depends on
  * PVM
  * TIMERS
  * TASKS
  * JAXB parser for BPMN
* Parsing & generating BPMN files
* Timer events etc

(*) Open topic: The persistence modules provide the implementations for TimerService and TaskService.  So I think it makes sense to put the interfaces in the core PVM module.
