package com.granishots.inventory.infraestructure.mapper;

import com.granishots.inventory.application.dto.SupplyRequestDTO;
import com.granishots.inventory.application.dto.SupplyResponseDTO;
import com.granishots.inventory.domain.model.Supply;
import com.granishots.inventory.infraestructure.driver_adapters.jpa_repository.SupplyData;
import org.springframework.stereotype.Component;

@Component
public class SupplyMapper {

    public SupplyData toSupplyData(Supply supply) {
        return new SupplyData(
                supply.getId(),
                supply.getName(),
                supply.getUnit(),
                supply.getCurrentStock(),
                supply.getMinStock(),
                supply.getSupplier(),
                supply.getPurchasePrice()
        );
    }

    public Supply toSupply(SupplyData data) {
        return new Supply(
                data.getId(),
                data.getName(),
                data.getUnit(),
                data.getCurrentStock(),
                data.getMinStock(),
                data.getSupplier(),
                data.getPurchasePrice()
        );
    }

    public Supply toSupplyFromDTO(SupplyRequestDTO dto) {
        return new Supply(
                null,
                dto.getName(),
                dto.getUnit(),
                dto.getCurrentStock(),
                dto.getMinStock(),
                dto.getSupplier(),
                dto.getPurchasePrice()
        );
    }

    public SupplyResponseDTO toSupplyResponseDTO(Supply supply) {
        String status;
        if (supply.getCurrentStock() == 0) status = "SIN_STOCK";
        else if (supply.getCurrentStock() <= supply.getMinStock()) status = "BAJO";
        else status = "OK";

        return new SupplyResponseDTO(
                supply.getId(),
                supply.getName(),
                supply.getUnit(),
                supply.getCurrentStock(),
                supply.getMinStock(),
                supply.getSupplier(),
                supply.getPurchasePrice(),
                status
        );
    }
}
