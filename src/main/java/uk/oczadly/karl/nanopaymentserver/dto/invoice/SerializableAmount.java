package uk.oczadly.karl.nanopaymentserver.dto.invoice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.oczadly.karl.jnano.model.NanoAmount;

import java.math.BigInteger;

/**
 * Wrapper for {@link NanoAmount}, which serializes into separate {@code raw} and {@code nano} fields.
 */
public class SerializableAmount {

    private final NanoAmount amount;

    public SerializableAmount(NanoAmount amount) {
        this.amount = amount;
    }

    public SerializableAmount(BigInteger amountRaw) {
        this.amount = NanoAmount.valueOfRaw(amountRaw);
    }


    @JsonIgnore
    public NanoAmount getAmount() {
        return amount;
    }

    @JsonProperty("nano")
    public String getNanoAmount() {
        return amount.getAsNano().toPlainString();
    }

    @JsonProperty("raw")
    public BigInteger getRawAmount() {
        return amount.getAsRaw();
    }

}
