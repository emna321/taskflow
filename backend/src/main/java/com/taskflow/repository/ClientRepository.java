package com.taskflow.repository;

import com.taskflow.dto.ClientDto;
import com.taskflow.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /** Projection sans le logo (colonne binaire) : jamais chargee dans la liste. */
    @Query("""
            select new com.taskflow.dto.ClientDto(c.id, c.nom, c.tel, c.email, c.adresse, size(c.projets),
                   (case when c.logoContenu is not null then true else false end))
            from Client c
            """)
    List<ClientDto> findAllAsDto();
}
