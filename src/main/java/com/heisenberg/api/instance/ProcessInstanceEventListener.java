package com.heisenberg.api.instance;

import com.heisenberg.api.builder.ProcessInstanceQuery;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.plugin.activities.ControllableActivityInstance;

public interface ProcessInstanceEventListener {
  void started(ControllableActivityInstance instance);
  void ended(ControllableActivityInstance instance);
  void transition(ControllableActivityInstance instance, TransitionDefinition transition);
}
