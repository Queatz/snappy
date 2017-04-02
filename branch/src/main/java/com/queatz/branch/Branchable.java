package com.queatz.branch;

/**
 * Created by jacob on 4/2/17.
 */

public interface Branchable<T> {
    void to(Branch<T> branch);
}
