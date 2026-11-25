package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.ItemVendaResponse;
import com.notoriusfight.api.dto.VendaResponse;
import com.notoriusfight.api.dto.VendaResumoResponse;
import com.notoriusfight.domain.ItemVenda;
import com.notoriusfight.domain.Venda;
import org.springframework.stereotype.Component;

@Component
public class VendaMapper {
    public VendaResponse toResponse(Venda venda) {
        return new VendaResponse(venda.getId(), venda.getCliente().getId(), venda.getCliente().getPessoa().getNome(),
                venda.getVendedor().getId(), venda.getVendedor().getPessoa().getNome(), venda.getDataVenda(),
                venda.getStatus(), venda.getItens().stream().map(this::toItemResponse).toList(), venda.calcularTotal());
    }

    public VendaResumoResponse toResumo(Venda venda) {
        return new VendaResumoResponse(venda.getId(), venda.getCliente().getId(),
                venda.getCliente().getPessoa().getNome(), venda.getVendedor().getId(),
                venda.getVendedor().getPessoa().getNome(), venda.getDataVenda(), venda.getStatus());
    }

    private ItemVendaResponse toItemResponse(ItemVenda item) {
        return new ItemVendaResponse(item.getId(), item.getArtigo().getId(), item.getArtigo().getDescricao(),
                item.getQuantidade(), item.getValorUnitario(), item.calcularSubtotal());
    }
}
