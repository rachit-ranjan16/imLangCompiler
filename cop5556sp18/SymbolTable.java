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
        scopeStack.pop();
        currentScope = scopeStack.peek();

    }

    public boolean insert(String identifier, Declaration dec) {
        if (!symTab.containsKey(identifier)) {
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
        Declaration dec = null;
        List<Pair> pairList = symTab.get(identifier);
        for (int i = pairList.size() - 1; i >= 0; i--)
            if (scopeStack.contains(pairList.get(i).getScope())) {
                dec = pairList.get(i).getDec();
                break;
            }
        return dec;

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