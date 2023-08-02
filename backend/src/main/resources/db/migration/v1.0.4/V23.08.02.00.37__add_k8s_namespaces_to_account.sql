CREATE TABLE k8s_namespace (
    namespace_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    namespace VARCHAR(256) NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (namespace, resource_id),
    FOREIGN KEY (resource_id) REFERENCES resource (resource_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE account_namespace (
    account_namespace_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL,
    namespace_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (account_id) REFERENCES account (account_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (namespace_id) REFERENCES k8s_namespace (namespace_id) ON UPDATE CASCADE ON DELETE CASCADE
);
