import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/functions`;

export async function listFunctionResources(functionId, token, setFunctionResources, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${functionId}/resources`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setFunctionResources(data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getFunctionResources(functionId, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${functionId}/resources`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.json();
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function addFunctionResources(functionId, resources, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${functionId}/resources`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(resources),
    });
    await response;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


export async function deleteFunctionResource(functionId, resourceId, token, setError) {
  try {
    const response = await fetch(
        `${API_ROUTE}/${functionId}/resources/${resourceId}`, {
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
