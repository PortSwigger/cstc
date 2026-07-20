package de.usd.cstchef;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import burp.api.montoya.core.ByteArray;

public class VariableStore {

    private static VariableStore instance;

    // reverse sorted by length to be able to replace longest matching variables first by iterating over them via for loop
    private TreeMap<String, ByteArray> variables = new TreeMap<>(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
    private ReentrantLock lock = new ReentrantLock();

    public static VariableStore getInstance() {
        if (VariableStore.instance == null) {
            VariableStore.instance = new VariableStore();
        }
        return VariableStore.instance;
    }

    private VariableStore() {
    }

    public void lock() {
         this.lock.lock();
    }

    public void unlock() {
         this.lock.unlock();
    }

    public synchronized ByteArray getVariable(String name) {
        return this.variables.get(name);
    }

    public synchronized void setVariable(String key, ByteArray value) {
        this.variables.put(key, value);
    }

    public synchronized void removeVariable(String key) {
        if(key == null || key.length() == 0) {
            return;
        }
        this.variables.remove(key);
    }

    public synchronized TreeMap<String, ByteArray> getVariables() {
        // return a copy each time to prevent ConcurrentModificationException
        TreeMap<String, ByteArray> variablesCopied = new TreeMap<>(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
        variablesCopied.putAll(variables);
        return variablesCopied;
    }

}
