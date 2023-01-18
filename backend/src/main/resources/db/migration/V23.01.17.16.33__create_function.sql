CREATE TABLE runtime (
    runtime_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE function (
     function_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     runtime_id BIGINT NOT NULL,
     code VARCHAR(2048) NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
     updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
     FOREIGN KEY (runtime_id) REFERENCES runtime (runtime_id)
);

CREATE TABLE function_resource(
      function_resource_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      resource_id BIGINT NOT NULL,
      function_id BIGINT NOT NULL,
      is_deployed BOOLEAN DEFAULT false,
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      UNIQUE (resource_id, function_id),
      FOREIGN KEY (resource_id)
          REFERENCES resource (resource_id) ON DELETE CASCADE,
      FOREIGN KEY (function_id)
          REFERENCES function (function_id) ON DELETE  CASCADE
);
