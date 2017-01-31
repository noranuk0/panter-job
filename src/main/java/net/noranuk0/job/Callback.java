package net.noranuk0.job;

/**
 * Callback.java
 * <p/>
 * Copyright (c) 2016 Hiroyuki Mizuhara
 * <p/>
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
public class Callback<T extends Job> {
    public void success(T sender) {
    }

    public void fail(T sender) {
    }

    public void exception(T sender, Exception e) {
    }

}
