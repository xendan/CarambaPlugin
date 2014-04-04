package com.bics.caramba.plugin.search;

import com.bics.caramba.plugin.CarambaComponent;

import java.util.ArrayList;
import java.util.List;

/**
* User: id967161
* Date: 24/06/13
*/
public class TreeNode {
    private List<TreeNode> children = new ArrayList<TreeNode>();
    private String name;

    public TreeNode(String name) {
        this.name = name;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void put(String[] parts) {
        if (parts.length > 0) {
            TreeNode node = findOrCreateNode(parts[0]);
            node.put(removeFirst(parts));
        }
    }

    public String getName() {
        return name;
    }

    private String[] removeFirst(String[] parts) {
        String[] newParts = new String[parts.length - 1];
        System.arraycopy(parts, 1, newParts, 0, parts.length - 1);
        return newParts;
    }

    private TreeNode findOrCreateNode(String part) {
        for (TreeNode child : children) {
            if (child.getName().equals(part)) {
                return child;
            }
        }
        TreeNode newNode = new TreeNode(part);
        children.add(newNode);
        return newNode;
    }
}
