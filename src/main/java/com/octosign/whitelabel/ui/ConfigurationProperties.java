package com.octosign.whitelabel.ui;

import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigurationProperties {
    private static final Properties PROPERTIES = new Properties();

    static {
        initializeProperties();
        initializeRules();
    }

    public static String getProperty(String key) {
        if (!PROPERTIES.containsKey(key))
            throw new IntegrationException(Code.PROPERTY_NOT_FOUND, "Requested unknown property key: " + key);

        return PROPERTIES.getProperty(key);
    }

    public static String[] getPropertyArray(String key) {
        assert(key.startsWith("[]"));

        return getProperty(key).split("<@>");
    }

    private static void initializeProperties() {
        try {
            PROPERTIES.load(ConfigurationProperties.class.getResourceAsStream("configuration.properties"));
        }
        catch (IOException e) {
            throw new IntegrationException(Code.PROPERTIES_NOT_LOADED, e);
        }
    }

    private static void initializeRules() {
        Rules.when(s -> s.startsWith("[]")).thenApply(s -> s.split("<@>")).as(String[].class);
    }

    public static class Rules {
        private static final Set<Rule<?>> RULES = new HashSet<>();

        public static <T> T apply(String value) {
            var applicableRules =
                    RULES.stream().filter(rule -> rule.criteria.test(value)).toList();

            if (applicableRules.size() > 1)
                throw new IntegrationException("too many applicable rules");

            else if (applicableRules.isEmpty())
                return (T) value;

            else
                return (T) applyRule(applicableRules.get(0), value);
        }

        private static <T> T applyRule(Rule<T> rule, String value) {
            var resultClass = rule.resultClass;

            return (T) resultClass.cast(rule.transformation.apply(value));
        }

        public static <T> Rule<T> when(Predicate<String> criteria) {
            var rule = new Rule<T>(criteria);
            RULES.add(rule);
            return rule;
        }

        private static class Rule<T> {
            private final Predicate<String> criteria;
            private Function<String, T> transformation;
            private Class<?> resultClass;

            private Rule(Predicate<String> criteria) {
                this.criteria = criteria;
            }

            public Rule<T> thenApply(Function<String, T> transformation) {
                this.transformation = transformation;
                return this;
            }

            public Rule<T> as(Class<?> resultClass) {
                this.resultClass = resultClass;
                return this;
            }
        }
    }
}
