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

import static jdk.internal.org.objectweb.asm.Type.*;
import jdk.internal.org.objectweb.asm.MethodVisitor;

/**
 * @version 2012/01/18 9:51:34
 */
public class IntValue extends Bytecode<IntValue> {

    /** The opration code. */
    public int opcode;

    /** The value. */
    public int operand;

    /**
     * @param opcode
     * @param operand
     */
    public IntValue(int opcode, int operand) {
        this.opcode = opcode;
        this.operand = operand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(MethodVisitor visitor, boolean isNonPrimitive) {
        visitor.visitIntInsn(opcode, operand);
        wrap(visitor, INT_TYPE);
    }
}
