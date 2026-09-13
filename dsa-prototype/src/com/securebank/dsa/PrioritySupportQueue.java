package com.securebank.dsa;

import java.util.PriorityQueue;

/**
 * PrioritySupportQueue demonstrates the PriorityQueue (Heap) data structure.
 * Why PriorityQueue?
 * In banking, high-net-worth clients or urgent security incidents (e.g. lost card, suspected fraud)
 * must jump ahead of general tickets regardless of when they arrived.
 *
 * Internal Mechanism:
 * Implemented as a Binary Min-Heap based on priority tier (1 = Highest),
 * and broken by highest account balance.
 *
 * Time Complexity:
 * - Insert (offer): O(log n)
 * - Remove Min (poll): O(log n)
 * - Peek (peek): O(1)
 */
public class PrioritySupportQueue {

    public enum PriorityTier {
        CRITICAL_FRAUD(1),
        VIP_CUSTOMER(2),
        HIGH_PRIORITY(3),
        STANDARD(4);

        private final int level;
        PriorityTier(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    public static class SupportRequest {
        private final String requestId;
        private final String customerName;
        private final PriorityTier tier;
        private final double customerBalance;
        private final String issue;

        public SupportRequest(String requestId, String customerName, PriorityTier tier, 
                              double customerBalance, String issue) {
            this.requestId = requestId;
            this.customerName = customerName;
            this.tier = tier;
            this.customerBalance = customerBalance;
            this.issue = issue;
        }

        public String getRequestId() { return requestId; }
        public String getCustomerName() { return customerName; }
        public PriorityTier getTier() { return tier; }
        public double getCustomerBalance() { return customerBalance; }
        public String getIssue() { return issue; }

        @Override
        public String toString() {
            return String.format("[%s] %s | Tier: %-14s (Lvl %d) | Bal: $%8.2f | Issue: %s",
                    requestId, customerName, tier, tier.getLevel(), customerBalance, issue);
        }
    }

    private final PriorityQueue<SupportRequest> priorityHeap;

    public PrioritySupportQueue() {
        // Custom Comparator:
        // 1. Sort by Priority Level ascending (1 comes before 2)
        // 2. If same priority level, sort by Customer Balance descending (higher balance first)
        this.priorityHeap = new PriorityQueue<>((a, b) -> {
            if (a.getTier().getLevel() != b.getTier().getLevel()) {
                return Integer.compare(a.getTier().getLevel(), b.getTier().getLevel());
            }
            return Double.compare(b.getCustomerBalance(), a.getCustomerBalance());
        });
    }

    /**
     * Inserts into Binary Heap in O(log n).
     */
    public void enqueue(SupportRequest request) {
        priorityHeap.offer(request);
        System.out.printf("[PRIORITY ENQUEUE] %s added to Heap. (Heap size: %d)%n",
                request, priorityHeap.size());
    }

    /**
     * Extracts highest priority item in O(log n).
     */
    public SupportRequest serveNextPriorityCustomer() {
        if (priorityHeap.isEmpty()) {
            System.out.println("[PRIORITY QUEUE EMPTY] No pending VIP tickets.");
            return null;
        }
        SupportRequest top = priorityHeap.poll();
        System.out.printf("[PRIORITY DEQUEUE] Serving Highest Priority: %s (Remaining in heap: %d)%n",
                top, priorityHeap.size());
        return top;
    }

    public int size() {
        return priorityHeap.size();
    }
}
