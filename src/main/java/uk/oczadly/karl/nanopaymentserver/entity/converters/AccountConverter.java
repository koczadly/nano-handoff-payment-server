package uk.oczadly.karl.nanopaymentserver.entity.converters;

import org.springframework.beans.factory.annotation.Autowired;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.nanopaymentserver.properties.PaymentProperties;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Reads/stores accounts as hexadecimal public keys in the database.
 */
@Converter
public class AccountConverter implements AttributeConverter<NanoAccount, byte[]> {
    
    @Autowired
    private PaymentProperties paymentProperties;
    
    
    @Override
    public byte[] convertToDatabaseColumn(NanoAccount account) {
        if (account == null) return null;
        return account.getPublicKeyBytes();
    }
    
    @Override
    public NanoAccount convertToEntityAttribute(byte[] val) {
        if (val == null) return null;
        return new NanoAccount(val, paymentProperties.getAddressPrefix());
    }
    
}
