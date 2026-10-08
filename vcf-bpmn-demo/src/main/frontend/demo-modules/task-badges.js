/*
 * BPMN Add-on
 *
 * Copyright (C) 2026 Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/** A sample custom module: a badge with the kind of task above every task. */

function TaskBadges(eventBus, overlays) {
  eventBus.on("shape.added", ({ element }) => {
    const kind = /^bpmn:(\w*)Task$/.exec(element.type);
    if (kind) {
      overlays.add(element, "task-badge", {
        position: { top: -10, left: 8 },
        html:
          '<div style="font: 10px sans-serif; padding: 1px 6px; border-radius: 8px;' +
          ' background: #1565c0; color: white;">' +
          (kind[1] || "Task") +
          "</div>",
      });
    }
  });
}
TaskBadges.$inject = ["eventBus", "overlays"];

const taskBadgesModule = {
  __init__: ["taskBadges"],
  taskBadges: ["type", TaskBadges],
};

// Stands in for a module that loads something first, as modules that import
// their dependencies dynamically do: the editor waits for it.
const ready = new Promise((resolve) => setTimeout(resolve, 500));

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.bpmnModules = window.Vaadin.Flow.bpmnModules || {};
window.Vaadin.Flow.bpmnModules.taskBadges = { module: taskBadgesModule, ready };
