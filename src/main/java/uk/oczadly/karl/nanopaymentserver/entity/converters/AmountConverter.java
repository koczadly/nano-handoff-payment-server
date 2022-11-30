package uk.oczadly.karl.nanopaymentserver.entity.converters;

import uk.oczadly.karl.jnano.model.NanoAmount;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigInteger;

/**
 * Reads/stores amounts as RAW strings in the database.
 */
@Converter
public class AmountConverter implements AttributeConverter<NanoAmount, byte[]> {
    
    @Override
    public byte[] convertToDatabaseColumn(NanoAmount amount) {
        if (amount == null) return null;
        byte[] bytes = amount.getAsRaw().toByteArray();
        if (bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }
        return bytes;
    }
    
    @Override
    public NanoAmount convertToEntityAttribute(byte[] val) {
        if (val == null) return null;
        return NanoAmount.valueOfRaw(new BigInteger(1, val));
    }
    
}
