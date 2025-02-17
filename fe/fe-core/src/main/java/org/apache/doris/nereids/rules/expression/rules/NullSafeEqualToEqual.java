// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.rules.expression.rules;

import org.apache.doris.nereids.rules.expression.ExpressionRewriteContext;
import org.apache.doris.nereids.rules.expression.ExpressionRewriteRule;
import org.apache.doris.nereids.trees.expressions.EqualTo;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.IsNull;
import org.apache.doris.nereids.trees.expressions.NullSafeEqual;
import org.apache.doris.nereids.trees.expressions.literal.BooleanLiteral;
import org.apache.doris.nereids.trees.expressions.literal.NullLiteral;
import org.apache.doris.nereids.trees.expressions.visitor.DefaultExpressionRewriter;

/**
 * convert "<=>" to "=", if any side is not nullable
 * convert "A <=> null" to "A is null"
 */
public class NullSafeEqualToEqual extends DefaultExpressionRewriter<ExpressionRewriteContext> implements
        ExpressionRewriteRule<ExpressionRewriteContext> {
    public static final NullSafeEqualToEqual INSTANCE = new NullSafeEqualToEqual();

    @Override
    public Expression rewrite(Expression expr, ExpressionRewriteContext ctx) {
        return expr.accept(this, null);
    }

    @Override
    public Expression visitNullSafeEqual(NullSafeEqual nullSafeEqual, ExpressionRewriteContext ctx) {
        if (nullSafeEqual.left() instanceof NullLiteral) {
            if (nullSafeEqual.right().nullable()) {
                return new IsNull(nullSafeEqual.right());
            } else {
                return BooleanLiteral.FALSE;
            }
        } else if (nullSafeEqual.right() instanceof NullLiteral) {
            if (nullSafeEqual.left().nullable()) {
                return new IsNull(nullSafeEqual.left());
            } else {
                return BooleanLiteral.FALSE;
            }
        } else if (!nullSafeEqual.left().nullable() || !nullSafeEqual.right().nullable()) {
            return new EqualTo(nullSafeEqual.left(), nullSafeEqual.right());
        }
        return nullSafeEqual;
    }
}
