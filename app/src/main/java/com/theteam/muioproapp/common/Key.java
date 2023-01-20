package com.theteam.muioproapp.common;

public class Key {
    private static final String publicKey = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAjkI4kBqGyQMJuMRxlDHSDcSFf4EUkWcKwbe1db8drTLAefO4tDHRY07pYcvBeYkRtbq2i6emv84ynNJxV30SVRC0BVp8Jg68MQYd5lZERcUTm5us7Dw1dYH1ehQ/EeoH9bt5b0y5qHDtrLoWZX/RX6KcTHDXkGpVVxit369gt/Ktte3Vco1pKZ3+HJPDAkwHBjsOJ9OosRHqhSaEK3jElzjQF/qGTnJE0+RUHKEhnUtR8zKD1zZEEem+rqNqwD7V1gPgjIWNF9rpepgWTI0NiKumoSKHu6b/VeENTxbO2Uobt+mCFrvuhjD6Lo7caU0Ks+X4onf/e8TIvkJBG6O5uZqKyMG3i/EcWqxojeyzn+x0dHipuv9GzvqvL3n520Sz8ECm8A9Ri5pecrQBR//p5FW2KoMzAvgTrATd7jF6yW2xr5ppGFVXkG2WJlnamvfMut8bEVQ+BWMtl/mvWQ5MorMh6T/9Pme0pdtj5gDCQhxcxDRk2t6tMq22S8cgxwSlAgMBAAE=";

    /**
     * get the publicKey of the application
     * During the encoding process, avoid storing the public key in clear text.
     * @return
     */
    public static String getPublicKey(){
        return publicKey;
    }
}
