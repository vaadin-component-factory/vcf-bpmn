# BPMN Add-on for Vaadin Flow

Vaadin Flow integration of [bpmn-js](https://bpmn.io/toolkit/bpmn-js/): a BPMN 2.0 modeler with a Camunda 7 properties panel.

This component is part of Vaadin Component Factory.

* [Vaadin Directory](https://vaadin.com/directory/component/bpmn-add-on)
* [Online demo](https://vcf-demos.org/v25/bpmn/)

## Features

* BPMN 2.0 editor (bpmn-js Modeler) with the properties panel on the right
* Camunda 7 properties and `camunda:*` attributes, read and written without loss
* Every BPMN element available from the palette and the context pad (create-append-anything)
* Load a diagram from Java and read the edited diagram back, with import success and error events
* Change notification on every edit
* Selection notification with the id and type of the selected elements, single or multiple
* Read-only mode (pan, zoom and select only), set at construction time or switched at runtime
* CSS classes on diagram elements, to style them from the application; kept across mode switches and cleared when a new diagram is loaded
* Custom bpmn-js modules, registered in JavaScript and enabled from Java at construction time

## Compatibility

| Add-on version | Vaadin | Java |
| --- | --- | --- |
| 1.0.0 | 25 (built against 25.3.0) | 21 |

The add-on ships the following npm packages:

| Package | Version |
| --- | --- |
| bpmn-js | 18.30.1 |
| diagram-js | 15.27.1 |
| bpmn-js-properties-panel | 5.65.1 |
| @bpmn-io/properties-panel | 3.55.0 |
| camunda-bpmn-moddle | 8.0.1 |
| camunda-bpmn-js-behaviors | 1.18.0 |
| bpmn-js-create-append-anything | 2.1.0 |

Diagrams are edited for Camunda 7. Camunda 8 (Zeebe) is not supported.

## How it works

bpmn-js is a JavaScript library that runs in the browser: it draws the
diagram, the palette and the properties panel, and reads and writes BPMN 2.0
XML. `BpmnModeler` makes it available as a regular Vaadin Flow component, so
an application only writes Java.

The add-on:

* **Bundles the npm packages.** bpmn-js, the properties panel, the Camunda 7
  moddle and behaviors, and create-append-anything are declared on
  `BpmnModeler`, so adding the Maven dependency is enough; Vaadin installs and
  bundles them.
* **Configures the editor.** The connector creates the bpmn-js Modeler with
  the properties panel on the right, Camunda 7 properties and behaviors, and
  every BPMN element in the palette. Camunda 8 (Zeebe) is not included.
* **Connects Java and the browser.** `setDiagramXml` imports a diagram in the
  browser, and the outcome comes back as a `DiagramImportedEvent` or a
  `DiagramImportErrorEvent`. After every edit the browser sends the diagram to
  the server, where `getDiagramXml` returns it and a `DiagramChangedEvent` is
  fired.
* **Handles the integration details.** The diagram is fitted once the editor
  has a size, also when it is loaded while the editor is hidden or while a
  dialog around it opens. The editor is rebuilt, edits included, when it is
  detached and attached again. Escape cancels a label edit or closes a popup
  menu without closing a surrounding `Dialog`.

The diagram is rendered in the page's regular DOM, not in a shadow root, so
the bpmn-js stylesheets and the application's own CSS apply to it.

## Running the component demo
Run from the command line:
- `mvn install`
- `mvn -pl vcf-bpmn-demo jetty:run`

Then navigate to `http://localhost:8080/`

The first start takes a few minutes while Vaadin installs the npm packages and
builds the frontend bundle.

The demo has seven pages, each with the steps to try and what to expect:

* **Basic modeler**: loading a diagram from Java, reading the edited one back, and the import and change events.
* **Read-only mode**: a modeler created read-only, switched between viewing and editing.
* **CSS classes**: diagram elements colored through CSS classes added from Java.
* **Custom modules**: a sample bpmn-js module that adds a badge to every task.
* **Modeler in a dialog**: editing a saved diagram in a dialog, with Save and Cancel.
* **Preserve on refresh**: the diagram, edits included, survives a browser refresh in a `@PreserveOnRefresh` view.
* **Camunda 7 properties**: Camunda 7 settings shown in the properties panel and written to the XML.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Profiles
### Profile "directory"
This profile, when enabled, will create the zip file for uploading to Vaadin's directory

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>org.vaadin.addons.componentfactory</groupId>
    <artifactId>vcf-bpmn</artifactId>
    <version>${component.version}</version>
</dependency>
```

## How to Use
```java
BpmnModeler modeler = new BpmnModeler();
modeler.setSizeFull();

modeler.addDiagramImportedListener(event -> {
    if (!event.getWarnings().isEmpty()) {
        Notification.show("Imported with warnings: " + event.getWarnings());
    }
});
modeler.addDiagramImportErrorListener(
        event -> Notification.show("Import failed: " + event.getMessage()));

// Fired after every edit made in the browser
modeler.addDiagramChangedListener(event -> {
    String xml = modeler.getDiagramXml(); // already includes the edit
});

modeler.setDiagramXml(bpmnXml);

Button save = new Button("Save", e -> repository.save(modeler.getDiagramXml()));
```

`setDiagramXml` imports the diagram in the browser, so its outcome arrives as a
`DiagramImportedEvent` or a `DiagramImportErrorEvent`. `getDiagramXml` returns
the diagram as the browser shows it: the browser sends it after each edit, and
right away when the focus leaves the editor, so a save button next to the
editor always gets the latest version.

By default the modeler is 100% wide and 600px high; `setWidth`, `setHeight` or
`setSizeFull` change that. The properties panel scrolls within that height.

### Selection

```java
modeler.addSelectionChangedListener(event -> {
    String id = event.getElementId();     // "Task_1", or null when nothing is selected
    String type = event.getElementType(); // "bpmn:UserTask", or null
    event.getSelectedElements();          // every selected element, for multiple selection
});
```

Selecting a label selects the element it belongs to. Selection works in
editing and read-only mode.

### Read-only mode

```java
BpmnModeler viewer = new BpmnModeler(true); // starts read-only
viewer.setReadOnly(false);                  // switches to editing at runtime
```

In read-only mode the diagram can be panned, zoomed and selected, but not
edited: there is no palette and no properties panel. Switching modes keeps the
diagram and any pending edit.

### Styling diagram elements

`addElementClassName` adds a CSS class to the SVG of a diagram element, found
by its id; the application styles it with its own stylesheet:

```java
@CssImport("./styles/bpmn-status.css")
public class ProcessView extends VerticalLayout {
    ...
    modeler.setDiagramXml(bpmnXml);
    modeler.addElementClassName("Task_1", "bpmn-status-failed");
}
```

```css
/* bpmn-js sets fill and stroke inline, hence !important. */
.djs-element.bpmn-status-failed .djs-visual > :first-child {
  stroke: #c62828 !important;
  fill: #ffebee !important;
}
```

Classes are kept when switching between editing and read-only mode and when
the component is attached again. `setDiagramXml` clears them, so add them after
setting the diagram. `removeElementClassName` removes a class again.

### Custom modules

Custom bpmn-js modules are registered under a name in a JavaScript file of the
application, and enabled by that name when the modeler is created:

```js
// frontend/bpmn-modules.js: one file for every custom module of the application
import myModule from "./my-module.js";
// This one also exports a promise that resolves once it has loaded what it needs.
import otherModule, { ready as otherModuleReady } from "./other-module.js";

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
const modules = (window.Vaadin.Flow.bpmnModules = window.Vaadin.Flow.bpmnModules || {});

modules.myModule = { module: myModule };
modules.otherModule = {
  module: otherModule,       // a bpmn-js module, or an array of them
  ready: otherModuleReady,   // optional: a promise the editor waits for
  moddleExtensions: {},      // optional: descriptors for custom XML attributes
};
```

```java
@JsModule("./bpmn-modules.js")
public class ProcessView extends VerticalLayout {
    ...
    BpmnModeler modeler = new BpmnModeler(false, List.of("myModule", "otherModule"));
}
```

Adding another module takes one more line in that file and its name in the
constructor. A module can also register itself the same way at the end of its
own file; then the view imports that file with `@JsModule` instead.

Custom modules are loaded only while editing; the moddle extensions also in
read-only mode, so that their attributes are kept. A name that is not
registered, a `ready` promise that rejects or a module that fails when the
editor is created is reported in the browser console, and the editor starts
without that module.

## Flow documentation
Documentation for Vaadin Flow can be found in [Flow documentation](https://vaadin.com/docs/latest/flow).

## License
Distributed under Apache Licence 2.0.

The npm packages the add-on ships have licenses of their own. bpmn-js is
distributed under the [bpmn.io license](https://github.com/bpmn-io/bpmn-js/blob/develop/LICENSE),
an MIT license with one extra condition: the bpmn.io watermark shown in the
diagram must not be removed or changed, and must stay fully visible. The other
packages are distributed under the MIT license.

### Sponsored development
Major pieces of development of this add-on has been sponsored by multiple customers of Vaadin. Read more about Expert on Demand at: [Support](https://vaadin.com/support) and [Pricing](https://vaadin.com/pricing).
