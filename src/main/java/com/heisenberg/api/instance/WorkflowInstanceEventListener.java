package com.heisenberg.api.instance;


import com.heisenberg.api.definition.Transition;
import com.heisenberg.impl.plugin.ControllableActivityInstance;

public interface WorkflowInstanceEventListener {
  void started(ControllableActivityInstance instance);
  void ended(ControllableActivityInstance instance);
  void transition(ControllableActivityInstance instance, Transition transition);
}
