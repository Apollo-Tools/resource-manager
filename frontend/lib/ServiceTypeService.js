import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/service-types`;

/**
 * Create a new service type.
 *
 * @param {string} name the name of the service type
 * @param {string} token the access token
 * @param {function} setServiceType the function to set the created service type
 * @param {function} setError the function to set the error if one occurred
 */
export async function createServiceType(name, token, setServiceType, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
      }),
    });
    const data = await response.json();
    setServiceType(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all service types.
 *
 * @param {string} token the access token
 * @param {function} setServiceTypes the function to set the retrieved service types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listServiceTypes(token, setServiceTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setServiceTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing service type.
 *
 * @param {number} id the id of the function type
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteServiceType(id, token, setError) {
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
