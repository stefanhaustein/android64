package org.kobjects.android64;

import java.util.ArrayList;
import java.util.Collection;

public class IntervalTree<T> {

  private static <T> IntervalNode<T> add(IntervalNode<T> root, IntervalNode<T> newNode) {
    if (root == null) {
      return newNode;
    }
    root.maxEnd = Math.max(newNode.maxEnd, root.maxEnd);
    if (newNode.start < root.start) {
      root.left = add(root.left, newNode);
    } else {
      root.right = add(root.right, newNode);
    }
    return root;
  }

  private static <T> IntervalNode<T> remove(IntervalNode<T> root, IntervalNode<T> oldNode) {
    if (root == null) {
      return null;
    }
    if (root == oldNode) {
      if (root.left == null) {
        return root.right;
      }
      if (root.right == null) {
        return root.left;
      }
      if (root.right.left == null) {
        root.right.left = root.left;
        return root.right;
      }
      IntervalNode<T> pullUpParent = root.right;
      while (pullUpParent.left.left != null) {
        pullUpParent = pullUpParent.left;
      }
      IntervalNode<T> pullUpNode = pullUpParent.left;
      pullUpParent.left = pullUpNode.right;
      pullUpParent.adjustMax();
      pullUpNode.left = root.left;
      pullUpNode.right = root.right;
      return pullUpNode;
    }
    if (oldNode.start < root.start) {
      root.left = remove(root.left, oldNode);
    } else {
      root.right = remove(root.right, oldNode);
    }
    root.adjustMax();
    return root;
  }


  public IntervalNode<T> root;

  public IntervalNode<T> add(int start, int end, T value) {
    IntervalNode<T> newNode = new IntervalNode<>(start, end, value);
    root = add(root, newNode);
    return newNode;
  }

  public void remove(IntervalNode<T> node) {
    root = remove(root, node);
  }

  public Collection<IntervalNode<T>> find(int address) {
    Collection<IntervalNode<T>> result = new ArrayList<>();
    if (root != null) {
      root.find(address, result);
    }
    return result;
  }


  static public class IntervalNode<T> {
    public final int start;
    public final int end;
    public final T data;
    IntervalNode<T> left;
    IntervalNode<T> right;
    int maxEnd;

    public IntervalNode(int start, int end, T data) {
      this.start = start;
      this.end = end;
      this.data = data;
      this.maxEnd = end;
    }

    private void find(int value, Collection<IntervalNode<T>> result) {
      if (value >= maxEnd) {
        return;
      }
      if (value >= start && value < end) {
        result.add(this);
      }
      if (right != null && value >= start) {
        right.find(value, result);
      }
      if (left != null) {
        left.find(value, result);
      }
    }

    private void adjustMax() {
      int newMaxEnd = end;
      if (left != null && newMaxEnd < left.maxEnd) {
        newMaxEnd = left.maxEnd;
      }
      if (right != null && newMaxEnd < right.maxEnd) {
        newMaxEnd = right.maxEnd;
      }
      maxEnd = newMaxEnd;
    }
  }
}
