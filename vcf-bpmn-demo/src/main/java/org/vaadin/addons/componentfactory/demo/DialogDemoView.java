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
package org.vaadin.addons.componentfactory.demo;

import java.time.LocalTime;

import org.vaadin.addons.componentfactory.bpmn.BpmnModeler;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/** Edits a copy of a saved diagram in a dialog; only Save keeps the edits. */
@SuppressWarnings("serial")
@Route(value = "dialog", layout = MainLayout.class)
public class DialogDemoView extends VerticalLayout {

    /** The diagram as last saved, standing in for a database. */
    private String savedXml = DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM);

    private final Span savedInfo = new Span();

    public DialogDemoView() {
        Button edit = new Button("Edit diagram", e -> openEditor());
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button showSaved = new Button("Show saved XML",
                e -> DemoSupport.showXml("Saved diagram XML", savedXml));

        Span name = new Span("Order process");
        name.getStyle().set("font-weight", "600");
        savedInfo.setText("Not edited yet");
        savedInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout process = new HorizontalLayout(name, savedInfo, edit,
                showSaved);
        process.setAlignItems(Alignment.BASELINE);
        process.setWrap(true);

        add(new H2("Modeler in a dialog"), description(), process);
    }

    private void openEditor() {
        BpmnModeler modeler = new BpmnModeler();
        modeler.setSizeFull();
        DemoSupport.showNotifications(modeler);
        modeler.setDiagramXml(savedXml);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit \"Order process\"");
        dialog.setWidth("90vw");
        dialog.setHeight("85vh");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.add(modeler);

        Button save = new Button("Save", e -> {
            savedXml = modeler.getDiagramXml();
            savedInfo.setText("Saved at " + LocalTime.now().withNano(0) + " ("
                    + savedXml.length() + " characters of XML)");
            Notification.show("Diagram saved");
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), save);
        dialog.open();
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "The modeler in a dialog. The page keeps the saved diagram; "
                        + "the dialog edits a copy of it, and only \"Save\" "
                        + "writes the edits back. The modeler shows the same "
                        + "notifications as on the basic page.",
                new DemoDescription.Step(
                        "Click \"Edit diagram\".",
                        "The dialog opens with the diagram centered and fitted "
                                + "to it, not squeezed into a corner, and a "
                                + "\"Diagram imported\" notification appears."),
                new DemoDescription.Step(
                        "Resize the dialog from its corner before touching the "
                                + "diagram.",
                        "The diagram is fitted again to the new size. Once "
                                + "you click, drag or scroll in the diagram, "
                                + "resizing no longer changes your zoom."),
                new DemoDescription.Step(
                        "Double-click an element to rename it on the canvas, "
                                + "then press Escape. Open the \"...\" menu of "
                                + "the palette, then press Escape.",
                        "Each Escape only cancels the rename or closes the "
                                + "menu; the dialog stays open."),
                new DemoDescription.Step(
                        "Rename an element and click \"Save\" right away.",
                        "A \"Diagram changed\" notification, then \"Diagram "
                                + "saved\". The label next to the process "
                                + "shows the save time, and \"Show saved "
                                + "XML\" has the new name."),
                new DemoDescription.Step(
                        "Click \"Edit diagram\" again, make a change and click "
                                + "\"Cancel\" (or press Escape).",
                        "The dialog closes and the saved diagram is "
                                + "unchanged; the next \"Edit diagram\" shows "
                                + "it without that change."));
    }
}
