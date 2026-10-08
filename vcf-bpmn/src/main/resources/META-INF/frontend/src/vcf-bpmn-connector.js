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

/** Connector for the BPMN add-on: installed on the element as `$connector`. */

import BpmnJsModeler from "bpmn-js/lib/Modeler";
import BpmnJsNavigatedViewer from "bpmn-js/lib/NavigatedViewer";
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule,
} from "bpmn-js-properties-panel";
import CamundaBehaviorsModule from "camunda-bpmn-js-behaviors/lib/camunda-platform";
import { CreateAppendAnythingModule } from "bpmn-js-create-append-anything";
import camundaModdleDescriptor from "camunda-bpmn-moddle/resources/camunda.json";

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};

const CHANGE_DEBOUNCE_MS = 300;

const NO_CUSTOM_MODULES = { modules: [], moddleExtensions: {} };

/**
 * Looks up the custom modules registered under `names` in
 * window.Vaadin.Flow.bpmnModules and waits for their `ready` promises.
 */
async function loadCustomModules(names) {
  const registry = window.Vaadin.Flow.bpmnModules || {};
  const loaded = { modules: [], moddleExtensions: {} };
  for (const name of names) {
    const entry = registry[name];
    if (!entry) {
      console.error(`vcf-bpmn: no custom module is registered as "${name}"`);
      continue;
    }
    try {
      await entry.ready;
    } catch (err) {
      console.error(`vcf-bpmn: custom module "${name}" failed to load`, err);
      continue;
    }
    loaded.modules.push(...[].concat(entry.module || []));
    Object.assign(loaded.moddleExtensions, entry.moddleExtensions);
  }
  return loaded;
}

/**
 * Builds a Modeler, or a NavigatedViewer when read-only, inside `element`.
 * The viewer takes the custom moddle extensions but not the custom modules.
 */
