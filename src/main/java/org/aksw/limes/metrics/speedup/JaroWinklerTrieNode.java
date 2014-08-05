package org.aksw.limes.metrics.speedup;

import java.util.*;

public class JaroWinklerTrieNode {

    public List<String> data;
    public char key;
	public JaroWinklerTrieNode parent;
	public HashMap<Character,JaroWinklerTrieNode> children;

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public JaroWinklerTrieNode(char key) {
        this.key = key;
		this.data = null;
		this.children = new HashMap<Character, JaroWinklerTrieNode>();
	}

    public JaroWinklerTrieNode() {
        this.key = ' ';
        this.data = null;
        this.children = new HashMap<Character, JaroWinklerTrieNode>();
    }

	public JaroWinklerTrieNode addChild(String key, String data) {
        char[] sortedKey = key.toCharArray();
        Arrays.sort(sortedKey);
        JaroWinklerTrieNode currentNode = this;
        for (char c : sortedKey) {
            if (!currentNode.children.containsKey(c)) {
                currentNode.children.put(c, new JaroWinklerTrieNode(c));
                currentNode.children.get(c).parent = currentNode;
            }
            currentNode = currentNode.children.get(c);
        }
        if (currentNode.data == null) {
            currentNode.data = new LinkedList<String>();
        }
        currentNode.data.add(data);
        return currentNode;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}
}
