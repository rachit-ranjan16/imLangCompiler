package cop5556sp18;

import java.util.*;

import cop5556sp18.AST.Declaration;


public class SymbolTable {
    private int currentScope, nextScope;
    private Stack<Integer> scopeStack;
    private Map<String, List<Pair>> symTab;

    public SymbolTable() {
        this.currentScope = 0;
        this.nextScope = 1;
        this.scopeStack = new Stack<>();
        this.symTab = new HashMap<>();
        scopeStack.push(currentScope);
    }

    public void enterScope() {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    public void leaveScope() {
        int endScope = scopeStack.pop();
        currentScope = scopeStack.peek();
        List<String> delKeyList = new ArrayList<>();
        for (String key : symTab.keySet())
            for (Pair p : symTab.get(key))
                if (p.getScope() == endScope)
                    delKeyList.add(key);
        for(String key: delKeyList)
            symTab.remove(key);
    }

    public boolean insert(String identifier, Declaration dec) {
        if (lookup(identifier) == null) {
            List<Pair> pairList = new ArrayList<>();
            pairList.add(new Pair(currentScope, dec));
            symTab.put(identifier, pairList);
        } else {
            for (Pair p : symTab.get(identifier))
                if (p.getScope() == currentScope)
                    return false;
            symTab.get(identifier).add(new Pair(currentScope, dec));
        }
        return true;
    }

    public Declaration lookup(String identifier) {
        if (!symTab.containsKey(identifier)) return null;

        for (Pair p : symTab.get(identifier))
            if (scopeStack.contains(p.getScope()))
                return p.getDec();

        return null;
    }
}

class Pair {
    private int scope;
    private Declaration dec;

    public Pair(int scope, Declaration dec) {
        this.scope = scope;
        this.dec = dec;
    }

    public int getScope() {
        return scope;
    }

    public Declaration getDec() {
        return dec;
    }

    @Override
    public String toString() {
        return "scope=" + scope + " " + dec;
    }
}