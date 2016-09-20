/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.bytecode;

import jdk.internal.org.objectweb.asm.MethodVisitor;

/**
 * @version 2012/01/18 9:29:55
 */
public class MethodCall extends Bytecode<MethodCall> {

    /** The operation code. */
    public int opcode;

    /** The method owner's internal name. */
    public String owner;

    /** The method name. */
    public String name;

    /** The method description. */
    public String desc;

    /**
     * @param opcode
     * @param owner
     * @param name
     * @param desc
     */
    public MethodCall(int opcode, String owner, String name, String desc) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(MethodVisitor visitor, boolean isNonPrimitive) {
        visitor.visitMethodInsn(opcode, owner, name, desc, false);
    }
}
