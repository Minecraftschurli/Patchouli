package vazkii.patchouli.rewrite.impl;

import java.util.*;
import java.util.function.Function;

public abstract class Util {
    private Util() { throw new IllegalAccessError("Utility class"); }

    public static <TKey, TValue> List<TKey> topologicalSort(Map<TKey, TValue> map, Function<TValue, Optional<TKey>> dependencyFunction) throws SortException {
        Set<TKey> visited = new HashSet<>();
        Set<TKey> visiting = new HashSet<>();
        List<TKey> sortedList = new ArrayList<>();

        for (TKey key : map.keySet()) {
            if (!visited.contains(key)) {
                dfs(key, dependencyFunction, map, visited, visiting, sortedList);
            }
        }

        return sortedList;
    }

    private static <TKey, TValue> void dfs(TKey key, Function<TValue, Optional<TKey>> dependencyFunction, Map<TKey, TValue> map, Set<TKey> visited, Set<TKey> visiting, List<TKey> sortedList) {
        if (visiting.contains(key)) {
            throw new SortException.Loop(key);
        }

        if (visited.contains(key)) {
            return;
        }

        visiting.add(key);

        Optional<TKey> parentKey = dependencyFunction.apply(map.get(key));

        if (parentKey.isPresent()) {
            if (!map.containsKey(parentKey.get())) {
                throw new SortException.Missing(parentKey.get(), key);
            }
            dfs(parentKey.get(), dependencyFunction, map, visited, visiting, sortedList);
        }

        visiting.remove(key);
        visited.add(key);
        sortedList.add(key);
    }

    public static abstract sealed class SortException extends RuntimeException {
        private final Object key;

        public SortException(String message, Object key) {
            super(message);
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        public static final class Loop extends SortException {

            public Loop(Object key) {
                super("Found loop for key '" + key + "'", key);
            }
        }

        public static final class Missing extends SortException {
            private final Object referenced;
            private final Object referencedBy;

            public Missing(Object referenced, Object referencedBy) {
                super("Key '" + referenced + "' is missing but was referenced by '" + referencedBy + "'", referencedBy);
                this.referenced = referenced;
                this.referencedBy = referencedBy;
            }
    
            public Object getReferenced() {
                return referenced;
            }
    
            public Object getReferencedBy() {
                return referencedBy;
            }
        }
    }
}
