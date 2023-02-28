package com.distribuida;

import kamon.Kamon;

public class Servidor {
    public static void main(String[] args) {
        Kamon.init();
        io.helidon.microprofile.cdi.Main.main(args);
    }
}
