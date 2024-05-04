package autocomplete;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * Ternary search tree (TST) implementation of the {@link Autocomplete} interface.
 *
 * @see Autocomplete
 */
public class TernarySearchTreeAutocomplete implements Autocomplete {
    /**
     * The overall root of the tree: the first character of the first autocompletion term added to this tree.
     */
    private Node overallRoot;

    /**
     * Constructs an empty instance.
     */
    public TernarySearchTreeAutocomplete() {
        overallRoot = null;
    }

    @Override
    public void addAll(Collection<? extends CharSequence> terms) {
        for (CharSequence term: terms) {
            put(term);
        }
    }

    @Override
    public List<CharSequence> allMatches(CharSequence prefix) {
        List<CharSequence> result = new ArrayList<>();
        if (prefix == null || prefix.length() == 0) {
            return result;
        }
        Node node = get(overallRoot, prefix, 0);

        if (node != null) {
            if (node.isTerm) {
                result.add(prefix);
            }
            collect(node.mid, new StringBuilder(prefix), result);
        }
        return result;
    }

    public void put(CharSequence term) {
        if (term == null || term.length() == 0) {
            return;
        }
        overallRoot = put(overallRoot, term, 0);
    }

    private Node put(Node node, CharSequence prefix, int index) {
        char c = prefix.charAt(index);
        if (node == null) {
            node = new Node(c);
        }
        if (c < node.data) {
            node.left  = put(node.left, prefix, index);
        } else if (c > node.data) {
            node.right = put(node.right, prefix, index);
        } else if (index < prefix.length() - 1) {
            node.mid = put(node.mid, prefix, index+1);
        } else {
            node.isTerm = true;
        }
        return node;
    }

    private Node get(Node node, CharSequence prefix, int index) {
        if (node == null) {
            return null;
        }

        char c = prefix.charAt(index);
        if (c < node.data) {
            return get(node.left, prefix, index);
        } else if (c > node.data)  {
            return get(node.right, prefix, index);
        } else if (index < prefix.length() - 1) {
            return get(node.mid, prefix, index+1);
        } else {
            return node;
        }
    }

    private void collect(Node node, StringBuilder prefix, List<CharSequence> result) {
        if (node == null) {
            return;
        }
        collect(node.left, prefix, result);
        if (node.isTerm) {
            result.add(prefix.toString() + node.data);
        }
        collect(node.mid, prefix.append(node.data), result);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(node.right, prefix, result);
    }

    /**
     * A search tree node representing a single character in an autocompletion term.
     */
    private static class Node {
        private final char data;
        private boolean isTerm;
        private Node left;
        private Node mid;
        private Node right;

        public Node(char data) {
            this.data = data;
            this.isTerm = false;
            this.left = null;
            this.mid = null;
            this.right = null;
        }
    }
}
