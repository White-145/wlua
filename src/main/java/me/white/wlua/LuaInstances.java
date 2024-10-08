package me.white.wlua;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

class LuaInstances {
    private static List<LuaState> states = new ArrayList<>();
    private static TreeSet<Integer> freeIndices = new TreeSet<>();

    static int add(LuaState state) {
        return add((i) -> state);
    }

    static int add(Function<Integer, LuaState> provider) {
        int i = freeIndices.isEmpty() ? states.size() : freeIndices.removeFirst();
        LuaState state = provider.apply(i);
        if (i < states.size()) {
            states.set(i, state);
        } else {
            states.add(state);
        }
        return i;
    }

    static LuaState get(int i) {
        if (i < 0 || i >= states.size() || freeIndices.contains(i)) {
            return null;
        }
        return states.get(i);
    }

    static void remove(int i) {
        if (i == states.size() - 1) {
            do {
                states.removeLast();
            } while (freeIndices.contains(states.size() - 1));
            while (!freeIndices.isEmpty() && freeIndices.getLast() >= states.size()) {
                freeIndices.removeLast();
            }
        } else if (i >= 0 && i < states.size() && !freeIndices.contains(i)) {
            freeIndices.add(i);
            states.set(i, null);
        }
    }
}
