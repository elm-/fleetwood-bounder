# Topics

- [ ] Rename process to workflow. Consider removing 'Definition' from all definition entities. So that the process builder code looks nicer.
- [ ] Is the API nicely split from the impl.  SPIs make it hard.  Where should it be improved? 
- [ ] Is script engine thread safe?
- [ ] In the data types, InvalidValueException vs ParseContext

# Tasks

- [ ] Move dataType and id as parameters into newActivity.  Move all required parameters in the constructor.
- [ ] Figure out how to secure java script for our own servers:  Check out Rhino's SandboxShutter
- [ ] Test if the script engine is thread safe. CompiledScript seems to be tied to a ScriptEngine. It should be investigated if concurrent script execution can overwrite each other's context.
- [ ] Move activity types to implementation and replace it with builder 
      methods in the ProcessDefintionBuilder similar to the ProcessDefinitionBuilder.newDataTypeXxx methods

# In progress

- [x] simple api 
  - [x] easy to use fluent api
  - [x] easy programmable creation and deployment of process models
  - [x] jackson json support for process engine interface
- [x] process parsing with error & warning reporting
  - [x] location support (java & file) 
  - [x] prepared to add i18n on top
- [x] easy plugin architecture
  - [x] programmable registration of pluggable types
  - [x] service loading of pluggable types
  - [x] activity types
     - [x] user defined activity types
     - [x] engine level user defined activity types
     - [x] inline defined and configured activity types
  - [x] data types
     - [x] user defined data types
     - [x] process level configured data types
     - [x] engine level configured data types
     - [x] inline defined and configured data types
     - [ ] default types like text, ... 
     - [ ] reference type
  - [ ] data sources
     - [ ] user defined object types (without beans) 
  - [ ] script functions
- [x] activity in / output parameters
  - [x] static value 
  - [x] variable binding 
  - [x] expression binding in any script language 
- [x] process execution
  - [x] easy to understand activity instance model
  - [x] support for BPMN default semantics
  - [x] synchonous and asynchronoux execution of activities
  - [ ] activity worker pattern
- [x] pluggable persistence architecture 
- [x] transient execution context variables
- [x] mongodb persistence
  - [x] mongodb mapping
  - [x] mongodb clustering

# Roadmap

- [ ] Load testing
- [ ] Pluggable task service
- [ ] Create an archivedActivityInstances collection in ScopeInstanceImpl  This way we can just flush the whole activity instances field if something was changed.
- [ ] Extend activity worker patter scalability
      We start with one scalable job executor.  But that is one scale for all jobs.
      When we add the ability to dedicate certain job executors to specific activities, then 
      we are on par with SWF
- [ ] BPMN serialization and parsing
  - [ ] BPMN process logic coverage
- [ ] Timers
- [ ] change ProcessEngine.startProcessInstance return value into StartProcessInstanceResponse
  - [ ] include process instance full state (as is returned now)
  - [ ] add all (or some) process events as a kind of logs
- [ ] Activity types
  - [ ] HTTP invocation
  - [ ] Send email
  - [ ] Remote implemented activity (http)
  - [x] Script (through ScriptEngine)
- [ ] Data flow (only start an activity when the input data becomes available)
- [ ] Static persistable process variables
- [ ] Derived variables
- [ ] Allow for easy collection of process instance logs to track what has happened
- [ ] Process debugger service (separate top level interface required)
  - [ ] based on the in memory process engine
  - [ ] add breakpoints
  - [ ] ensure all async work is executed synchronous 
- [ ] Cassandra persistence ?

# Design principles

* Minimal library dependencies
