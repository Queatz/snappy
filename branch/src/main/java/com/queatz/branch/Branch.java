package com.queatz.branch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Branch<T> {
    private T value;
    private final Map<Class<?>, HashSet<Branch>> branches = new HashMap<>();

    /**
     * Get the branch context.
     */
    protected T me() {
        return value;
    }

    /**
     * Set the branch context.
     */
    public Branch<T> with(T value) {
        this.value = value;
        return this;
    }

    /**
     * Execute the branch.
     */
    protected void execute() {}

    /**
     * Branch to another branch.
     *
     * @param branch The branch to execute.
     */
    public void to(Branch<T> branch) {
        branch.with(me()).execute();
    }

    public static <T> Branch<T> from(T context) {
        return new Branch<T>().with(context);
    }

    public <V> Branch<T> when(Class<V> result, Branch<V> branch) {
        if (!branches.containsKey(result)) {
            branches.put(result, new HashSet<Branch>());
        }

        branches.get(result).add(branch);

        return this;
    }

    protected void emit(Object value) {
        if (branches.containsKey(value == null ? null : value.getClass())) {
            for (Branch branch : branches.get(value == null ? null : value.getClass())) {
                branch.with(value).execute();
            }
        }
    }
}
