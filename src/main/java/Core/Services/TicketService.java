package Core.Services;

import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.exceptions.CustomerException;
import Core.Models.exceptions.EventException;
import Core.Models.exceptions.TicketException;
import Core.Interfaces.TicketServiceInterface;

import java.time.LocalDate;
import java.util.*;
import Core.Models.Ticket;

public class TicketService implements TicketServiceInterface {

    private static final int MAX_TICKETS_PER_CUSTOMER_PER_EVENT = 5;

    private final Map<UUID, Ticket> ticketsById = new HashMap<>();
    private CustomerService customerService;
    private EventService eventService;

    public void setCustomerService(CustomerService customerService){
        this.customerService = customerService;
    }

    public void setEventService(EventService eventService){
        this.eventService = eventService;
    }

    @Override
    public Ticket createTicket(UUID customerId, UUID eventId) throws TicketException, EventException, CustomerException {
        // Verify the customer exists - throws CustomerException if not
        Customer customer = customerService.getCustomerById(customerId);

        // Verify the event exists - throws EventException if not
        Event event = eventService.getEventById(eventId);

        // The ticket can only be bought if there is contingent left
        if (!event.hasAvailableTickets()) {
            throw TicketException.noTicketsAvailable();
        }

        // A customer can buy at most 5 tickets per event
        long ticketsAlreadyBoughtForEvent = ticketsById.values().stream()
                .filter(t -> t.getCustomerId().equals(customerId)
                        && t.getEventId().equals(eventId))
                .count();
        if (ticketsAlreadyBoughtForEvent >= MAX_TICKETS_PER_CUSTOMER_PER_EVENT) {
            throw TicketException.maximumNumberOfTickets();
        }

        // Create the new ticket
        Ticket ticket = new Ticket(
                UUID.randomUUID(),
                LocalDate.now(),
                customerId,
                eventId
        );

        validateTicket(ticket);

        ticketsById.put(ticket.getId(), ticket);

        // Assign ticket to event (decrements contingent + adds id to ticketsSold)
        eventService.ticketSoldForEvent(ticket);

        // Assign ticket to customer (adds id to ticketsBought)
        customerService.addTicketToCustomer(ticket);

        return ticket;
    }

    @Override
    public Ticket getTicketById(UUID id) throws TicketException {
        if (id == null || !ticketsById.containsKey(id)) {
            throw TicketException.ticketDoesNotExist();
        }
        return ticketsById.get(id);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(ticketsById.values());
    }

    @Override
    public void deleteTicket(UUID id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Ticket ID cannot be null");
        }

        Ticket ticket = ticketsById.remove(id);
        if (ticket == null) {
            return;
        }

        // Remove ticket reference from the customer (might already be deleted)
        try {
            customerService.removeTicketFromCustomer(ticket);
        } catch (CustomerException ignored) {
            // Customer was already deleted - nothing more to do here
        }

        // Remove ticket reference from the event and free its contingent again
        // (Event might already be deleted)
        try {
            eventService.deleteTicketSoldForEvent(ticket);
        } catch (EventException ignored) {
            // Event was already deleted - nothing more to do here
        }
    }

    @Override
    public void deleteAllTickets() {
        ticketsById.clear();
    }

    private void validateTicket(Ticket ticket) throws TicketException, CustomerException, EventException {
        if (ticket == null
                || ticket.getId() == null
                || ticket.getCustomerId() == null
                || ticket.getEventId() == null
                || ticket.getDateOfPurchase() == null) {
            throw TicketException.ticketDoesNotExist();
        }

        // The customer and the event have to exist
        customerService.getCustomerById(ticket.getCustomerId());
        eventService.getEventById(ticket.getEventId());
    }

    @Override
    public boolean verifyTicket(UUID id) {
        if (id == null) {
            return false;
        }
        return ticketsById.containsKey(id);
    }

}
