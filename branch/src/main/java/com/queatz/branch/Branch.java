package com.queatz.branch;

public class Branch<T> {
    private T value;

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
     * Shorthand for
     *
     * new OtherBranch().with(me()).execute();
     *
     * @param branch The branch to execute.
     */
    public void to(Branch<T> branch) {
        branch.with(me()).execute();
    }

    public static <T> Branch<T> from(T context) {
        return new Branch<T>().with(context);
    }
}
