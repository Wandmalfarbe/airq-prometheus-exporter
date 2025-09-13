package de.pascalwagler.airq.model.internal;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Data
public class Sensor {
    private String id;
    private String nameDe;
    private String nameEn;
    private String unit;
    private String type;

    @Getter(AccessLevel.NONE)
    private Boolean hasErrorMargin;

    public Boolean getHasErrorMargin() {
        return hasErrorMargin;
    }

    public String getDescription() {
        if (this.getUnit() == null) {
            return this.getNameEn();
        } else {
            return this.getNameEn() + " (" + this.getUnit() + ")";
        }
    }
}
