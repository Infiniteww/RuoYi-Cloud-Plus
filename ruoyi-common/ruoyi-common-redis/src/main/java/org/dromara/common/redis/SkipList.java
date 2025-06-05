package org.dromara.common.redis;

import java.util.Random;

public class SkipList {
    private static final int MAX_LEVEL = 32;
    private static final double P = 0.25;

    private final SkipListNode head = new SkipListNode(MAX_LEVEL, 0, null);
    private SkipListNode tail = null;
    private int level = 1;
    private int length = 0;
    private final Random rand = new Random();

    public static class SkipListNode {
        public String member;
        public double score;
        public SkipListLevel[] levels;
        public SkipListNode backward;

        public SkipListNode(int level, double score, String member) {
            this.score = score;
            this.member = member;
            this.levels = new SkipListLevel[level];
            for (int i = 0; i < level; i++) {
                this.levels[i] = new SkipListLevel();
            }
        }
    }

    public static class SkipListLevel {
        public SkipListNode forward;
        public int span;
    }

    private int randomLevel() {
        int lvl = 1;
        while (rand.nextDouble() < P && lvl < MAX_LEVEL) {
            lvl++;
        }
        return lvl;
    }

    public void insert(double score, String member) {
        SkipListNode[] update = new SkipListNode[MAX_LEVEL];
        int[] rank = new int[MAX_LEVEL];
        SkipListNode x = head;

        for (int i = level - 1; i >= 0; i--) {
            rank[i] = i == level - 1 ? 0 : rank[i + 1];
            while (x.levels[i].forward != null &&
                (x.levels[i].forward.score < score ||
                    (x.levels[i].forward.score == score &&
                        x.levels[i].forward.member.compareTo(member) < 0))) {
                rank[i] += x.levels[i].span;
                x = x.levels[i].forward;
            }
            update[i] = x;
        }

        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level; i < newLevel; i++) {
                rank[i] = 0;
                update[i] = head;
                update[i].levels[i].span = length;
            }
            level = newLevel;
        }

        x = new SkipListNode(newLevel, score, member);
        for (int i = 0; i < newLevel; i++) {
            x.levels[i].forward = update[i].levels[i].forward;
            update[i].levels[i].forward = x;

            x.levels[i].span = update[i].levels[i].span - (rank[0] - rank[i]);
            update[i].levels[i].span = (rank[0] - rank[i]) + 1;
        }

        for (int i = newLevel; i < level; i++) {
            update[i].levels[i].span++;
        }

        x.backward = (update[0] == head) ? null : update[0];
        if (x.levels[0].forward != null) {
            x.levels[0].forward.backward = x;
        } else {
            tail = x;
        }
        length++;
    }

    public boolean delete(double score, String member) {
        SkipListNode[] update = new SkipListNode[MAX_LEVEL];
        SkipListNode x = head;

        for (int i = level - 1; i >= 0; i--) {
            while (x.levels[i].forward != null &&
                (x.levels[i].forward.score < score ||
                    (x.levels[i].forward.score == score &&
                        x.levels[i].forward.member.compareTo(member) < 0))) {
                x = x.levels[i].forward;
            }
            update[i] = x;
        }

        x = x.levels[0].forward;
        if (x != null && x.score == score && x.member.equals(member)) {
            for (int i = 0; i < level; i++) {
                if (update[i].levels[i].forward == x) {
                    update[i].levels[i].span += x.levels[i].span - 1;
                    update[i].levels[i].forward = x.levels[i].forward;
                } else {
                    update[i].levels[i].span--;
                }
            }

            if (x.levels[0].forward != null) {
                x.levels[0].forward.backward = x.backward;
            } else {
                tail = x.backward;
            }

            while (level > 1 && head.levels[level - 1].forward == null) {
                level--;
            }
            length--;
            return true;
        }
        return false;
    }

    public void print() {
        SkipListNode x = head.levels[0].forward;
        while (x != null) {
            System.out.printf("(%s, %.2f) -> ", x.member, x.score);
            x = x.levels[0].forward;
        }
        System.out.println("null");
    }

    public int size() {
        return length;
    }

    public static void main(String[] args) {
        SkipList sl = new SkipList();
        sl.insert(10, "A");
        sl.insert(20, "B");
        sl.insert(15, "C");
        sl.insert(25, "D");
        sl.insert(17, "E");

        sl.print();

        sl.delete(15, "C");
        sl.print();
    }
}
