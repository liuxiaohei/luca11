package org.ld.beans;

import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Todo implements Data {
    private ActorRef target;
    private List<Object> queue;

    public Todo addElement(Object element) {
        List<Object> nQueue = new LinkedList<>(queue);
        nQueue.add(element);
        return new Todo(this.target, nQueue);
    }

    public Todo copy(List<Object> queue) {
        return new Todo(this.target, queue);
    }
}
