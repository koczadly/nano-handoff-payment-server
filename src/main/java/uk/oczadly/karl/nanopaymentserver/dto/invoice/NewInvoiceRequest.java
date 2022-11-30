package uk.oczadly.karl.nanopaymentserver.dto.invoice;

import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;

import java.time.Instant;

public class NewInvoiceRequest {

    private String amount, destination;
    private boolean variableAmount = false, useDecimalAmount = false, oneTime = true;
    private long expireTime;
    private Instant expireDate;


    public NanoAmount getAmount() {
        if (amount == null) return NanoAmount.ZERO;
        return useDecimalAmount ? NanoAmount.valueOfNano(amount) : NanoAmount.valueOfRaw(amount);
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public NanoAccount getDestination() {
        if (destination == null) return null;
        return NanoAccount.parseAddress(destination);
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isVariableAmount() {
        return variableAmount;
    }

    public void setVariableAmount(boolean variableAmount) {
        this.variableAmount = variableAmount;
    }

    public boolean isUseDecimalAmount() {
        return useDecimalAmount;
    }

    public void setUseDecimalAmount(boolean useDecimalAmount) {
        this.useDecimalAmount = useDecimalAmount;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    public long getExpireTime() {
        return expireTime * 1000;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public Instant getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Instant expireDate) {
        this.expireDate = expireDate;
    }
}
