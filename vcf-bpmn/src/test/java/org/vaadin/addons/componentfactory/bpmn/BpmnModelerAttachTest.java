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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.server.VaadinSession;

/** What the modeler sends to the browser when it is attached. */
public class BpmnModelerAttachTest {

    private static final String DIAGRAM_A = "<bpmn:definitions id=\"A\"/>";
    private static final String DIAGRAM_B = "<bpmn:definitions id=\"B\"/>";

    private final BpmnModeler modeler = new BpmnModeler();

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void attach_buildsTheEditorAndImportsTheDiagram() {
        modeler.setDiagramXml(DIAGRAM_A);

        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(2, sent.size());
        assertTrue(sent.get(0).getExpression().contains("initLazy"));
        assertImport(sent.get(1), DIAGRAM_A, true);
    }

    @Test
    public void attach_withoutDiagram_onlyBuildsTheEditor() {
        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(1, sent.size());
        assertTrue(sent.get(0).getExpression().contains("initLazy"));
    }

    @Test
    public void setDiagramXml_whenAttached_importsRightAway() {
        attachTo(new UI());

        modeler.setDiagramXml(DIAGRAM_A);
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(1, sent.size());
        assertImport(sent.get(0), DIAGRAM_A, true);
    }

    @Test
    public void preserveOnRefresh_rebuildsTheEditorWithTheEditedDiagram() {
        modeler.setDiagramXml(DIAGRAM_A);
        attachTo(new UI());
        ComponentUtil.fireEvent(modeler,
                new DiagramImportedEvent(modeler, true, "[]"));
        ComponentUtil.fireEvent(modeler,
                new DiagramChangedEvent(modeler, true, DIAGRAM_B));

        // What Flow does on a refresh with @PreserveOnRefresh: the component
        // leaves the old UI's tree and joins the new UI as a fresh attach.
        modeler.getElement().removeFromTree();
        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(2, sent.size());
        assertTrue(sent.get(0).getExpression().contains("initLazy"));
        assertImport(sent.get(1), DIAGRAM_B, false);
    }

    @Test
    public void readOnlyConstructor_buildsAViewer() {
        BpmnModeler viewer = new BpmnModeler(true);

        List<JavaScriptInvocation> sent = attach(viewer, new UI());

        assertTrue(viewer.isReadOnly());
        assertInit(sent.get(0), true);
    }

    @Test
    public void editableByDefault() {
        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertFalse(modeler.isReadOnly());
        assertInit(sent.get(0), false);
    }

    @Test
    public void setReadOnly_whenAttached_switchesTheEditor() {
        attachTo(new UI());

        modeler.setReadOnly(true);
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(1, sent.size());
        assertTrue(sent.get(0).getExpression().contains("setReadOnly"));
        assertTrue(sent.get(0).getParameters().contains(true));
    }

    @Test
    public void setReadOnly_sameMode_sendsNothing() {
        attachTo(new UI());

        modeler.setReadOnly(false);

        assertTrue(flush(modeler.getUI().get()).isEmpty());
    }

    @Test
    public void setReadOnly_whenDetached_appliesOnAttach() {
        modeler.setReadOnly(true);

        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(1, sent.size());
        assertInit(sent.get(0), true);
    }

    @Test
    public void addElementClassName_whenAttached_addsTheMarker() {
        attachTo(new UI());

        modeler.addElementClassName("Task_1", "failed");
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(1, sent.size());
        assertMarker(sent.get(0), "addMarker", "Task_1", "failed");
    }

    @Test
    public void addElementClassName_twice_sendsItOnce() {
        attachTo(new UI());

        modeler.addElementClassName("Task_1", "failed");
        modeler.addElementClassName("Task_1", "failed");

        assertEquals(1, flush(modeler.getUI().get()).size());
    }

    @Test
    public void removeElementClassName_removesTheMarker() {
        attachTo(new UI());
        modeler.addElementClassName("Task_1", "failed");
        flush(modeler.getUI().get());

        modeler.removeElementClassName("Task_1", "failed");
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(1, sent.size());
        assertMarker(sent.get(0), "removeMarker", "Task_1", "failed");
    }

