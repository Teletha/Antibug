/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
public class Project extends bee.api.Project {

    {
        product("com.github.teletha", "antibug", "0.3");
        producer("Nameless Production Committee");
        describe("JUnit Extension Tools.");

        require("net.bytebuddy", "byte-buddy", "1.8.0");
        require("net.bytebuddy", "byte-buddy-agent", "1.8.0");
        require("com.github.teletha", "sinobu", "1.0");
        require("com.github.teletha", "filer", "0.5");
        require("junit", "junit", "4.12");

        versionControlSystem("https://github.com/teletha/antiBug");
    }
}
