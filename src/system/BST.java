package system;
// Generic BST class
public class BST<K extends Comparable<K>, T> implements IBST<K, T> {

    private BSTNode<K, T> root, current;
    private int size;                       // Number of keys in the binary search tree.

    public BST() {
        root = current = null;
        size = 0;
    }

    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean findKey(K key) {
        if (empty())
            return false;

        BSTNode<K, T> p = root;
        BSTNode<K, T> q = null;
        while (p != null) {
            
            if (p.key.compareTo(key) == 0) {               // Found key.
                current = p;
                return true;
            }

            q = p;

            if (p.key.compareTo(key) < 0) {                // Key at current node is less than key. Eliminate left subtree.
                p = p.right;
            
            } else {                                       // Key at current node is greater than key. Eliminate right subtree.
                p = p.left; 
            }
        }

        current = q;
        return false;
    }

    public T retrive() {
        if (current == null)
            return null;
        
        return current.data;
    }

    public boolean insert(K key, T data) {
        BSTNode<K, T> p = current;

        if (findKey(key)) {                                           // Moves current.
            current = p;
            return false;
        }

        BSTNode<K, T> newNode = new BSTNode<K, T>(key, data);         // Current is at the supposed parent of the new node.

        if (empty()) {
            root = current = newNode;
            return true;
        }

        if (current.key.compareTo(key) > 0)       {                   // Key at current node is greater than key. Add at left.
            current.left = newNode;

        } else {                                                      // Key at current node is less than key. Add at right.
            current.right = newNode;
        } 
        
        return true;
    }

}