    @Test
    public void removeElementClassName_notAdded_sendsNothing() {
        attachTo(new UI());

        modeler.removeElementClassName("Task_1", "failed");

        assertTrue(flush(modeler.getUI().get()).isEmpty());
    }

    @Test
    public void elementClassNames_areAppliedAgainOnAttach() {
        modeler.setDiagramXml(DIAGRAM_A);
        modeler.addElementClassName("Task_1", "failed");
        modeler.addElementClassName("Task_2", "done");
        attachTo(new UI());
        modeler.removeElementClassName("Task_2", "done");

        modeler.getElement().removeFromTree();
        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(3, sent.size());
        assertImport(sent.get(1), DIAGRAM_A, true);
        assertMarker(sent.get(2), "addMarker", "Task_1", "failed");
    }

    @Test
    public void setDiagramXml_clearsTheClassNames() {
        modeler.setDiagramXml(DIAGRAM_A);
        attachTo(new UI());
        modeler.addElementClassName("Task_1", "failed");
        flush(modeler.getUI().get());

        modeler.setDiagramXml(DIAGRAM_B);
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(2, sent.size());
        assertTrue(sent.get(0).getExpression().contains("clearMarkers"));
        assertImport(sent.get(1), DIAGRAM_B, true);
    }

    @Test
    public void setDiagramXml_withoutClassNames_doesNotClear() {
        attachTo(new UI());

        modeler.setDiagramXml(DIAGRAM_A);
        List<JavaScriptInvocation> sent = flush(modeler.getUI().get());

        assertEquals(1, sent.size());
        assertImport(sent.get(0), DIAGRAM_A, true);
    }

    @Test
    public void setDiagramXml_whenDetached_dropsTheClassNames() {
        modeler.addElementClassName("Task_1", "failed");
        modeler.setDiagramXml(DIAGRAM_A);

        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertEquals(2, sent.size());
        assertImport(sent.get(1), DIAGRAM_A, true);
    }

    @Test
    public void customModules_areSentToTheEditor() {
        BpmnModeler withModules = new BpmnModeler(false,
                List.of("qsTaskConfig", "taskBadges"));

        List<JavaScriptInvocation> sent = attach(withModules, new UI());

        assertTrue(sent.get(0).getParameters()
                .contains("[\"qsTaskConfig\",\"taskBadges\"]"));
    }

    @Test
    public void customModules_noneByDefault() {
        List<JavaScriptInvocation> sent = attachTo(new UI());

        assertTrue(modeler.getCustomModules().isEmpty());
        assertTrue(sent.get(0).getParameters().contains("[]"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void customModules_cannotBeChangedAfterConstruction() {
        List<String> names = new ArrayList<>(List.of("taskBadges"));
        BpmnModeler withModules = new BpmnModeler(false, names);
        names.add("other");

        assertEquals(List.of("taskBadges"), withModules.getCustomModules());
        withModules.getCustomModules().add("other");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addElementClassName_rejectsSeveralClassNames() {
        modeler.addElementClassName("Task_1", "failed urgent");
    }

    private List<JavaScriptInvocation> attachTo(UI ui) {
        return attach(modeler, ui);
    }

    private static void assertMarker(JavaScriptInvocation invocation,
            String function, String elementId, String className) {
        assertTrue(invocation.getExpression().contains(function));
        assertTrue(invocation.getParameters().contains(elementId));
        assertTrue(invocation.getParameters().contains(className));
    }

    private static List<JavaScriptInvocation> attach(BpmnModeler component,
            UI ui) {
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
        UI.setCurrent(ui);
        ui.add(component);
        return flush(ui);
    }

    private static List<JavaScriptInvocation> flush(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation).toList();
    }

    private static void assertInit(JavaScriptInvocation invocation,
            boolean readOnly) {
        assertTrue(invocation.getExpression().contains("initLazy"));
        assertTrue(invocation.getParameters().contains(readOnly));
    }

    private static void assertImport(JavaScriptInvocation invocation,
            String xml, boolean notify) {
        assertTrue(invocation.getExpression().contains("importXml"));
        List<Object> parameters = invocation.getParameters();
        assertTrue(parameters.contains(xml));
        assertTrue(parameters.contains(notify));
    }
}
