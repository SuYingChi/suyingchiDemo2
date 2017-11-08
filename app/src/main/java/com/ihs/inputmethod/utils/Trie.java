package com.ihs.inputmethod.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guonan.lv on 17/11/3.
 */

public class Trie {

    private class TrieNode {
        Map<Character, TrieNode> children= new HashMap<>();
        int frequency;//该字串的重复数目,频数
        int prefix_num;//以该子串为前缀的单词数
        char val;//可以没有，数组下标可以知道val值
        boolean isLeaf;

        public TrieNode(char val) {
            this.val = val;
            isLeaf = false;
        }
    }

    private TrieNode root;

    public Trie() {
        root = new TrieNode(' ');
    }

    //插入一个单词
    public void insert(String word) {
        TrieNode cur = root;
        char[] data = word.toLowerCase().toCharArray();
        for (char aData : data) {
            if (cur.children.get(aData) == null) {
                cur.children.put(aData, new TrieNode(aData));
            }
            cur = cur.children.get(aData);
            cur.prefix_num++;
        }
        cur.isLeaf = true;
        cur.frequency++;
    }

    //寻找是否存在某个单词
    public boolean search(String word) {
        TrieNode cur = root;
        char[] data = word.toLowerCase().toCharArray();
        for (char aData : data) {
            if (cur.children.get(aData) == null) {
                return false;
            }
            cur = cur.children.get(aData);
        }
        return cur.isLeaf;
    }

    //查找是否有以某个字符串为开头的单词
    public boolean startsWith(String prefix) {
        TrieNode cur = root;
        char[] data = prefix.toLowerCase().toCharArray();
        for (char aData : data) {
            if (cur.children.get(aData) == null) {
                return false;
            }
            cur = cur.children.get(aData);
        }
        return true;
    }

    /*
    * 得到所有单词及出现次数
    * */
    public HashMap<String, Integer> getAllwords() {
        return helper(root, "");
    }

    private HashMap<String, Integer> helper(TrieNode cur, String s) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        if (cur != null) {
            if (cur.isLeaf) {
                hashMap.put(s, cur.frequency);
            }
        }
        if (cur != null) {
            for(Map.Entry<Character, TrieNode> entry : cur.children.entrySet()) {
                String tmp = s + entry.getKey();
                hashMap.putAll(helper(entry.getValue(), tmp));
            }
        }
        return hashMap;
    }

    /*
    * 得到以某字串为前缀的字串集，包括字串本身！
    * */
    public HashMap<String, Integer> getWordsForPrefix(String prefix) {
        return getWordsForPrefix(root, prefix);
    }

    private HashMap<String, Integer> getWordsForPrefix(TrieNode cur, String prefix) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        char[] data = prefix.toLowerCase().toCharArray();
        for (char aData : data) {
            if (cur.children.get(aData) == null) {
                return hashMap;
            }
            cur = cur.children.get(aData);
        }
        return helper(cur, prefix);
    }
}