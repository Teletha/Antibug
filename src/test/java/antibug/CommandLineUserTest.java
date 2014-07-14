/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kiss.I;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/09/22 16:22:02
 */
public class CommandLineUserTest {

    @Rule
    public static final CommandLineUser user = new CommandLineUser();

    @Test
    public void input() throws Exception {
        user.willInput("1");
        assert "1".equals(read());
    }

    @Test
    public void inputSeparately() throws Exception {
        user.willInput("1");
        assert "1".equals(read());

        user.willInput("2");
        assert "2".equals(read());
    }

    @Test
    public void inputSequentially() throws Exception {
        user.willInput("1");
        user.willInput("2");
        assert "1".equals(read());
        assert "2".equals(read());
    }

    @Test
    public void output() throws Exception {
        assert user.receive("test") == false;
        System.out.println("test");
        assert user.receive("test") == true;

    }

    /**
     * Helper method to read character from user input.
     * 
     * @return
     */
    private String read() {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
