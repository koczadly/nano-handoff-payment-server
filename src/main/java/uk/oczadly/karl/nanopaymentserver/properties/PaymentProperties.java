package uk.oczadly.karl.nanopaymentserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.model.NanoAmount;
import uk.oczadly.karl.jnano.util.NanoUnit;

import java.util.Objects;

@Component
@ConfigurationProperties("payment-server.payment")
public class PaymentProperties {

    private long confirmationTimeout = 60 * 60000; // 1 hour
    private NanoAmount minimumAmount = NanoAmount.valueOf(1, NanoUnit.KILO);
    private long defaultTimeout = 10 * 60000; // 10 Min
    private NanoAccount defaultDestination;
    private String addressPrefix;


    /**
     * @return the number of milliseconds to wait for confirmation
     */
    public long getConfirmationTimeout() {
        return confirmationTimeout;
    }
    
    public void setConfirmationTimeout(long seconds) {
        this.confirmationTimeout = seconds * 1000;
    }

    /**
     * @return the address prefix used with this node (ie. nano or ban)
     */
    public String getAddressPrefix() {
        if (addressPrefix != null) {
            return addressPrefix;
        } else if (defaultDestination != null) {
            return defaultDestination.getPrefix();
        } else {
            return "nano";
        }
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    /**
     * @return the minimum permitted amount for a transaction; amounts below this value will be ignored and rejected
     */
    public NanoAmount getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = NanoAmount.valueOfNano(minimumAmount);
    }

    /**
     * @return the default Nano address where funds should be sent to
     */
    public NanoAccount getDefaultDestination() {
        return defaultDestination;
    }

    public void setDefaultDestination(String defaultDestination) {
        this.defaultDestination = NanoAccount.parse(defaultDestination);
    }

    /**
     * @return the amount of time (in millis) the customer has to make the payment(s) before expiring
     */
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(long seconds) {
        this.defaultTimeout = seconds * 1000;
    }

}
