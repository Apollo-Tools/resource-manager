import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/resources`;

/**
 * Create a new resource.
 *
 * @param {number} resourceTypeId the id of the resource type
 * @param {boolean} isSelfManaged whether the resource is self-managed or not
 * @param {number} regionId the id of the region
 * @param {string} token the access token
 * @param {function} setResource the function to set the created resource
 * @param {function} setError the function to set the error if one occurred
 */
export async function createResource(resourceTypeId, isSelfManaged, regionId, token, setResource, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        resource_type: {
          type_id: resourceTypeId,
        },
        is_self_managed: isSelfManaged,
        region: {
          region_id: regionId,
        },
      }),
    });
    const data = await response.json();
    setResource(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function listResources(token, setResources, setError) {
  try {
    const route = `${API_ROUTE}`;
    const response = await fetch(route, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getResource(id, token, setResource, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResource(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function updateResource(id, resourceTypeId, isSelfManaged, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        resource_type: {
          type_id: resourceTypeId,
        },
        is_self_managed: isSelfManaged,
      }),
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteResource(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function addResourceMetrics(resourceId, metricValues, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(metricValues),
    });
    await response;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


export async function listResourcesBySLOs(slos, token, setResources, setError) {
  try {
    const route = `${API_ROUTE}/slo`;
    const response = await fetch(route, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        slos: slos,
      }),
    });
    const data = await response.json();
    setResources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
