/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless.node;

import org.elasticsearch.painless.Location;
import org.elasticsearch.painless.Scope;
import org.elasticsearch.painless.ir.BraceSubDefNode;
import org.elasticsearch.painless.ir.ClassNode;
import org.elasticsearch.painless.lookup.def;
import org.elasticsearch.painless.symbol.ScriptRoot;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents an array load/store or shortcut on a def type.  (Internal only.)
 */
final class PSubDefArray extends AStoreable {
    private AExpression index;

    PSubDefArray(Location location, AExpression index) {
        super(location);

        this.index = Objects.requireNonNull(index);
    }

    @Override
    Output analyze(ScriptRoot scriptRoot, Scope scope, AStoreable.Input input) {
        this.input = input;
        output = new Output();

        Output indexOutput = index.analyze(scriptRoot, scope, new Input());
        index.input.expected = indexOutput.actual;
        index.cast();

        // TODO: remove ZonedDateTime exception when JodaCompatibleDateTime is removed
        output.actual = input.expected == null || input.expected == ZonedDateTime.class || input.explicit ? def.class : input.expected;

        return output;
    }

    @Override
    BraceSubDefNode write(ClassNode classNode) {
        BraceSubDefNode braceSubDefNode = new BraceSubDefNode();

        braceSubDefNode.setChildNode(index.cast(index.write(classNode)));

        braceSubDefNode.setLocation(location);
        braceSubDefNode.setExpressionType(output.actual);

        return braceSubDefNode;
    }

    @Override
    boolean isDefOptimized() {
        return true;
    }

    @Override
    void updateActual(Class<?> actual) {
        this.output.actual = actual;
    }

    @Override
    public String toString() {
        return singleLineToString(prefix, index);
    }
}
