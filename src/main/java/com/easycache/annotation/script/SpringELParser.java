package com.easycache.annotation.script;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringEL表达式解析器
 *
 * @author 陈敬
 * @since 1.0.0-SNAPSHOT
 */
public class SpringELParser {
    protected static final String TARGET = "target";
    protected static final String ARGS = "args";
    protected static final String RET_VAL = "retVal";
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public <T> T getELValue(String springEL, Object target, Object[] args, Object retVal, Class<T> valueType) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable(TARGET, target);
        context.setVariable(ARGS, args);
        if (retVal != null) {
            context.setVariable(RET_VAL, retVal);
        }
        if (!expressionCache.containsKey(springEL)) {
            expressionCache.put(springEL, parser.parseExpression(springEL));
        }
        Expression expression = expressionCache.get(springEL);
        return expression.getValue(context, valueType);
    }
}
