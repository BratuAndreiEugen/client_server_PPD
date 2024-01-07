package org.example;

import java.util.List;

public class ThreadSafeLinkedList<T extends Comparable<T>> {
    private List<T> javaLinkedList;

    public ThreadSafeLinkedList(java.util.LinkedList<T> javaLinkedList) {
        this.javaLinkedList = javaLinkedList;
    }

    public List<T> getJavaLinkedList() {
        return javaLinkedList;
    }

    public int indexOf(T element){
        return javaLinkedList.indexOf(element);
    }

    public void add(T element){
        this.javaLinkedList.add(element);
    }

    public void remove(T element) {
        this.javaLinkedList.remove(element);

    }

    public T get(Integer index) {
        return this.javaLinkedList.get(index);

    }

    public Integer size(){
        return javaLinkedList.size();
    }

    public void set(Integer index, T element) {
        this.javaLinkedList.set(index, element);
    }

    public void sort() {
        this.javaLinkedList.sort(null);
    }
}
