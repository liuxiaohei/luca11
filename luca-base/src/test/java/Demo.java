import java.util.Stack;

//给定一个二叉树，返回其节点值的锯齿形层次遍历。（即先从左往右，再从右往左进行下一层遍历，以此类推，层与层之间交替进行）。
public class Demo {
    public static void main(String... args) {
        Node n = new Node();
        Stack<Node> a = new Stack<>();
        Stack<Node> b = new Stack<>();
        a.push(n);
        while (!a.empty() && !b.empty()) {
            while (!a.empty()) {
                Node nn = a.pop();
                System.out.println(nn.key);
                if (null != nn.left) {
                    b.push(nn.left);
                }
                if (null != nn.right) {
                    b.push(nn.right);
                }
            }
            while (!b.empty()) {
                Node nn = b.pop();
                System.out.println(nn.key);
                if (null != nn.right) {
                    a.push(nn.right);
                }
                if (null != nn.left) {
                    a.push(nn.left);
                }
            }
        }
    }

    public static class Node {
        public String key;
        public Node left;
        public Node right;
    }
}
