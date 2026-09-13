package com.securebank.dsa;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

/**
 * CustomerServiceQueue demonstrates the Queue (FIFO) data structure.
 * Why Queue?
 * General customer inquiries must be processed strictly in the order they arrived (First-Come, First-Served).
 * Time Complexity:
 * - Enqueue (offer): O(1)
 * - Dequeue (poll): O(1)
 * - Peek: O(1)
 */
public class CustomerServiceQueue {

    public static class ServiceTicket {
        private final String ticketId;
        private final String customerId;
        private final String issueDescription;
        private final LocalDateTime createdAt;

        public ServiceTicket(String ticketId, String customerId, String issueDescription) {
            this.ticketId = ticketId;
            this.customerId = customerId;
            this.issueDescription = issueDescription;
            this.createdAt = LocalDateTime.now();
        }

        public String getTicketId() { return ticketId; }
        public String getCustomerId() { return customerId; }
        public String getIssueDescription() { return issueDescription; }

        @Override
        public String toString() {
            return String.format("Ticket[%s] Cust: %s | Issue: %s", ticketId, customerId, issueDescription);
        }
    }

    private final Queue<ServiceTicket> ticketQueue;

    public CustomerServiceQueue() {
        // LinkedList implements the Queue interface
        this.ticketQueue = new LinkedList<>();
    }

    /**
     * Enqueue a new ticket at the tail of the queue. O(1)
     */
    public void submitTicket(String ticketId, String customerId, String issue) {
        ServiceTicket ticket = new ServiceTicket(ticketId, customerId, issue);
        ticketQueue.offer(ticket);
        System.out.printf("[QUEUE ENQUEUE] %s added to queue. (Queue size: %d)%n", ticket, ticketQueue.size());
    }

    /**
     * Dequeue and process the next ticket from the head of the queue. O(1)
     */
    public ServiceTicket processNextTicket() {
        if (ticketQueue.isEmpty()) {
            System.out.println("[QUEUE EMPTY] No pending customer tickets.");
            return null;
        }
        ServiceTicket next = ticketQueue.poll();
        System.out.printf("[QUEUE DEQUEUE] Processing: %s (Remaining in queue: %d)%n", next, ticketQueue.size());
        return next;
    }

    public int getPendingCount() {
        return ticketQueue.size();
    }
}
