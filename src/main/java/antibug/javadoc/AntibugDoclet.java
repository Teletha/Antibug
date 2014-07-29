/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.javadoc;

import java.util.HashMap;
import java.util.Map;

import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import antibug.javadoc.info.Identifier;
import antibug.javadoc.info.MethodInfo;
import antibug.javadoc.info.PackageInfo;
import antibug.javadoc.info.TypeInfo;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

/**
 * @version 2014/07/26 22:34:28
 */
@Manageable(lifestyle = Singleton.class)
public class AntibugDoclet extends Doclet {

    static {
        I.load(Identifier.class, true);
    }

    /** All documents. */
    protected final Documents documents = I.make(Documents.class);

    /** Avoid linear search. */
    private Map<ClassDoc, TypeInfo> types = new HashMap();

    /** Avoid linear search. */
    private Map<PackageDoc, PackageInfo> packages = new HashMap();

    /**
     * <p>
     * Start entry point.
     * </p>
     */
    public static boolean start(RootDoc root) {
        AntibugDoclet doclet = I.make(AntibugDoclet.class);
        doclet.build(root);

        return true;
    }

    /**
     * <p>
     * Build custom documents.
     * </p>
     * 
     * @param root
     */
    protected void build(RootDoc root) {
        for (ClassDoc classDoc : root.classes()) {
            TypeInfo typeInfo = findTypeInfoBy(classDoc);
        }
    }

    /**
     * <p>
     * Find type info by {@link ClassDoc}.
     * </p>
     * 
     * @param doc
     * @return
     */
    private TypeInfo findTypeInfoBy(ClassDoc doc) {
        // check cache
        TypeInfo info = types.get(doc);

        if (info == null) {
            // build type info
            info = new TypeInfo();
            info.name = doc.qualifiedTypeName();
            info.simpleName = doc.simpleTypeName();

            if (doc.containingClass() != null) {
                TypeInfo parent = findTypeInfoBy(doc.containingClass());
                info.name = parent.name + "$" + info.simpleName;

                parent.inners.add(info);
            }

            for (MethodDoc methodDoc : doc.methods()) {
                info.methods.add(findMethodInfo(methodDoc, info));
            }

            PackageInfo packageInfo = findPackageBy(doc.containingPackage());
            packageInfo.types.add(info);

            // store
            types.put(doc, info);
        }

        // API definition
        return info;
    }

    /**
     * <p>
     * Find method info by {@link MethodDoc}.
     * </p>
     * 
     * @param methodDoc
     * @return
     */
    private MethodInfo findMethodInfo(MethodDoc doc, TypeInfo declaring) {
        MethodInfo info = new MethodInfo();
        info.name = doc.name();
        info.signature = doc.signature();
        info.declaring = declaring.getId();

        // API definition
        return info;
    }

    /**
     * <p>
     * Find package info by {@link ClassDoc}.
     * </p>
     * 
     * @param doc
     * @return
     */
    private PackageInfo findPackageBy(PackageDoc doc) {
        // check cache
        PackageInfo info = packages.get(doc);

        if (info == null) {
            // build package info
            info = new PackageInfo();
            info.name = doc.name();

            // store
            documents.packages.add(info);
        }

        // API definition
        return info;
    }
}
