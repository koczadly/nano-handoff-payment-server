package uk.oczadly.karl.nanopaymentserver.entity.converters;

import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.NanoAmount;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Reads/stores amounts as RAW strings in the database.
 */
@Converter
public class HexDataConverter implements AttributeConverter<HexData, byte[]> {
    
    @Override
    public byte[] convertToDatabaseColumn(HexData val) {
        if (val == null) return null;
        return val.toByteArray();
    }
    
    @Override
    public HexData convertToEntityAttribute(byte[] val) {
        if (val == null) return null;
        return new HexData(val);
    }
    
}
