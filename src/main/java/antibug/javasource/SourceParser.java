/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.javasource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import kiss.I;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;

/**
 * @version 2014/07/31 10:41:40
 */
public class SourceParser {

    public static final void main(final String[] args) throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

        Path dir = I.locate("src/test/java/antibug/javasource");
        List<File> files = new ArrayList();

        for (Path path : I.walk(dir, "Sample.java")) {
            files.add(path.toFile());
        }

        StandardJavaFileManager fileManager = javac.getStandardFileManager(null, null, null);
        CompilationTask task = javac.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(files));

        JavacTask javacTask = (JavacTask) task;
        Trees trees = Trees.instance(task);

        for (CompilationUnitTree unit : javacTask.parse()) {
            SourceXML xml = new SourceXML(I.xml("source"));
            SourceMapper mapper = new SourceMapper(unit, trees.getSourcePositions());

            // start analyzing
            SourceTreeVisitor visitor = new SourceTreeVisitor(xml, mapper);
            unit.accept(visitor, xml);

            System.out.println(xml);

            // SAMPLE
            // LineWriter writer = new LineWriter();
            // Analyzer analyzer = new Analyzer(writer, mapper);
            // analyzer.visitTopLevel((JCCompilationUnit) unit);
            //
            // int number = 1;
            //
            // for (StringBuilder stringBuilder : writer) {
            // System.out.println(number++ + "   " + stringBuilder);
            // }

            // SAMPLE
            // StringWriter writer = new StringWriter();
            // Pretty analyzer = new Pretty(writer, false);
            // analyzer.visitTopLevel((JCCompilationUnit) unit);
            // System.out.println(writer.toString());
        }
    }
}
