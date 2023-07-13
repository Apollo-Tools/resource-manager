CREATE TABLE ensemble (
  ensemble_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  regions BIGINT[],
  providers BIGINT[],
  resource_types BIGINT[],
  is_valid BOOLEAN DEFAULT true NOT NULL,
  created_by_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE (name, created_by_id),
  FOREIGN KEY (created_by_id)
      REFERENCES account (account_id) ON DELETE CASCADE
);

CREATE TABLE resource_ensemble(
    resource_ensemble_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ensemble_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (ensemble_id, resource_id),
    FOREIGN KEY (ensemble_id)
        REFERENCES ensemble (ensemble_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id)
        REFERENCES resource (resource_id) ON DELETE  CASCADE
);

CREATE TABLE ensemble_slo(
    ensemble_slo_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ensemble_id BIGINT NOT NULL,
    name VARCHAR(256) NOT NULL,
    expression VARCHAR(2) NOT NULL,
    value_strings TEXT[],
    value_numbers DOUBLE PRECISION[],
    value_bools BOOLEAN[],
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (ensemble_id, name),
    FOREIGN KEY (ensemble_id)
        REFERENCES ensemble (ensemble_id) ON DELETE  CASCADE
);
