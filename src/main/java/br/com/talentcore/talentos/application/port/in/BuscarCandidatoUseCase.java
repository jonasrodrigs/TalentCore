package br.com.talentcore.talentos.application.port.in;

import br.com.talentcore.talentos.domain.Candidato;
import java.util.List;

public interface BuscarCandidatoUseCase {

    class Filtros {
        public String tecnologia;   // ex.: "Angular"
        public String nivel;        // ex.: "AVANCADO"
        public String cidade;       // ex.: "Barueri"
        public String estado;       // ex.: "SP"
        public String idioma;       // ex.: "en-US"
        public String nivelIdioma;  // ex.: "FLUENTE"
    }

    List<Candidato> executar(Filtros filtros);
}