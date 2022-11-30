package uk.oczadly.karl.nanopaymentserver.dto.invoice;

import java.util.UUID;

/**
 * @author Karl Oczadly
 */
public class NewInvoiceResponse {

    private UUID invoiceId;
    private String handoffUri;

    public NewInvoiceResponse(UUID invoiceId, String handoffUri) {
        this.invoiceId = invoiceId;
        this.handoffUri = handoffUri;
    }


    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getHandoffUri() {
        return handoffUri;
    }

    public void setHandoffUri(String handoffUri) {
        this.handoffUri = handoffUri;
    }

}
