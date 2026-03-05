package br.com.talentcore.talentos.config;

import br.com.talentcore.talentos.application.port.out.CandidatoRepository;
import br.com.talentcore.talentos.infrastructure.persistence.CandidatoRepositoryOracle;
import br.com.talentcore.talentos.infrastructure.persistence.mvp.CandidatoRepositoryInMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class BeanConfig {

    @Bean
    @Profile("oracle")
    public CandidatoRepository repoOracle() {
        return new CandidatoRepositoryOracle();
    }

    @Bean
    @Profile("mvp")
    public CandidatoRepository repoInMemory() {
        return new CandidatoRepositoryInMemory();
    }
}