function createEditor(element, readOnly, fire, custom) {
  // Side by side, so the panel never covers the watermark (bpmn-js license).
  const canvas = document.createElement("div");
  canvas.className = "vcf-bpmn-canvas";
  const moddleExtensions = {
    camunda: camundaModdleDescriptor,
    ...custom.moddleExtensions,
  };
  let bpmnJs;
  if (readOnly) {
    element.replaceChildren(canvas);
    bpmnJs = new BpmnJsNavigatedViewer({ container: canvas, moddleExtensions });
  } else {
    const propertiesPanel = document.createElement("div");
    propertiesPanel.className = "vcf-bpmn-properties-panel";
    element.replaceChildren(canvas, propertiesPanel);
    bpmnJs = new BpmnJsModeler({
      container: canvas,
      propertiesPanel: { parent: propertiesPanel },
      additionalModules: [
        BpmnPropertiesPanelModule,
        BpmnPropertiesProviderModule,
        CamundaPlatformPropertiesProviderModule,
        CamundaBehaviorsModule,
        CreateAppendAnythingModule,
        ...custom.modules,
      ],
      moddleExtensions,
    });
  }
  const eventBus = bpmnJs.get("eventBus");

  // Refit on every resize until the user touches the canvas: at import time
  // it may have no size yet (hidden, re-attached) or still grow (a dialog).
  let autoFit = false;
  const fit = () => {
    if (canvas.clientWidth === 0 || canvas.clientHeight === 0) {
      return;
    }
    try {
      const diagramCanvas = bpmnJs.get("canvas");
      // diagram-js caches the container size.
      diagramCanvas.resized();
      // Centered, so the palette does not cover the diagram.
      diagramCanvas.zoom("fit-viewport", "auto");
    } catch (err) {
      // Not laid out yet; the next resize retries.
    }
  };
  const resizeObserver = new ResizeObserver(() => {
    if (autoFit) {
      fit();
    }
  });
  resizeObserver.observe(canvas);
  const stopAutoFit = () => {
    autoFit = false;
  };
  canvas.addEventListener("pointerdown", stopAutoFit, true);
  canvas.addEventListener("wheel", stopAutoFit, { capture: true, passive: true });

  // Sends the XML after each edit, debounced; chained so they arrive in order.
  let importing = false;
  let dirty = false;
  let changeTimer = null;
  let saving = Promise.resolve();
  const sendDiagram = () => {
    clearTimeout(changeTimer);
    changeTimer = null;
    if (!dirty) {
      return saving;
    }
    dirty = false;
    saving = saving
      .then(() => bpmnJs.saveXML({ format: true }))
      .then(({ xml }) => fire("vcf-bpmn-diagram-changed", { xml }))
      .catch((err) => {
        dirty = true;
        console.error("vcf-bpmn: could not serialize the diagram", err);
      });
    return saving;
  };
  eventBus.on("commandStack.changed", (event) => {
    // An import clears the command stack, which is not an edit.
    if (importing || event.trigger === "clear") {
      return;
    }
    dirty = true;
    clearTimeout(changeTimer);
    changeTimer = setTimeout(sendDiagram, CHANGE_DEBOUNCE_MS);
  });

  // A selected label stands for the element it belongs to.
  eventBus.on("selection.changed", ({ newSelection }) => {
    const elements = [...new Set(newSelection.map((e) => e.labelTarget || e))];
    fire("vcf-bpmn-selection-changed", {
      elements: JSON.stringify(elements.map((e) => ({ id: e.id, type: e.type }))),
    });
  });

  // Escape while editing a label or in a popup menu must not close a Dialog.
  const onEscape = (event) => {
    if (event.key !== "Escape") {
      return;
    }
    const directEditing = bpmnJs.get("directEditing", false);
    const popupMenu = bpmnJs.get("popupMenu", false);
    if (directEditing?.isActive()) {
      directEditing.cancel();
    } else if (popupMenu?.isOpen()) {
      popupMenu.close();
    } else {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
  };
  element.addEventListener("keydown", onEscape, true);

  // Flush on leaving the editor, so a save button sees the latest diagram.
  const onFocusOut = (event) => {
    if (!element.contains(event.relatedTarget)) {
      sendDiagram();
    }
  };
  element.addEventListener("focusout", onFocusOut);

  return {
    /** Imports a diagram; `notify` is false when restoring one. */
    importXml: async (xml, notify) => {
      clearTimeout(changeTimer);
      dirty = false;
      importing = true;
      let warnings;
      try {
        ({ warnings } = await bpmnJs.importXML(xml));
      } catch (err) {
        if (notify) {
          fire("vcf-bpmn-import-error", { message: err.message });
        }
        return;
      } finally {
        importing = false;
      }
      autoFit = true;
      fit();
      if (notify) {
        fire("vcf-bpmn-imported", {
          warnings: JSON.stringify(warnings.map((w) => w.message)),
        });
      }
    },

    /** Adds or removes a CSS class on an element; unknown ids are ignored. */
    setMarker: (elementId, className, on) => {
      const element = bpmnJs.get("elementRegistry").get(elementId);
      if (element) {
        const diagramCanvas = bpmnJs.get("canvas");
        if (on) {
          diagramCanvas.addMarker(element, className);
        } else {
          diagramCanvas.removeMarker(element, className);
        }
      }
    },

    /** Sends a pending edit, then resolves to the diagram, or null if none. */
    currentXml: async () => {
      await sendDiagram();
      try {
        return (await bpmnJs.saveXML({ format: true })).xml;
      } catch (err) {
        return null;
      }
    },

    destroy: () => {
      clearTimeout(changeTimer);
      resizeObserver.disconnect();
      canvas.removeEventListener("pointerdown", stopAutoFit, true);
      canvas.removeEventListener("wheel", stopAutoFit, { capture: true });
      element.removeEventListener("keydown", onEscape, true);
      element.removeEventListener("focusout", onFocusOut);
      bpmnJs.destroy();
    },
  };
}

window.Vaadin.Flow.bpmnConnector = {
  /**
   * Builds the editor on `element`; called on every attach. `customModules`
   * is a JSON array of names registered in window.Vaadin.Flow.bpmnModules.
   */
  initLazy: function (element, readOnly, customModules) {
    if (element.$connector) {
      element.$connector.destroy();
    }
    const fire = (name, detail) =>
      element.dispatchEvent(new CustomEvent(name, { detail }));
    const names = JSON.parse(customModules || "[]");
    let custom = null;
    let editor = null;
    let destroyed = false;

    // A module that breaks the editor is left out rather than taking it down.
    const build = async () => {
      custom = custom || (await loadCustomModules(names));
      if (destroyed) {
        return;
      }
      try {
        editor = createEditor(element, readOnly, fire, custom);
      } catch (err) {
        console.error("vcf-bpmn: a custom module failed, left it out", err);
        editor = createEditor(element, readOnly, fire, NO_CUSTOM_MODULES);
      }
    };

    // Calls run one after the other: building the editor and switching modes
    // are asynchronous.
    let queue = Promise.resolve();
    const enqueue = (task) => {
      queue = queue.catch(() => {}).then(() => (destroyed ? undefined : task()));
      return queue;
    };
    enqueue(build);

    // Element CSS classes, applied again after every import and mode switch.
    const markers = new Map();
    const applyMarkers = () =>
      markers.forEach((classNames, elementId) =>
        classNames.forEach((className) =>
          editor.setMarker(elementId, className, true)));

    element.$connector = {
      importXml: (xml, notify) =>
        enqueue(async () => {
          await editor.importXml(xml, notify);
          applyMarkers();
        }),

      addMarker: (elementId, className) =>
        enqueue(() => {
          if (!markers.has(elementId)) {
            markers.set(elementId, new Set());
          }
          markers.get(elementId).add(className);
          editor.setMarker(elementId, className, true);
        }),

      removeMarker: (elementId, className) =>
        enqueue(() => {
          markers.get(elementId)?.delete(className);
          editor.setMarker(elementId, className, false);
        }),

      /** Removes every element CSS class, before a new diagram is imported. */
      clearMarkers: () =>
        enqueue(() => {
          markers.forEach((classNames, elementId) =>
            classNames.forEach((className) =>
              editor.setMarker(elementId, className, false)));
          markers.clear();
        }),

      /** Rebuilds the editor in the other mode, keeping the diagram. */
      setReadOnly: (value) =>
        enqueue(async () => {
          if (value === readOnly) {
            return;
          }
          const xml = await editor.currentXml();
          editor.destroy();
          readOnly = value;
          await build();
          if (xml) {
            await editor.importXml(xml, false);
            applyMarkers();
          }
        }),

      destroy: () => {
        destroyed = true;
        editor?.destroy();
        delete element.$connector;
      },
    };
  },
};
