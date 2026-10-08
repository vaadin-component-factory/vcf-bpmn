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
package org.vaadin.addons.componentfactory.bpmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;

public class BpmnModelerTest {

    private static final String DIAGRAM_A = "<bpmn:definitions id=\"A\"/>";
    private static final String DIAGRAM_B = "<bpmn:definitions id=\"B\"/>";

    private final BpmnModeler modeler = new BpmnModeler();

    @Test
    public void hasStylingClassName() {
        assertTrue(modeler.hasClassName("vcf-bpmn"));
    }

    @Test
    public void noDiagramByDefault() {
        assertNull(modeler.getDiagramXml());
    }

    @Test
    public void setDiagramXml_isReturnedRightAway() {
        modeler.setDiagramXml(DIAGRAM_A);

        assertEquals(DIAGRAM_A, modeler.getDiagramXml());
    }

    @Test
    public void importError_fallsBackToTheLastImportedDiagram() {
        modeler.setDiagramXml(DIAGRAM_A);
        imported();
        modeler.setDiagramXml("<not-bpmn>");
        importFailed();

        assertEquals(DIAGRAM_A, modeler.getDiagramXml());
    }

    @Test
    public void importError_withNothingImportedBefore_leavesNoDiagram() {
        modeler.setDiagramXml("<not-bpmn>");
        importFailed();

        assertNull(modeler.getDiagramXml());
    }

    @Test
    public void diagramChanged_updatesTheDiagram() {
        modeler.setDiagramXml(DIAGRAM_A);
        imported();
        changedInBrowser(DIAGRAM_B);

        assertEquals(DIAGRAM_B, modeler.getDiagramXml());
    }

    @Test
    public void diagramChanged_listenersSeeTheEditedDiagram() {
        AtomicReference<String> seen = new AtomicReference<>();
        modeler.addDiagramChangedListener(
                event -> seen.set(event.getSource().getDiagramXml()));

        changedInBrowser(DIAGRAM_B);

        assertEquals(DIAGRAM_B, seen.get());
    }

    @Test
    public void importError_afterAnEdit_fallsBackToTheEditedDiagram() {
        modeler.setDiagramXml(DIAGRAM_A);
        imported();
        changedInBrowser(DIAGRAM_B);
        modeler.setDiagramXml("<not-bpmn>");
        importFailed();

        assertEquals(DIAGRAM_B, modeler.getDiagramXml());
    }

    @Test
    public void importedEvent_parsesWarnings() {
        DiagramImportedEvent event = new DiagramImportedEvent(modeler, true,
                "[\"unresolved reference <Flow_1>\",\"unknown type\"]");

        assertEquals(List.of("unresolved reference <Flow_1>", "unknown type"),
                event.getWarnings());
    }

    @Test
    public void importedEvent_noWarnings() {
        DiagramImportedEvent event = new DiagramImportedEvent(modeler, true,
                "[]");

        assertTrue(event.getWarnings().isEmpty());
    }

    private void imported() {
        ComponentUtil.fireEvent(modeler,
                new DiagramImportedEvent(modeler, true, "[]"));
    }

    private void importFailed() {
        ComponentUtil.fireEvent(modeler,
                new DiagramImportErrorEvent(modeler, true, "unparsable"));
    }

    private void changedInBrowser(String xml) {
        ComponentUtil.fireEvent(modeler,
                new DiagramChangedEvent(modeler, true, xml));
    }
}
