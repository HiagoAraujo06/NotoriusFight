package com.notoriusfight.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoFiscalTest {
    @Test
    void deveValidarCpfPelosDoisDigitosVerificadores() {
        assertTrue(DocumentoFiscal.cpfValido("52998224725"));
        assertFalse(DocumentoFiscal.cpfValido("52998224724"));
        assertFalse(DocumentoFiscal.cpfValido("11111111111"));
        assertFalse(DocumentoFiscal.cpfValido("529.982.247-25"));
    }

    @Test
    void deveValidarCnpjPelosDoisDigitosVerificadores() {
        assertTrue(DocumentoFiscal.cnpjValido("11222333000181"));
        assertTrue(DocumentoFiscal.cnpjValido("12345678000195"));
        assertTrue(DocumentoFiscal.cnpjValido("12ABC34501DE35"));
        assertFalse(DocumentoFiscal.cnpjValido("12ABC34501DE36"));
        assertFalse(DocumentoFiscal.cnpjValido("11222333000182"));
        assertFalse(DocumentoFiscal.cnpjValido("00000000000000"));
    }
}
