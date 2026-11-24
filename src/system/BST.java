package system;
// Generic BST class
public class BST<K extends Comparable<K>, T> implements IBST<K, T> {

    public BSTNode<K, T> root, current;
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

        BSTNode<K, T> node = root;
        BSTNode<K, T> parent = null;
        while (node != null) {
            
            if (key.compareTo(node.key) == 0) {               // Found key.
                current = node;
                return true;
            }

            parent = node;

            if (key.compareTo(node.key) > 0) {                // Key at current node is less than key. Eliminate left subtree.
                node = node.right;
            
            } else {                                       // Key at current node is greater than key. Eliminate right subtree.
                node = node.left; 
            }
        }

        current = parent;
        return false;
    }

    public T retrieve() {
        if (current == null)
            return null;
        
        return current.data;
    }

    public boolean insert(K key, T data) {
        BSTNode<K, T> temp = current;

        if (findKey(key)) {                                           // Moves current.
            current = temp;
            return false;
        }

        BSTNode<K, T> newNode = new BSTNode<K, T>(key, data);         // Current is at the supposed parent of the new node.

        if (empty()) {
            root = current = newNode;
            size++;
            return true;
        }

        if (key.compareTo(current.key) < 0)       {                   // Key at current node is greater than key. Add at left.
            current.left = newNode;

        } else {                                                      // Key at current node is less than key. Add at right.
            current.right = newNode;
        } 
        
        current = newNode;
        size++;
        return true;
    }

    public boolean removeKey(K key) {
        BSTNode<K, T> parent = null;
        BSTNode<K, T> nodeToRemove = root;
        boolean found = false;

        while (nodeToRemove != null && !found) {                             // Find the node and its parent.

            if(key.compareTo(nodeToRemove.key) > 0) {
                parent = nodeToRemove;
                nodeToRemove = nodeToRemove.right;
                
            } else if (key.compareTo(nodeToRemove.key) < 0) {
                parent = nodeToRemove;
                nodeToRemove = nodeToRemove.left;

            } else
                found = true; 
        }

        if (!found)
            return false;

        if (nodeToRemove.left != null && nodeToRemove.right != null) {        // Case 3: Both children exist.
           
            BSTNode<K, T> min = nodeToRemove.right;
            parent = nodeToRemove;

            while (min.left != null) {
                parent = min;
                min = min.left;
            }

            nodeToRemove.key = min.key;
            nodeToRemove.data = min.data;

            deleteNode(min, parent);
        } else                                                                // Case 1 and 2. Only one child or no children.
            deleteNode(nodeToRemove, parent);                                

        current = root;
        size--;
        return true;
            
    }

    private void deleteNode(BSTNode<K, T> node, BSTNode<K, T> parent) {      // No risk of NullPointerException, since this method is
        BSTNode<K, T> grandChild;                                            // used exclusively in "deleteKey", and "deleteKey" will not pass null arguments except for parent.

        if (node.left != null)  
            grandChild = node.left;
        else
            grandChild = node.right;

        if (parent == null)                                                  // Root case.
            root = grandChild;
            
        else {
            if (node.key.compareTo(parent.key) < 0)
                parent.left = grandChild;
            else
                parent.right = grandChild;
        }
    }

    public LinkedList<T> inOrder() {
        LinkedList<T> resultList = new LinkedList<T>();

        inOrderHelper(root, resultList);                        

        return resultList;
    }

    private void inOrderHelper(BSTNode<K, T> node, LinkedList<T> list) {
        if (node == null)
            return;
        
        inOrderHelper(node.left, list);

        list.insertAtBack(node.data);

        inOrderHelper(node.right, list);

    }

    public LinkedList<T> rangeSearch(K low, K high) {
        LinkedList<T> resultList = new LinkedList<T>();

        rangeSearchHelper(low, high, root, resultList);

        return resultList;
    }

    private void rangeSearchHelper(K low, K high, BSTNode<K, T> node, LinkedList<T> list) {
        if (node == null)
            return;
        
        if (node.key.compareTo(low) >= 0 && node.key.compareTo(high) <= 0) {            // Node in range.
            rangeSearchHelper(low, high, node.left, list);

            list.insertAtBack(node.data);

            rangeSearchHelper(low, high, node.right, list);
        }

        else if (node.key.compareTo(low) < 0)                                           // Node's key is lower than minimum
            rangeSearchHelper(low, high, node.right, list);
        else
            rangeSearchHelper(low, high, node.left, list);                              // Node's key is higher than maximum.

    }

    public int height() {
        return heightHelper(root);
    }

    private int heightHelper(BSTNode<K, T> node) {         
        if (node == null) {
            return 0;
        }

        int left = heightHelper(node.left);
        int right = heightHelper(node.right);
        return Math.max(left, right) + 1;
    }

    public int nbKeyComp(K key) {
        if (empty())
            return 0;

        int result = 0;
        BSTNode<K, T> temp = root;

        while (temp != null) {
           result++;       

            if (key.compareTo(temp.key) == 0)
                break;

            if (key.compareTo(temp.key) < 0) 
                temp = temp.left;
            else
                temp = temp.right;

            
        }
        return result;
    }

}
