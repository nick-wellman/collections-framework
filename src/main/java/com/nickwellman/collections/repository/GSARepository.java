package com.nickwellman.collections.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GSARepository extends AbstractGSARepository {
    private String repositoryName;
    private String definitionFiles;
    private String dataSource;
}
